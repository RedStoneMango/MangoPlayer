package io.github.redstonemango.mangoplayer.graphic.controller.songManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.logic.YtDlpManager;

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
                System.err.println("<Result header>  " + searchResult.getName());
                System.err.println("<Result footer>  " + searchResult.getUrl());
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