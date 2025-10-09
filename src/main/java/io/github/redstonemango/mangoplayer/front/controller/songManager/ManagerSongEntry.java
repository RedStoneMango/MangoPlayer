package io.github.redstonemango.mangoplayer.front.controller.songManager;

import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.ISongControllable;
import io.github.redstonemango.mangoplayer.front.entryBases.ManagerSongEntryBase;
import io.github.redstonemango.mangoplayer.back.Finals;
import io.github.redstonemango.mangoplayer.back.Song;
import io.github.redstonemango.mangoplayer.back.Utilities;

import java.io.File;

public class ManagerSongEntry extends ManagerSongEntryBase {

    private final Song song;
    private final ISongControllable controller;
    private final boolean selectionMode;
    private final MenuItem actionMenuItem;

    public ManagerSongEntry(final Song song, final boolean selectionMode, final ISongControllable controller) {
        this.song = song;
        this.controller = controller;
        this.selectionMode = selectionMode;

        // init name
        nameLabel.setText(song.getName());

        // init thumbnail. if no thumbnail exists, the default fallback image "thumbnail_fallback.png" will be used
        File thumbnailFile = new File(Utilities.thumbnailPathFromSong(song));
        if (thumbnailFile.exists()) {
            thumbnailView.setImage(new Image(thumbnailFile.toURI().toString()));
        }

        // init faces
        deleteButton.setVisible(!selectionMode);
        if (selectionMode) {
            ((ImageView) actionButton.getGraphic()).setImage(Finals.IMAGE_SELECT);
        }

        //init context menu
        ContextMenu contextMenu = new ContextMenu();
        actionMenuItem = new MenuItem(selectionMode ? (actionButton.isSelected() ? "Deselect" : "Select") : "Open");
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem browseMenuItem = new MenuItem("Browse assets");
        MenuItem exportMenuItem = new MenuItem("Export to disk");
        actionMenuItem.setOnAction(e -> {
            actionButton.setSelected(!actionButton.isSelected());
            onAction(e);
        });
        deleteMenuItem.setOnAction(_ -> controller.onSongEntryDelete(song));
        browseMenuItem.setOnAction(_ -> controller.onSongEntryBrowse(song));
        exportMenuItem.setOnAction(_ -> export());
        contextMenu.getItems().add(actionMenuItem);
        if (!selectionMode) contextMenu.getItems().addAll(deleteMenuItem, browseMenuItem, exportMenuItem);
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            }
            else {
                contextMenu.hide();
            }
        });
    }

    private void export() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export song");
        chooser.setInitialFileName(Utilities.formatAsFriendlyText(song.getName()) + ".mp3");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Mp3 audio file", "*.mp3"));
        File file = chooser.showSaveDialog(actionButton.getScene().getWindow());
        if (file != null) {
            if (!file.getName().endsWith(".mp3")) {
                file = new File(file.getAbsolutePath() + ".mp3");
            }
            boolean success = song.exportToFile(file);
            if (success) {
                ButtonType browseButton = new ButtonType("Browse file", ButtonBar.ButtonData.YES);
                ButtonType continueButton = new ButtonType("Stay in application", ButtonBar.ButtonData.NO);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", browseButton, continueButton);
                alert.setTitle("Export successful");
                alert.setHeaderText("Successfully exported song '" + song.getName() + "' to " + file.getName());
                alert.setContentText("Do you want to browse the file?");
                alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
                alert.showAndWait();
                if (alert.getResult() == browseButton) {
                    OperatingSystem.loadCurrentOS().browse(file);
                }
            }
        }
    }

    public ToggleButton getActionButton() {
        return actionButton;
    }

    public Label getNameLabel() {
        return nameLabel;
    }

    @Override
    protected void onAction(ActionEvent actionEvent) {
        boolean valid = controller.onSongEntryAction(song, actionButton.isSelected());
        if (!valid) actionButton.setSelected(!actionButton.isSelected());
        updateTexts();
    }

    protected void updateTexts() {
        actionMenuItem.setText(selectionMode ? (actionButton.isSelected() ? "Deselect" : "Select") : "Open");
    }

    @Override
    protected void onDelete(ActionEvent actionEvent) {
        controller.onSongEntryDelete(song);
    }
}
