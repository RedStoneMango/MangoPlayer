package io.github.redstonemango.mangoplayer.graphic.controller.processExecution;

import io.github.redstonemango.mangoplayer.logic.YtDlpManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;

public class ProcessExecutionController implements IInitializable {

    @FXML private Label processNameLabel;
    @FXML private Label processStatusLabel;
    @FXML private TextArea originCmdArea;

    @Override
    public void init() {
        processNameLabel.getScene().getWindow().setOnCloseRequest(_ -> onClose());

        processNameLabel.getScene().getWindow().widthProperty().addListener((_, _, newValue) -> {
            processStatusLabel.setPrefWidth(newValue.intValue() - 82);
        });
        if (processNameLabel.getScene() instanceof ProcessExecutionScene scene) {
            scene.registerController(this);
            processNameLabel.setText(scene.getProcessName());
            originCmdArea.setText(scene.getOriginCmd());
            System.out.println("Running external process '" + scene.getProcessName() + "' using command '" + scene.getOriginCmd() + "':");
        }
    }

    protected void onStatusUpdate(String newStatus) {
        processStatusLabel.setText(newStatus);
        System.out.println("<Process Output>  " + newStatus);
    }

    protected void onFinished() {
        ProcessExecutionScene scene = (ProcessExecutionScene) processNameLabel.getScene();
        System.out.println("Process '" + scene.getProcessName() + "' finished");
        ((Stage)processNameLabel.getScene().getWindow()).close();
    }

    private void onClose() {
        YtDlpManager.getInstance().destroyRunningProcess(true);
    }
}
