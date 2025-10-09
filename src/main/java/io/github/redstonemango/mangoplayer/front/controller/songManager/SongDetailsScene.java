package io.github.redstonemango.mangoplayer.front.controller.songManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.ISongViewable;
import io.github.redstonemango.mangoplayer.back.Song;

import java.io.IOException;

public class SongDetailsScene extends Scene {
    private final FXMLLoader loader;
    private final Song song;
    private final ISongViewable mainWindowController;
    public SongDetailsScene(Song song, FXMLLoader loader, ISongViewable mainWindowController) throws IOException {
        super(loader.load());
        this.song = song;
        this.loader = loader;
        this.mainWindowController = mainWindowController;
    }
    public static SongDetailsScene createNewScene(Song song, ISongViewable mainWindowController) {
        try {
            FXMLLoader loader = new FXMLLoader(SongDetailsScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/songManager/songDetails.fxml"));
            return new SongDetailsScene(song, loader, mainWindowController);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Song getSong() {
        return song;
    }

    public FXMLLoader getLoader() {
        return loader;
    }

    public ISongViewable getMainWindowController() {
        return mainWindowController;
    }
}
