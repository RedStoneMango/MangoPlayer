package io.github.redstonemango.mangoplayer.front.controller.playlistScreen;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import io.github.redstonemango.mangoplayer.back.Playlist;

import java.io.IOException;

public class PlaylistScreenScene extends Scene {
    private final FXMLLoader loader;
    private final Playlist playlist;
    public PlaylistScreenScene(Playlist playlist, FXMLLoader loader) throws IOException {
        super(loader.load());
        this.playlist = playlist;
        this.loader = loader;
    }
    public static PlaylistScreenScene createNewScene(Playlist playlist) {
        try {
            FXMLLoader loader = new FXMLLoader(PlaylistScreenScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/playlistScreen/playlistScreen.fxml"));
            return new PlaylistScreenScene(playlist, loader);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}
