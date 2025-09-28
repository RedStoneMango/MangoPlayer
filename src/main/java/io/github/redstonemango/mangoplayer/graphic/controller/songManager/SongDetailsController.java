package io.github.redstonemango.mangoplayer.graphic.controller.songManager;

import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IProcessExecuteable;
import io.github.redstonemango.mangoplayer.graphic.controller.playlistScreen.PlaylistScreenController;
import io.github.redstonemango.mangoplayer.logic.*;
import io.github.redstonemango.mangoplayer.logic.config.MainConfigWrapper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

public class SongDetailsController implements IInitializable, IProcessExecuteable {
    private Song song;

    private final double sMiddleSectionBounds = 5;

    private @Nullable MediaPlayer testListenPlayer = null;
    private double msMiddleSectionStart = -1;

    @FXML private TextField songNameField;
    @FXML private ImageView songThumbnailView;
    @FXML private ImageView editThumbnailImage;
    @FXML private Label playStatLabel;
    @FXML private Label useStatLabel;
    @FXML private Label sourceStatLabel;
    @FXML private Label idStatLabel;
    @FXML private Button backButton;
    @FXML private Slider volumeAdjustmentSlider;
    @FXML private ImageView testListenButtonIcon;

    @Override
    public void init() {
        if (songNameField.getScene() instanceof SongDetailsScene scene) {
            this.song = scene.getSong();
        }

        songNameField.setText(song.getName());
        songNameField.textProperty().addListener((_, _, newValue) -> {
            backButton.setDisable(newValue.isBlank());
        });
        Utilities.applyHeldPropertyListener(this, songNameField.getScene().getWindow().widthProperty(), (_, _, newValue) -> {
            songNameField.setPrefWidth(newValue.doubleValue() - 130);
            volumeAdjustmentSlider.setPrefWidth(newValue.doubleValue() - 210);
        });
        songNameField.getScene().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE && !backButton.isDisabled()) onBackButton();
        });

        volumeAdjustmentSlider.valueProperty().addListener((_, _, volume) -> {
            song.setVolumeAdjustment(volume.doubleValue());
            PlaylistAudioManager.updateVolume();
            if (testListenPlayer != null) {
                testListenPlayer.setVolume(MainConfigWrapper.loadConfig().volume * volume.doubleValue());
            }
        });

        songNameField.getScene().getWindow().setOnCloseRequest(_ -> {
            ((SongDetailsScene) songNameField.getScene()).getMainWindowController().onSongViewClosed();
            Utilities.removeHeldListeners(this);
            songNameField.getScene().getWindow().setOnCloseRequest(_ -> {}); // Override close listener
            if (!songNameField.getText().isBlank()) song.setName(songNameField.getText());
            if (testListenPlayer != null) {
                testListenPlayer.stop();
                testListenPlayer = null;
            }
        });

        volumeAdjustmentSlider.setValue(song.getVolumeAdjustment());


        // Init width
        songNameField.setPrefWidth(songNameField.getScene().getWindow().getWidth() - 130);

        File songThumbnailFile = new File(Utilities.thumbnailPathFromSong(song));
        if (songThumbnailFile.exists()) {
            songThumbnailView.setImage(new Image(songThumbnailFile.toURI().toString()));
        }

        playStatLabel.setText(song.getListenCount() + "x");
        useStatLabel.setText(song.summarizeUseCount() + "x");

        if (song.isFromYoutube()) {
            sourceStatLabel.setText(Utilities.youtubeUrlFromSong(song));
            sourceStatLabel.setTextFill(Color.BLUE);
            sourceStatLabel.setFont(new Font(null, 17));
            sourceStatLabel.setUnderline(true);
            sourceStatLabel.setCursor(Cursor.HAND);
            sourceStatLabel.setOnMouseClicked(_ -> {
                OperatingSystem.loadCurrentOS().open(Utilities.youtubeUrlFromSong(song));
            });
        }

        idStatLabel.setText(song.getId());
    }

    private boolean prepareAndStartPlayer() {
        Media songMedia;
        File audioFile = new File(Utilities.audioPathFromSong(song));
        if (audioFile.exists()) {
            try {
                songMedia = new Media(audioFile.toURI().toString());
                testListenPlayer = new MediaPlayer(songMedia);
            }
            catch (MediaException e) {
                System.err.println("Media error instantiating audio playback objects for volume test: " + e);
                if (e.getType() == MediaException.Type.UNKNOWN) {
                    Utilities.showCodecErrorMessage();
                }
                testListenPlayer = null;
                return false;
            }
        }
        else {
            Utilities.showErrorScreen("Play '" + song.getName() + "'", "The audio asset for the song could not be found.\nPlease try re-downloading/importing the song");
            return false;
        }

        testListenPlayer.setOnReady(() -> {
            testListenPlayer.setVolume(MainConfigWrapper.loadConfig().volume * song.getVolumeAdjustment());
            msMiddleSectionStart = Math.max(songMedia.getDuration().toMillis() / 2 - sMiddleSectionBounds * 1000, 0);
            testListenPlayer.seek(Duration.millis(msMiddleSectionStart));
            testListenPlayer.play();
        });

        testListenPlayer.currentTimeProperty().addListener((_, _, currentTime) -> {
            double currentMs = currentTime.toMillis();
            if (currentMs > msMiddleSectionStart + sMiddleSectionBounds * 1000) { // Middle section ended
                testListenPlayer.stop();
                testListenPlayer = null;
                testListenButtonIcon.setImage(Finals.IMAGE_PLAY_THIS);
            }
        });
        testListenPlayer.setOnEndOfMedia(() -> {
            testListenButtonIcon.setImage(Finals.IMAGE_PLAY_THIS);
            testListenPlayer = null;
        });
        return true;
    }

    @FXML
    private void onTestListenButton() {
        if (testListenPlayer == null) {
            if (prepareAndStartPlayer()) {
                testListenButtonIcon.setImage(Finals.IMAGE_STOP);
            }
        }
        else {
            testListenPlayer.stop();
            testListenPlayer = null;
            testListenButtonIcon.setImage(Finals.IMAGE_PLAY_THIS);
        }
    }

    @FXML
    private void onBackButton() {
        Utilities.removeHeldListeners(this);
        songNameField.getScene().getWindow().setOnCloseRequest(_ -> {}); // Override close listener
        song.setName(songNameField.getText());
        if (testListenPlayer != null) {
            testListenPlayer.stop();
            testListenPlayer = null;
        }

        Stage stage = (Stage) songNameField.getScene().getWindow();
        SongDetailsScene thisScene = (SongDetailsScene) songNameField.getScene();
        if (thisScene.getMainWindowController() instanceof PlaylistScreenController) {
            thisScene.getMainWindowController().onSongViewClosed();
            stage.hide();
        }
        else {
            SongListScene scene = SongListScene.createViewingScene(thisScene.getMainWindowController());
            stage.setTitle("MangoPlayer | Song manager");
            Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
        }
    }

    @FXML
    private void onDeleteButton() {
        if (song.askAndRunDelete()) {
            onBackButton();
        }
    }

    @FXML
    private void onThumbnailEdit() {
        editThumbnailImage.setVisible(false);
        File songThumbnailFile = new File(Utilities.thumbnailPathFromSong(song));
        ButtonType removeButtonType = new ButtonType("Remove", ButtonBar.ButtonData.LEFT);
        ButtonType replaceButtonType = new ButtonType("Replace", ButtonBar.ButtonData.LEFT);
        ButtonType regenButtonType = new ButtonType("Regenerate", ButtonBar.ButtonData.LEFT);
        boolean canRegen = song.isFromYoutube();

        // show dialogs
        if (songThumbnailFile.exists()) {
            Alert alert;
            if (canRegen) alert = new Alert(Alert.AlertType.NONE, "", removeButtonType, replaceButtonType, regenButtonType, ButtonType.CANCEL);
            else alert = new Alert(Alert.AlertType.NONE, "", removeButtonType, replaceButtonType, ButtonType.CANCEL);
            alert.setTitle("MangoPlayer | Selection");
            alert.setHeaderText("How do you want to edit the thumbnail?");
            alert.setContentText("You can either remove or it replace with a new one" + (canRegen ? ".\nAs this song was loaded from YouTube, you're also able to regenerate the original thumbnail" : ""));
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.showAndWait();
            if (alert.getResult() == replaceButtonType) thumbnailReplace();
            if (alert.getResult() == regenButtonType) thumbnailRegen();
            if (alert.getResult() == removeButtonType) thumbnailRemove();
        }
        else {
            if (canRegen) {
                Alert alert = new Alert(Alert.AlertType.NONE, "", replaceButtonType, regenButtonType, ButtonType.CANCEL);
                alert.setTitle("MangoPlayer | Selection");
                alert.setHeaderText("How do you want to edit the thumbnail?");
                alert.setContentText("You can either set a custom one or regenerate the original YouTube thumbnail");
                alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
                alert.showAndWait();
                if (alert.getResult() == replaceButtonType) thumbnailReplace();
                if (alert.getResult() == regenButtonType) thumbnailRegen();
            }
            else {
                thumbnailReplace();
            }
        }
    }

    private void thumbnailRemove() {
        File thumbnailAsset = new File(Utilities.thumbnailPathFromSong(song));
        try {
            Files.delete(thumbnailAsset.toPath());
            System.out.println("Removed the thumbnail for song '" + song.getName() + "' (ID is '" + song.getId() + "')");
        }
        catch (IOException e) {
            Utilities.showErrorScreen("Remove thumbnail", "Cannot delete: " + e.getMessage());
            processFinished(song, null, false, IProcessExecuteable.THUMBNAIL_DOWNLOAD_PROCESS_TYPE);
        }
        processFinished(song, null, true, IProcessExecuteable.THUMBNAIL_DOWNLOAD_PROCESS_TYPE);
    }
    private void thumbnailRegen() {
        if (YtDlpManager.ensureConnection() && YtDlpManager.getInstance().checkAvailable() && YtDlpManager.getInstance().checkFfmpegAvailable()) {
            File thumbnailAsset = new File(Utilities.thumbnailPathFromSong(song));
            if (thumbnailAsset.exists()) {
                try {
                    Files.delete(thumbnailAsset.toPath());
                }
                catch (IOException e) {
                    Utilities.showErrorScreen("Remove thumbnail", String.valueOf(e));
                    processFinished(song, null, false, IProcessExecuteable.THUMBNAIL_DOWNLOAD_PROCESS_TYPE);
                }
            }
            if (!thumbnailAsset.exists()) {
                this.startThumbnailDownloadProcess(song);
                System.out.println("Regeneration of the thumbnail for song '" + song.getName() + "' (ID is '" + song.getId() + "') finished");
            }
        }
    }
    private void thumbnailReplace() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("MangoPlayer | Replace thumbnail");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image file", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.tif", "*.tiff", "*.gif", "*.webp", "*.heic", "*.heif", "*.avif", "*.ppm", "*.pgm", "*.pbm", "*.pnm", "*.jp2", "*.j2k", "*.jpf", "*.tga", "*.psd", "*.exr", "*.svg"));
        File newThumbnail = chooser.showOpenDialog(songNameField.getScene().getWindow());
        if (newThumbnail != null) {
            FfmpegFileConverter.getInstance().convertFilesToPng(Collections.singletonList(newThumbnail)).thenAccept(pngFiles -> {
                if (!pngFiles.isEmpty()) {
                    if (pngFiles.getFirst().getAbsolutePath().equals(Utilities.thumbnailPathFromSong(song))) {
                        Utilities.showErrorScreen("Replace thumbnail", "Cannot replace thumbnail with itself");
                        processFinished(song, null, false, IProcessExecuteable.THUMBNAIL_DOWNLOAD_PROCESS_TYPE);
                        return;
                    }

                    try {
                        new File(Utilities.thumbnailPathFromSong(song)).getParentFile().mkdirs();
                        Files.copy(pngFiles.getFirst().toPath(), new File(Utilities.thumbnailPathFromSong(song)).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Updated thumbnail for song '" + song.getName() + "' (ID is '" + song.getId() + "')");
                    }
                    catch (IOException e) {
                        Utilities.showErrorScreen("Replace thumbnail", String.valueOf(e));
                        processFinished(song, null, false, IProcessExecuteable.THUMBNAIL_DOWNLOAD_PROCESS_TYPE);
                    }
                }
                processFinished(song, null, true, IProcessExecuteable.THUMBNAIL_DOWNLOAD_PROCESS_TYPE);
                FfmpegFileConverter.getInstance().cleanupFilesToPngConversion();
            });
        }
    }

    @FXML
    private void onMouseThumbnailEnter() {
        editThumbnailImage.setVisible(true);
    }
    @FXML
    private void onMouseThumbnailExit() {
        editThumbnailImage.setVisible(false);
    }

    @Override
    public void processFinished(Object source, @Nullable Object additionalData, boolean success, int processType) {
        if (source instanceof Song thumbnailOwnerSong && processType == THUMBNAIL_DOWNLOAD_PROCESS_TYPE) {
            File songThumbnailFile = new File(Utilities.thumbnailPathFromSong(thumbnailOwnerSong));
            if (songThumbnailFile.exists()) {
                songThumbnailView.setImage(new Image(songThumbnailFile.toURI().toString()));
            }
            else {
                songThumbnailView.setImage(new Image(getClass().getResourceAsStream("/io/github/redstonemango/mangoplayer/images/thumbnail_fallback.png")));
            }
        }
    }
}
