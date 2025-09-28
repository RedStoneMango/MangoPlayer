package io.github.redstonemango.mangoplayer.logic;

import io.github.redstonemango.mangoutils.MangoIO;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import io.github.redstonemango.mangoplayer.graphic.MangoPlayer;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IProcessExecuteable;
import io.github.redstonemango.mangoplayer.graphic.controller.songManager.SongDownloadResultScene;
import io.github.redstonemango.mangoplayer.graphic.controller.songManager.SongListController;
import io.github.redstonemango.mangoplayer.logic.config.SongConfigWrapper;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SongAdding implements IProcessExecuteable {

    public static final SongAdding INSTANCE = new SongAdding();

    private SongListController controller;
    private String lastSearch;

    public void onImportSong(SongListController controller) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio file", "*.aac", "*.ac3", "*.aiff", "*.alac", "*.amr", "*.ape", "*.dts", "*.flac", "*.m4a", "*.mka", "*.mp2", "*.mp3", "*.oga", "*.ogg", "*.opus", "*.ra", "*.shn", "*.tta", "*.voc", "*.wav", "*.wma", "*.wv"));
        chooser.setTitle("MangoPlayer | Import song");
        List<File> files = chooser.showOpenMultipleDialog(controller.getWindow());
        if (files != null) {
            controller.setActionOccupied(true);
            FfmpegFileConverter.getInstance().convertFilesToMp3(files).thenAccept(mp3Files -> {
                for (File file : mp3Files) {
                    try {
                        // Fetch data
                        MP3File mp3file = new MP3File(file);
                        Tag tag = mp3file.getTag();
                        String name = tag.getFirst(FieldKey.TITLE);
                        if (name.isBlank()) name = file.getName().substring(0, file.getName().length() - ".mp3".length());
                        Artwork artwork = tag.getFirstArtwork();

                        // Instantiate
                        Song song = new Song(name, null, 0, 0.5);

                        // Load assets
                        new File(Utilities.audioPathFromSong(song)).getParentFile().mkdirs();
                        if (artwork != null) {
                            byte[] imageData = artwork.getBinaryData();
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
                            BufferedImage image = ImageIO.read(inputStream);
                            new File(Utilities.thumbnailPathFromSong(song)).getParentFile().mkdirs();
                            ImageIO.write(image, "PNG", new File(Utilities.thumbnailPathFromSong(song)));
                        }

                        tag.deleteArtworkField(); // do not save artwork in the audio asset file
                        Files.copy(file.toPath(), new File(Utilities.audioPathFromSong(song)).toPath());
                        mp3file.save(new File(Utilities.audioPathFromSong(song)));

                        SongConfigWrapper.loadConfig().songs.put(song.getId(), song);
                        Platform.runLater(controller::sortAndRepaintSongs);
                        System.out.println("Imported song '" + song.getName() + "' (ID is '" + song.getId() + "') from file '" + file.getAbsolutePath() + "'" + (file.getName().endsWith(".mp3") ? "" : " (Used ffmpeg conversion for importing)"));
                    }
                    catch (IOException | TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException e) {
                        Utilities.showErrorScreen("Import song", "Error importing file '" + file.getName() + "': " + e, false);
                    }
                }
                FfmpegFileConverter.getInstance().cleanupFilesToMp3Conversion();
                controller.setActionOccupied(false);
            });
        }
    }

    public void onDownloadSong(SongListController controller) {
        this.controller = controller;
        if (YtDlpManager.ensureConnection()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("MangoPlayer | Download song");
            dialog.setHeaderText("Please enter a YouTube video reference.\nThis can either be a direct link or a title to search for.");
            dialog.setContentText("Link / YouTube search: ");
            dialog.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            dialog.showAndWait();
            if (dialog.getResult() != null && YtDlpManager.ensureConnection()) {
                startSearch(dialog.getResult());
            }
        }
    }

    public void startSearch(String search) {
        lastSearch = search;
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(MangoPlayer.primaryStage);
        stage.setTitle("MangoPlayer | Load YouTube video");
        SongDownloadResultScene scene = SongDownloadResultScene.createNewScene(search, () -> YtDlpManager.getInstance().destroyRunningProcess(), this::videoSearchSelected);
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());

        new Thread(() -> {
            if (Utilities.isValidYoutubeLink(search)) {
                System.out.println("Looking up youtube video for link '" + search + "'");
                scene.sendNewResultInformation(YtDlpManager.getInstance().loadUrl(search));
            }
            else {
                System.out.println("Performing youtube video search for search query '" + search + "'");
                YtDlpManager.getInstance().searchYoutube(search, scene::sendNewResultInformation);
            }
            scene.sendEndInformation();
        }).start();
    }

    private boolean videoSearchSelected(YtDlpManager.SearchResult searchResult) {
        TextInputDialog dialog = new TextInputDialog(searchResult.getName());
        dialog.setTitle("MangoPlayer | Download YouTube audio");
        dialog.setHeaderText("Please set a name for the downloaded audio.\nYou can always change the name using the song manager");
        dialog.setContentText("Song name: ");
        dialog.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
        dialog.showAndWait();
        if (dialog.getResult() != null) {
            Song song = new Song(dialog.getResult(), Utilities.youtubeIdFromLink(searchResult.getUrl()), 0, 0.5);
            this.startAudioDownloadProcess(song);
            return true;
        }
        return false;
    }

    @Override
    public void processFinished(Object source, @Nullable Object additionalData, boolean success, int processType) {
        if (processType == AUDIO_DOWNLOAD_PROCESS_TYPE && source instanceof Song song) {
            try {
                if (success) {
                    Files.move(Path.of(Utilities.dummyPathFromSong(song) + ".mp3"), Path.of(Utilities.audioPathFromSong(song)));
                    Files.move(Path.of(Utilities.dummyPathFromSong(song) + ".png"), Path.of(Utilities.thumbnailPathFromSong(song)));
                    SongConfigWrapper.loadConfig().songs.put(song.getId(), song);
                    controller.sortAndRepaintSongs();
                    controller.scrollToSong(song);
                    System.out.println("Downloaded song '" + song.getName() + "' (ID is '" + song.getId() + "') from youtube video with ID '" + song.getYoutubeId() + "' using the yt-dlp integration");
                }

                System.out.println("Deleting temporary download directory...");
                MangoIO.deleteDirectoryRecursively(new File(Utilities.dummyPathFromSong(song)).getParentFile()); // 'Path.of(Utilities.dummyPathFromSong(song)).getParent()' resolves path of temp folder
            }
            catch (IOException e) {
                Utilities.showErrorScreen("Download song", "Error processing downloaded assets: " + e);
            }
        }
    }

    public String getLastSearch() {
        return lastSearch;
    }
}
