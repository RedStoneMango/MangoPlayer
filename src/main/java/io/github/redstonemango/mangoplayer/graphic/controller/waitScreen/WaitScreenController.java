package io.github.redstonemango.mangoplayer.graphic.controller.waitScreen;

import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class WaitScreenController implements IInitializable {

    private @FXML Label heading;
    private @FXML Label content;

    @Override
    public void init() {
        WaitScreenScene scene = (WaitScreenScene) heading.getScene();

        heading.setText(scene.getHeading());
        content.setText(scene.getContent());
    }
}
