package io.github.redstonemango.mangoplayer.front.controller.interfaces;

import io.github.redstonemango.mangoplayer.back.Playlist;

public interface IPlaylistListable {
    void onPlaylistDelete(Playlist playlist);
    void onPlaylistOpen(Playlist playlist);
    void onPlaylistRename(Playlist playlist);
    void onGraphicManage(Playlist playlist);
}
