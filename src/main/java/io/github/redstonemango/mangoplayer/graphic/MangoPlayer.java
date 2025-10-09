package io.github.redstonemango.mangoplayer.graphic;

import io.github.redstonemango.mangoutils.LogManager;
import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.logic.Utilities;
import io.github.redstonemango.mangoplayer.logic.config.MainConfigWrapper;
import io.github.redstonemango.mangoplayer.logic.config.PlaylistConfigWrapper;
import io.github.redstonemango.mangoplayer.logic.config.SongConfigWrapper;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class MangoPlayer extends Application {

    public static Stage primaryStage = null;
    public static final String APP_FOLDER_PATH = OperatingSystem.loadCurrentOS().createAppConfigDir("MangoPlayer").getAbsolutePath();
    private static MangoPlayer APPLICATION;
    private static final Duration saveInterval = Duration.minutes(5);

    public static MangoPlayer getApplication() {
        return APPLICATION;
    }

    @Override
    public void start(Stage stage) throws IOException {
        APPLICATION = this;
        MainConfigWrapper.WindowData windowPosition = MainConfigWrapper.loadConfig().windowData;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/redstonemango/mangoplayer/fxml/playlistOverview/playlistOverview.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage = stage;
        stage.setTitle("MangoPlayer");
        stage.setScene(scene); // Initialization already up here (before initializing position and size) to prevent window from clipping out of screen
        stage.setX(windowPosition.x);
        stage.setY(windowPosition.y);
        stage.setWidth(windowPosition.width);
        stage.setHeight(windowPosition.height);
        Utilities.prepareAndShowStage(stage, scene, loader, true);

        ScheduledService<Void> saveService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        save(true);
                        return null;
                    }
                };
            }
        };
        saveService.setPeriod(saveInterval);
        saveService.setDelay(saveInterval); // Ensure that the first call is delayed by the final interval, so ScheduledService#start() does not immediately trigger this::save()
        saveService.start();
    }

    private static void setupLogManagement() {
        LogManager.logDir(Paths.get(APP_FOLDER_PATH, "logs"));
        LogManager.logFileHeaderFunction(date -> """
                This is a log file for the MangoPlayer application.
                Inside this log file, all outputs the app gave at $DATE$ are logged.
                This file will compress itself to a .gz archive in one day. 7 Days after compression, the archive will delete itself to save storage
                """.replace("$DATE$", DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date)));
        LogManager.logFileNameFunction(date -> "mangoPlayer_" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date));
        LogManager.shutdownDelay(20);
        LogManager.start();
        System.out.println("Initialized log manager using the default configurations");
    }

    @Override
    public void stop() {
        save(false);
        System.exit(0);
    }

    public void save(boolean showNotification) {
        System.out.println("Beginning to save application data (" + (showNotification ? "" : "not ") + "notifying the user)...");
        if (showNotification) {
            Platform.runLater(() -> {
                String oldName = primaryStage.getTitle();
                AtomicInteger cycles = new AtomicInteger(0);
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(0.2), _ -> {
                            if (!primaryStage.getTitle().replace(".", "").equals(oldName.replace(".", ""))) {
                                return; // If the original title was changed during the saving animation was played, cancel it
                            }
                            if (cycles.incrementAndGet() >= 5) {
                                primaryStage.setTitle(oldName);
                            }
                            else {
                                primaryStage.setTitle(primaryStage.getTitle() + ".");
                            }
                        })
                );
                primaryStage.setTitle(primaryStage.getTitle() + ".");
                timeline.setCycleCount(5);
                timeline.play();
            });
        }
        MainConfigWrapper.save();
        PlaylistConfigWrapper.save();
        SongConfigWrapper.save();
        System.out.println("Done saving application data!");
    }

    public static void main(String[] args) {
        setupLogManagement();
        launch();
    }
}