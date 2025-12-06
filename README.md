# MangoPlayer

MangoPlayer is a user-friendly, feature-rich music playlist manager that enables you to **import, edit, organize, and play music playlists** with ease. With powerful integration features, flexible song/playlist management, and a clean UI, MangoPlayer is built for users who want both simplicity and control.

### Want to stay up-to-date?

I document my future ideas for this project in [this document](./IDEAS.md). Have a look at it if you are interested in possible future features.

---


## Features

- Simple song management
- Playlist creation
- Song metadata customization (title, thumbnail, volume)
- Playlist thumbnail management
- Export songs individually or as a whole playlist
- All default playback features: pause, resume, stop, loop, shuffle, etc.
- Detachable player controls for seamless multitasking
- View statistics: times played, playback time, etc.

### External Tool Integration (Optional)

- Support for every major audio format (songs) and image format (thumbnails)
- Direct audio download from YouTube

---

## Installation

### 1. Download the Application

Get the latest version of **MangoPlayer** from the [Releases page](https://github.com/RedStoneMango/MangoPlayer/releases). Choose the installer that matches your operating system:

- `MangoPlayer_XX.XX.XX_windows_x64` for 64-bit Windows
- `MangoPlayer_XX.XX.XX_mac_arm64` for Apple Silicon (arm64) Macs
- `MangoPlayer_XX.XX.XX_mac_x64` for Intel-based Macs
- `MangoPlayer_XX.XX.XX_linux_x64` for 64-bit Linux
- `MangoPlayer_XX.XX.XX_linux_arm64` for ARM64-based Linux systems

> Please note that the release files may not be up to date with the repository, as the releases only contain the latest stable version of the app. If you are looking for this stable version's code, download the `Source code` file found among the release assets.

---

### 2. Optional: Enable Advanced Features

To unlock additional features like direct song downloads from YouTube or media file conversion, install the following external tools:

#### ffmpeg
1. Open the **MangoPlayer** application.
2. In the menu bar, go to:  
   `MangoPlayer -> Ffmpeg -> Open download page`
3. Follow the instructions on the website to download and install **ffmpeg**.

#### yt-dlp  
1. Open the **MangoPlayer** application.
2. In the menu bar, go to:  
   `MangoPlayer -> Yt-dlp -> Open download page`
3. Follow the instructions on the website to download and install **yt-dlp**.

> ⚠️ Note: **yt-dlp** requires **ffmpeg** to function properly.

#### Having Issues?

If the tools aren't being detected after installation, refer to:  
`Help -> External Tools -> Installing External Tools -> Linking the Executable`  
from the application's menu bar for troubleshooting steps.

---

## License

MangoPlayer is licensed under the **MIT License**. See [License](LICENSE.txt) for more information
