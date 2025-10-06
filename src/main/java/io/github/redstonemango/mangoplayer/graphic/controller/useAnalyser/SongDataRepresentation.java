package io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser;

import io.github.redstonemango.mangoplayer.graphic.ComboBoxSearching;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import io.github.redstonemango.mangoplayer.logic.Finals;
import io.github.redstonemango.mangoplayer.logic.Song;
import io.github.redstonemango.mangoplayer.logic.Utilities;

import java.io.File;
import java.util.Locale;

public class SongDataRepresentation implements ComboBoxSearching.ISearchComparable {
    private final SimpleStringProperty name;
    private final SimpleStringProperty listenCount;
    private final SimpleStringProperty useCount;
    private final SimpleObjectProperty<Image> thumbnail;
    private final Song song;

    public SongDataRepresentation(Song song) {
        this.song = song;
        this.name = new SimpleStringProperty(song.getName());
        this.listenCount = new SimpleStringProperty(song.getListenCount() + "x");
        this.useCount = new SimpleStringProperty(song.summarizeUseCount() + "x");

        File thumbnailExpected = new File(Utilities.thumbnailPathFromSong(song));
        if (thumbnailExpected.exists()) {
            this.thumbnail = new SimpleObjectProperty<>(new Image(thumbnailExpected.toURI().toString()));
        }
        else {
            this.thumbnail = new SimpleObjectProperty<>(Finals.IMAGE_THUMBNAIL_FALLBACK);
        }
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty listenCountProperty() {
        return listenCount;
    }

    public SimpleStringProperty useCountProperty() {
        return useCount;
    }

    public SimpleObjectProperty<Image> thumbnailProperty() {
        return thumbnail;
    }

    public Song getSong() {
        return song;
    }

    @Override
    public boolean matches(String typedText) {
        return name.get().toLowerCase(Locale.ROOT).contains(typedText.toLowerCase());
    }

    @Override
    public String toString() {
        return name.get();
    }
}
