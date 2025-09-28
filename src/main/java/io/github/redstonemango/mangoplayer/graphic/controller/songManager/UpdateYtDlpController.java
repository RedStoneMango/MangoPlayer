package io.github.redstonemango.mangoplayer.graphic.controller.songManager;

import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.logic.SongAdding;
import io.github.redstonemango.mangoplayer.logic.Utilities;
import io.github.redstonemango.mangoplayer.logic.YtDlpManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

import java.util.List;

public class UpdateYtDlpController implements IInitializable {

    private boolean updateSuccess = false;

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
        if (!updateSuccess) {
            updateButton.setDisable(true);

            System.out.println("Performing an update for yt-dlp using '" + constructUserFriendlyUpdateCommand());
            updateSuccess = YtDlpManager.getInstance().performSelfUpdate(line -> {
                outputField.appendText("\n" + line);
                System.out.println("<Process Output>  " + line);
            });
            outputField.appendText("\n-------------------\n\n" + "$ " + constructUserFriendlyUpdateCommand());
            if (updateSuccess) System.out.println("The yt-dlp update was successful");
            else System.err.println("An error occurred while updating yt-dlp");

            if (updateSuccess) {
                header1.setTextFill(Color.GREEN);
                header1.setText("Successfully updated yt-dlp!");
                header2.setTextFill(Color.GREEN);
                header2.setText("The YouTube access should now work again");
                header3.setTextFill(Color.GREEN);
                header3.setText("Would you like to run your last search again?");
                updateButton.setText("Retry YouTube search");
            }
            updateButton.setDisable(false);

            return;
        }

        onCloseButton();
        SongAdding.INSTANCE.startSearch(SongAdding.INSTANCE.getLastSearch());
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
