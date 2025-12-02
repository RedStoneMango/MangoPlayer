package io.github.redstonemango.mangoplayer.front.controller.songManager;

import io.github.redstonemango.mangoplayer.front.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.back.Utilities;
import io.github.redstonemango.mangoplayer.back.YtDlpManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateYtDlpController implements IInitializable {

    @FXML TextArea outputField;
    @FXML Button updateButton;
    @FXML Button closeButton;
    @FXML Label header1;
    @FXML Label header2;
    @FXML Label header3;

    @Override
    public void init() {
        outputField.setText("$ " + constructUserFriendlyUpdateCommand());

        outputField.getScene().getWindow().setOnHiding(_ -> YtDlpManager.getInstance().destroyRunningProcess());
    }

    @FXML
    private void onUpdateButton() {
        updateButton.setDisable(true);

        System.out.println("Performing an update for yt-dlp using '" + constructUserFriendlyUpdateCommand());
        CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                YtDlpManager.getInstance().performSelfUpdate(line -> {
                    System.out.println("<Process Output>  " + line);
                    Platform.runLater(() -> outputField.appendText("\n" + line));
                }
        ));
        future.thenRun(() -> Platform.runLater(() -> {
            updateButton.setDisable(false);
            outputField.appendText("\n-------------------\n\n" + "$ " + constructUserFriendlyUpdateCommand());
        }));
    }

    public static String constructUserFriendlyUpdateCommand() {
        return Utilities.cmdStringFromList(List.of(
                YtDlpManager.getInstance().createSelfUpdateCommand()
        ));
    }

    @FXML
    private void onCloseButton() {
        outputField.getScene().getWindow().hide();
    }
}
