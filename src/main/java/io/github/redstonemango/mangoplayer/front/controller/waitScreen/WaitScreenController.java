package io.github.redstonemango.mangoplayer.front.controller.waitScreen;

import io.github.redstonemango.mangoplayer.front.controller.interfaces.IInitializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
