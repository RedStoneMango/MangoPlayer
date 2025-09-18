package io.github.redstonemango.mangoplayer.graphic.controller.interfaces;

import io.github.redstonemango.mangoplayer.logic.Playlist;

public interface IPlaylistListable {
    void onPlaylistDelete(Playlist playlist);
    void onPlaylistOpen(Playlist playlist);
    void onPlaylistRename(Playlist playlist);
    void onGraphicManage(Playlist playlist);
}
