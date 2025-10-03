package io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import io.github.redstonemango.mangoplayer.logic.Finals;
import io.github.redstonemango.mangoplayer.logic.Playlist;
import io.github.redstonemango.mangoplayer.logic.Utilities;

import java.io.File;

public class PlaylistDataRepresentation {
    private final SimpleStringProperty name;
    private final SimpleStringProperty playedSongCount;
    private final SimpleStringProperty totalPlayTime;
    private final SimpleStringProperty songCount;
    private final SimpleObjectProperty<Image> graphic;
    private final Playlist playlist;

    public PlaylistDataRepresentation(Playlist playlist) {
        this.playlist = playlist;
        this.name = new SimpleStringProperty(playlist.getName());
        this.playedSongCount = new SimpleStringProperty(playlist.getPlayedSongCount() + "x");
        this.totalPlayTime = new SimpleStringProperty(Utilities.formatDuration(playlist.getPlayTime()));
        int songCountInt = playlist.getSongs().size();
        this.songCount = new SimpleStringProperty(songCountInt == 0 ? "No songs" : (songCountInt == 1 ? "1 song" : songCountInt + " songs"));

        File playlistGraphicFile = new File(Utilities.graphicPathFromPlaylist(playlist));
        if (playlistGraphicFile.exists()) {
            graphic = new SimpleObjectProperty<>(new Image(playlistGraphicFile.toURI().toString()));
        }
        else if (!playlist.getSongs().isEmpty()) {
            File songThumbnail = new File(Utilities.thumbnailPathFromSong(playlist.getSongs().getFirst()));
            if (songThumbnail.exists()) {
                graphic = new SimpleObjectProperty<>(new Image(songThumbnail.toURI().toString()));
            }
            else {
                graphic = new SimpleObjectProperty<>(Finals.IMAGE_THUMBNAIL_FALLBACK);
            }
        }
        else {
            graphic = new SimpleObjectProperty<>(Finals.IMAGE_NO_SONG);
        }
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleObjectProperty<Image> graphicProperty() {
        return graphic;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public SimpleStringProperty totalPlayTimeProperty() {
        return totalPlayTime;
    }

    public SimpleStringProperty playedSongCountProperty() {
        return playedSongCount;
    }

    public SimpleStringProperty songCountProperty() {
        return songCount;
    }
}
