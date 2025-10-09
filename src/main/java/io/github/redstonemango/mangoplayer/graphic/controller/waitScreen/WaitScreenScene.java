package io.github.redstonemango.mangoplayer.graphic.controller.waitScreen;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class WaitScreenScene extends Scene {
    private final FXMLLoader loader;
    private final String heading;
    private final String content;

    public WaitScreenScene(FXMLLoader loader, String heading, String content) throws IOException {
        super(loader.load());
        this.loader = loader;
        this.heading = heading;
        this.content = content;
    }

    public static WaitScreenScene createNewScene(String heading, String content) {
        try {
            FXMLLoader loader = new FXMLLoader(WaitScreenScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/util/wait.fxml"));
            return new WaitScreenScene(loader, heading, content);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public FXMLLoader getLoader() {
        return loader;
    }

    public String getHeading() {
        return heading;
    }

    public String getContent() {
        return content;
    }
}
