package io.github.redstonemango.mangoplayer.front.controller.textDisplay;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import io.github.redstonemango.mangoplayer.back.Utilities;

import java.io.IOException;

public class TextDisplay extends Scene {
    private final FXMLLoader loader;
    private final String header;
    private final String markdown;
    private TextDisplay(FXMLLoader loader, String header, String markdown) throws IOException {
        super(loader.load());
        this.loader = loader;
        this.header = header;
        this.markdown = markdown;
    }
    private static TextDisplay createNewScene(String header, String markdown) {
        try {
            FXMLLoader loader = new FXMLLoader(TextDisplay.class.getResource("/io/github/redstonemango/mangoplayer/fxml/util/textDisplay.fxml"));
            return new TextDisplay(loader, header, markdown);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getMarkdown() {
        return markdown;
    }

    protected String getHeader() {
        return header;
    }

    public static Stage showNewDisplay(Window parent, String title, String header, String markdown) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(MangoPlayer.primaryStage);
        stage.setTitle(title);
        TextDisplay scene = createNewScene(header, markdown);
        Utilities.prepareAndShowStage(stage, scene, scene.loader);
        stage.setX(parent.getX() + 50);
        stage.setY(parent.getY() + 50);
        return stage;
    }
}
