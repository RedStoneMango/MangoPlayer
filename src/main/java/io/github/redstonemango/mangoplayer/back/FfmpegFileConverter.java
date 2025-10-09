package io.github.redstonemango.mangoplayer.back;

import io.github.redstonemango.mangoutils.MangoIO;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.IProcessExecuteable;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class FfmpegFileConverter implements IProcessExecuteable {

    private static final FfmpegFileConverter INSTANCE = new FfmpegFileConverter();

    private final Map<File, CompletableFuture<Void>> audioConversionFutures = new ConcurrentHashMap<>();
    private final Map<File, CompletableFuture<Void>> imageConversionFutures = new ConcurrentHashMap<>();

    private final File audioTempDir = new File(MangoPlayer.APP_FOLDER_PATH + "/filesToMp3ConversionTmp/");
    private final File imageTempDir = new File(MangoPlayer.APP_FOLDER_PATH + "/filesToPngConversionTmp/");

    private FfmpegFileConverter() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cleanupFilesToMp3Conversion();
            cleanupFilesToPngConversion();
        }));
    }

    public static FfmpegFileConverter getInstance() {
        return INSTANCE;
    }

    private boolean promptSkipConversion(File file, boolean hasFfmpeg, String typeLabel) {
        ButtonType skipButton = new ButtonType("Skip this " + typeLabel);
        if (!hasFfmpeg) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "", skipButton);
            alert.setTitle("Unsupported file type");
            alert.setHeaderText(file.getName() + " is not a supported file type");
            alert.setContentText("The file will be skipped as no ffmpeg implementation is available for converting");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.showAndWait();
            return true;
        }
        ButtonType convertButton = new ButtonType("Convert using ffmpeg");
        Alert alert = new Alert(Alert.AlertType.WARNING, "", skipButton, convertButton);
        alert.setTitle("Unsupported file type");
        alert.setHeaderText(file.getName() + " is not a supported file type");
        alert.setContentText("You can convert the file to a supported type using ffmpeg");
        alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
        alert.showAndWait();
        return alert.getResult() != convertButton;
    }

    public CompletableFuture<List<File>> convertFilesToMp3(List<File> files) {
        List<File> result = Collections.synchronizedList(new ArrayList<>());
        audioTempDir.mkdirs();
        audioTempDir.deleteOnExit();

        List<CompletableFuture<Void>> allFutures = new ArrayList<>();
        boolean hasFfmpeg = YtDlpManager.getInstance().isFfmpegAvailable();

        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                result.add(file);
                continue;
            }

            if (promptSkipConversion(file, hasFfmpeg, "song")) continue;

            String name = file.getName();
            int dotIndex = name.lastIndexOf('.');
            String baseName = (dotIndex > 0) ? name.substring(0, dotIndex) : name;
            File outputFile = new File(audioTempDir, baseName + ".mp3");

            CompletableFuture<Void> future = new CompletableFuture<>();
            audioConversionFutures.put(file, future);

            startAudioFileToMp3ConversionProcess(file, outputFile);

            CompletableFuture<Void> handleFuture = future.handle((_, ex) -> {
                audioConversionFutures.remove(file);
                if (ex == null) {
                    result.add(outputFile);
                } else {
                    ex.printStackTrace();
                }
                return null;
            });

            allFutures.add(handleFuture);
        }

        return CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]))
                .thenApply(_ -> result);
    }
    public void cleanupFilesToMp3Conversion() {
        if (audioTempDir.exists()) {
            try {
                System.out.println("Cleaning up temporary audio conversion directory");
                MangoIO.deleteDirectoryRecursively(audioTempDir);
            } catch (IOException e) {
                System.err.println("Error cleaning up temporary audio conversion directory: " + e);
            }
        }
    }

    public CompletableFuture<List<File>> convertFilesToPng(List<File> files) {
        List<File> result = Collections.synchronizedList(new ArrayList<>());
        imageTempDir.mkdirs();
        imageTempDir.deleteOnExit();

        List<CompletableFuture<Void>> allFutures = new ArrayList<>();
        boolean hasFfmpeg = YtDlpManager.getInstance().isFfmpegAvailable();

        for (File file : files) {
            if (file.getName().endsWith(".png")) {
                result.add(file);
                continue;
            }

            if (promptSkipConversion(file, hasFfmpeg, "image")) continue;

            String name = file.getName();
            int dotIndex = name.lastIndexOf('.');
            String baseName = (dotIndex > 0) ? name.substring(0, dotIndex) : name;
            File outputFile = new File(imageTempDir, baseName + ".png");

            CompletableFuture<Void> future = new CompletableFuture<>();
            imageConversionFutures.put(file, future);

            startImageFileToPngConversionProcess(file, outputFile);

            CompletableFuture<Void> handleFuture = future.handle((_, ex) -> {
                imageConversionFutures.remove(file);
                if (ex == null) {
                    result.add(outputFile);
                } else {
                    ex.printStackTrace();
                }
                return null;
            });

            allFutures.add(handleFuture);
        }

        return CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]))
                .thenApply(_ -> result);
    }
    public void cleanupFilesToPngConversion() {
        if (imageTempDir.exists()) {
            try {
                System.out.println("Cleaning up temporary image conversion directory");
                MangoIO.deleteDirectoryRecursively(imageTempDir);
            } catch (IOException e) {
                System.err.println("Error cleaning up temporary image conversion directory: " + e);
            }
        }
    }

    @Override
    public void processFinished(Object source, @Nullable Object additionalData, boolean success, int processType) {
        if (processType == IProcessExecuteable.AUDIO_CONVERSION_PROCESS_TYPE) {
            File file = (File) source;
            CompletableFuture<Void> future = audioConversionFutures.get(file);
            if (future != null) {
                if (success) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new RuntimeException("Conversion failed for file: " + file.getName()));
                }
            }
        }
        else if (processType == IProcessExecuteable.IMAGE_CONVERSION_PROCESS_TYPE) {
            File file = (File) source;
            CompletableFuture<Void> future = imageConversionFutures.get(file);
            if (future != null) {
                if (success) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new RuntimeException("Conversion failed for file: " + file.getName()));
                }
            }
        }
    }
}
