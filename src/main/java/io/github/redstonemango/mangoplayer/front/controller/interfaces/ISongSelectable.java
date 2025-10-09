package io.github.redstonemango.mangoplayer.front.controller.interfaces;

import io.github.redstonemango.mangoplayer.back.Playlist;
import io.github.redstonemango.mangoplayer.back.Song;

import java.util.Map;

public interface ISongSelectable {
    void onSelectionProcessContentChanges(Map<Song, Boolean> changedSongs);
    Playlist getSelectionParent();
}
