package io.github.redstonemango.mangoplayer.front.controller.songManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.ISongSelectable;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.ISongViewable;
import io.github.redstonemango.mangoplayer.back.Playlist;
import io.github.redstonemango.mangoplayer.back.Song;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public class SongListScene extends Scene {
    private final FXMLLoader loader;
    private final @Nullable ISongSelectable selectionController;
    private final @Nullable ISongViewable viewingController;
    private final @Nullable String playlistName;
    private final boolean selectionMode;
    public SongListScene(boolean selectionMode, @Nullable ISongSelectable selectionController, @Nullable ISongViewable viewingController, @Nullable String playlistName, FXMLLoader loader) throws IOException {
        super(loader.load());
        this.selectionMode = selectionMode;
        this.selectionController = selectionController;
        this.viewingController = viewingController;
        this.loader = loader;
        this.playlistName = playlistName;
    }
    public static SongListScene createViewingScene(ISongViewable controller) {
        try {
            FXMLLoader loader = new FXMLLoader(SongListScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/songManager/songList.fxml"));
            return new SongListScene(false, null, controller, null, loader);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static SongListScene createSelectionScene(ISongSelectable controller, String playlistName) {
        try {
            FXMLLoader loader = new FXMLLoader(SongListScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/songManager/songList.fxml"));
            return new SongListScene(true, controller, null, playlistName, loader);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInSelectionMode() {
        return selectionMode;
    }

    public FXMLLoader getLoader() {
        return loader;
    }

    public @Nullable Playlist getSelectionParent() {
        return selectionController == null ? null : selectionController.getSelectionParent();
    }

    protected void sendSelectedSongInfo(Map<Song, Boolean> changedSongs) {
        if (selectionController == null) throw new IllegalStateException("Method #sendSelectedSongInfo should only be called when in selection mode");
        selectionController.onSelectionProcessContentChanges(changedSongs);
    }

    protected ISongViewable getMainWindowController() {
        if (viewingController == null) throw new IllegalStateException("Method #getMainWindowController should only be called when not in selection mode");
        return viewingController;
    }

    protected void sendWindowClosedInfo() {
        if (viewingController != null) {
            viewingController.onSongViewClosed();
        }
    }

    public @Nullable String getPlaylistName() {
        return playlistName;
    }
}
