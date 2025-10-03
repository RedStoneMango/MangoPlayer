package io.github.redstonemango.mangoplayer.graphic.controller.playlistScreen;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.graphic.DraggableGraphicCell;
import io.github.redstonemango.mangoplayer.graphic.MangoPlayer;
import io.github.redstonemango.mangoplayer.graphic.TextFieldAutoCompletion;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.ISongPlayable;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.ISongSelectable;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.ISongViewable;
import io.github.redstonemango.mangoplayer.graphic.controller.songManager.SongDetailsScene;
import io.github.redstonemango.mangoplayer.graphic.controller.songManager.SongListScene;
import io.github.redstonemango.mangoplayer.logic.*;
import io.github.redstonemango.mangoplayer.logic.config.MainConfigWrapper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PlaylistScreenController implements IInitializable, ISongSelectable, ISongPlayable, ISongViewable {
    public Playlist playlist;

    private @Nullable Duration cachedSongTime;
    private Label endAfterSongIcnContainer;
    private Popup volumePopup;
    private @Nullable PlayControlWindow detachedControl;
    private boolean showVolumePopup = false;
    private boolean hoveringVolumePopup = false;
    private boolean initialized = false;
    protected Slider volumeSlider;
    private boolean seekingWhilePausing = false;
    protected Consumer<KeyEvent> onKeyPress = _ -> {};

    @FXML private TextField songsFilterField;
    @FXML private Button addSongButton;
    @FXML private Button smallBackButton;
    @FXML private ImageView smallSongView;
    @FXML private Label volumeContainer;
    @FXML protected ImageView volumeImage;
    @FXML protected ImageView bigSongView;
    @FXML private TitledPane controlsTitledPane;
    @FXML protected Button shuffleButton;
    @FXML protected Button loopButton;
    @FXML protected Tooltip songNameTooltip;
    @FXML private ListView<Song> songsView;
    @FXML protected Button playButton;
    @FXML protected Button stopButton;
    @FXML protected Slider durationSlider;
    @FXML protected Label currentTimeLabel;
    @FXML protected ToggleButton stopAfterSongButton;
    @FXML protected ImageView detachImage;
    @FXML protected Label detachContainer;
    @FXML protected Button forwardButton;
    @FXML protected Button backwardButton;


    @Override
    public void init() {
        PlaylistAudioManager.registerController(this);

        PlaylistScreenController thisInstance = this;
        songsView.setCellFactory(_ -> new DraggableGraphicCell<>(song -> {          // Cell display settings
            PlaylistSongEntry entry = new PlaylistSongEntry(song, thisInstance);
            double width = songsFilterField.getScene().getWindow().getWidth();
            entry.setSortable(songsFilterField.getText().isEmpty() && !PlaylistAudioManager.isPlaying());
            entry.setDeletable(!PlaylistAudioManager.isPlaying());
            entry.setPrefWidth(width - 18);
            entry.getNameLabel().setPrefWidth(width - 232);
            if (song.equals(PlaylistAudioManager.getCurrentlyPlayingSong()))
                entry.setStyle(Finals.STYLE_CODE_SONG_PLAYING);
            entry.getDeleteButton().setDisable(PlaylistAudioManager.isPlaying());
            return new DraggableGraphicCell.GraphicData(entry, entry.getSortLabel());
        }, song -> {                                                                               // Drag view settings
            File thumbnailFile = new File(Utilities.thumbnailPathFromSong(song));
            Image songImage = Finals.IMAGE_THUMBNAIL_FALLBACK;
            if (thumbnailFile.exists()) {
                songImage = new Image(thumbnailFile.toURI().toString());
            }
            return Utilities.scaleToSmallBounds(songImage, 50);
        }, _ -> {
            return songsFilterField.getText().isEmpty() && !PlaylistAudioManager.isPlaying();            // Allow sorting settings
        }, new DraggableGraphicCell.EventAdapter() {                                                     // Event settings
            @Override
            public void onDragDone(DragEvent event) {
                onSongsSorted();
            }
        }, new DraggableGraphicCell.LineData(Color.DARKGREEN, 5)));                                // Separator line settings

        Platform.runLater(() -> {
            Node header = controlsTitledPane.lookup(".title");
            if (header instanceof Region headerRegion) {
                ImageView view = new ImageView();
                view.setPreserveRatio(true);
                view.setFitWidth(10);
                view.setFitHeight(10);
                view.setImage(Finals.IMAGE_STOP_AFTER_SONG_WARNING);
                Text tooltipText = new Text("The playlist is forced to end after this song");
                tooltipText.setFont(Font.font(Font.getDefault().getName(), 13));
                tooltipText.setFill(Color.RED);
                Tooltip tooltip = new Tooltip();
                tooltip.setShowDelay(Duration.millis(20));
                tooltip.setGraphic(tooltipText);
                endAfterSongIcnContainer = new Label();
                endAfterSongIcnContainer.setTooltip(tooltip);
                endAfterSongIcnContainer.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                endAfterSongIcnContainer.setGraphic(view);
                endAfterSongIcnContainer.setVisible(false);

                if (headerRegion instanceof Pane headerPane) {
                    headerPane.getChildren().add(endAfterSongIcnContainer);
                    endAfterSongIcnContainer.setTranslateY(15);
                    endAfterSongIcnContainer.setTranslateX(headerPane.getWidth() - endAfterSongIcnContainer.getWidth() - 20);
                }
                else {
                    System.err.println("Error initializing 'end after song icon': 'headerRegion' is not an instance of " + Pane.class.getName());
                }
            }
            else {
                System.err.println("Error initializing 'end after song icon': 'header' is not an instance of " + Region.class.getName());
            }
        });

        volumePopup = new Popup();
        volumeSlider = new Slider();
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

        Utilities.applyHeldPropertyListener(this, volumeSlider.valueProperty(), (_, _, newValue) -> {
            if (initialized) {
                Utilities.tryMovePlaylistToListTop(playlist);
                onVolumeSliderUpdate(newValue.doubleValue());
            }
        });
        Utilities.applyHeldPropertyListener(this, volumeSlider.valueChangingProperty(), (_, _, newValue) -> {
            if (initialized && !newValue && volumePopup.isShowing() && !hoveringVolumePopup) {
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


        Utilities.applyHeldPropertyListener(this, songsFilterField.getScene().getWindow().xProperty(), (_, _, _) -> updatePopups());
        Utilities.applyHeldPropertyListener(this, songsFilterField.getScene().getWindow().yProperty(), (_, _, _) -> updatePopups());
        Utilities.applyHeldPropertyListener(this, songsFilterField.getScene().getWindow().focusedProperty(), (_, _, _) -> updatePopups());

        if (songsFilterField.getScene() instanceof PlaylistScreenScene scene) {
            this.playlist = scene.getPlaylist();
        }

        songsFilterField.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            onKeyPress.accept(e);
            if (Objects.requireNonNull(e.getCode()) == KeyCode.ESCAPE) {
                if (PlaylistAudioManager.isPlaying()) {
                    onStopButton();
                } else {
                    onBackButton();
                }
            }
        });
        songNameTooltip.setShowDelay(Duration.millis(10));

        controlsTitledPane.expandedProperty().addListener((_, _, isExpanded) -> {
            if (!initialized) return;
            if (isExpanded) {
                //smallSongView.setVisible(false);
                controlsTitledPane.setGraphic(null);
                ((HBox) controlsTitledPane.getParent()).getChildren().remove(smallBackButton);
            }
            else {
                //smallSongView.setVisible(true);
                controlsTitledPane.setGraphic(smallSongView);
                if (!((HBox) controlsTitledPane.getParent()).getChildren().contains(smallBackButton))
                    ((HBox) controlsTitledPane.getParent()).getChildren().addFirst(smallBackButton);
            }
            MainConfigWrapper.loadConfig().isSongControlExpanded = isExpanded;
            double width = songsFilterField.getScene().getWindow().getWidth();
            controlsTitledPane.setPrefWidth(width - (isExpanded ? 0 : 31));
        });

        // load songs
        playlist.getSongs().forEach(song -> {
            songsView.getItems().add(song);
            TextFieldAutoCompletion.autoCompletable(songsFilterField).getCompletions().add(song.getName());
        });

        // register filter listener
        songsFilterField.textProperty().addListener((_, _, newValue) -> {
            songsView.getItems().clear();
            playlist.getSongs().forEach(song -> {
                if (song.getName().toLowerCase(Locale.ROOT).contains(newValue.toLowerCase(Locale.ROOT))) {
                    songsView.getItems().add(song);
                }
            });
        });

        // register width listeners
        Utilities.applyHeldPropertyListener(this, songsFilterField.getScene().getWindow().widthProperty(), (_, _, newValue) -> {
            songsView.refresh();
            addSongButton.setPrefWidth(newValue.doubleValue());
            controlsTitledPane.setPrefWidth(newValue.doubleValue() - (controlsTitledPane.isExpanded() ? 0 : 31));
            Node header = controlsTitledPane.lookup(".title");
            if (header instanceof Pane headerPane) {
                endAfterSongIcnContainer.setTranslateX(headerPane.getWidth() - endAfterSongIcnContainer.getWidth() - 20);
            }
        });

        // init widths
        double width = songsFilterField.getScene().getWindow().getWidth();
        addSongButton.setPrefWidth(width);
        controlsTitledPane.setPrefWidth(width - (controlsTitledPane.isExpanded() ? 0 : 31));

        // load appearance
        ((ImageView) playButton.getGraphic()).setImage(Finals.IMAGE_PLAY);
        controlsTitledPane.setExpanded(MainConfigWrapper.loadConfig().isSongControlExpanded);
        if (controlsTitledPane.isExpanded()) {
            ((HBox) controlsTitledPane.getParent()).getChildren().remove(smallBackButton);
            controlsTitledPane.setGraphic(null);
            controlsTitledPane.setText("");
        }
        ((ImageView) shuffleButton.getGraphic()).setImage(MainConfigWrapper.loadConfig().isShuffleActive ? Finals.IMAGE_SHUFFLE_ON : Finals.IMAGE_SHUFFLE_OFF);
        ((ImageView) loopButton.getGraphic()).setImage(MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_NONE ? Finals.IMAGE_LOOP_NONE : (MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_ALL ? Finals.IMAGE_LOOP_ALL : Finals.IMAGE_LOOP_SINGLE));
        volumeImage.setImage(MainConfigWrapper.loadConfig().isMuted ? Finals.IMAGE_VOLUME_OFF : Finals.IMAGE_VOLUME_ON);
        volumeSlider.setValue(MainConfigWrapper.loadConfig().volume);
        songsFilterField.requestFocus();
        detachImage.setImage(Finals.IMAGE_UNDETACHED);
        playButton.setDisable(playlist.getSongs().isEmpty());
        initialized = true;
    }

    private void updatePopups() {
        Window window = songsFilterField.getScene().getWindow();

        if (showVolumePopup && window.isFocused()) {
            Point2D containerPos = volumeContainer.localToScreen(0, 0);
            if (!volumePopup.isShowing()) {
                volumePopup.show(volumeContainer, containerPos.getX() - 4, containerPos.getY() + volumeContainer.getHeight());
            }
            volumePopup.setX(containerPos.getX() - 4);
            volumePopup.setY(containerPos.getY() + volumeContainer.getHeight());
        }
        else {
            if (volumePopup.isShowing()) {
                Platform.runLater(volumePopup::hide); // Schedule so JavaFX dragging calculations can finish without throwing a NullPointer
            }
        }
    }

    @FXML
    protected void onVolumeSliderUpdate(double newValue) {
        MainConfigWrapper.loadConfig().volume = newValue;
        if (MainConfigWrapper.loadConfig().isMuted && initialized) {  // Unmute volume if muted. This also calls PlaylistAudioManager#updateVolume(), so no need to to it again
            onVolumeMute();
        }
        else {
            PlaylistAudioManager.updateVolume();
        }
    }

    @FXML
    protected void onVolumeMute() {
        Utilities.tryMovePlaylistToListTop(playlist);
        MainConfigWrapper.loadConfig().isMuted = !MainConfigWrapper.loadConfig().isMuted;
        volumeImage.setImage(MainConfigWrapper.loadConfig().isMuted ? Finals.IMAGE_VOLUME_OFF : Finals.IMAGE_VOLUME_ON);
        PlaylistAudioManager.updateVolume();
    }

    @FXML
    private void onDetach() {
        Utilities.tryMovePlaylistToListTop(playlist);
        if (detachImage.getImage() == Finals.IMAGE_DETACHED) {
            detachImage.setImage(Finals.IMAGE_UNDETACHED);
            if (detachedControl != null) {
                detachedControl.hide(); // This will invoke the window's onHiding method which then calls destroy() on the object, therefore, there's no need to call it again inside this class
                detachedControl = null;
            }
        }
        else {
            detachImage.setImage(Finals.IMAGE_DETACHED);
            if (detachedControl == null) {
                detachedControl = PlayControlWindow.createNewWindow(this);
                ((IInitializable) detachedControl.getLoader().getController()).init();
                detachedControl.setOnHidden(_ -> {
                    detachImage.setImage(Finals.IMAGE_UNDETACHED);
                    MainConfigWrapper.loadConfig().detachedControlPosition.x = detachedControl.getX();
                    MainConfigWrapper.loadConfig().detachedControlPosition.y = detachedControl.getY();
                    detachedControl = null;
                });

                final double popupWidth = 326;
                final double popupHeight = 66;
                MainConfigWrapper.WindowPosition controlPos = MainConfigWrapper.loadConfig().detachedControlPosition;
                if (controlPos.x != null && controlPos.y != null) {
                    Screen.getScreens().forEach(screen -> {
                        Rectangle2D screenBounds = screen.getBounds();
                        if (controlPos.x >= screenBounds.getMinX() && controlPos.x + popupWidth <= screenBounds.getMaxX() && controlPos.y >= screenBounds.getMinY() && controlPos.y + popupHeight <= screenBounds.getMaxY()) {
                            detachedControl.show(controlsTitledPane, controlPos.x, controlPos.y);
                        }
                    });
                }
                if (!detachedControl.isShowing()) {
                    detachedControl.show(controlsTitledPane, controlsTitledPane.localToScreen(0, 0).getX(), controlsTitledPane.localToScreen(0, 0).getY());
                }
            }
        }
    }

    @FXML
    protected void onShuffleButton() {
        Utilities.tryMovePlaylistToListTop(playlist);
        MainConfigWrapper.loadConfig().isShuffleActive = !MainConfigWrapper.loadConfig().isShuffleActive;
        ((ImageView) shuffleButton.getGraphic()).setImage(MainConfigWrapper.loadConfig().isShuffleActive ? Finals.IMAGE_SHUFFLE_ON : Finals.IMAGE_SHUFFLE_OFF);
        PlaylistAudioManager.initializeSongQueue(PlaylistAudioManager.getCurrentlyPlayingSong()); // When toggling shuffle, initialize a new song queue. If we are currently playing, the new queue shall always have the current song as the first one. If we are not playing, 'null' will be passed in, resulting in the normal queue initialization logic.
        forwardButton.setDisable(PlaylistAudioManager.cannotMoveForwardInQueue());
        backwardButton.setDisable(PlaylistAudioManager.cannotMoveBackwardInQueue());
    }

    @FXML
    protected void onLoopButton() {
        Utilities.tryMovePlaylistToListTop(playlist);
        MainConfigWrapper.loadConfig().loopType++;
        if (MainConfigWrapper.loadConfig().loopType > MainConfigWrapper.LOOP_TYPE_SINGLE) MainConfigWrapper.loadConfig().loopType = MainConfigWrapper.LOOP_TYPE_NONE;
        ((ImageView) loopButton.getGraphic()).setImage(MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_NONE ? Finals.IMAGE_LOOP_NONE : (MainConfigWrapper.loadConfig().loopType == MainConfigWrapper.LOOP_TYPE_ALL ? Finals.IMAGE_LOOP_ALL : Finals.IMAGE_LOOP_SINGLE));
        forwardButton.setDisable(PlaylistAudioManager.cannotMoveForwardInQueue());
        backwardButton.setDisable(PlaylistAudioManager.cannotMoveBackwardInQueue());
    }

    @FXML
    private void onBackButton() {
        try {
            PlaylistAudioManager.stop();
            Utilities.removeHeldListeners(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/redstonemango/mangoplayer/fxml/playlistOverview/playlistOverview.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) songsFilterField.getScene().getWindow();
            stage.setTitle("MangoPlayer");
            Utilities.prepareAndShowStage(stage, scene, loader);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onSongAddButton() {
        Utilities.tryMovePlaylistToListTop(playlist);
        Stage stage = new Stage();
        SongListScene scene = SongListScene.createSelectionScene(this, playlist.getName());
        stage.setX(MangoPlayer.primaryStage.getX() + 50);
        stage.setY(MangoPlayer.primaryStage.getY() + 50);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(MangoPlayer.primaryStage);
        stage.setTitle("MangoPlayer | Song selection");
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
    }

    @FXML
    protected void onSeekSliderReleased() {
        PlaylistAudioManager.seek(Duration.seconds((long) durationSlider.getValue()), seekingWhilePausing);
        seekingWhilePausing = false;
        songsFilterField.requestFocus();
    }
    @FXML
    protected void onSeekSliderPressed() {
        if (!PlaylistAudioManager.isPaused()) PlaylistAudioManager.pauseOrResume();
        else seekingWhilePausing = true;
    }
    @FXML
    protected void onSeekSliderDrag() {
        if (PlaylistAudioManager.isPlaying()) currentTimeLabel.setText(Utilities.formatDuration(Duration.seconds((long) durationSlider.getValue())) + " " + currentTimeLabel.getText().substring(currentTimeLabel.getText().indexOf("/")));
    }

    @FXML
    private void onDownloadYtDlpMenu() {
        GlobalMenuBarActions.onDownloadYtDlpMenu();
    }
    @FXML
    private void onChangeYtDlpMenu() {
        GlobalMenuBarActions.onChangeYtDlpMenu();
    }
    @FXML
    private void onDefaultYtDlpMenu() {
        GlobalMenuBarActions.onDefaultYtDlpMenu();
    }
    @FXML
    private void onDownloadFfmpegMenu() {
        GlobalMenuBarActions.onDownloadFfmpegMenu();
    }
    @FXML
    private void onChangeFfmpegMenu() {
        GlobalMenuBarActions.onChangeFfmpegMenu();
    }
    @FXML
    private void onDefaultFfmpegMenu() {
        GlobalMenuBarActions.onDefaultFfmpegMenu();
    }
    @FXML
    private void onOpenDirectoryMenu() {
        GlobalMenuBarActions.onOpenDirectoryMenu();
    }
    @FXML
    private void onHelpPlaylistMenu() {
        GlobalMenuBarActions.onHelpPlaylistMenu();
    }
    @FXML
    private void onHelpSongMenu() {
        GlobalMenuBarActions.onHelpSongMenu();
    }
    @FXML
    private void onHelpPlaylistUseMenu() {
        GlobalMenuBarActions.onHelpPlaylistUseMenu();
    }
    @FXML
    private void onHelpExternalGeneralMenu() {
        GlobalMenuBarActions.onHelpExternalGeneralMenu();
    }
    @FXML
    private void onLicenseMenu() {
        GlobalMenuBarActions.onLicenseMenu();
    }
    @FXML
    private void onAboutMenu() {
        GlobalMenuBarActions.onAboutMenu();
    }
    @FXML
    private void onHomepageMenu() {
        GlobalMenuBarActions.onHomepageMenu();
    }
    @FXML
    private void onIssuesMenu() {
        GlobalMenuBarActions.onIssuesMenu();
    }
    @FXML
    private void onManualSaveManu() {
        GlobalMenuBarActions.onManualSaveMenu();
    }

    @Override
    public void onSelectionProcessContentChanges(Map<Song, Boolean> changedSongs) {
        AtomicReference<Song> lastAddedSong = new AtomicReference<>(null);
        changedSongs.forEach((song, shallBeContained) -> {
            if (shallBeContained && !playlist.getSongs().contains(song)) {
                playlist.getSongs().add(song);
                songsView.getItems().add(song);
                TextFieldAutoCompletion.autoCompletable(songsFilterField).getCompletions().add(song.getName());
                lastAddedSong.set(song);
            }
            else if (!shallBeContained && playlist.getSongs().contains(song)) {
                onSongDelete(song);
            }
        });
        Platform.runLater(() -> {
            if (lastAddedSong.get() != null) {
                System.out.println(lastAddedSong.get().getName());
                songsView.getSelectionModel().select(lastAddedSong.get());
                songsView.scrollTo(lastAddedSong.get());
                songsView.requestFocus();
            }
        });
        playButton.setDisable(playlist.getSongs().isEmpty());
    }

    @Override
    public Playlist getSelectionParent() {
        return playlist;
    }

    @Override
    public void onSongPlay(Song song) {
        Utilities.tryMovePlaylistToListTop(playlist);
        ((ImageView) playButton.getGraphic()).setImage(Finals.IMAGE_PAUSE);
        PlaylistAudioManager.play(song);
        songsFilterField.requestFocus();
    }

    @Override
    public void onSongDelete(Song song) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("MangoPlayer | Delete song");
        alert.setHeaderText("Please confirm the removal of '" + song.getName() + "' from '" + playlist.getName() + "'?");
        alert.setContentText("After removing it, the song will still be accessible in the song manager.");
        alert.showAndWait();
        if (alert.getResult() != null && alert.getResult() == ButtonType.OK) {
            playlist.getSongs().remove(song);
            songsView.getItems().remove(song);
            TextFieldAutoCompletion.autoCompletable(songsFilterField).getCompletions().remove(song.getName());
        }
    }

    @Override
    public void onSongEditorOpen(Song song) {
        Stage stage = new Stage();
        stage.setTitle("MangoPlayer | Song editor");
        SongDetailsScene scene = SongDetailsScene.createNewScene(song, this);
        stage.setX(MangoPlayer.primaryStage.getX() + 50);
        stage.setY(MangoPlayer.primaryStage.getY() + 50);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(MangoPlayer.primaryStage);
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
    }

    @Override
    public void onSongsSorted() {
        playlist.getSongs().clear();
        songsView.getItems().forEach(song -> playlist.getSongs().add(song));
    }

    @FXML
    protected void onPlayButton() {
        Utilities.tryMovePlaylistToListTop(playlist);
        ImageView view = (ImageView) playButton.getGraphic();
        if (view.getImage() == Finals.IMAGE_PLAY) {
            view.setImage(Finals.IMAGE_PAUSE);
        }
        else {
            view.setImage(Finals.IMAGE_PLAY);
        }

        if (PlaylistAudioManager.isPlaying()) {
            PlaylistAudioManager.pauseOrResume();
        }
        else {
            PlaylistAudioManager.startPlay();
        }
        songsFilterField.requestFocus();
    }

    @FXML
    protected void onStopButton() {
        ((ImageView) playButton.getGraphic()).setImage(Finals.IMAGE_PLAY);
        PlaylistAudioManager.stop();
        songsFilterField.requestFocus();
    }

    @FXML
    protected void onStopAfterSongButton() {
        endAfterSongIcnContainer.setVisible(stopAfterSongButton.isSelected());
    }

    @FXML
    protected void onForwardButton() {
        PlaylistAudioManager.songQueueForward(true);
    }

    @FXML
    protected void onBackwardButton() {
        if (PlaylistAudioManager.getCurrentTime().toSeconds() < 2) {
            PlaylistAudioManager.songQueueBackwards(true);
        }
        else {
            PlaylistAudioManager.seek(Duration.seconds(0), false);
        }
    }

    public void onNewSongStart(Song song, Duration duration, boolean jumpTo) {
        durationSlider.setMax(duration.toSeconds());
        songNameTooltip.setText(song.getName());
        controlsTitledPane.setText(song.getName());
        File thumbnailFile = new File(Utilities.thumbnailPathFromSong(song));
        Image thumbnail = thumbnailFile.exists() ? new Image(thumbnailFile.toURI().toString()) : Finals.IMAGE_THUMBNAIL_FALLBACK;
        smallSongView.setImage(thumbnail);
        bigSongView.setImage(thumbnail);
        currentTimeLabel.setText("00:00 / " + Utilities.formatDuration(duration.add(Duration.seconds(1))));
        addSongButton.setDisable(true);
        stopButton.setDisable(false);

        songsView.refresh();
        if (songsFilterField.getText().isEmpty() && jumpTo) {
            songsView.scrollTo(song);
        }
        stopAfterSongButton.setDisable(false);

        forwardButton.setDisable(PlaylistAudioManager.cannotMoveForwardInQueue());
        backwardButton.setDisable(PlaylistAudioManager.cannotMoveBackwardInQueue());
    }

    public void onSongEnd(Song song, boolean canceled) {
        if (!canceled) {
            song.increaseListenCount();
            playlist.increasePlayedSongCount();
            if (cachedSongTime != null) {
                playlist.increasePlayTime(cachedSongTime);
                cachedSongTime = null;
            }
        }
        // No need to refresh song view as either a new song will start, refreshing the view, or the playlist will end, also refreshing the view. Calling it here is redundant and bad for performance
    }

    public void onPlayEnd() {
        durationSlider.setMax(0);
        songNameTooltip.setText("NO SONG PLAYING");
        controlsTitledPane.setText("");
        smallSongView.setImage(Finals.IMAGE_NO_SONG);
        bigSongView.setImage(Finals.IMAGE_NO_SONG);
        ((ImageView) playButton.getGraphic()).setImage(Finals.IMAGE_PLAY);
        Platform.runLater(() -> Platform.runLater(() -> currentTimeLabel.setText("--:-- / --:--")));

        addSongButton.setDisable(false);
        songsFilterField.requestFocus();
        songsView.refresh();
        stopAfterSongButton.setSelected(false);
        stopAfterSongButton.setDisable(true);
        endAfterSongIcnContainer.setVisible(false);
        forwardButton.setDisable(true);
        backwardButton.setDisable(true);
        stopButton.setDisable(true);

        if (cachedSongTime != null) {
            playlist.increasePlayTime(cachedSongTime);
            cachedSongTime = null;
        }
    }
    public void onProgressUpdate(Duration duration) {
        if (!PlaylistAudioManager.isPlaying()) return;
        duration = duration.add(Duration.seconds(1));
        durationSlider.setValue(duration.toSeconds());
        currentTimeLabel.setText(Utilities.formatDuration(duration) + " " + currentTimeLabel.getText().substring(currentTimeLabel.getText().indexOf("/")));
        cachedSongTime = duration;
    }
    public boolean shouldStopAfterSong() {
        return stopAfterSongButton.isSelected();
    }

    @Override
    public void onSongViewClosed() {
        songsView.refresh();
        Song currentSong = PlaylistAudioManager.getCurrentlyPlayingSong();
        if (currentSong != null) {
            songNameTooltip.setText(currentSong.getName());
            controlsTitledPane.setText(currentSong.getName());
            File thumbnailFile = new File(Utilities.thumbnailPathFromSong(currentSong));
            Image thumbnail = thumbnailFile.exists() ? new Image(thumbnailFile.toURI().toString()) : Finals.IMAGE_THUMBNAIL_FALLBACK;
            smallSongView.setImage(thumbnail);
            bigSongView.setImage(thumbnail);
        }
    }
}
