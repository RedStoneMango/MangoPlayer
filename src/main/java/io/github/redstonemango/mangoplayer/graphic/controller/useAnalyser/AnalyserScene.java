package io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.ISongViewable;

import java.io.IOException;
import java.util.List;

public class AnalyserScene extends Scene {
    private final FXMLLoader loader;
    private final ISongViewable mainWindowController;
    private final List<PlaylistDataRepresentation> playlistData;

    private final List<SongDataRepresentation> songData;

    public AnalyserScene(FXMLLoader loader, ISongViewable mainWindowController, List<PlaylistDataRepresentation> playlistData, List<SongDataRepresentation> songData) throws IOException {
        super(loader.load());
        this.loader = loader;
        this.mainWindowController = mainWindowController;
        this.playlistData = playlistData;
        this.songData = songData;
    }

    public static AnalyserScene createNewScene(ISongViewable mainWindowController, List<PlaylistDataRepresentation> playlistData, List<SongDataRepresentation> songData) {
        try {
            FXMLLoader loader = new FXMLLoader(AnalyserScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/useAnalyser/analyserScreen.fxml"));
            return new AnalyserScene(loader, mainWindowController, playlistData, songData);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public FXMLLoader getLoader() {
        return loader;
    }

    public ISongViewable getMainWindowController() {
        return mainWindowController;
    }

    public List<SongDataRepresentation> getSongData() {
        return songData;
    }

    public List<PlaylistDataRepresentation> getPlaylistData() {
        return playlistData;
    }
}
