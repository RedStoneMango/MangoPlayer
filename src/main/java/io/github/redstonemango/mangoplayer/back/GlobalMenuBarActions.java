package io.github.redstonemango.mangoplayer.back;

import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Platform;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import io.github.redstonemango.mangoplayer.front.controller.textDisplay.TextDisplay;
import io.github.redstonemango.mangoplayer.back.config.MainConfigWrapper;

import java.io.File;

public class GlobalMenuBarActions {
    public static void onOpenDirectoryMenu() {
        OperatingSystem.loadCurrentOS().open(new File(MangoPlayer.APP_FOLDER_PATH));
    }
    public static void onManualSaveMenu() {
        MangoPlayer.getApplication().save(true);
    }
    public static void onDownloadYtDlpMenu() {
        OperatingSystem.loadCurrentOS().open(YtDlpManager.YOUTUBE_DLP_DOWNLOAD);
    }
    public static void onChangeYtDlpMenu() {
        boolean valid = YtDlpManager.getInstance().showRedefineDialog();
        if (valid) Utilities.showInformationScreen("Define yt-dlp path", "Successfully saved path to '" + MainConfigWrapper.loadConfig().ytDlpPath + "'. This file will be used to run yt-dlp in future.");
    }
    public static void onDefaultYtDlpMenu() {
        boolean valid = YtDlpManager.getInstance().useDefaultExecuteable();
        if (valid) Utilities.showInformationScreen("Resolve yt-dlp from PATH", "Successfully configured the player to resolve the yt-dlp executeable from the PATH. Calling yt-dlp will use the first 'yt-dlp' file found in your PATH");
    }
    public static void onDownloadFfmpegMenu() {
        OperatingSystem.loadCurrentOS().open(YtDlpManager.FFMPEG_DOWNLOAD);
    }
    public static void onChangeFfmpegMenu() {
        boolean valid = YtDlpManager.getInstance().showRedefineFfmpegDialog();
        if (valid) Utilities.showInformationScreen("Define ffmpeg path", "Successfully saved path to '" + MainConfigWrapper.loadConfig().ffmpegPath + "'. This file will be used to run ffmpeg in future.");
    }
    public static void onDefaultFfmpegMenu() {
        boolean valid = YtDlpManager.getInstance().useDefaultFfmpegExecuteable();
        if (valid) Utilities.showInformationScreen("Resolve ffmpeg from PATH", "Successfully configured the player to resolve the ffmpeg executeable from the PATH. Calling ffmpeg will use the first 'ffmpeg' file found in your PATH");
    }

    public static void onHelpPlaylistMenu() {
        TextDisplay.showNewDisplay(MangoPlayer.primaryStage, "Help Window", "Help -> How To -> Manage Playlists", """
                # Manage Playlists
                ## Content:
                1. [Adding a playlist](#adding-a-playlist)
                2. [Opening&Deleting a playlist](#openingdeleting-a-playlist)
                3. [Advanced actions](#advanced-actions)
                    1. [Rename](#rename)
                    2. [Manage Graphic](#manage-graphic)
                    3. [Export](#export)
                
                ---
                
                ## Adding a playlist
                In the playlist overview (First screen to be shown when staring the application) all existing playlists are listed.<p>
                To add one, use the _Add Playlist_ button at the screen's bottom. Pressing it brings up a dialog asking you for a name to set for your playlist _(The Name can always be changed afterwards)_<p>
                After you entered a name for the playlist, it will appear at the playlist list's top. _(The playlists are ordered by their last use. Using a playlist will automatically sort it to the list's top)_
                
                ## Opening&Deleting a playlist
                One a playlist is created, it can be opened and deleted using the buttons at it's right, i.e.:
                - Checkmark<p>Opens the playlist and displays the songs inside it.
                - Bin<p>Deleted the playlist. If the playlist has content, you will get a confirmation dialog first
                
                ---
                
                ## Advanced actions
                When context-clicking a playlist, a menu will pop up, containing some further functionalities:
                
                ### Rename
                This option shows an input dialog requesting you to enter a new name for the playlist. The new name will be applied (No data loss)
                
                ### Manage Graphic
                This options allows you to set an own thumbnail graphic for the playlist:
                - If none is set _(default)_, the first song's image will be used as thumbnail.
                - If a custom thumbnail exists, it will be used instead.
                The thumbnail can be imported from your local disk and has to be a `.png` file. However, if FFMPEG is linked as an external tool, the application can automatically convert most image formats to `.png` for you.
                
                ### Export
                This option exports the whole playlist as a `.zip` file.<br>
                The file contains every song inside the playlist, exported for use outside of the app and ordered by the order defined in the playlist itself.
                """);
    }

    public static void onHelpSongMenu() {
        TextDisplay.showNewDisplay(MangoPlayer.primaryStage, "Help Window", "Help -> How To -> Manage Playlists", """
                # Manage Songs
                ## Content:
                1. [The Song Manager](#the-song-manager)
                    1. [Importing songs](#importing-songs)
                    2. [Downloading Songs](#downloading-songs)
                    3. [Deleting songs](#deleting-songs)
                    4. [Advanced actions](#advanced-actions)
                2. [The Song Editor](#the-song-editor)
                    1. [Deleting](#deleting)
                    2. [Naming](#naming)
                    3. [Thumbnail management](#thumbnail-management)
                    4. [Volume adjustment](#volume-adjustment)
                    5. [Statistics](#statistics)
                3. [The Use Analyser](#the-use-analyser)
                    1. [Songs](#songs)
                    2. [Playlists](#playlists)
                
                ---
                
                ## The Song Manager
                In the playlist overview (First screen to be shown when staring the application) you will find a button at the bottom-left with the icon of a note. Pressing this button opens the 'Song Manager'<p>
                In the song manager, you can add/remove your songs, as well as using some other functionalities:
                
                ### Importing songs
                In the bottom part of the manager, there are two buttons. The left one _(Image of a file and an arrow)_ opens a file selection screen, where you can select any `.mp3` file you have saved _(If FFMPEG is linked as an external tool, the application can convert most audio formats to `.mp3` for you)_.<br>
                By selecting them, the player will automatically store them as asset and register them as new songs. Metadata like icon and title will be copied to the player's memory and applied automatically to the song's configurations.<br>
                An imported song can then be used in any playlist you wish.
                
                ### Downloading songs
                The second button in the bottom part of the song manager allows integration with the tools 'yt-dlp' and 'ffmpeg' for downloading audio directly from YouTube.<br>
                For further information, refer to `Help -> External Tools` in this application's menu bar.
                A downloaded song can then be used in any playlist you wish.
                
                ### Deleting songs
                Once a song is added (by either importing or downloading it), it will be shown in the song list.<br>
                When pressing the delete button _(bin icon)_, the song will be removed again. (This shows a confirmation dialog first).<br>
                A deleted song will be removed from every playlist it is used in and clear all memory connected to it, letting the application completely forget the song.
                
                ### Advanced actions
                When context-clicking a song, a menu will pop up, containing some further functionalities:
                
                - Browse assets<br>MangoPlayer saves all assets required to run in a separated directory. This behavior allows the application to keep full control over the required files and preventing them from being deleted accidentally when the user cleans up a directory on the drive.<br>However, sometimes people who are a bit more experienced with this application may want to access a song's data directly. To do so, MangoPlayer offers this shortcut. If you just want to save your song locally, please use the export function _(see next entry)_ instead of this option. The accessible assets are:
                    - Audio<br>The audio file storing the song's musical data. This is a plain `.mp3` file without important metadata.
                    - Thumbnail<br>The thumbnail (icon) of the song. This is a classic `.png` image file. _(Only accessible if a thumbnail is set)_
                    - YouTube<br>If this song was downloaded from YouTube using the yt-dlp integration, this option opens the original video inside your web browser.
                
                - Export to disk<br>Exports the song from the MangoPlayer.<br>This automatically adds all known metadata to the exported song file _(e.g. Thumbnail / Name)_ and saves it to a location on your disk, chosen by the user.<br>If possible, use this option instead of "Browse Assets" _(see previous entry)_ as the export is designed specifically for saving data without modifying the application's memory _(an issue that may occur if asset browsing is used incorrectly)_
                
                ---
                
                ## The Song Editor
                When pressing the edit button _(piece of paper & pen)_ of a song inside the song manager's list, the song editor opens with the following possible actions:
                
                ### Deleting
                In the top-right corner, the song can be deleted (see [this entry](#deleting-songs) for more information on deleting)
                
                ### Naming
                In the top of the song entry, you will find a text field containing the song's name. This field can be edited any time to rename the song safely.
                The name cannot be empty or blank _(blank text: consists of whitespaces only)_
                
                ### Thumbnail management
                When clicking on the song's icon (thumbnail), you will be asked whether to perform one of the following actions _(may differ depending on the song)_:
                - Replace<br>Allows you to set a custom thumbnail. The thumbnail can be imported from your local disk and has to be a `.png` file. However, if FFMPEG is linked as an external tool, the application can automatically convert most image formats to `.png` for you.
                - Remove<br>Removes the thumbnail and leaves back a fallback image. _(Only possible if a thumbnail exists)_
                - Regenerate<br>If the song was downloaded from YouTube using the yt-dlp integration, the original video's thumbnail can be downloaded and applied to this song.
                
                ### Volume adjustment
                Some songs may be louder or more quiet than other ones. For this, every song's volume can be adjusted individually using the slider below the name field:<br>
                The adjusted volume will add up with the player's current volume and allow you to customize how a song is played. If the adjustment is low, the song will be more quiet than it in fact is, if the adjustment is high, the song will be louder.<br>
                By pressing the test-listen button _(next to the adjustment slider)_, you can listen to an excerpt of the song's middle part with the volume adjustment applied.
                
                ### Statistics
                Below the volume adjustment, you'll find some statistics about the song:
                - Played<br>The amount of times, the song was played completely
                - Uses<br>The amount of playlists that contain this song
                - Source<br>Where this song is from. Most time, this will be _`-- Local disk --`_, but when downloaded using yt-dlp, it is a direct link to the original YouTube video
                - Internal identifier<br>The internal unique identifier of this song. This is used for identifying the song, even if it's name has changed and won't be interesting to most users
                
                ---
                
                ## The use analyser
                Pressing the button with the magnifying glass icon in the song manager's bottom-left corner, opens the use analyser providing and comparing song statistics ordered by context:
                
                ### Songs
                - Listen count<br>The amount of times, the song was played completely
                - Use count<br>The amount of playlists that contain this song
                
                ### Playlists
                - Songs played<br>The amount of times this playlist has played any song inside it
                - Total play time<br>The total amount of time this playlist has played, formatted as `HOURS:MINUTES:SECONDS`
                """);
    }

    public static void onHelpPlaylistUseMenu() {
        TextDisplay.showNewDisplay(MangoPlayer.primaryStage, "Help Window", "Help -> How To -> Use a playlist", """
                # Using a playlist
                ## Content:
                1. [Opening a playlist](#opening-a-playlist)
                2. [Collapsing the controls](#collapsing-the-controls)
                3. [Controlling the playlist's songs](#controlling-the-playlists-songs)
                   1. [Adding songs](#adding-songs)
                   2. [Removing songs](#removing-songs)
                   3. [Sorting songs](#sorting-songs)
                4. [Controlling the playlist's audio](#controlling-the-playlists-audio)
                   1. [Playing the playlist](#playing-the-playlist)
                   2. [Playing a specific song](#playing-a-specific-song)
                   3. [Pausing / Resuming the playlist](#pausing-resuming-the-playlist)
                   4. [Stopping the playlist](#stopping-the-playlist)
                   5. [Skipping songs](#skipping-songs)
                   6. [Seeking a song's position](#seeking-a-songs-position)
                   7. [Adjusting the volume](#adjusting-the-volume)
                5. [Controlling the play flow](#controlling-the-play-flow)
                   1. [Shuffle](#shuffle)
                   2. [Loop](#loop)
                   3. ["End After Song"](#end-after-song)
                6. [Detaching the controls](#detaching-the-controls)
                    1. [Native pause combination](#native-pause-combination)
                
                ---
                
                ## Opening a playlist
                Once a playlist is created _(refer to `Help -> How To -> Manage Playlists` in the application's menu bar)_, it can be opened by pressing the checkmark button on its entry's right.<br>
                The newly opening screen can be used to interact with your playlist.
                
                ## Collapsing the controls
                The playlist screen mainly consists of two parts: The upper control pane, and the lower song list _(refer to [this](#controlling-the-playlists-audio) for further information on the controls)_.<br>
                Sometimes (when focusing on the songs themselves and not on the play controls) it can be helpful to move the controls out of the way. This can simply be done by clicking on the control pane's top bar, causing it to collapse / expand.
                
                ## Controlling the playlist's songs
                
                ### Adding songs
                To add songs to your playlist, use the `Add / Remove songs` button at the screen's bottom. This will being up a restricted version of the Song Manager _(refer to `Help -> How To -> Manage Songs` in the application's menu bar)_ allowing you to set which songs to use inside our playlist by checking the checkmark button at every song's right<br>
                Once all songs are selected / deselected the way you wish, you can confirm the changes by pressing the blue button in the window's top-right corner or just by pressing `Enter`. This will...
                - ... Add all newly selected songs to the playlist's bottom and automatically scroll to the lastly added song.
                - ... Remove all deselected songs from the playlist. Before a song is removed, a confirmation dialog will be shown.
                In order to be able to add a song to your playlist, you have to have imported / downloaded it using the song manager first.
                
                ### Removing songs
                To remove song from your playlist, there are two major ways:
                1. By pressing the delete button _(bin icon)_ at it's right.
                2. By pressing the `Add / Remove songs` button at the screen's bottom and deselecting the song in the newly opened window.
                With both ways, you will be prompted a confirmation dialog first and the song will then be removed from your playlist.<br>
                A removed song stays accessible using the Song Manager _(refer to `Help -> How To -> Manage Playlists` in the application's menu bar)_ and is still part of other playlist if they contained it before. In order to fully delete a song, the Song Manager has to be used.
                
                ### Sorting songs
                Sometimes you may want to re-arrange your songs in a different order.<br>
                To do so, you have to click on the `=` at a song's left and hold down the mouse button. When then moving the mouse, a green line will appear between two existing songs, based on where your cursor currently is and when releasing the mouse button, the song will be moved between these two songs, exactly where the line was.<br>
                This is only possible when you are neither filtering your songs _(entering something inside the text field below the control pane)_ nor playing your playlist at the moment.
                
                ---
                
                ## Controlling the playlist's audio
                
                ### Playing the playlist
                To play your playlist, beginning with the first song _(or a random one if shuffle is active)_, press the play button _(middle button inside the control pane)_ while the playlist is not playing _(or press `Ctrl + Space`)_.<br>
                If you want to start, with a specific song, use [this](#playing-a-specific-song) feature instead.
                
                ### Playing a specific song
                To play your playlist, while ensuring that it starts with a specific song, you can press the arrow button at a song's right. This will play this song and then continue playing the playlist based on your [flow control settings](#controlling-the-play-flow).<br>
                If you want to start the playlist with the first song _(or a random one if shuffle is active)_, use [this](#playing-the-playlist) feature instead
                
                ### Pausing / Resuming the playlist
                While your playlist is playing, you can pause and resume it using the play button _(middle button inside the control pane)_ or by pressing `Ctrl + Space`.<br>
                Mind that the play button changes its icon from the arrow to two lines if the playlist is currently playing.<br>
                Pausing allows you to resume your playlist at the same position later on. If you want to completely end the playing of your playlist, [stop](#stopping-the-playlist) it entirely.
                
                ### Stopping the playlist
                When your playlist has started playing, the stop button _(icon of a black square)_ is enabled. By clicking it (or by pressing `Escape` on your keyboard) you can stop the playlist and end the playing process entirely.<br>
                If you want to just pause, the playlist and later resume it at the same position, use [this](#pausing-resuming-the-playlist)<br>
                Note, that if your playlist is not playing and you press the stop-shortcut (`Escape` on your keyboard), the playlist will close.
                
                ### Skipping songs
                While your playlist is playing, you can skip forwards and backwards in the song queue by using the corresponding buttons to the stop button's left and right _(the buttons have the image of two arrows pointing towards a line)_<br>
                The skipping works like this:
                - Forwards<br>Play the next upcoming song. This is either the next song in the randomized queue _(if shuffle is active)_ or the next song in your order _(without shuffle)_. If the playlist ends after this song, the button is disabled.
                - Backwards<br>Play the previous song. This is either the previous song in the randomized queue _(if shuffle is active)_ or the previous song in your order _(without shuffle)_. If the playlist ends after this song, the button is disabled.<br>Mind, that if shuffle is active and the playlists loops, the quere is re-shuffled, what may mean that the previous song is last one in the new queue, not the actually previous song.
                
                ### Seeking a song's position
                While a song is playing, the slider in the control pane's middle displays the current progress, i.e. how much of the song has already been played.<br>
                By dragging this slider to another position, the audio position can be adjusted to play a later / earlier part of the song.
                
                ### Adjusting the volume
                The MangoPlayer has an in-built volume mechanism that can be muted, increased or decreased by the user like this:
                - Muting<br>When clicking the speaker in the control pane's top-right corner, the player can be muted / unmuted.
                - Increasing / Decreasing volume<br>When hovering over the speaker, a slider appears below it. This slider can be used to set the player's global volume by dragging the thumb up and down _(up: louder; down: more quiet)_.
                Note that the final volume depends on the current song's volume adjustment as explained in the Song Editor help page _(refer to `Help -> How To -> Manage Songs`)_
                
                ---
                
                ## Controlling the play flow
                
                ### Shuffle
                By pressing the shuffle button _(image of two crossing arrows)_ inside the control pane, the shuffle mode can be toggled. If shuffle is active, the player will automatically genrate a random queue for the songs inside your playlist. This queue will be used when playing the playlist instead of the declared song order.<br>
                The queue is re-shuffled every time the playlist plays and always creates a new order to play the songs. If shuffle is activated while a song is currently playing, the current song will always be the first inside the queue
                
                ### Loop
                By pressing the loop button _(image of two circular arrows)_ inside the control pane, the loop mode can be set. Possible loop types are:
                - Off _(2 light gray arrows with space between them)_<br>The playlist will not loop. Instead it will stop as soon as the end of the playing queue is reached
                - All _(2 black arrows with space between them)_<br>The playlist will loop. As soon as the queue's end is reached, it will play the queue again (Exception: In shuffle mode, the queue will be re-shuffled first)
                - Single _(2 black arrows with a "1" them)_<br>The current song will loop. As soon as it finished it will start to play again.
                Mind that the loop mode is overridden by the ["End After Song" option](#end-after-song)
                
                ### "End After Song"
                When a song is playing, the "End After Song" button is enabled _(image of a triangle pointing towards a line)_. When the button is toggled on, it will force the playlist to stop after the current song has ended, regardless of other play flow settings.<br>
                While this button is activated, a red version of its icon is shown on the control pane's top bar, ensuring that is will be visible, even if the pane is collapsed.<br>
                When the song finished and the playlist has ended, the button will automatically toggle itself off again.
                
                ---
                
                ## Detaching the controls
                Sometimes it can be annoying to have a whole window visible when simply wanting to have an interface for controlling the player. In these cases, the controls can be detached from the actual application window by clicking the lock icon in the control pane's top-right corner.<br>
                Once the controls are detached, a new window with the control's exact content will appear, allowing the user to control the player the same way it is done in the classic app window.<br>
                Other than a normal window, detached controls have the following advantages:
                1. Consistency<br>Detached controls are visible to you even if the application itself is minimized / not visible. This allows the controlling of the playlist without needing to have the whole application window visible.
                2. Unfocusability<br>The controls are shown on your screen but do not count as an actual window. This means, while focusing one window _(e.g. by typing something inside a text field there)_ and then interacting with the controls, the focus remains on the window _(in this example: The cursor is still inside the text field)_,.
                To close the detached window view, press the light gray highlighted `x` in its top bar or click the lock icon inside the MangoPlayer's main window again.
                
                ### Native pause combination
                Instead of the "song control detach icon", the detached control pane has a little icon of a keyboard in its top-right corner.<br>
                By clicking this icon while the MangoPlayer is focused, you can define a custom key combination by pressing the desired keys on your keyboard.<br>
                Once a key combination is defined, the MangoPlayer will automatically check for its press while you have the song controls detached. (This detection works even if you have another application focused !!!) Pressing this native combination, the detached song controls will automatically pause/resume the current song, allowing for a simpler use of the detached controls.<br>
                To remove a combination, you can either overwrite it by defining a new combination (as explained above) or you can press the keyboard icon again while being in the "combination defining mode" to remove any current key combination.
                """);
    }
    public static void onHelpExternalGeneralMenu() {
        TextDisplay.showNewDisplay(MangoPlayer.primaryStage, "Help Window", "Help -> External Tools", """
                # External tools
                ## Content:
                1. [FAQ on external tools](#faq-on-external-tools)
                    1. [What are external tools?](#what-are-external-tools)
                    2. [What do you mean by "integrating the tools"?](#what-do-you-mean-by-integrating-the-tools)
                    3. [Do I have to install these tools?](#do-i-have-to-install-these-tools)
                2. [Installing external tools](#installing-external-tools)
                    1. [Installing the executeable](#installing-the-executeable)
                    2. [Linking the executeable](#linking-the-executeable)
                
                ---
                
                ## FAQ on external tools
                
                ### What are external tools?
                In the context of this application, an external tool is considered a command line application that was integrated into this player by the developers.<br>
                The tools themselves are completely independent from the MangoPlayer and their developers do not have anything to do with the player itself. However the huge possibilities these tools offer allow a lot of software devs to implement them inside their own application and combine the best of both products.<br>
                Because the external tools are independent from us, there always is a little risk to use them and we are not responsible for any bugs connected with them, however we try to build our implementation as robust as possible to ensure a save use of our application.
                
                ### What do you mean by "integrating the tools"?
                When talking about the integration of external tools into our application, we mean that we offer some functionalities _(like downloading songs directly from YouTube)_ inside our application that depend on using external tools for processing. These functions are optional but may be used by some users.<br>
                Basically, we write code for our application _(like the windows you see when using it)_ but the logic that is computing your actions is executed by other programms. This allows us to focus on other aspects of the app while still being able to use fully functional, maintained logic for some computations.
                
                ### Do I have to install these tools?
                No, you don't. The tools offer you some for functionalities, but they are not required for the application to run. Most features can be used without them and work completely fine if you do not want to install any other things.<br>
                The tools are just for some quality of life enhancements but not mandatory for the MangoPlayer to run.
                
                ## Installing external tools
                
                ### Installing the executeable
                Because the external tools are not connected with us, we are legally not allowed to share or distribute them. However, you can download the executeable on the vendor's website and install it that way.<br>
                To simplify this process for you, we offer a direct link to the download pages of these tools. They can be accessed by using `MangoPlayer -> TOOL NAME -> Open download page` in the application's menu bar.<br>
                Once the executeable is downloaded, you have to install it like any CLI tool. In most cases, this is not a classic application, so please refer to the vendor's instructions on how to install it.<br>
                Sometimes, the executeable may not be resolved correctly by the MangoPlayer. In this case, please [link it](#linking-the-executeable)
                
                ### Linking the executeable
                Once the executeable is installed, it may be possible that the MangoPlayer cannot resolve it correctly because it doesn't have permissions to check your system's PATH.<br>
                The PATH is a list of folders in your computer that contain CLI applications and expose them to other tools like this player.<br>
                If you however do not want the player to grant all permissions over the PATH or your system's security manager does so by default, you can give the player the location of your tool, so it automatically uses it instead of searching your PATH. In most cases this will redundant as on most operating systems _(except MacOS)_ you will never get a problem like this.<br>
                To define the absolute path to your executeable, you can use `MangoPlayer -> TOOL NAME -> Set executeable directory` in the application's menu bar and to resolve using the PATh, use `MangoPlayer -> TOOL NAME -> Resolve from PATH`.<br>
                <br>
                If you've linked your tool and the player is still unable to find it, you are most likely running the application in a sandboxed _(= restricted)_ environment. In this case, try in a not-sandboxed environment first, before opening an issue in the issue tracker.
                """);
    }
    public static void onLicenseMenu() {
        TextDisplay.showNewDisplay(MangoPlayer.primaryStage, "License Window", "Help -> License", """
                # The MIT License (MIT)
                
                ## Copyright (c) 2025 RedStoneMango

                Permission is hereby granted, free of charge, to any person obtaining a copy
                of this software and associated documentation files (the "Software"), to deal
                in the Software without restriction, including without limitation the rights
                to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
                copies of the Software, and to permit persons to whom the Software is
                furnished to do so, subject to the following conditions:

                The above copyright notice and this permission notice shall be included in
                all copies or substantial portions of the Software.

                <u>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
                IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
                FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
                AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
                LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
                OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
                THE SOFTWARE.</u>
                """);
    }
    public static void onAboutMenu() {
        TextDisplay.showNewDisplay(MangoPlayer.primaryStage, "About Window", "Help -> About", """
                # About
                
                ## Content
                1. [General](#general)
                2. [Contributions Welcome](#contributions-welcome)
                
                ---
                
                ## General
                
                **MangoPlayer** started as a personal side project — a simple music player built to suit my own workflow and listening habits. Frustrated with existing players (whether due to bulky UIs or missing features), I wanted something lightweight, clean, and distraction-free.
                
                Over time, the project has grown beyond its original scope, shaped by experimentation, user feedback, and continuous iteration.
                
                Today, MangoPlayer is an open-source project aimed at delivering a **clean**, **efficient**, and **customizable** music experience for anyone who values simplicity without sacrificing functionality. With every release, it continues to evolve — adding new features, refining usability, and improving cross-platform support.
                
                ---
                
                ## Contributions Welcome
                
                MangoPlayer is still actively evolving, and I’m always open to new ideas, feature requests, and improvements from the community. Whether you're a developer, designer, tester, or someone with helpful feedback — your contributions are genuinely appreciated.
                
                Ways you can help:
                
                - Report bugs or issues
                - Suggest new features or UI/UX improvements
                - Submit pull requests with enhancements or fixes
                - Improve the documentation
                - Share the project with others
                
                Feel free to reach out via GitHub, open an issue, or submit a PR using the links under
                `Help → Links` in the application's menu bar.
                
                Thanks for being part of the journey!
                """);
    }
    public static void onHomepageMenu() {
        Platform.runLater(() -> {
            OperatingSystem.loadCurrentOS().open("https://github.com/RedStoneMango/MangoPlayer");
        });
    }
    public static void onIssuesMenu() {
        Platform.runLater(() -> {
            OperatingSystem.loadCurrentOS().open("https://github.com/RedStoneMango/MangoPlayer/issues");
        });
    }
}
