package io.github.redstonemango.mangoplayer.front.controller.songManager;

import io.github.redstonemango.mangoplayer.back.Finals;
import io.github.redstonemango.mangoplayer.back.Utilities;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.back.YtDlpManager;

import java.io.IOException;

public class SongDownloadResultController implements IInitializable {
    @FXML private Label headerLabel;
    @FXML private ProgressBar loadingBar;
    @FXML private VBox resultsVBox;

    @Override
    public void init() {
        if (headerLabel.getScene() instanceof SongDownloadResultScene scene) {
            scene.registerController(this);
            headerLabel.setText(scene.getHeaderText());
            scene.getWindow().setOnCloseRequest(_ -> scene.getOnKill().run());
        }
        headerLabel.getScene().getWindow().widthProperty().addListener((_, _, newValue) -> {
            loadingBar.setPrefWidth(newValue.intValue() - 40);
            resultsVBox.setPrefWidth(newValue.intValue());
            resultsVBox.getChildren().forEach(child -> {
                if (child instanceof DownloadResultEntry entry) {
                    entry.setPrefWidth(newValue.intValue());
                    entry.getTitleLabel().setPrefWidth(newValue.intValue() - 89);
                    entry.getUrlLabel().setPrefWidth(newValue.intValue() - 88);
                }
            });
        });
        headerLabel.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeStage();
            }
        });
    }

    protected void onFinished() {
        loadingBar.setProgress(1);
    }

    protected void onNewResultInformation(YtDlpManager.SearchResult searchResult) {
        if (headerLabel.getScene() instanceof SongDownloadResultScene scene) {
            double currentWidth = headerLabel.getScene().getWindow().getWidth();
            DownloadResultEntry entry = new DownloadResultEntry(searchResult, scene.getOnSelected(), this);
            resultsVBox.getChildren().add(entry);
            entry.setPrefWidth(currentWidth);
            entry.getTitleLabel().setPrefWidth(currentWidth - 89);
            entry.getUrlLabel().setPrefWidth(currentWidth - 88);

            if (entry.isValidResult()) {
                System.out.println("Found new search result with name '" + searchResult.getName() + "' and URL '" + searchResult.getUrl() + "'");
            }
            else {
                System.err.println("Received an invalid search result from yt-dlp. Result data are:");
                System.err.println("<Name>  " + searchResult.getName());
                System.err.println("<URL>  " + searchResult.getUrl());

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("MangoPlayer | Invalid Search Result");
                alert.setHeaderText("Received an invalid search result from yt-dlp");
                alert.setContentText("Please consider updating yt-dlp using 'MangoPlayer -> Yt-dlp -> Update'");
                alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
                alert.show();
            }
        }
    }

    protected void closeStage() {
        if (headerLabel.getScene() instanceof SongDownloadResultScene scene) {
            scene.getOnKill().run();
        }
        ((Stage) headerLabel.getScene().getWindow()).close();
    }
}