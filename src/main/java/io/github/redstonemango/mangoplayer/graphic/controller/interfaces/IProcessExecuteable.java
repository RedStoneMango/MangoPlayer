package io.github.redstonemango.mangoplayer.graphic.controller.interfaces;

import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.stage.Stage;
import io.github.redstonemango.mangoplayer.graphic.MangoPlayer;
import io.github.redstonemango.mangoplayer.graphic.controller.processExecution.ProcessExecutionScene;
import io.github.redstonemango.mangoplayer.logic.Song;
import io.github.redstonemango.mangoplayer.logic.Utilities;
import io.github.redstonemango.mangoplayer.logic.YtDlpManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface IProcessExecuteable {
    int AUDIO_DOWNLOAD_PROCESS_TYPE = 1;
    int THUMBNAIL_DOWNLOAD_PROCESS_TYPE = 2;
    int AUDIO_CONVERSION_PROCESS_TYPE = 3;
    int IMAGE_CONVERSION_PROCESS_TYPE = 4;
    void processFinished(Object source, @Nullable Object additionalData, boolean success, int processType);

    default void startThumbnailDownloadProcess(Song song) {
        if (YtDlpManager.ensureConnection()) {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(MangoPlayer.primaryStage);
            stage.setTitle("MangoPlayer | Process information");
            ProcessExecutionScene scene = ProcessExecutionScene.createNewScene("Download thumbnail", Utilities.cmdStringFromList(YtDlpManager.getInstance().createDownloadCommand(Utilities.youtubeUrlFromSong(song), new File(Utilities.thumbnailPathFromSong(song, false)), true)));
            Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
            new Thread(() -> {
                boolean bl = YtDlpManager.getInstance().downloadThumbnail(Utilities.youtubeUrlFromSong(song), new File(Utilities.thumbnailPathFromSong(song, false)), scene::sendStatusUpdate);
                Platform.runLater(() -> {
                    scene.sendEndInformation();
                    processFinished(song, new File(Utilities.thumbnailPathFromSong(song)), bl, THUMBNAIL_DOWNLOAD_PROCESS_TYPE);
                });
            }).start();
        }
    }
    default void startAudioDownloadProcess(Song song) {
        if (YtDlpManager.ensureConnection()) {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(MangoPlayer.primaryStage);
            stage.setTitle("MangoPlayer | Process information");
            ProcessExecutionScene scene = ProcessExecutionScene.createNewScene("Download song", Utilities.cmdStringFromList(YtDlpManager.getInstance().createDownloadCommand(Utilities.youtubeUrlFromSong(song), new File(Utilities.dummyPathFromSong(song)), false)));
            Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
            new Thread(() -> {
                boolean bl = YtDlpManager.getInstance().downloadSong(Utilities.youtubeUrlFromSong(song), new File(Utilities.dummyPathFromSong(song)), scene::sendStatusUpdate);
                Platform.runLater(() -> {
                    scene.sendEndInformation();
                    processFinished(song, null, bl, AUDIO_DOWNLOAD_PROCESS_TYPE);
                });
            }).start();
        }
    }
    default void startAudioFileToMp3ConversionProcess(File audioFile, File outputFile) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(MangoPlayer.primaryStage);
        stage.setTitle("MangoPlayer | Process information");
        ProcessExecutionScene scene = ProcessExecutionScene.createNewScene("Convert audio file to .mp3", Utilities.cmdStringFromList(YtDlpManager.getInstance().createConversionCommand(audioFile, outputFile, true)));
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
        new Thread(() -> {
            boolean bl = YtDlpManager.getInstance().convertToMp3(audioFile, outputFile, scene::sendStatusUpdate);
            Platform.runLater(() -> {
                scene.sendEndInformation();
                processFinished(audioFile, outputFile, bl, AUDIO_CONVERSION_PROCESS_TYPE);
            });
        }).start();
    }
    default void startImageFileToPngConversionProcess(File imageFile, File outputFile) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(MangoPlayer.primaryStage);
        stage.setTitle("MangoPlayer | Process information");
        ProcessExecutionScene scene = ProcessExecutionScene.createNewScene("Convert image file to .png", Utilities.cmdStringFromList(YtDlpManager.getInstance().createConversionCommand(imageFile, outputFile, false)));
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
        new Thread(() -> {
            boolean bl = YtDlpManager.getInstance().convertToPng(imageFile, outputFile, scene::sendStatusUpdate);
            Platform.runLater(() -> {
                scene.sendEndInformation();
                processFinished(imageFile, outputFile, bl, IMAGE_CONVERSION_PROCESS_TYPE);
            });
        }).start();
    }
}
