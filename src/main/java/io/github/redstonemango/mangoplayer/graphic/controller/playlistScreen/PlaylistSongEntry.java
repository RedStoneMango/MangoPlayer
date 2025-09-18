package io.github.redstonemango.mangoplayer.graphic.controller.playlistScreen;

import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.ISongPlayable;
import io.github.redstonemango.mangoplayer.graphic.entryBases.PlaylistSongEntryBase;
import io.github.redstonemango.mangoplayer.logic.Finals;
import io.github.redstonemango.mangoplayer.logic.Song;
import io.github.redstonemango.mangoplayer.logic.Utilities;

import java.io.File;

public class PlaylistSongEntry extends PlaylistSongEntryBase {

    private final Song song;
    private final ISongPlayable controller;
    public final MenuItem deleteMenuItem;

    public PlaylistSongEntry(final Song song, final ISongPlayable controller) {
        this.song = song;
        this.controller = controller;

        // init name
        nameLabel.setText(song.getName());

        // init thumbnail. if no thumbnail exists, the default fallback image "thumbnail_fallback.png" will be used
        File thumbnailFile = new File(Utilities.thumbnailPathFromSong(song));
        if (thumbnailFile.exists()) {
            ((ImageView) ((FlowPane) ((HBox) this.getLeft()).getChildren().get(1)).getChildren().getFirst()).setImage(new Image(thumbnailFile.toURI().toString()));
        }
        // init button actions
        deleteButton.setOnAction(_ -> controller.onSongDelete(song));

        // init sorting
        unsortableTooltip.setShowDelay(Duration.millis(250));
        sortLabel.setTooltip(null);

        //init context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem playMenuItem = new MenuItem("Play");
        deleteMenuItem = new MenuItem("Delete");
        MenuItem editMenuItem = new MenuItem("Edit");
        playMenuItem.setOnAction(_ -> controller.onSongPlay(song));
        deleteMenuItem.setOnAction(_ -> controller.onSongDelete(song));
        editMenuItem.setOnAction(_ -> controller.onSongEditorOpen(song));
        contextMenu.getItems().addAll(playMenuItem, deleteMenuItem, editMenuItem);
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            }
            else {
                contextMenu.hide();
            }
        });
    }

    public Label getNameLabel() {
        return nameLabel;
    }

    public Label getSortLabel() {
        return sortLabel;
    }

    public void setSortable(boolean sortable) {
        if (sortable) {
            sortImageView.setImage(Finals.IMAGE_SORT_ENTRY_ON);
            sortLabel.setTooltip(null);
            sortLabel.setCursor(Cursor.MOVE);
        }
        else {
            sortImageView.setImage(Finals.IMAGE_SORT_ENTRY_OFF);
            sortLabel.setTooltip(unsortableTooltip);
            sortLabel.setCursor(Cursor.DEFAULT);
        }
    }

    public void setDeletable(boolean deletable) {
        deleteMenuItem.setDisable(!deletable);
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    @Override
    protected void onDelete(ActionEvent actionEvent) {
        controller.onSongDelete(song);
    }

    @Override
    protected void onPlayThis(ActionEvent actionEvent) {
        controller.onSongPlay(song);
    }
}
