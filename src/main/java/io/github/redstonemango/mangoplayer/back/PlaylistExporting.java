package io.github.redstonemango.mangoplayer.back;

import io.github.redstonemango.mangoplayer.front.controller.waitScreen.WaitScreenScene;
import io.github.redstonemango.mangoutils.MangoIO;
import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaylistExporting {

    public static void export(Playlist playlist) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("MangoPlayer | Export playlist");
        chooser.setInitialFileName(Utilities.formatAsFriendlyText(playlist.getName()) + ".zip");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zip compressed archive", "*.zip"));
        File file = chooser.showSaveDialog(MangoPlayer.primaryStage);
        if (file != null) {
            if (!file.getName().endsWith(".zip")) {
                file = new File(file.getAbsolutePath() + ".zip");
            }
            resumeExport(playlist, file);
        }
    }

    private static void resumeExport(Playlist playlist, File targetFile, boolean m3u8, boolean wpl) {
        String targetFileName = targetFile.getName().substring(0, targetFile.getName().lastIndexOf("."));
        File tempFolder = new File(MangoPlayer.APP_FOLDER_PATH + "/exportTemp/" + targetFileName);
        tempFolder.mkdirs();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cleaning up temporary playlist exporting directory...");
            if (tempFolder.exists()) {
                try {
                    MangoIO.deleteDirectoryRecursively(tempFolder.getParentFile());
                } catch (IOException e) {
                    System.err.println("Error deleting temporary exporting directory: " + e);
                }
            }
            System.out.println("Done cleaning up temporary playlist exporting directory!");
        }, "delete temp files thread"));
        AtomicInteger latestSongNumberLength = new AtomicInteger(String.valueOf(playlist.getSongs().size()).length());
        if (latestSongNumberLength.get() == 1) {
            latestSongNumberLength.set(2);
        }
        if (targetFile.exists()) {
            targetFile.delete();
        }

        System.out.println("Exporting playlist '" + playlist.getName()
                + "' (ID is '" + playlist.getId() + "') to file '" + targetFile.getAbsolutePath()
                + "' using the temporary directory '" + tempFolder.getAbsolutePath() + "':");

        Stage stage = new Stage();
        stage.setTitle("MangoPlayer | Export playlist (running)");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(MangoPlayer.primaryStage);
        stage.setOnCloseRequest(Event::consume);
        WaitScreenScene scene = WaitScreenScene.createNewScene("Exporting playlist...",
                "The playlist is currently being exported. It will be done any minute.");
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());

        // Async export
        {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(() -> {
                playlist.getSongs().forEach(song -> {
                    StringBuilder songNumber = new StringBuilder(String.valueOf(playlist.getSongs().indexOf(song) + 1));
                    while (songNumber.length() < latestSongNumberLength.get()) {
                        songNumber.insert(0, "0");
                    }

                    String newFilePath = tempFolder.getAbsolutePath();
                    newFilePath = newFilePath + "/Track_" + songNumber + "--" + Utilities.formatAsFriendlyText(song.getName()) + ".mp3";
                    song.exportToFile(new File(newFilePath), playlist.getName());
                });

                boolean success = true;
                try {
                    MangoIO.compressFile(tempFolder, targetFile);
                } catch (IOException e) {
                    success = false;
                    Utilities.showErrorScreen("Compress playlist", String.valueOf(e), false);
                }
                try {
                    MangoIO.deleteDirectoryRecursively(tempFolder);
                } catch (IOException e) {
                    Utilities.showErrorScreen("Delete temporary export directory", String.valueOf(e), false);
                }

                if (success) {
                    System.out.println("Export of playlist '" + playlist.getName()
                            + "' (ID is '" + playlist.getId() + "') to file '" + targetFile.getAbsolutePath() + "' finished");

                    Platform.runLater(() -> {
                        ButtonType browseButton = new ButtonType("Browse file", ButtonBar.ButtonData.YES);
                        ButtonType continueButton = new ButtonType("Stay in application", ButtonBar.ButtonData.NO);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", browseButton, continueButton);
                        alert.setTitle("Export finished");
                        alert.setHeaderText("Finished exporting \"" + playlist.getName() + "\" to \"" + targetFile.getName() + "\"");
                        alert.setContentText("Do you want to browse the file?");
                        alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
                        alert.showAndWait();
                        if (alert.getResult() == browseButton) {
                            OperatingSystem.loadCurrentOS().browse(targetFile);
                        }
                    });
                }
                Platform.runLater(stage::close);
            });
        }
    }

}
