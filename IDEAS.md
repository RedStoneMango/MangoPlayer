# Feature Ideas

- [X] **Yt-dlp update dialog on search error**

  Currently, the user has to update yt-dlp manually when an error occurs while searching YouTube for a track. Add a dialog that automatically detects a known error and suggests the user to update, maybe even runs `yt-dlp -U` automatically.

- [X] **"Missing library" error**

  Audio playback requires system dependencies. While they should normally be pre-installed, on some Linux devices `avcodec`, `ffmpeg`, or similar may be missing. Detect this situation and prompt the user to install the required packages.

- [ ] **Export playlists as `.m3u` files**

  Allow the user to export playlists into `.m3u` format in addition to other formats, enabling compatibility with a wide range of media players.

- [X] **Expanded “scroll to element” behavior**

  Improve UX by auto-scrolling to relevant UI components, such as:
  - When adding a song to a playlist
  - When returning from a detail view to a playlist overview

- [X] **Expanded ListView element selection support**

  Automatically highlight/select a song in the song manager after it was added, or after an operation that modifies it, improving workflow clarity.

- [X] **Song restart behavior for “queue backwards” button**

  When a user presses the “previous” or “queue backwards” button and the current playback time is greater than 2 seconds, restart the current song instead of going to the previous one.

- [X] **Trim redundant characters from exported song filenames**

  Clean exported filenames by stripping unnecessary characters such as emojis, excess whitespace, or non-standard symbols, ensuring better compatibility across filesystems.

- [X] **Track and display song duration in statistics**

  Store a song's length in its metadata, display it:
  - In the song editor’s statistics section
  - In the use-analyser
  
  Also compute and show the total playlist length in the analyser. Consider lazy-loading the media metadata at startup (with a splash screen while initializing?).

- [X] **“Jump to song/playlist” in use-analyser**

  In the use-analyser tables, allow the user to click on a song or playlist entry to directly jump to it in the UI.

- [X] **Remove duplicate songs in playlists after config load**

  After loading the I/O config, check for and remove multiple occurrences of the same song within a playlist.

- [X] **Make the song download process cancelable**

  Add the ability for users to cancel song downloads in progress, particularly helpful if the process is stalled or the wrong item is being downloaded.

  **This is not meant to be an official cancel feature but a killing mechanism if a process gets stuck**
