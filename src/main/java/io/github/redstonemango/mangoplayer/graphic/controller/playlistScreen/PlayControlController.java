package io.github.redstonemango.mangoplayer.graphic.controller.playlistScreen;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Popup;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.logic.NativeHookManager;
import io.github.redstonemango.mangoplayer.logic.Utilities;
import io.github.redstonemango.mangoplayer.logic.config.MainConfigWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PlayControlController implements IInitializable, NativeKeyListener {

    private Popup volumePopup;
    private Popup pauseKeyPopup;
    private boolean showVolumePopup = false;
    private boolean showPauseKeyPopup = false;
    private boolean hoveringVolumePopup = false;
    private @Nullable Label newPauseKeyCombinationLabel = null;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private boolean combinationTriggered = false;
    private boolean gottaRestoreDefaultKeyPopupFace = false;

    @FXML private Tooltip controlSongNameTooltip;
    @FXML private ToggleButton controlStopAfterSongButton;
    @FXML private Label controlCurrentTimeLabel;
    @FXML private ImageView controlSongView;
    @FXML private Button controlShuffleButton;
    @FXML private Button controlPlayButton;
    @FXML private Button controlStopButton;
    @FXML private Button controlLoopButton;
    @FXML private Slider controlDurationSlider;
    @FXML private Label volumeContainer;
    @FXML private ImageView volumeImage;
    @FXML private Label pauseKeyLabel;
    @FXML private Button backwardButton;
    @FXML private Button forwardButton;

    private PlaylistScreenController bindingSource;

    @Override
    public void init() {
        NativeHookManager.getInstance().setChild(this);
        bindingSource = ((PlayControlWindow) controlLoopButton.getScene().getWindow()).getBindingSource();
        Slider volumeSlider = new Slider();

        // setup synchronisations
        Utilities.applyHeldPropertyListener(this, bindingSource.songNameTooltip.textProperty(), (_, _, s) -> controlSongNameTooltip.setText(s));
        Utilities.applyHeldPropertyListener(this, bindingSource.stopAfterSongButton.disableProperty(), (_, _, b) -> controlStopAfterSongButton.setDisable(b));
        Utilities.applyHeldPropertyListener(this, controlStopAfterSongButton.selectedProperty(), (_, _, b) -> bindingSource.stopAfterSongButton.setSelected(b));
        Utilities.applyHeldPropertyListener(this, bindingSource.stopAfterSongButton.selectedProperty(), (_, _, b) -> controlStopAfterSongButton.setSelected(b));
        Utilities.applyHeldPropertyListener(this, bindingSource.currentTimeLabel.textProperty(), (_, _, s) -> controlCurrentTimeLabel.setText(s));
        Utilities.applyHeldPropertyListener(this, bindingSource.bigSongView.imageProperty(), (_, _, i) -> controlSongView.setImage(i));
        Utilities.applyHeldPropertyListener(this, ((ImageView) bindingSource.shuffleButton.getGraphic()).imageProperty(), (_, _, i) -> ((ImageView) controlShuffleButton.getGraphic()).setImage(i));
        Utilities.applyHeldPropertyListener(this, ((ImageView) bindingSource.loopButton.getGraphic()).imageProperty(), (_, _, i) -> ((ImageView) controlLoopButton.getGraphic()).setImage(i));
        Utilities.applyHeldPropertyListener(this, ((ImageView) bindingSource.playButton.getGraphic()).imageProperty(), (_, _, i) -> ((ImageView) controlPlayButton.getGraphic()).setImage(i));
        Utilities.applyHeldPropertyListener(this, bindingSource.durationSlider.maxProperty(), (_, _, n) -> controlDurationSlider.setMax(n.doubleValue()));
        Utilities.applyHeldPropertyListener(this, bindingSource.durationSlider.valueProperty(), (_, _, n) -> controlDurationSlider.setValue(n.doubleValue()));
        Utilities.applyHeldPropertyListener(this, controlDurationSlider.valueProperty(), (_, _, n) -> bindingSource.durationSlider.setValue(n.doubleValue()));
        Utilities.applyHeldPropertyListener(this, bindingSource.volumeImage.imageProperty(), (_, _, i) -> volumeImage.setImage(i));
        Utilities.applyHeldPropertyListener(this, bindingSource.volumeSlider.valueProperty(), (_, _, n) -> volumeSlider.setValue(n.doubleValue()));
        Utilities.applyHeldPropertyListener(this, volumeSlider.valueProperty(), (_, _, n) -> bindingSource.volumeSlider.setValue(n.doubleValue()));
        Utilities.applyHeldPropertyListener(this, bindingSource.forwardButton.disableProperty(), (_, _, b) -> forwardButton.setDisable(b));
        Utilities.applyHeldPropertyListener(this, bindingSource.backwardButton.disableProperty(), (_, _, b) -> backwardButton.setDisable(b));
        Utilities.applyHeldPropertyListener(this, bindingSource.playButton.disableProperty(), (_, _, b) -> controlPlayButton.setDisable(b));
        Utilities.applyHeldPropertyListener(this, bindingSource.stopButton.disableProperty(), (_, _, b) -> controlStopButton.setDisable(b));

        // init
        controlSongNameTooltip.setText(bindingSource.songNameTooltip.getText());
        controlStopAfterSongButton.setDisable(bindingSource.stopAfterSongButton.isDisable());
        controlStopAfterSongButton.setSelected(bindingSource.stopAfterSongButton.isSelected());
        controlCurrentTimeLabel.setText(bindingSource.currentTimeLabel.getText());
        controlSongView.setImage(bindingSource.bigSongView.getImage());
        ((ImageView) controlShuffleButton.getGraphic()).setImage(((ImageView) bindingSource.shuffleButton.getGraphic()).getImage());
        ((ImageView) controlLoopButton.getGraphic()).setImage(((ImageView) bindingSource.loopButton.getGraphic()).getImage());
        ((ImageView) controlPlayButton.getGraphic()).setImage(((ImageView) bindingSource.playButton.getGraphic()).getImage());
        controlDurationSlider.setMax(bindingSource.durationSlider.getMax());
        controlDurationSlider.setValue(bindingSource.durationSlider.getValue());
        volumeImage.setImage(bindingSource.volumeImage.getImage());
        volumeSlider.setValue(bindingSource.volumeSlider.getValue());
        forwardButton.setDisable(bindingSource.forwardButton.isDisable());
        backwardButton.setDisable(bindingSource.backwardButton.isDisable());
        controlPlayButton.setDisable(bindingSource.playButton.isDisable());
        controlStopButton.setDisable(bindingSource.stopButton.isDisable());




        // volume management
        volumePopup = new Popup();
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setMin(0.01);
        volumeSlider.setMax(1);
        volumeSlider.setBlockIncrement(0.05);
        volumeSlider.setLayoutX(3);
        volumeSlider.setLayoutY(2);
        Pane volumePane = new Pane(volumeSlider);
        volumePane.setPrefWidth(21.5);
        volumePane.setPrefHeight(144);
        volumePane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(10), Insets.EMPTY)));
        volumePopup.getContent().add(volumePane);
        volumePopup.setAutoHide(false);

        Utilities.applyHeldPropertyListener(this, volumeSlider.valueChangingProperty(), (_, _, newValue) -> {
            if (!newValue && volumePopup.isShowing() && !hoveringVolumePopup) {
                volumePopup.hide();
            }
        });
        volumeContainer.setOnMouseEntered(_ -> {
            showVolumePopup = true;
            updatePopups();
        });
        volumeContainer.setOnMouseExited(e -> {
            Point2D pos = volumeContainer.localToScreen(0, 0);
            double mx = e.getScreenX(), my = e.getScreenY();
            double x = pos.getX(), y = pos.getY(), w = volumeContainer.getWidth();
            boolean isOutside = my <= y || mx <= x || mx >= x + w;
            if (isOutside) {
                showVolumePopup = false;
                updatePopups();
            }
        });
        volumePane.setOnMouseEntered(_ -> hoveringVolumePopup = true);
        volumePane.setOnMouseExited(e -> {
            Point2D pos = volumeContainer.localToScreen(0, 0);
            double mx = e.getScreenX(), my = e.getScreenY();
            double x = pos.getX(), y = pos.getY(), w = volumeContainer.getWidth();
            boolean isOutside = my <= y || mx <= x || mx >= x + w;
            if (isOutside && !volumeSlider.isValueChanging()) {
                showVolumePopup = false;
                updatePopups();
            }
            hoveringVolumePopup = false;
        });



        // pause key management
        pauseKeyPopup = new Popup();
        applyPauseKeyPopupDefaultFace();
        pauseKeyPopup.setAutoHide(false);
        pauseKeyPopup.setHideOnEscape(false);

        pauseKeyLabel.setOnMouseEntered(_ -> {
            showPauseKeyPopup = true;
            updatePopups();
        });
        pauseKeyLabel.setOnMouseExited(_ -> {
            showPauseKeyPopup = false;
            updatePopups();
        });
    }

    protected void hidePopups() {
        showVolumePopup = false;
        showPauseKeyPopup = false;
        Platform.runLater(() -> {
            volumePopup.hide();
            pauseKeyPopup.hide();
        });
    }

    private void updatePopups() {
        if (showVolumePopup) {
            Point2D containerPos = volumeContainer.localToScreen(0, 0);
            if (!volumePopup.isShowing()) {
                volumePopup.show(volumeContainer, containerPos.getX() - 4, containerPos.getY() + volumeContainer.getHeight());
            }
            volumePopup.setX(containerPos.getX() - 4);
            volumePopup.setY(containerPos.getY() + volumeContainer.getHeight());
        }
        else {
            if (volumePopup.isShowing()) {
                Platform.runLater(volumePopup::hide); // Schedule so JavaFX dragging calculations can finish without throwing a NullPointerException
            }
        }

        if (showPauseKeyPopup || newPauseKeyCombinationLabel != null /*Always show while defining new combination*/) {
            Point2D containerPos = pauseKeyLabel.localToScreen(0, 0);
            if (!pauseKeyPopup.isShowing()) {
                pauseKeyPopup.show(pauseKeyLabel, containerPos.getX() - 4, containerPos.getY() + pauseKeyLabel.getHeight());
            }
            pauseKeyPopup.setX(containerPos.getX() - 4);
            pauseKeyPopup.setY(containerPos.getY() + pauseKeyLabel.getHeight());
        }
        else {
            if (pauseKeyPopup.isShowing()) {
                Platform.runLater(pauseKeyPopup::hide); // Schedule so JavaFX calculations can finish without throwing a NullPointerException
            }
        }
    }

    private void applyPauseKeyPopupDefaultFace() {
        newPauseKeyCombinationLabel = null;
        gottaRestoreDefaultKeyPopupFace = false;
        pauseKeyPopup.getContent().clear();
        StringBuilder combinationLiteral = new StringBuilder();
        MainConfigWrapper.loadConfig().nativePauseKeyCombination.forEach(keyCode -> {
            combinationLiteral.append(combinationLiteral.isEmpty() ? "" : " + ").append(NativeKeyEvent.getKeyText(keyCode));
        });
        Label pauseKeyLabel1 = new Label("Pause/Resume using '" + combinationLiteral + "'");
        pauseKeyLabel1.setLayoutX(5);
        pauseKeyLabel1.setLayoutY(5);
        Label pauseKeyLabel2 = new Label("Click icon to change");
        pauseKeyLabel2.setLayoutX(5);
        pauseKeyLabel2.setLayoutY(25);
        pauseKeyLabel2.setFont(Font.font(Font.getDefault().getName(), FontPosture.ITALIC, Font.getDefault().getSize() - 2));
        Pane pauseKeyPane = new Pane(pauseKeyLabel1, pauseKeyLabel2);
        pauseKeyPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(10), Insets.EMPTY)));
        pauseKeyPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(5))));
        pauseKeyPopup.getContent().add(pauseKeyPane);
    }

    private void applyPauseKeyPopupRedefineFace() {
        gottaRestoreDefaultKeyPopupFace = false;
        pauseKeyPopup.getContent().clear();
        Label pauseKeyLabel1 = new Label("Press the new key combination (click icon to cancel):");
        pauseKeyLabel1.setLayoutX(5);
        pauseKeyLabel1.setLayoutY(5);
        newPauseKeyCombinationLabel = new Label();
        newPauseKeyCombinationLabel.setLayoutX(5);
        newPauseKeyCombinationLabel.setLayoutY(25);
        newPauseKeyCombinationLabel.setFont(Font.font(Font.getDefault().getName(), Font.getDefault().getSize() - 1));
        Pane pauseKeyPane = new Pane(pauseKeyLabel1, newPauseKeyCombinationLabel);
        pauseKeyPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(10), Insets.EMPTY)));
        pauseKeyPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(5))));
        pauseKeyPopup.getContent().add(pauseKeyPane);
    }

    private void savePauseCombination() {
        if (newPauseKeyCombinationLabel != null) { // We are currently changing the pause key combination
            MainConfigWrapper.loadConfig().nativePauseKeyCombination.clear();
            MainConfigWrapper.loadConfig().nativePauseKeyCombination.addAll(pressedKeys);
            System.out.println("Updated native pause key combination: '" + newPauseKeyCombinationLabel.getText() + "' " + MainConfigWrapper.loadConfig().nativePauseKeyCombination.toString().replace("[", "(").replace("]", ")").replace(",", ";"));

            Platform.runLater(this::applyPauseKeyPopupDefaultFace);
            updatePopups(); // If the cursor exited the key popup during re-defining, hide the popup

        }
    }

    protected void destroy() {
        bindingSource.onKeyPress = _ -> {};
        NativeHookManager.getInstance().setChild(null);
        Utilities.removeHeldListeners(this);
    }

    @FXML
    private void onPauseKeyChange() {
        if (!pauseKeyPopup.isShowing()) {
            showPauseKeyPopup = true;
            updatePopups();
        }

        pressedKeys.clear();
        if (newPauseKeyCombinationLabel != null) { // If we currently are selecting, stop selection
            newPauseKeyCombinationLabel.setText("");
            savePauseCombination(); // Because the storage is cleared and the label text was reset, this will save empty data, effectively canceling the operation
            return;
        }

        applyPauseKeyPopupRedefineFace();
    }

    @FXML
    private void onPlayButton() {
        bindingSource.onPlayButton();
    }

    @FXML
    private void onStopButton() {
        bindingSource.onStopButton();
    }

    @FXML
    private void onShuffleButton() {
        bindingSource.onShuffleButton();
    }

    @FXML
    private void onLoopButton() {
        bindingSource.onLoopButton();
    }

    @FXML
    private void onStopAfterSongButton() {
        bindingSource.onStopAfterSongButton();
    }

    @FXML
    private void onSeekSliderDrag() {
        bindingSource.onSeekSliderDrag();
    }

    @FXML
    private void onSeekSliderPressed() {
        bindingSource.onSeekSliderPressed();
    }

    @FXML
    private void onSeekSliderReleased() {
        bindingSource.onSeekSliderReleased();
    }

    @FXML
    private void onVolumeMute() {
        bindingSource.onVolumeMute();
    }

    @FXML
    private void onBackwardButton() {
        bindingSource.onBackwardButton();
    }

    @FXML
    private void onForwardButton() {
        bindingSource.onForwardButton();
    }


    /* --- Native key listener implementations --- */
    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        if (pressedKeys.add(nativeKeyEvent.getKeyCode())) { // Prevent key repeating

            if (newPauseKeyCombinationLabel == null) { // We are not changing the combination
                if (!combinationTriggered && pressedKeys.equals(MainConfigWrapper.loadConfig().nativePauseKeyCombination)) {
                    combinationTriggered = true;
                    Platform.runLater(controlPlayButton::fire);
                }
            } else {
                Platform.runLater(() -> newPauseKeyCombinationLabel.setText(newPauseKeyCombinationLabel.getText() + (newPauseKeyCombinationLabel.getText().isEmpty() ? "" : " + ") + NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode())));
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        if (newPauseKeyCombinationLabel != null) { // Is defining a new combination?
            savePauseCombination();
        }
        pressedKeys.remove(nativeKeyEvent.getKeyCode());
        combinationTriggered = false;
    }
}
