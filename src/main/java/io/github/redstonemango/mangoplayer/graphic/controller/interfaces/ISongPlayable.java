package io.github.redstonemango.mangoplayer.graphic.controller.interfaces;

import io.github.redstonemango.mangoplayer.logic.Song;

public interface ISongPlayable {
    void onSongPlay(Song song);
    void onSongDelete(Song song);
    void onSongEditorOpen(Song song);
    void onSongsSorted();
}
