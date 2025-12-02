package io.github.redstonemango.mangoplayer.front.controller.playlistScreen;

import javafx.fxml.FXMLLoader;
import io.github.redstonemango.mangoplayer.front.OverlayWindow;

import java.io.IOException;

public class PlayControlWindow extends OverlayWindow {
    private final FXMLLoader loader;
    private final PlaylistScreenController bindingSource;
    public PlayControlWindow(PlaylistScreenController bindingSource, FXMLLoader loader) throws IOException {
        super(loader.load(), null, null, true);
        this.bindingSource = bindingSource;
        this.loader = loader;
        getRoot().setStyle("-fx-background-color: -fx-background;"); // Enforce background color to prevent transparency
        setOnHiding(_ -> {
            ((PlayControlController) loader.getController()).destroy();
            ((PlayControlController) loader.getController()).hidePopups();
        });
        xProperty().addListener((_, _, _) -> ((PlayControlController) loader.getController()).hidePopups());
        yProperty().addListener((_, _, _) -> ((PlayControlController) loader.getController()).hidePopups());

    }
    public static PlayControlWindow createNewWindow(PlaylistScreenController bindingSource) {
        try {
            FXMLLoader loader = new FXMLLoader(PlayControlWindow.class.getResource("/io/github/redstonemango/mangoplayer/fxml/playlistScreen/playControl.fxml"));
            PlayControlWindow window = new PlayControlWindow(bindingSource, loader);
            window.setName("MangoPlayer | Song control");
            return window;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PlaylistScreenController getBindingSource() {
        return bindingSource;
    }

    public FXMLLoader getLoader() {
        return loader;
    }
}
