package io.github.redstonemango.mangoplayer.logic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import io.github.redstonemango.mangoplayer.graphic.MangoPlayer;
import io.github.redstonemango.mangoplayer.logic.GlobalMenuBarActions;
import io.github.redstonemango.mangoplayer.logic.Song;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SongConfigWrapper {
    private static SongConfigWrapper INSTANCE = null;

    public static final String configFilePath = MangoPlayer.APP_FOLDER_PATH + "/songs.json";
    public static boolean loadError = false;

    @Expose public LinkedHashMap<String, Song> songs;

    public static synchronized SongConfigWrapper loadConfig() {
        if (INSTANCE == null) {
            SongConfigWrapper wrapper = tryReadFile();
            INSTANCE = Objects.requireNonNullElseGet(wrapper, SongConfigWrapper::new);
            INSTANCE.ensureFields();
        }
        return INSTANCE;
    }

    public static synchronized void save() {
        if (loadError) {
            System.out.println("Not saving song config due to an error during its I/O load!"); // Do not save (i.e. possibly overwrite) the config file if loading failed
            return;
        }
        System.out.println("Saving song config...");

        File file = new File(configFilePath);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(INSTANCE) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Unable to save SongConfigWrapper: " + e);
        }
    }

    private static synchronized @Nullable SongConfigWrapper tryReadFile() {
        File file = new File(configFilePath);
        if (!file.exists()) return null;
        try (Scanner scanner = new Scanner(file).useDelimiter("\\Z")) {
            if (!scanner.hasNext()) return null;
            return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(scanner.next(), SongConfigWrapper.class);
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            loadError = true;
            System.err.println("Error loading song config:");
            e.printStackTrace(System.err);

            ButtonType errorButton = new ButtonType("Submit issue");
            Alert alert = new Alert(Alert.AlertType.ERROR, "", errorButton, ButtonType.CLOSE);
            alert.setTitle("FknPlayer | Config I/O Error");
            alert.setHeaderText("An error occurred while loading the configuration file '" + new File(configFilePath).getName() + "'");
            alert.setContentText("This might be due to a malformed JSON text.\nPlease submit an issue if you are unable to fix this");
            alert.showAndWait();
            if (alert.getResult() == errorButton) {
                GlobalMenuBarActions.onIssuesMenu();
            }
            MangoPlayer.primaryStage.close();
            return null;
        }
    }

    public void ensureFields() {
        if (songs == null) {
            songs = new LinkedHashMap<>();
        }
        songs.forEach((id, song) -> song.ensureFields(id));
    }
}
