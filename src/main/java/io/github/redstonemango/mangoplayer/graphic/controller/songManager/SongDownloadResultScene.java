package io.github.redstonemango.mangoplayer.graphic.controller.songManager;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import io.github.redstonemango.mangoplayer.logic.YtDlpManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Predicate;

public class SongDownloadResultScene extends Scene {
    private final FXMLLoader loader;
    private final String headerText;
    private final Runnable onKill;
    private final Predicate<YtDlpManager.SearchResult> onSelected;
    private @Nullable SongDownloadResultController controller;
    public SongDownloadResultScene(FXMLLoader loader, String headerText, Runnable onKill, Predicate<YtDlpManager.SearchResult> onSelected) throws IOException {
        super(loader.load());
        this.loader = loader;
        this.onKill = onKill;
        this.headerText = headerText;
        this.onSelected = onSelected;
    }
    public static SongDownloadResultScene createNewScene(String headerText, Runnable onKill, Predicate<YtDlpManager.SearchResult> onSelected) {
        try {
            FXMLLoader loader = new FXMLLoader(SongDownloadResultScene.class.getResource("/io/github/redstonemango/mangoplayer/fxml/songManager/songDownloadResult.fxml"));
            return new SongDownloadResultScene(loader, headerText, onKill, onSelected);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendNewResultInformation(YtDlpManager.SearchResult searchResult) {
        Platform.runLater(() -> {
            if (controller != null) {
                controller.onNewResultInformation(searchResult);
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

    public Runnable getOnKill() {
        return onKill;
    }

    public Predicate<YtDlpManager.SearchResult> getOnSelected() {
        return onSelected;
    }

    public String getHeaderText() {
        return headerText;
    }

    protected void registerController(SongDownloadResultController controller) {
        this.controller = controller;
    }
}
