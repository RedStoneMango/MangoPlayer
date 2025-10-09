package io.github.redstonemango.mangoplayer.front.controller.interfaces;

import io.github.redstonemango.mangoplayer.back.Song;

public interface ISongPlayable {
    void onSongPlay(Song song);
    void onSongDelete(Song song);
    void onSongEditorOpen(Song song);
    void onSongsSorted();
}
