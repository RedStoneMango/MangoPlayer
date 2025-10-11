package io.github.redstonemango.mangoplayer.back;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.back.config.PlaylistConfigWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Playlist implements Comparable<Playlist> {
    @Expose private String id;
    @Expose private String name;
    @Expose @SerializedName("songs") private Set<String> songIds;
    @Expose private long playedSongCount;
    @Expose private long secondsPlayed;
    private ObservableList<Song> songObjects;



    public Playlist(String name, Set<String> songIds, long playedSongCount, long secondsPlayed) {
        this.id = UniqueIdGenerator.generateUniqueString(UniqueIdGenerator.IdUse.PLAYLIST_ID);
        this.name = name;
        this.songIds = songIds;
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
        if (songIds == null) {
            songIds = new HashSet<>();
        }
        songIds.removeIf(Objects::isNull);

        List<Song> songsObjs = new ArrayList<>();
        songObjects = FXCollections.observableList(songsObjs);
        songIds.removeIf(id -> Song.songFromId(id) == null);
        songIds.forEach(id -> songsObjs.add(Song.songFromId(id)));
        songObjects.removeIf(Objects::isNull); // Just to be sure. Actually, all 'null' songs should already have been removed
        songObjects.addListener((ListChangeListener<Song>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(song -> songIds.add(song.getId()));
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(song -> songIds.remove(song.getId()));
                }
            }
        });
    }

    public Duration computeTotalDuration() {
        Duration duration = Duration.ZERO;
        for (Song song : songObjects) {
            Duration dur = song.loadDuration();
            if (dur != null) duration = duration.add(dur);
        }
        return duration;
    }

    public List<Song> getSongs() {
        return songObjects;
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
        if (!songIds.isEmpty()) {
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
