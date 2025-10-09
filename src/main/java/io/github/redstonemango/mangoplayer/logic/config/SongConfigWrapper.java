package io.github.redstonemango.mangoplayer.logic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import io.github.redstonemango.mangoplayer.graphic.MangoPlayer;
import io.github.redstonemango.mangoplayer.logic.Song;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SongConfigWrapper {
    private static SongConfigWrapper INSTANCE = null;

    public static final String configFilePath = MangoPlayer.APP_FOLDER_PATH + "/songs.json";

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
            return new Gson().fromJson(scanner.next(), SongConfigWrapper.class);
        } catch (FileNotFoundException e) {
            return null;
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON syntax in song config file: " + e);
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
