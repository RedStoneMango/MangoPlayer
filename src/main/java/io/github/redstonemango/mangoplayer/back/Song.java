package io.github.redstonemango.mangoplayer.back;

import com.google.gson.annotations.Expose;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import io.github.redstonemango.mangoplayer.back.config.PlaylistConfigWrapper;
import io.github.redstonemango.mangoplayer.back.config.SongConfigWrapper;
import javafx.util.Duration;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicInteger;

public class Song implements Comparable<Song>, Serializable {
    @Expose private String name;
    private String id;
    private @Nullable Duration duration;
    @Expose private final @Nullable String youtubeId;
    @Expose private long listenCount;
    @Expose private double volumeAdjustment;
    public Song(String name,  @Nullable String youtubeId, long listenCount, double volumeAdjustment) {
        this.id = UniqueIdGenerator.generateUniqueString(UniqueIdGenerator.IdUse.SONG_ID);
        this.name = name;
        this.listenCount = listenCount;
        this.youtubeId = youtubeId;
        this.volumeAdjustment = Math.clamp(volumeAdjustment, 0.01, 1);
    }

    public void ensureFields(String requiredId) {
        id = requiredId;

        if (name == null || name.isBlank()) {
            name = "Unnamed Song";
            System.err.println("Song with ID '" + id + "' does not have a name. Initialized name to 'Unnamed Song'");
        }

        volumeAdjustment = Math.clamp(volumeAdjustment, 0.01, 1);
    }

    // === Lazy-Load Duration Value To Simplify Tag-Read For Analyzer === //
    public @Nullable Duration loadDuration() {
        if (duration == null) {
            try {
                AudioFile af = AudioFileIO.read(new File(Utilities.audioPathFromSong(this)));
                duration = Duration.seconds(af.getAudioHeader().getTrackLength());
            } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException _) {
                return null;
            }
        }
        return duration;
    }
    public void registerDurationIfNeeded(Duration duration) {
        if (this.duration == null) {
            this.duration = duration;
        }
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

    public long getListenCount() {
        return listenCount;
    }

    public void increaseListenCount() {
        listenCount++;
    }

    public @Nullable String getYoutubeId() {
        return youtubeId;
    }

    public boolean isFromYoutube() {
        return youtubeId != null;
    }

    /**
     * <font color=red>!!! This method walks through every song in every playlist and therefore should not be called too frequently. When wanting to re-use data more often, it is recommended to cache them !!!</font>
     */
    public int summarizeUseCount() {
        AtomicInteger integer = new AtomicInteger(0);
        PlaylistConfigWrapper.loadConfig().playlists.forEach(playlist -> {
            playlist.getSongs().forEach(song -> {
                if (song.id.equals(this.id)) integer.incrementAndGet();
            });
        });
        return integer.get();
    }

    @Override
    public int compareTo(@NotNull Song o) {
        return this.name.compareToIgnoreCase(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Song oSong)) return false;
        return oSong.id.equals(this.id);
    }

    public static Song songFromId(String id) {
        return SongConfigWrapper.loadConfig().songs.get(id);
    }

    public double getVolumeAdjustment() {
        return volumeAdjustment;
    }

    public void setVolumeAdjustment(double volumeAdjustment) {
        this.volumeAdjustment = volumeAdjustment;
    }

    public boolean exportToFile(File file) {
        return exportToFile(file, null);
    }

    public boolean exportToFile(File file, @Nullable String albumName) {
        File audioAsset = new File(Utilities.audioPathFromSong(this));

        try {
            MP3File mp3File = new MP3File(audioAsset);

            Tag tag = mp3File.getTag();
            if (tag == null) {
                tag = new ID3v24Tag();
                mp3File.setTag(tag);
            }
            tag.setField(FieldKey.TITLE, name);
            if (albumName != null) tag.setField(FieldKey.ALBUM, albumName);

            File thumbnail = new File(Utilities.thumbnailPathFromSong(this));
            if (thumbnail.exists()) tag.addField(StandardArtwork.createArtworkFromFile(thumbnail));

            Files.copy(audioAsset.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            mp3File.save(file);
            System.out.println("Exported song '" + name + "' (ID is '" + id + "') to file '" + file.getAbsolutePath() + "'");
            return true;
        } catch (Exception e) {
            Utilities.showErrorScreen("Export song '" + name + "'", String.valueOf(e), false);
            return false;
        }
    }

    public boolean askAndRunDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("MangoPlayer | Confirmation");
        alert.setHeaderText("Do you really want to delete '" + name + "'?");
        alert.setContentText("The song will be removed from this apps memory entirely");
        alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            new File(Utilities.thumbnailPathFromSong(this)).delete();
            new File(Utilities.audioPathFromSong(this)).delete();
            SongConfigWrapper.loadConfig().songs.remove(this.getId());
            PlaylistConfigWrapper.loadConfig().playlists.forEach(playlist -> playlist.getSongs().remove(this));
            System.out.println("Deleted song '" + name + "' (ID was '" + id + "')");
            return true;
        }
        return false;
    }
}
