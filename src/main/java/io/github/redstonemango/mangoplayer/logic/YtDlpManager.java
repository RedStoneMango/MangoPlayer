package io.github.redstonemango.mangoplayer.logic;

import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import io.github.redstonemango.mangoplayer.logic.config.MainConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class YtDlpManager {

    public static final String YOUTUBE_DLP_DOWNLOAD = "https://github.com/yt-dlp/yt-dlp/wiki/Installation#installing-the-release-binary";
    public static final String FFMPEG_DOWNLOAD = "https://www.ffmpeg.org/download.html";

    private static YtDlpManager INSTANCE;

    private String path;
    private String ffmpegPath;
    @Nullable
    private Process runningProcess;

    private YtDlpManager(String path, String ffmpegPath) {
        this.path = path;
        this.ffmpegPath = ffmpegPath;
        Runtime.getRuntime().addShutdownHook(new Thread(this::destroyRunningProcess));
    }

    public static YtDlpManager getInstance() {

        if (INSTANCE == null) {
            String path = MainConfigWrapper.loadConfig().ytDlpPath;
            String ffmpegPath = MainConfigWrapper.loadConfig().ffmpegPath;

            INSTANCE = new YtDlpManager(path, ffmpegPath);
        }
        return INSTANCE;
    }

    public boolean convertToMp3(File audioFile, File outputFile, Consumer<String> consumer) {
        destroyRunningProcess();

        ProcessBuilder processBuilder = new ProcessBuilder(createConversionCommand(audioFile, outputFile, true));
        processBuilder.redirectErrorStream(true);

        Process process;
        try {
            process = processBuilder.start();
            this.runningProcess = process;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            }

            int code = process.waitFor();

            if (code != 0 && code != 143) {
                showConversionErrorDialog(audioFile.getName());
            }

            return code == 0;

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> Utilities.showErrorScreen("Convert file to .mp3", String.valueOf(e)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> Utilities.showErrorScreen("Convert file to .mp3", String.valueOf(e)));
        } finally {
            destroyRunningProcess();
        }

        return false;
    }
    public boolean convertToPng(File imageFile, File outputFile, Consumer<String> consumer) {
        destroyRunningProcess();

        ProcessBuilder processBuilder = new ProcessBuilder(createConversionCommand(imageFile, outputFile, false));
        processBuilder.redirectErrorStream(true);

        Process process;
        try {
            process = processBuilder.start();
            this.runningProcess = process;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            }

            int code = process.waitFor();

            if (code != 0 && code != 143) {
                showConversionErrorDialog(imageFile.getName());
            }

            return code == 0;

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> Utilities.showErrorScreen("Convert file to .png", String.valueOf(e)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> Utilities.showErrorScreen("Convert file to .png", String.valueOf(e)));
        } finally {
            destroyRunningProcess();
        }

        return false;
    }

    public boolean downloadSong(String url, @NotNull File path, Consumer<String> consumer) {
        destroyRunningProcess();
        ProcessBuilder processBuilder = new ProcessBuilder(createDownloadCommand(url, path, false));
        processBuilder.redirectErrorStream(true);
        Process process;
        try {
            process = processBuilder.start();
            this.runningProcess = process;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            }

            int code = process.waitFor();

            if (code != 0 && code != 143) {
                showDownloadErrorDialog(true, url);
            }

            return code == 0;

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> Utilities.showErrorScreen("Download song", String.valueOf(e)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> Utilities.showErrorScreen("Download song", String.valueOf(e)));
        } finally {
            destroyRunningProcess();
        }
        return false;
    }


    /**
     * Checks whether a connection with 'youtube.com' can be established.<br>
     * If the connection is not possible, an {@linkplain Utilities#showErrorScreen(String, String, boolean) error screen} will be shown.
     * @return Whether the app can connect to YouTube
     */
    public static boolean ensureConnection() {
        try (Socket sock = new Socket()) {
            InetSocketAddress address = new InetSocketAddress("youtube.com", 80);
            sock.connect(address, 3000);
            return true;
        } catch (IOException e) {
            Platform.runLater(() -> Utilities.showErrorScreen("Establish YouTube connection", "Error connecting to YouTube.\nPlease check your WIFI status"));
            return false;
        }
    }

    public List<String> createDownloadCommand(String url, @NotNull File path, boolean thumbnailOnly) {
        List<String> cmd;
        if (thumbnailOnly) {
            cmd = new ArrayList<>(List.of(
                    this.path,
                    "-o",
                    path.getAbsolutePath(),
                    "--write-thumbnail",
                    "--skip-download",
                    "--convert-thumbnails",
                    "png",
                    url
            ));
        }
        else {
            cmd = new ArrayList<>(List.of(
                    this.path,
                    "-x",
                    "--audio-format",
                    "mp3",
                    "--write-thumbnail",
                    "--convert-thumbnails",
                    "png",
                    "-o",
                    path.getAbsolutePath(),
                    url
            ));
        }
        if (!this.ffmpegPath.equals("ffmpeg")) {
            cmd.add(1, "--ffmpeg-location");
            cmd.add(2, this.ffmpegPath);
        }
        return cmd;
    }

    public List<String> createConversionCommand(File sourceFile, File outputFile, boolean audio) {
        return audio ? List.of(
                this.ffmpegPath,
                "-i", sourceFile.getAbsolutePath(),
                "-codec:a", "libmp3lame",
                "-qscale:a", "2", // Quality scale: lower is better (range 0–9)
                outputFile.getAbsolutePath()
        ) : List.of(
                this.ffmpegPath,
                "-i", sourceFile.getAbsolutePath(),
                "-frames:v", "1",
                outputFile.getAbsolutePath()
        );
    }

    public boolean downloadThumbnail(String url, @NotNull File path, Consumer<String> consumer) {
        destroyRunningProcess();
        ProcessBuilder processBuilder = new ProcessBuilder(createDownloadCommand(url, path, true));
        processBuilder.redirectErrorStream(true);
        Process process;
        try {
            process = processBuilder.start();
            this.runningProcess = process;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            }

            int code = process.waitFor();

            if (code != 0 && code != 143) {
                showDownloadErrorDialog(true, url);
            }

            return code == 0;

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> Utilities.showErrorScreen("Download thumbnail", String.valueOf(e)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> Utilities.showErrorScreen("Download thumbnail", String.valueOf(e)));
        } finally {
            destroyRunningProcess();
        }

        return false;
    }

    public void searchYoutube(String search, Consumer<SearchResult> consumer) {
        destroyRunningProcess();
        SearchResult[] searchResults = new SearchResult[]{new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET"), new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET")};

        ProcessBuilder processBuilder = new ProcessBuilder(
                this.path,
                "ytsearch10:" + search,
                "--get-title",
                "--get-id");
        if (!this.ffmpegPath.equals("ffmpeg")) {
            List<String> cmd = processBuilder.command();
            cmd.add(1, "--ffmpeg-location");
            cmd.add(2, this.ffmpegPath);
            processBuilder.command(cmd);
        }

        processBuilder.redirectErrorStream(true);
        try {

            Process process = processBuilder.start();

            this.runningProcess = process;

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int index = 0;
            String line;
            boolean url = false;
            while ((line = reader.readLine()) != null) {
                if (url) {
                    searchResults[index].url = String.format("https://www.youtube.com/watch?v=%s", line);
                    consumer.accept(searchResults[index]);
                    index ++;
                }
                else {
                    searchResults[index].name = line;
                }
                url = !url;

            }

            int code = process.waitFor();
            if (code != 0 && code != 143) {
                Platform.runLater(() -> Utilities.showErrorScreen("Search youtube", "Unexpected not-zero exit code received"));
            }
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Platform.runLater(() -> Utilities.showErrorScreen("Search youtube", String.valueOf(e)));
        }
    }

    public SearchResult loadUrl(String url) {
        destroyRunningProcess();
        SearchResult searchResult = new SearchResult("Unknown", "INVALID URL TO INDICATE THAT THERE HASN'T BEEN AN ANSWER YET");

        ProcessBuilder processBuilder = new ProcessBuilder(
                this.path,
                "--get-title",
                url);
        if (!this.ffmpegPath.equals("ffmpeg")) {
            List<String> cmd = processBuilder.command();
            cmd.add(1, "--ffmpeg-location");
            cmd.add(2, this.ffmpegPath);
            processBuilder.command(cmd);
        }

        processBuilder.redirectErrorStream(true);
        try {

            Process process = processBuilder.start();

            this.runningProcess = process;

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                    searchResult = new SearchResult(line, url);
            }

            int code = process.waitFor();
            if (code != 0 && code != 143) {
                Platform.runLater(() -> Utilities.showErrorScreen("Load url", "Unexpected, not-zero exit code received"));
            }
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Platform.runLater(() -> Utilities.showErrorScreen("Load url", e.getMessage()));
        }
        return searchResult;
    }

    public boolean performSelfUpdate(Consumer<String> onLine) {
        destroyRunningProcess();

        ProcessBuilder processBuilder = new ProcessBuilder(createSelfUpdateCommand());

        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();

            this.runningProcess = process;

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                onLine.accept(line);
            }

            int code = process.waitFor();
            if (code != 0 && code != 143) {
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Platform.runLater(() -> Utilities.showErrorScreen("Update yt-dlp", e.getMessage()));
            return false;
        }
        return true;
    }

    public String[] createSelfUpdateCommand() {
        String ytdlp = Utilities.findExecutableInPath(path);
        if (ytdlp == null) ytdlp = path;

        return OperatingSystem.loadCurrentOS().createProcessElevationCommand(new String[]{ytdlp, "--update"}, new String[]{});
    }

    public synchronized void destroyRunningProcess() {
        destroyRunningProcess(false);
    }

    public synchronized void destroyRunningProcess(boolean forceKill) {
        System.out.println(runningProcess);
        if (runningProcess != null && runningProcess.isAlive()) {
            System.out.println((forceKill ? "Force-d" : "D") + "estroying the running external process (" +
                    runningProcess.info().commandLine().
                            map(s -> "'" + s + "'").
                            orElse("Unknown command")
                    + ")...");

            if (forceKill) runningProcess.destroyForcibly();
            else runningProcess.destroy();
            System.out.println("Done " + (forceKill ? "force-" : "") + "destroying the running external process!");
        }
    }

    private void showDownloadErrorDialog(boolean exception, String asset) {
        if (exception) System.err.println("An error occurred while downloading asset '" + asset + "'!");
        int choice = JOptionPane.showOptionDialog(null, "An error occurred while downloading asset '" + asset + "'.\n\n This may have been caused by a lack of the ffmpeg library or a canceled execution of the process.\nIf this is not the case, please report this to the developer", "Error while downloading asset", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Ffmpeg download", "Back", "Report Bug"}, exception ? "Ffmpeg download" : "Back");
        if (choice == 0) {
            OperatingSystem.loadCurrentOS().open(FFMPEG_DOWNLOAD);
            showDownloadErrorDialog(false, asset);
        }
        else if (choice == 2) {
            GlobalMenuBarActions.onIssuesMenu();
        }
    }
    private void showConversionErrorDialog(String asset) {
        System.err.println("An error occurred during conversion of '" + asset + "'!");
        int choice = JOptionPane.showOptionDialog(null, "An error occurred during conversion of '" + asset + "'.\n\n This may have been caused by a canceled execution of the process.\nIf this is not the case, please report this to the developer", "Error while conversion of asset", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Ignore", "Report Bug"}, "Ignore");
        if (choice == 1) {
            GlobalMenuBarActions.onIssuesMenu();
        }
    }



    public boolean isAvailable() {
        try {
            ProcessBuilder builder = new ProcessBuilder(path, "--version");
            Process process = builder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                return false;
            }
        }
        catch (IOException | InterruptedException e) {
            return false;
        }
        return true;
    }
    public boolean isFfmpegAvailable() {
        try {
            ProcessBuilder builder = new ProcessBuilder(ffmpegPath, "-version");
            Process process = builder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                return false;
            }
        }
        catch (IOException | InterruptedException e) {
            return false;
        }
        return true;
    }

    public boolean checkAvailable() {
        if (!isAvailable()) {
            return showNotAvailableDialog(true);
        }
        return true;
    }
    public boolean checkFfmpegAvailable() {
        if (!isFfmpegAvailable()) {
            return showFfmpegNotAvailableDialog(true);
        }
        return true;
    }


    public void showNotAvailableDialog() {
        showNotAvailableDialog(false);
    }
    private boolean showNotAvailableDialog(boolean firstCall) {
        int choice = JOptionPane.showOptionDialog(null, "Yt-dlp could not be run\n\nIf you believe that you have yt-dlp installed, try defining the absolute binary path\n (Run \"" + (OperatingSystem.isWindows() ? "where yt-dlp" : "which yt-dlp") + "\" in the terminal to get the save directory) or letting this application automatically resolve yt-dlp from the PATH.\nIf you've tried both and neither works, make sure the executeable works from the terminal and report this as an error", "Error starting yt-dlp", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Open download page", "Define absolute path", "Automatically resolve from PATH", "Close"}, firstCall ? "Open download page" : "Close");
        if (choice == 0) {
            OperatingSystem.loadCurrentOS().open(YOUTUBE_DLP_DOWNLOAD);
            return showNotAvailableDialog(false);
        }
        else if (choice == 1) {
            return showRedefineDialog();
        }
        else if (choice == 2) {
            return useDefaultExecuteable();
        }
        return false;
    }
    public boolean showRedefineDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(path).getParentFile());
        chooser.setTitle("Select \"yt-dlp\"-" + (OperatingSystem.isWindows() ? "executable" : "binary") + " file");
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            path = file.getAbsolutePath();
            MainConfigWrapper.loadConfig().ytDlpPath = file.getAbsolutePath();
        }
        else {
            return false;
        }

        return checkAvailable();
    }
    public void showFfmpegNotAvailableDialog() {
        showFfmpegNotAvailableDialog(false);
    }
    public boolean showFfmpegNotAvailableDialog(boolean firstCall) {
        int choice = JOptionPane.showOptionDialog(null, "Ffmpeg could not be run\n\nIf you believe that you have ffmpeg installed, try defining the absolute binary path\n (Run \"" + (OperatingSystem.isWindows() ? "where ffmpeg" : "which ffmpeg") + "\" in the terminal to get the save directory) or letting this application automatically resolve ffmpeg from the PATH.\nIf you've tried both and neither works, make sure the executeable works from the terminal and report this as an error", "Error starting ffmpeg", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Open download page", "Define absolute path", "Automatically resolve from PATH", "Close"}, firstCall ? "Open download page" : "Close");
        if (choice == 0) {
            OperatingSystem.loadCurrentOS().open(FFMPEG_DOWNLOAD);
            return showFfmpegNotAvailableDialog(false);
        }
        else if (choice == 1) {
            return showRedefineFfmpegDialog();
        }
        else if (choice == 2) {
            return useDefaultFfmpegExecuteable();
        }
        return false;
    }
    public boolean showRedefineFfmpegDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(path).getParentFile());
        chooser.setTitle("Select \"ffmpeg\"-" + (OperatingSystem.isWindows() ? "executable" : "binary") + " file");
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            ffmpegPath = file.getAbsolutePath();
            MainConfigWrapper.loadConfig().ffmpegPath = file.getAbsolutePath();
        }
        else {
            return false;
        }
        return checkFfmpegAvailable();
    }
    public boolean useDefaultExecuteable() {
        path = "yt-dlp";
        MainConfigWrapper.loadConfig().ytDlpPath = "yt-dlp";
        return checkAvailable();
    }
    public boolean useDefaultFfmpegExecuteable() {
        ffmpegPath = "ffmpeg";
        MainConfigWrapper.loadConfig().ffmpegPath = "ffmpeg";
        return checkFfmpegAvailable();
    }


    public static class SearchResult {
        private String name;
        private String url;
        public SearchResult(String name, String url) {
            this.url = url;
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public String getUrl() {
            return url;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setUrl(String url) {
            this.url = url;
        }
    }
}
