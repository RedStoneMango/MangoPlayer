package io.github.redstonemango.mangoplayer.back.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import io.github.redstonemango.mangoplayer.back.Finals;
import io.github.redstonemango.mangoplayer.back.GlobalMenuBarActions;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MainConfigWrapper {
    private static MainConfigWrapper INSTANCE = null;

    public static final String configFilePath = MangoPlayer.APP_FOLDER_PATH + "/mainConfiguration.json";
    public static boolean loadError = false;

    public WindowData windowData;
    public WindowPosition detachedControlPosition;
    public String ytDlpPath;
    public String ffmpegPath;
    public Set<Integer> nativePauseKeyCombination;
    public boolean isShuffleActive;
    public int loopType;
    public boolean isSongControlExpanded;
    public boolean isMuted;
    public double volume;

    public static final int LOOP_TYPE_NONE = 0;
    public static final int LOOP_TYPE_ALL = 1;
    public static final int LOOP_TYPE_SINGLE = 2;

    public static synchronized MainConfigWrapper loadConfig() {
        if (INSTANCE == null) {
            MainConfigWrapper wrapper = tryReadFile();
            INSTANCE = Objects.requireNonNullElseGet(wrapper, MainConfigWrapper::new);
            INSTANCE.ensureFields();
        }
        return INSTANCE;
    }

    public static synchronized void save() {
        if (loadError) {
            System.out.println("Not saving main config due to an error during its I/O load!"); // Do not save (i.e. possibly overwrite) the config file if loading failed
            return;
        }
        System.out.println("Saving main config...");

        if (MangoPlayer.primaryStage != null) {
            INSTANCE.windowData.x = MangoPlayer.primaryStage.getX();
            INSTANCE.windowData.y = MangoPlayer.primaryStage.getY();
            INSTANCE.windowData.width = MangoPlayer.primaryStage.getWidth();
            INSTANCE.windowData.height = MangoPlayer.primaryStage.getHeight();
        }
        File file = new File(configFilePath);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(INSTANCE) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Unable to save MainConfigWrapper: " + e);
        }
    }

    private static synchronized @Nullable MainConfigWrapper tryReadFile() {
        File file = new File(configFilePath);
        if (!file.exists()) return null;
        try (Scanner scanner = new Scanner(file).useDelimiter("\\Z")) {
            if (!scanner.hasNext()) return null;
            return new Gson().fromJson(scanner.next(), MainConfigWrapper.class);
        } catch (FileNotFoundException e) {
            return null;
        }
        catch (Exception e) {
            loadError = true;
            System.err.println("Error loading main config:");
            e.printStackTrace(System.err);

            ButtonType errorButton = new ButtonType("Submit issue");
            Alert alert = new Alert(Alert.AlertType.ERROR, "", errorButton, ButtonType.CLOSE);
            alert.setTitle("FknPlayer | Config I/O Error");
            alert.setHeaderText("An error occurred while loading the configuration file '" + new File(configFilePath).getName() + "'");
            alert.setContentText("This might be due to a malformed JSON text.\nPlease submit an issue if you are unable to fix this");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.showAndWait();
            if (alert.getResult() == errorButton) {
                GlobalMenuBarActions.onIssuesMenu();
            }
            MangoPlayer.primaryStage.close();
            return null;
        }
    }

    public void ensureFields() {
        if (windowData == null) {
            windowData = new WindowData();
        }

        if (detachedControlPosition == null) {
            detachedControlPosition = new WindowPosition();
        }

        if (ffmpegPath == null || ffmpegPath.isBlank()) {
            ffmpegPath = "ffmpeg";
        }

        if (ytDlpPath == null || ytDlpPath.isBlank()) {
            ytDlpPath = "yt-dlp";
        }

        if (nativePauseKeyCombination == null) {
            nativePauseKeyCombination = new HashSet<>();
        }

        volume = Math.clamp(volume, 0.01, 1.0);

        if (!(loopType == LOOP_TYPE_NONE ||
                loopType == LOOP_TYPE_ALL ||
                loopType == LOOP_TYPE_SINGLE)) {
            System.err.println("Configuration 'loopType' in main config is out of range 0..2. Automatically updating to " + LOOP_TYPE_NONE + " (LOOP_TYPE_NONE)");
            loopType = LOOP_TYPE_NONE;
        }
    }


    public static class WindowPosition {
        public @Nullable Double x = null;
        public @Nullable Double y = null;
    }

    public static class WindowData {
        public double x = 0;
        public double y = 0;
        public double width = 770;
        public double height = 500;
    }
}
