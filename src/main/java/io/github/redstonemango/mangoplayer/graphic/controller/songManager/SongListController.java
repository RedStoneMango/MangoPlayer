package io.github.redstonemango.mangoplayer.graphic.controller.songManager;

import io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser.AnalyserController;
import io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser.PlaylistDataRepresentation;
import io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser.SongDataRepresentation;
import io.github.redstonemango.mangoplayer.logic.config.PlaylistConfigWrapper;
import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.graphic.TextFieldAutoCompletion;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.ISongControllable;
import io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser.AnalyserScene;
import io.github.redstonemango.mangoplayer.logic.*;
import io.github.redstonemango.mangoplayer.logic.config.SongConfigWrapper;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongListController implements IInitializable, ISongControllable {
    @FXML private Tooltip songFilterTooltip;
    @FXML private Tooltip songImportTooltip;
    @FXML private Tooltip songDownloadTooltip;
    @FXML private Text songDownloadTooltipText;
    @FXML private Button downloadButton;
    @FXML private TextField songFilterField;
    @FXML private Text addPlaylistNameText;
    @FXML private BorderPane selectionModeHeader;
    @FXML private Button analyserButton;
    @FXML private ListView<Song> songsView;

    private final Map<Song, Boolean> songSelectionData = new HashMap<>();
    // If we are in selection mode, this is the playlist the opened the screen. If we are not in selection mode (viewing mode only), this is 'null'
    private @Nullable Playlist parent = null;
    private boolean actionOccupied = false;
    private int ytDlpAvailableState = -1; // -1: Currently indexing, 0: Nothing existing, 1: Only dlp, 2: Only ffmpeg, 3: All existing

    public void setActionOccupied(boolean actionOccupied) {
        this.actionOccupied = actionOccupied;
        songFilterField.getScene().setCursor(actionOccupied ? Cursor.WAIT : Cursor.DEFAULT);
    }

    @Override
    public void init() {
        boolean selectionMode;
        if (songFilterField.getScene() instanceof SongListScene scene) {
            selectionMode = scene.isInSelectionMode();
            parent = scene.getSelectionParent();
            if (selectionMode) {
                addPlaylistNameText.setText(scene.getPlaylistName());
                analyserButton.setVisible(false);
            }
            else {
                ((VBox) selectionModeHeader.getParent()).getChildren().remove(selectionModeHeader); // do not show header when not in selection mode
            }
        } else {
            selectionMode = false;
            parent = null;
            ((VBox) selectionModeHeader.getParent()).getChildren().remove(selectionModeHeader); // do not show header when not in selection mode
        }

        updateYtDlpAvailable();

        Utilities.applyListViewCellFactory(songsView, song -> {
            ManagerSongEntry entry = new ManagerSongEntry(song, selectionMode, this);
            double width = songFilterField.getScene().getWindow().getWidth();
            entry.setPrefWidth(width - 18);
            entry.getNameLabel().setPrefWidth(width - 214);
            if (selectionMode && parent != null) {
                boolean selected = parent.getSongs().contains(song);
                if (songSelectionData.containsKey(song)) {
                    selected = songSelectionData.get(song);
                }
                entry.getActionButton().setSelected(selected);
                entry.updateTexts();
            }
            return entry;
        });

        songFilterTooltip.setShowDelay(Duration.millis(50));
        songImportTooltip.setShowDelay(Duration.millis(150));
        songDownloadTooltip.setShowDelay(Duration.millis(150));

        TextFieldAutoCompletion.autoCompletable(songFilterField);

        songFilterField.getScene().getWindow().setOnCloseRequest(e -> {
            if (actionOccupied) {
                Toolkit.getDefaultToolkit().beep();
                e.consume();
            }
            else {
                sendCloseInformation();
            }
        });
        songFilterField.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (actionOccupied) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                sendCloseInformation();
                songFilterField.getScene().getWindow().hide();
            }
        });
        Utilities.applyHeldPropertyListener(this, songFilterField.getScene().getWindow().widthProperty(), (_, _, _) -> songsView.refresh());
        songFilterField.textProperty().addListener((_, _, newValue) -> {
            songsView.getItems().clear();
            SongConfigWrapper.loadConfig().songs.forEach((_, song) -> {
                if (song.getName().toLowerCase(Locale.ROOT).contains(newValue.toLowerCase(Locale.ROOT))) {
                    songsView.getItems().add(song);
                }
            });
        });

        this.sortAndRepaintSongs();
    }

    @FXML
    private void onSongImport() {
        if (actionOccupied) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        SongAdding.INSTANCE.onImportSong(this);
    }

    @FXML
    private void onSongDownload() {
        if (actionOccupied || ytDlpAvailableState == -1) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (ytDlpAvailableState == 3) {
            SongAdding.INSTANCE.onDownloadSong(this);
        }
        else {
            if (ytDlpAvailableState == 1) {
                YtDlpManager.getInstance().showFfmpegNotAvailableDialog();
                updateYtDlpAvailable();
            }
            else  {
                YtDlpManager.getInstance().showNotAvailableDialog();
                updateYtDlpAvailable();
            }
        }
    }

    private void updateYtDlpAvailable() {
        ytDlpAvailableState = -1;
        downloadButton.setOpacity(0.5);
        songDownloadTooltipText.setText("Indexing external tools...");
        songDownloadTooltipText.setFill(Color.ORANGE);
        new Thread(() -> {
            boolean dlp = YtDlpManager.getInstance().isAvailable();
            boolean ffmpeg = YtDlpManager.getInstance().isFfmpegAvailable();
            if (dlp && ffmpeg) ytDlpAvailableState = 3;
            else if (!dlp && ffmpeg) ytDlpAvailableState = 2;
            else if (dlp) ytDlpAvailableState = 1;
            else ytDlpAvailableState = 0;

            Platform.runLater(() -> {
                downloadButton.setOpacity(ytDlpAvailableState == 3 ? 1 : 0.5);
                songDownloadTooltipText.setText(ytDlpAvailableState == 3 ? "Download song using yt-dlp and ffmpeg" : "Missing external tools. Click for further information");
                songDownloadTooltipText.setFill(ytDlpAvailableState == 3 ? Color.LIGHTGREEN : Color.RED);
            });
        }, "Check external tools").start();
    }

    @FXML
    private void  onSongsSelected() {
        if (actionOccupied) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (songFilterField.getScene() instanceof SongListScene scene) {
            scene.sendSelectedSongInfo(Collections.unmodifiableMap(songSelectionData));
        }
        songsView.getScene().getWindow().hide();
    }

    @Override
    public boolean onSongEntryAction(Song song, @Nullable Boolean isSelected) {
        if (actionOccupied) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }

        if (isSelected != null && parent != null) {
            if (isSelected && parent.getSongs().contains(song)) { // Selected and contained in playlist? This is the default -> Remove from map
                songSelectionData.remove(song);
            }
            else if (!isSelected && !parent.getSongs().contains(song)) { // Not selected and not contained in playlist? This is the default -> Remove from map
                songSelectionData.remove(song);
            }
            else { // Selection state and contained in playlist state differ? Store this inside map
                songSelectionData.put(song, isSelected);
            }
        }
        else if (isSelected != null) {
            Utilities.removeHeldListeners(this);
            songFilterField.getScene().getWindow().setOnCloseRequest(_ -> {}); // Override window close event
            SongDetailsScene scene = SongDetailsScene.createNewScene(song, ((SongListScene) songFilterField.getScene()).getMainWindowController());
            Stage stage = (Stage) songsView.getScene().getWindow();
            stage.setTitle("MangoPlayer | Song editor");
            Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
        }
        return true;
    }

    @Override
    public void onSongEntryBrowse(Song song) {
        if (actionOccupied) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        File songThumbnailFile = new File(Utilities.thumbnailPathFromSong(song));
        ButtonType audioButtonType = new ButtonType("Audio", ButtonBar.ButtonData.LEFT);
        ButtonType thumbnailButtonType = new ButtonType("Thumbnail", ButtonBar.ButtonData.LEFT);
        ButtonType youtubeButtonType = new ButtonType("YouTube", ButtonBar.ButtonData.LEFT);

        if (songThumbnailFile.exists()) {
            Alert alert;
            if (song.isFromYoutube()) {
                alert = new Alert(Alert.AlertType.NONE, "", audioButtonType, thumbnailButtonType, youtubeButtonType, ButtonType.CANCEL);
                alert.setHeaderText("This song has three assets:\nThe thumbnail the audio and the YouTube source");
            }
            else {
                alert = new Alert(Alert.AlertType.NONE, "", audioButtonType, thumbnailButtonType, ButtonType.CANCEL);
                alert.setHeaderText("This song has two assets:\nThe thumbnail, as well as the audio");
            }
            alert.setTitle("MangoPlayer | Selection");
            alert.setContentText("Which one do you want to access?");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.CANCEL) return;
            if (alert.getResult() == youtubeButtonType) OperatingSystem.loadCurrentOS().open(Utilities.youtubeUrlFromSong(song));
            else OperatingSystem.loadCurrentOS().browse(alert.getResult() == audioButtonType ? Utilities.audioPathFromSong(song) : Utilities.thumbnailPathFromSong(song));
        }
        else {
            if (song.isFromYoutube()) {
                Alert alert = new Alert(Alert.AlertType.NONE, "", audioButtonType, youtubeButtonType, ButtonType.CANCEL);
                alert.setTitle("MangoPlayer | Selection");
                alert.setHeaderText("This song has two assets:\nThe audio and the YouTube source");
                alert.setContentText("Which one do you want to access?");
                alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.CANCEL) return;
                if (alert.getResult() == youtubeButtonType) OperatingSystem.loadCurrentOS().open(Utilities.youtubeUrlFromSong(song));
                else OperatingSystem.loadCurrentOS().browse(alert.getResult() == audioButtonType ? Utilities.audioPathFromSong(song) : Utilities.thumbnailPathFromSong(song));
            }
            else {
                OperatingSystem.loadCurrentOS().browse(Utilities.audioPathFromSong(song));
            }
        }
    }

    @FXML
    private void onOpenAnalyser() {
        if (actionOccupied) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        Stage stage = (Stage) songFilterField.getScene().getWindow();

        // Async load
        {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(() -> {
                List<SongDataRepresentation> songData = new ArrayList<>();
                List<PlaylistDataRepresentation> playlistData = new ArrayList<>();

                SongConfigWrapper.loadConfig().songs.forEach((_, song) -> songData.add(new SongDataRepresentation(song)));
                PlaylistConfigWrapper.loadConfig().playlists.forEach(playlist -> playlistData.add(new PlaylistDataRepresentation(playlist)));

                AnalyserScene analyserScene = AnalyserScene.createNewScene(
                        ((SongListScene) songFilterField.getScene()).getMainWindowController(),
                        playlistData,
                        songData
                );
                ((AnalyserController) analyserScene.getLoader().getController()).init();

                Utilities.removeHeldListeners(this);

                Platform.runLater(() -> {
                    if (stage.isShowing()) {
                        stage.setTitle("MangoPlayer | Use analyser");
                        Utilities.prepareAndShowStage(stage, analyserScene, analyserScene.getLoader());
                    }
                });
            });
        }

        stage.setTitle("MangoPlayer | Use analyser (load)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/redstonemango/mangoplayer/fxml/useAnalyser/loading.fxml"));
            Scene scene = new Scene(loader.load());
            Utilities.prepareAndShowStage(stage, scene, loader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSongEntryDelete(Song song) {
        if (actionOccupied) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        if (song.askAndRunDelete()) {
            songsView.getItems().remove(song);
        }
    }

    private void sendCloseInformation() {
        if (songFilterField.getScene() instanceof SongListScene scene) {
            scene.sendWindowClosedInfo();
        }
    }

    public Window getWindow() {
        return songsView.getScene().getWindow();
    }

    public void sortAndRepaintSongs() {
        Utilities.sortSongs();
        songsView.getItems().clear();

        SongConfigWrapper.loadConfig().songs.forEach((_, song) -> TextFieldAutoCompletion.autoCompletable(songFilterField).getCompletions().add(song.getName()));
        songsView.getItems().addAll(SongConfigWrapper.loadConfig().songs.values());
    }

    public void highlightSong(Song song) {
        Platform.runLater(() -> {
            songsView.getSelectionModel().select(song);
            songsView.scrollTo(song);
            songsView.requestFocus();
        });
    }
}
