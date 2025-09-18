package io.github.redstonemango.mangoplayer.graphic.controller.processExecution;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ProcessExecutionScene extends Scene {
    private final FXMLLoader loader;
    private final String processName;
    private final String originCmd;
    private @Nullable ProcessExecutionController controller;
    public ProcessExecutionScene(FXMLLoader loader, String processName, String originCmd) throws IOException {
        super(loader.load());
        this.loader = loader;
        this.processName = processName;
        this.originCmd = originCmd;
    }
    public static ProcessExecutionScene createNewScene(String processName, String originCmd) {
        try {
            FXMLLoader loader = new FXMLLoader(ProcessExecutionScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/util/processExecution.fxml"));
            return new ProcessExecutionScene(loader, processName, originCmd);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendStatusUpdate(String newStatus) {
        Platform.runLater(() -> {
            if (controller != null) {
                controller.onStatusUpdate(newStatus);
            }
        });
    }
    public void sendEndInformation() {
        Platform.runLater(() -> {
            if (controller != null) {
                controller.onFinished();
            }
        });
    }

    public FXMLLoader getLoader() {
        return loader;
    }

    public String getProcessName() {
        return processName;
    }

    public String getOriginCmd() {
        return originCmd;
    }

    protected void registerController(ProcessExecutionController controller) {
        this.controller = controller;
    }
}
