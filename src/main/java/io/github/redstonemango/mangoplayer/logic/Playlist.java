package io.github.redstonemango.mangoplayer.logic;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.logic.config.PlaylistConfigWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class Playlist implements Comparable<Playlist> {
    private String id;
    private String name;
    private List<String> songs;
    private long playedSongCount;
    private long secondsPlayed;


    public Playlist(String name, List<String> songs, long playedSongCount, long secondsPlayed) {
        this.id = UniqueIdGenerator.generateUniqueString(UniqueIdGenerator.IdUse.PLAYLIST_ID);
        this.name = name;
        this.songs = songs;
        this.playedSongCount = playedSongCount;
        this.secondsPlayed = secondsPlayed;
    }

    public void ensureFields() {
        if (id == null || id.isBlank()) {
            id = UniqueIdGenerator.generateUniqueString(UniqueIdGenerator.IdUse.PLAYLIST_ID);
            System.err.println("Found a playlist without ID!" + (name != null && !name.isBlank() ? " (Name is '" + name + "')." : "") + " Initialized ID to newly generated value '" + id + "'");
        }
        if (name == null || name.isBlank()) {
            name = "Unnamed Playlist";
            System.err.println("Playlist with ID '" + id + "' does not have a name. Initialized name to 'Unnamed Playlist'");
        }
        if (songs == null) {
            songs = new ArrayList<>();
        }
    }

    public Song getSong(int index) {
        return Song.songFromId(songs.get(index));
    }

    public List<Song> getSongs() {
        List<Song> songs = new ArrayList<>();
        this.songs.forEach(id -> songs.add(Song.songFromId(id)));
        return Collections.unmodifiableList(songs);
    }

    public int songCount() {
        return songs.size();
    }

    public boolean hasSong(Song song) {
        return songs.contains(song.getId());
    }

    public void forEachSong(BiConsumer<Song, Integer> action) {
        AtomicInteger index = new AtomicInteger(0);
        this.songs.forEach(id -> action.accept(Song.songFromId(id), index.getAndIncrement()));
    }

    public void removeSongOccurrences(Song song) {
        songs.removeIf(id -> id.equals(song.getId()));
    }

    public void clearSongs() {
        this.songs.clear();
    }

    public void addSong(Song song) {
        songs.add(song.getId());
    }

    public void removeSongIndex(int index) {
        songs.remove(index);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public long getPlayedSongCount() {
        return playedSongCount;
    }

    public Duration getPlayTime() {
        return Duration.seconds(secondsPlayed);
    }

    public void increasePlayedSongCount() {
        playedSongCount++;
    }

    public void increasePlayTime(Duration duration) {
        secondsPlayed += (long) duration.toSeconds();
    }

    @Override
    public int compareTo(@NotNull Playlist o) {
        return this.name.compareToIgnoreCase(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Playlist other)) return false;
        return other.getId().equals(getId());
    }

    public boolean askAndRunDelete() {
        if (!songs.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("MangoPlayer | Delete playlist");
            alert.setHeaderText("Do you really want to delete '" + name + "'?");
            alert.setContentText("When deleting the playlist, the songs themselves still exist until they are deleted using the song manager");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.showAndWait();
            if (alert.getResult() == null || alert.getResult() != ButtonType.OK) {
                return false;
            }
        }

        PlaylistConfigWrapper.loadConfig().playlists.remove(this);
        System.out.println("Deleting playlist '" + name + "' (ID was '" + id + "')");
        return true;
    }

    public void askAndRunRename() {
        TextInputDialog dialog = new TextInputDialog(name);
        dialog.setTitle("MangoPlayer | Rename playlist");
        dialog.setHeaderText("Please enter a new name for '" + name + "'");
        dialog.setContentText("New name: ");
        dialog.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
        dialog.showAndWait();
        if (dialog.getResult() != null) {
            System.out.println("Renaming playlist '" + name + "' to '" + dialog.getResult() + "' (ID is '" + id + "')");
            name = dialog.getResult();
        }
    }
}
