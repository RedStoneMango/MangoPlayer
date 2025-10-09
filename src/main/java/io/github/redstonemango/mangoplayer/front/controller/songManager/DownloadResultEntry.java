package io.github.redstonemango.mangoplayer.front.controller.songManager;

import io.github.redstonemango.mangoutils.LogManager;
import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import io.github.redstonemango.mangoplayer.front.entryBases.DownloadResultEntryBase;
import io.github.redstonemango.mangoplayer.back.Utilities;
import io.github.redstonemango.mangoplayer.back.YtDlpManager;

import java.util.function.Predicate;

public class DownloadResultEntry extends DownloadResultEntryBase {

    private final YtDlpManager.SearchResult searchResult;
    private final Predicate<YtDlpManager.SearchResult> onSelected;
    private final SongDownloadResultController controller;

    public DownloadResultEntry(final YtDlpManager.SearchResult searchResult, Predicate<YtDlpManager.SearchResult> onSelected, SongDownloadResultController controller) {
        this.searchResult = searchResult;
        this.onSelected = onSelected;
        this.controller = controller;

        if (Utilities.isValidYoutubeLink(searchResult.getUrl())) {
            titleLabel.setText(searchResult.getName());
            urlLabel.setText(searchResult.getUrl());
        }
        else {
            titleLabel.setTextFill(Color.RED);
            urlLabel.setTextFill(Color.RED);
            titleLabel.setText("Got an invalid answer from yt-dlp");
            urlLabel.setText("You should now see an update prompt");
            button.setDisable(true);
        }
    }

    protected Label getTitleLabel() {
        return titleLabel;
    }

    protected Label getUrlLabel() {
        return urlLabel;
    }

    public boolean isValidResult() {
        return !button.isDisabled();
    }

    @Override
    protected void select(ActionEvent actionEvent) {
        if (onSelected.test(searchResult)) {
            controller.closeStage();
        }
    }

    @Override
    protected void onLinkOpen(MouseEvent mouseEvent) {
        if (isValidResult()) OperatingSystem.loadCurrentOS().open(searchResult.getUrl());
        else OperatingSystem.loadCurrentOS().open(LogManager.resolveTodayLog());
    }
}
