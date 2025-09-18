package io.github.redstonemango.mangoplayer.logic;

import javafx.scene.image.Image;

public class Finals {
    public static final Image IMAGE_SHUFFLE_ON = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/shuffle_on.png"));
    public static final Image IMAGE_SHUFFLE_OFF = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/shuffle_off.png"));

    public static final Image IMAGE_LOOP_NONE = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/loop_none.png"));
    public static final Image IMAGE_LOOP_SINGLE = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/loop_single.png"));
    public static final Image IMAGE_LOOP_ALL = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/loop_all.png"));

    public static final Image IMAGE_SORT_ENTRY_ON = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/sort_entry_on.png"));
    public static final Image IMAGE_SORT_ENTRY_OFF = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/sort_entry_off.png"));

    public static final Image IMAGE_PLAY = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/play.png"));
    public static final Image IMAGE_PLAY_THIS = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/play_this.png"));
    public static final Image IMAGE_PAUSE = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/pause.png"));
    public static final Image IMAGE_STOP = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/stop.png"));

    public static final Image IMAGE_NO_SONG = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/no_song_playing.png"));
    public static final Image IMAGE_THUMBNAIL_FALLBACK = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/thumbnail_fallback.png"));

    public static final Image IMAGE_UNDETACHED = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/undetached.png"));
    public static final Image IMAGE_DETACHED = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/detached.png"));

    public static final Image IMAGE_VOLUME_OFF = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/volume_off.png"));
    public static final Image IMAGE_VOLUME_ON = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/volume_on.png"));

    public static final Image IMAGE_STOP_AFTER_SONG_WARNING = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/stop_after_song_warning.png"));

    public static final String STYLESHEET_FORM_APPLICATION_MAIN = Finals.class.getResource("/io/github/redstonemango/mangoplayer/styles/none.css").toExternalForm();

    public static final Image IMAGE_SELECT = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/select.png"));

    public static final Image IMAGE_SAVING = new Image(Finals.class.getResourceAsStream("/io/github/redstonemango/mangoplayer/images/saving.png"));

    public static final String STYLE_CODE_SONG_PLAYING = "-fx-border-color: blue; -fx-border-width: 2;";
    public static final String STYLE_CODE_SONG_NOT_PLAYING = "-fx-border-color: gray; -fx-border-width: 1";
}
