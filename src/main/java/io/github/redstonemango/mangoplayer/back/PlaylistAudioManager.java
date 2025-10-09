package io.github.redstonemango.mangoplayer.back;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.front.controller.playlistScreen.PlaylistScreenController;
import io.github.redstonemango.mangoplayer.back.config.MainConfigWrapper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistAudioManager {
    private static @Nullable Media currentMedia;
    private static @Nullable MediaPlayer currentPlayer;
    private static @Nullable Song currentlyPlayingSong = null;
    private static @Nullable PlaylistScreenController controller;

    private static final List<Song> songQueue = new ArrayList<>();
    private static int songQueuePosition = 0;

    public static void registerController(PlaylistScreenController controller) {
        PlaylistAudioManager.controller = controller;
    }

    // Starts playlist play (first song in queue)
    public static void startPlay() {
        if (controller == null) throw new IllegalStateException("No controller registered");
        initializeSongQueue(null);
        playCurrentSongSafely(true);
    }

    // Plays specific song
    public static void play(Song song) {
        play(song, false, true);
    }
    private static void play(Song song, boolean jumpTo, boolean initializeQueue) {
        if (controller == null) throw new IllegalStateException("No controller registered");

        File audioFile = new File(Utilities.audioPathFromSong(song));
        if (currentPlayer != null) {
            controller.onSongEnd(currentlyPlayingSong, true);
            currentPlayer.stop();
        }
        if (!audioFile.exists()) {
            Utilities.showErrorScreen("Play '" + song.getName() + "'", "The audio asset for the song could not be found.\nPlease try re-downloading/importing the song");
            currentlyPlayingSong = null;
            controller.onPlayEnd();
            return;
        }

        if (initializeQueue) {
            initializeSongQueue(song);
        }

        currentlyPlayingSong = song;
        try {
            currentMedia = new Media(audioFile.toURI().toString());
            currentPlayer = new MediaPlayer(currentMedia);
        }
        catch (MediaException e) {
            System.err.println("Media error while instantiating audio playback objects for playlist: " + e);
            if (e.getType() == MediaException.Type.UNKNOWN) {
                Utilities.showCodecErrorMessage();
            }
            currentlyPlayingSong = null;
            currentPlayer = null;
            currentMedia = null;
            controller.onPlayEnd();
            return;
        }
        updateVolume();

        currentPlayer.setOnReady(() -> {
            currentPlayer.play();
            song.registerDurationIfNeeded(currentMedia.getDuration()); // Lazy-load to simplify tag read for analyzer
            controller.onNewSongStart(song, currentMedia.getDuration(), jumpTo);
        });

        currentPlayer.currentTimeProperty().addListener((_, _, newValue) -> controller.onProgressUpdate(newValue));

        currentPlayer.setOnEndOfMedia(() -> {
            controller.onSongEnd(song, false);
            currentlyPlayingSong = null;

            songQueueForward(true); // Play the next song if existing. If not, send playlist end notification
        });
    }

    private static void playCurrentSongSafely(boolean jumpTo) {
        if (controller == null) throw new IllegalStateException("No controller registered");

        if (songQueuePosition < songQueue.size() && songQueuePosition >= 0) {
            play(songQueue.get(songQueuePosition), jumpTo, false);
        }
        else {
            stop();
        }
    }

    public static boolean cannotMoveForwardInQueue() {
        if (!isInitialized()) return true;
        if (MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_ALL || MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_SINGLE) return false;
        return songQueuePosition >= songQueue.size() - 1;
    }

    public static boolean cannotMoveBackwardInQueue() {
        if (!isInitialized()) return true;
        if (MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_ALL || MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_SINGLE) return false;
        return songQueuePosition <= 0;
    }

    public static void songQueueForward(boolean jumpTo) {
        if (controller == null) throw new IllegalStateException("No controller registered");

        if (MainConfigWrapper.loadConfig().loopType != MainConfigWrapper.LOOP_TYPE_SINGLE) { // If we are looping single, do not change the current queue position
            songQueuePosition++;
            if (MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_ALL && songQueuePosition >= songQueue.size()) {
                songQueuePosition = 0;
                if (MainConfigWrapper.loadConfig().isShuffleActive) { // If we have shuffle active and the playlist loops itself, re-shuffle the list when doing so
                    Song notThisSong = Utilities.getRandomElementExcluding(controller.playlist.getSongs(), songQueue.getLast());
                    initializeSongQueue(notThisSong); // Configure the new queue to start with a song that definitely is not the lastly played song. This avoids the edge case of one song playing 2 times in a row if the user has bad luck
                }
            }
        }
        if (controller.shouldStopAfterSong()) songQueuePosition = -1;
        playCurrentSongSafely(jumpTo);
    }

    public static void songQueueBackwards(boolean jumpTo) {
        if (controller == null) throw new IllegalStateException("No controller registered");

        if (MainConfigWrapper.loadConfig().loopType != MainConfigWrapper.LOOP_TYPE_SINGLE) { // If we are looping single, do not change the current queue position
            songQueuePosition--;
            if (MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_ALL && songQueuePosition < 0) {
                songQueuePosition = songQueue.size() - 1;
            }
        }
        if (controller.shouldStopAfterSong()) songQueuePosition = -1;
        playCurrentSongSafely(jumpTo);
    }

    public static void pauseOrResume() {
        if (currentPlayer != null) {
            if (currentPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                currentPlayer.play();
            }
            else {
                currentPlayer.pause();
            }
        }
    }

    public static void seek(Duration duration, boolean isPausing) {
        if (currentPlayer != null) {
            currentPlayer.seek(duration);
            if (!isPausing) currentPlayer.play();
        }
    }

    public static void stop() {
        if (controller == null) throw new IllegalStateException("No controller registered");

        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
            currentlyPlayingSong = null;
            controller.onSongEnd(currentlyPlayingSong, true);
            Platform.runLater(controller::onPlayEnd); // Delay play end a bit to give the player time to stop. Without this, the time label would, for example, show a wrong time
        }
    }
    public static boolean isInitialized() {
        return currentPlayer != null;
    }
    public static boolean isPlaying() {
        return isInitialized();
    }

    public static void updateVolume() {
        if (currentPlayer != null && currentlyPlayingSong != null) {
            currentPlayer.setVolume(MainConfigWrapper.loadConfig().isMuted ? 0 : MainConfigWrapper.loadConfig().volume * currentlyPlayingSong.getVolumeAdjustment());
        }
    }

    public static boolean isPaused() {
        if (currentPlayer != null) {
            return currentPlayer.getStatus() == MediaPlayer.Status.PAUSED;
        }
        return false;
    }

    public static @Nullable Song getCurrentlyPlayingSong() {
        return currentlyPlayingSong;
    }

    public static @Nullable Duration getCurrentTime() {
        if (currentPlayer != null && currentlyPlayingSong != null) {
            return currentPlayer.getCurrentTime();
        }
        return null;
    }

    public static void initializeSongQueue(@Nullable Song firstSong) {
        if (controller == null) throw new IllegalStateException("No controller registered");

        songQueue.clear();
        songQueue.addAll(controller.playlist.getSongs());
        songQueuePosition = 0;
        if (MainConfigWrapper.loadConfig().isShuffleActive) {
            Collections.shuffle(songQueue);
        }
        if (firstSong != null) {
            int firstSongIndex = songQueue.indexOf(firstSong);
            if (MainConfigWrapper.loadConfig().isShuffleActive) {
                songQueue.remove(firstSong);
                songQueue.addFirst(firstSong);
            }
            else {
                songQueuePosition = firstSongIndex == -1 ? 0 : firstSongIndex;
            }
        }
    }
}
