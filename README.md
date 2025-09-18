# MangoPlayer 🎵

MangoPlayer is a lightweight, feature-rich music playlist manager that enables you to **import, edit, organize, and play music playlists** with ease. With powerful integration features, flexible song/playlist management, and a clean UI, MangoPlayer is built for users who want both simplicity and control.

---

## ✨ Features

### 🎶 Playlist Management
- Create, rename, delete, and open playlists.
- Set custom thumbnails for playlists.
- Export playlists as `.zip` archives (songs included).
- Context menu for advanced playlist actions.

### 🎵 Song Management
- Import songs from local `.mp3` files.
- Download songs directly from YouTube (via `yt-dlp` & `ffmpeg`).
- Edit song metadata (title, thumbnail, volume).
- Export individual songs with metadata included.
- View usage stats: play count, playlists used in, source link, and unique ID.

### ▶️ Playback & Control
- Play playlists or individual songs.
- Pause, resume, stop, and skip tracks.
- Adjust global volume and per-song volume.
- Shuffle, loop (all or single), and "End After Song" modes.
- Detachable player controls for seamless multitasking.

### 🔍 Song & Playlist Statistics
- Built-in usage analyzer with detailed stats:
    - Songs: play count, use count
    - Playlists: total songs played, total playtime

### 🧰 External Tool Integration (Optional)
- **`yt-dlp`** for YouTube downloading
- **`ffmpeg`** for audio conversion and thumbnail handling
- Configure executable path or auto-detect via system `PATH`

---

## 📦 Installation

<!-- > Currently distributed as a standalone desktop application.

1. Download the latest release from the Releases tab.
2. (Optional) For extended features:
    - Install `yt-dlp` and/or `ffmpeg` from their official websites.
    - Link them via:  
      `MangoPlayer -> TOOL NAME -> Set Executable Directory`  
      or resolve from PATH:  
      `MangoPlayer -> TOOL NAME -> Resolve from PATH` -->

> There are no official releases yet. Please come back later

---

## 📚 Usage Overview

### 🔧 Creating & Managing Playlists
- Use the **Add Playlist** button on the home screen.
- Rename and set thumbnails via right-click context menu.
- Export a playlist (with songs) to a `.zip` file for external use.

### 🎧 Managing Songs
- Open **Song Manager** using the note icon.
- Import `.mp3` files or download via YouTube.
- Edit song details, including:
    - Title
    - Thumbnail (manual, YouTube, or removal)
    - Volume adjustment
- Export songs to disk with metadata.
- View song stats and source.

### 🎼 Editing Playlists
- Add/remove songs via the **Add / Remove Songs** button.
- Reorder songs via drag-and-drop (`=` icon).
- Remove individual songs (bin icon).
- Songs must be imported before they can be added to playlists.

### 🔊 Playing Music
- Play full playlists or individual tracks.
- Use player controls to:
    - Pause/Resume (`Ctrl + Space`)
    - Stop (`Escape`)
    - Seek, skip, adjust volume
    - Enable shuffle/loop/"end after song"
- Collapse controls for a cleaner view.
- Detach the control window for background playback.
- Assign a **native pause key combination** for global shortcuts.

---

## 🧩 External Tools (Optional)

### Tools Supported:
- `yt-dlp` – For downloading songs from YouTube
- `ffmpeg` – For converting audio and image formats

### Integration Options:
- Link executable manually via top menu:
    - `MangoPlayer -> TOOL NAME -> Set Executable Directory`
- Or let the app resolve it from your system `PATH`.

> You **do not need** these tools to use MangoPlayer.  
They simply **enable additional features**.

---

## 📄 License

MangoPlayer is licensed under the **MIT License**. See [License](LICENSE.txt) for more information

