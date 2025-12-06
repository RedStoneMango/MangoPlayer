package io.github.redstonemango.mangoplayer.front.controller.playlistOverview;

import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import io.github.redstonemango.mangoplayer.front.TextFieldAutoCompletion;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.IPlaylistListable;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.ISongViewable;
import io.github.redstonemango.mangoplayer.front.controller.playlistScreen.PlaylistScreenScene;
import io.github.redstonemango.mangoplayer.front.controller.songManager.SongListScene;
import io.github.redstonemango.mangoplayer.back.*;
import io.github.redstonemango.mangoplayer.back.config.PlaylistConfigWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class PlaylistOverviewController implements IInitializable, IPlaylistListable, ISongViewable {

    @FXML private Tooltip songManagerTooltip;
    @FXML private Tooltip songFilterTooltip;
    @FXML private TextField songFilterField;
    @FXML private ListView<Playlist> playlistsView;

    @Override
    public void init() {
        songManagerTooltip.setShowDelay(Duration.millis(50));
        songFilterTooltip.setShowDelay(Duration.millis(50));
        TextFieldAutoCompletion.autoCompletable(songFilterField);

        repaintPlaylists();

        Utilities.applyListViewCellFactory(playlistsView, playlist -> {
            PlaylistEntry entry = new PlaylistEntry(playlist, this);
            double width = songFilterField.getScene().getWindow().getWidth();
            entry.setPrefWidth(width - 18);
            entry.getNameLabel().setPrefWidth(width - 204);
            entry.getSongCountLabel().setPrefWidth(width - 204);
            return entry;
        });

        Utilities.applyHeldPropertyListener(this, playlistsView.getScene().getWindow().widthProperty(), (_, _, _) -> playlistsView.refresh());

        songFilterField.textProperty().addListener((_, _, newValue) -> {
            playlistsView.getItems().clear();
            PlaylistConfigWrapper.loadConfig().playlists.forEach(playlist -> {
                if (playlist.getName().toLowerCase(Locale.ROOT).contains(newValue.toLowerCase(Locale.ROOT))) {
                    playlistsView.getItems().add(playlist);
                }
            });
        });
    }

    @FXML
    public void onSongManagerOpened() {
        Stage stage = new Stage();
        stage.setTitle("MangoPlayer | Song manager");
        SongListScene scene = SongListScene.createViewingScene(this);
        stage.setX(MangoPlayer.primaryStage.getX() + 50);
        stage.setY(MangoPlayer.primaryStage.getY() + 50);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(MangoPlayer.primaryStage);
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
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
    private void onUpdateYtDlpMenu() {
        GlobalMenuBarActions.onUpdateYtDlpMenu();
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
        Platform.runLater(songFilterTooltip::hide);
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

    private synchronized void repaintPlaylists() {
        playlistsView.getItems().clear();
        Set<String> completions = TextFieldAutoCompletion.autoCompletable(songFilterField).getCompletions();
        completions.clear();
        PlaylistConfigWrapper.loadConfig().playlists.forEach(playlist -> {
            playlistsView.getItems().add(playlist);
            completions.add(playlist.getName());
        });
    }

    @FXML
    private void onPlaylistCreate() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
        dialog.setTitle("MangoPlayer | Create playlist");
        dialog.setHeaderText("Please choose a name for your playlist.\nIt can be changed at any time");
        dialog.setContentText("Playlist name: ");
        dialog.showAndWait();
        if (dialog.getResult() != null) {
            Playlist playlist = new Playlist(dialog.getResult(), new HashSet<>(), 0, 0);
            playlist.ensureFields();
            PlaylistConfigWrapper.loadConfig().playlists.addFirst(playlist);
            repaintPlaylists();
            System.out.println("Creating new playlist '" + dialog.getResult() + "' with ID '" + playlist.getId() + "'");
        }
    }

    @Override
    public void onPlaylistDelete(Playlist playlist) {
        if (playlist.askAndRunDelete()) {
            repaintPlaylists();
        }
    }

    @Override
    public void onPlaylistOpen(Playlist playlist) {
        Utilities.removeHeldListeners(this);
        PlaylistScreenScene scene = PlaylistScreenScene.createNewScene(playlist);
        Stage stage = (Stage) songFilterField.getScene().getWindow();
        stage.setTitle("MangoPlayer | " + playlist.getName());
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
    }

    @Override
    public void onPlaylistRename(Playlist playlist) {
        playlist.askAndRunRename();
        repaintPlaylists();

        playlistsView.scrollTo(playlist);
        playlistsView.getSelectionModel().select(playlist);
    }

    @Override
    public void onSongViewClosed() {
        // Update playlist thumbnails when closing song view, as it might have been changed by the user
        playlistsView.refresh();
    }

    @Override
    public void onGraphicManage(Playlist playlist) {
        File graphicFile = new File(Utilities.graphicPathFromPlaylist(playlist));
        ButtonType replaceButton = new ButtonType("Replace", ButtonBar.ButtonData.LEFT);
        ButtonType removeButton = new ButtonType("Remove", ButtonBar.ButtonData.LEFT);
        ButtonType browseButton = new ButtonType("Browse", ButtonBar.ButtonData.LEFT);
        ButtonType result;
        if (graphicFile.exists()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", replaceButton, removeButton, browseButton, ButtonType.CANCEL);
            alert.setTitle("MangoPlayer | Playlist graphic");
            alert.setHeaderText("'" + playlist.getName() + "' has a pre-set graphic associated with it:\nYou can either replace with a new one, remove it entirely (falls back to first\nsong's thumbnail as graphic) or just browse it in your file manager");
            alert.setContentText("How do you want to continue?");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.showAndWait();
            result = alert.getResult();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.YES, ButtonType.NO);
            alert.setTitle("MangoPlayer | Playlist graphic");
            alert.setHeaderText("'" + playlist.getName() + "' does not have a pre-set graphic associated with it.\n This means it falls back to the first song's thumbnail as graphic.");
            alert.setContentText("Do you want to set a graphic for this playlist?");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.showAndWait();
            result = alert.getResult();
        }

        if (result == replaceButton || result == ButtonType.YES) {
            setPlaylistGraphic(playlist);
        }
        else if (result == removeButton) {
            removePlaylistGraphic(playlist);
        }
        else if (result == browseButton) {
            OperatingSystem.loadCurrentOS().browse(graphicFile);
        }
        playlistsView.refresh();
    }

    private void setPlaylistGraphic(Playlist playlist) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("MangoPlayer | Playlist graphic");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image file", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.tif", "*.tiff", "*.gif", "*.webp", "*.heic", "*.heif", "*.avif", "*.ppm", "*.pgm", "*.pbm", "*.pnm", "*.jp2", "*.j2k", "*.jpf", "*.tga", "*.psd", "*.exr", "*.svg"));
        File chosenFile = chooser.showOpenDialog(songFilterField.getScene().getWindow());
        if (chosenFile != null) {
            FfmpegFileConverter.getInstance().convertFilesToPng(Collections.singletonList(chosenFile)).thenAccept(pngFiles -> {
                if (!pngFiles.isEmpty()) {
                    File graphicFile = new File(Utilities.graphicPathFromPlaylist(playlist));
                    if (!graphicFile.getParentFile().exists()) {
                        graphicFile.getParentFile().mkdirs();
                    }
                    try {
                        Files.copy(pngFiles.getFirst().toPath(), graphicFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Updated graphic of playlist '" + playlist.getName() + "' (ID is '" + playlist.getId() + "')");
                        repaintPlaylists();
                    }
                    catch (IOException e) {
                        Utilities.showErrorScreen("Import song graphic", String.valueOf(e));
                    }
                }
                FfmpegFileConverter.getInstance().cleanupFilesToPngConversion();
            });
        }
    }
    private void removePlaylistGraphic(Playlist playlist) {
        File file = new File(Utilities.graphicPathFromPlaylist(playlist));
        try {
            Files.delete(file.toPath());
            System.out.println("Deleted graphic of playlist '" + playlist.getName() + "' (ID is '" + playlist.getId() + "')");
        }
        catch (IOException e) {
            Utilities.showErrorScreen("Delete song graphic", String.valueOf(e));
        }
    }

}
