package io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.ISongViewable;

import java.io.IOException;

public class AnalyserScene extends Scene {
    private final FXMLLoader loader;
    private final ISongViewable mainWindowController;
    public AnalyserScene(FXMLLoader loader, ISongViewable mainWindowController) throws IOException {
        super(loader.load());
        this.loader = loader;
        this.mainWindowController = mainWindowController;
    }
    public static AnalyserScene createNewScene(ISongViewable mainWindowController) {
        try {
            FXMLLoader loader = new FXMLLoader(AnalyserScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/useAnalyser/analyserScreen.fxml"));
            return new AnalyserScene(loader, mainWindowController);
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
}
