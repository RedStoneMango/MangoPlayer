package io.github.redstonemango.mangoplayer.front.controller.interfaces;

import io.github.redstonemango.mangoplayer.back.Song;
import org.jetbrains.annotations.Nullable;

public interface ISongControllable {
    /**
     * @param isSelected This represents whether the user selected the current song (Only applies to selection mode). If we are in viewing mode (not selecting), this is 'null'
     * @return Whether the action is valid. In viewing mode, this does not change anything. In selection mode, this resets the change done to the selection state if the action was invalid
     */
    boolean onSongEntryAction(Song song, @Nullable Boolean isSelected);
    void onSongEntryDelete(Song song);
    void onSongEntryBrowse(Song song);
}
