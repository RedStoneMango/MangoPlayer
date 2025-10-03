package io.github.redstonemango.mangoplayer.graphic.controller.playlistOverview;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IPlaylistListable;
import io.github.redstonemango.mangoplayer.graphic.entryBases.PlaylistEntryBase;
import io.github.redstonemango.mangoplayer.logic.Finals;
import io.github.redstonemango.mangoplayer.logic.Playlist;
import io.github.redstonemango.mangoplayer.logic.PlaylistExporting;
import io.github.redstonemango.mangoplayer.logic.Utilities;

import java.io.File;

public class PlaylistEntry extends PlaylistEntryBase {

    private final Playlist playlist;
    private final IPlaylistListable controller;

    public PlaylistEntry(final Playlist playlist, IPlaylistListable controller) {
        this.playlist = playlist;
        this.controller = controller;


        //init name
        nameLabel.setText(playlist.getName());

        //init song count
        int songCount = playlist.getSongs().size();
        songCountLabel.setText(songCount == 0 ? "No songs" : (songCount == 1 ? "1 song" : songCount + " songs"));

        //init graphic
        updateGraphic();

        //init context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openMenuItem = new MenuItem("Open");
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem renameMenuItem = new MenuItem("Rename");
        MenuItem graphicMenuItem = new MenuItem("Manage graphic");
        MenuItem exportMenuItem = new MenuItem("Export");
        openMenuItem.setOnAction(_ -> controller.onPlaylistOpen(playlist));
        deleteMenuItem.setOnAction(_ -> controller.onPlaylistDelete(playlist));
        renameMenuItem.setOnAction(_ -> controller.onPlaylistRename(playlist));
        graphicMenuItem.setOnAction(_ -> controller.onGraphicManage(playlist));
        exportMenuItem.setOnAction(_ -> PlaylistExporting.export(playlist));
        contextMenu.getItems().addAll(openMenuItem, deleteMenuItem, renameMenuItem, graphicMenuItem, exportMenuItem);
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            }
            else {
                contextMenu.hide();
            }
        });
    }

    public void updateGraphic() {
        File songGraphicFile = new File(Utilities.graphicPathFromPlaylist(playlist));
        if (songGraphicFile.exists()) {
            graphicView.setImage(new Image(songGraphicFile.toURI().toString()));
        }
        else if (!playlist.getSongs().isEmpty()) {
            File songThumbnail = new File(Utilities.thumbnailPathFromSong(playlist.getSongs().getFirst()));
            if (songThumbnail.exists()) {
                graphicView.setImage(new Image(songThumbnail.toURI().toString()));
            }
            else {
                graphicView.setImage(Finals.IMAGE_THUMBNAIL_FALLBACK);
            }
        }
        else {
            graphicView.setImage(Finals.IMAGE_NO_SONG);
        }
    }

    public Label getNameLabel() {
        return nameLabel;
    }

    public Label getSongCountLabel() {
        return songCountLabel;
    }

    @Override
    protected void onDelete(ActionEvent actionEvent) {
        controller.onPlaylistDelete(playlist);
    }

    @Override
    protected void onOpen(ActionEvent actionEvent) {
        controller.onPlaylistOpen(playlist);
    }
}
