package io.github.redstonemango.mangoplayer.graphic.controller.interfaces;

import io.github.redstonemango.mangoplayer.logic.Playlist;
import io.github.redstonemango.mangoplayer.logic.Song;

import java.util.Map;

public interface ISongSelectable {
    void onSelectionProcessContentChanges(Map<Song, Boolean> changedSongs);
    Playlist getSelectionParent();
}
