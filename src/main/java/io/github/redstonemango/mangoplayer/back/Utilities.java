package io.github.redstonemango.mangoplayer.back;

import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.back.config.PlaylistConfigWrapper;
import io.github.redstonemango.mangoplayer.back.config.SongConfigWrapper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    private static final Pattern YOUTUBE_VIDEO_PATTERN = Pattern.compile(
            "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?(?:.*&)?v=|embed/|shorts/)|youtu\\.be/)([A-Za-z0-9_-]{11})(?:[&?#].*)?$"
    );

    private static final Map<Object, List<ListenerData<?>>> propertyListenerRegister = new HashMap<>();

    public static <T> void applyHeldPropertyListener(Object holder, ObservableValue<T> property, ChangeListener<T> action) {
        List<ListenerData<?>> existingListeners = propertyListenerRegister
                .computeIfAbsent(holder, _ -> new ArrayList<>());

        property.addListener(action);
        existingListeners.add(new ListenerData<>(property, action));
    }
    public static void removeHeldListeners(Object holder) {
        List<ListenerData<?>> listeners = propertyListenerRegister.remove(holder);
        if (listeners != null) {
            for (ListenerData<?> data : listeners) {
                data.removeListener();
            }
        }
    }
    private record ListenerData<T>(ObservableValue<T> observable, ChangeListener<T> listener) {
        public void removeListener() {
            observable.removeListener(listener);
        }
    }

    public static boolean isValidYoutubeLink(String string) {
        return YOUTUBE_VIDEO_PATTERN.matcher(string).matches();
    }

    /**
     * Applies a custom cell factory to a ListView that displays a Node per cell,
     * generated based on the item value.
     *
     * @param listView      The ListView to apply the cell factory to.
     * @param nodeFunction  A function that takes an item of type T and returns a Node to display.
     * @param <T>           The type of items in the ListView.
     * @apiNote             Originally part of another utility class of mine. This method has been copied into the player's utility class for consistency.
     * @author              Fabian Krohn
     */
    public static <T> void applyListViewCellFactory(ListView<T> listView, Function<T, Node> nodeFunction) {
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> lv) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(nodeFunction.apply(item));
                            setPadding(new Insets(0));
                        }
                    }
                };
            }
        });
    }

    public static <T> void applyComboBoxCellFactory(ComboBox<T> comboBox, Function<T, Node> nodeFunction) {
        comboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> lv) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(nodeFunction.apply(item));
                            setText(null);
                        }
                    }
                };
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
            }
        });
    }

    public static Image scaleToSmallBounds(Image origin, int val) {
        double originalWidth = origin.getWidth();
        double originalHeight = origin.getHeight();
        // If already within bounds, return the original
        if (originalWidth <= val && originalHeight <= val) {
            return origin;
        }
        // Calculate the scale factor to keep the aspect ratio
        double scaleFactor = Math.min(val / originalWidth, val / originalHeight);
        double newWidth = originalWidth * scaleFactor;
        double newHeight = originalHeight * scaleFactor;
        // Use JavaFX's built-in method to resize while keeping the aspect ratio
        return new Image(origin.getUrl(), newWidth, newHeight, true, true);
    }

    public static @Nullable String youtubeIdFromLink(String link) {
        if (link == null || link.isEmpty()) {
            return null;
        }
        Matcher matcher = YOUTUBE_VIDEO_PATTERN.matcher(link);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void showErrorScreen(String processName, String text) {
        showErrorScreen(processName, text, true);
    }

    public static void showErrorScreen(String processName, String text, boolean wait) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("MangoPlayer | Error");
            alert.setHeaderText("An error occurred during process '" + processName + "'");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.setContentText(text);
            if (wait) {
                alert.showAndWait();
            }
            else {
                alert.show();
            }
            System.err.println("An error occurred during process '" + processName + "': " + text);
        });
    }

    public static void showInformationScreen(String processName, String text) {
        showInformationScreen(processName, text, true);
    }

    public static void showInformationScreen(String processName, String text, boolean wait) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("MangoPlayer | Info");
            alert.setHeaderText("Information about process '" + processName + "'");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.setContentText(text);
            if (wait) {
                alert.showAndWait();
            }
            else {
                alert.show();
            }
        });
    }

    public static <T> T getRandomElementExcluding(List<T> list, T exclusion) {
        Random rand = new Random();
        T result = null;
        int count = 0;

        for (T item : list) {
            if (!item.equals(exclusion)) {
                count++;
                if (rand.nextInt(count) == 0) {
                    result = item;
                }
            }
        }

        return count == 0 ? null : result;
    }

    public static String formatDuration(Duration duration) {
        StringBuilder builder = new StringBuilder();

        int hours = Math.max((int) duration.toHours(), 0);
        int minutes = (int) duration.toMinutes() - (hours * 60);
        int seconds = (int) duration.toSeconds() - (hours * 3600) - (minutes * 60);

        if (hours > 0) builder.append(hours).append(":");

        if (minutes >= 10) builder.append(minutes).append(":");
        else builder.append("0").append(minutes).append(":");

        if (seconds >= 10) builder.append(seconds);
        else builder.append("0").append(seconds);

        return builder.toString();
    }

    public static void prepareAndShowStage(Stage stage, Scene scene, FXMLLoader loader) {
        prepareAndShowStage(stage, scene, loader, false);
    }

    public static void prepareAndShowStage(Stage stage, Scene scene, FXMLLoader loader, boolean isInit) {
        Platform.runLater(() -> {
            double oldWidth = stage.getWidth();
            double oldHeight = stage.getHeight();
            double oldX = stage.getX(); // This value was not needed in any tests I made, but because "oldY" exists to prevent JFX from not accounting the title bar while computations, "oldX" was added for completeness
            double oldY = stage.getY();
            scene.getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            if (!isInit) stage.setScene(scene);
            stage.show();
            if (loader.getController() instanceof IInitializable controller) {
                controller.init();
            }
            if (!isInit && !Double.isNaN(oldWidth) && !Double.isNaN(oldHeight)) {
                stage.setWidth(oldWidth);
                stage.setHeight(oldHeight);
            }
            Platform.runLater(() -> {
                if (!isInit) {
                    double contentWidth = scene.getRoot().prefWidth(-1);
                    double contentHeight = scene.getRoot().prefHeight(-1);
                    double decorationWidth = stage.getWidth() - scene.getWidth();
                    double decorationHeight = stage.getHeight() - scene.getHeight();
                    double minWidth = contentWidth + decorationWidth;
                    double minHeight = contentHeight + decorationHeight;
                    stage.setMinWidth(Math.min(minWidth, stage.getWidth()));
                    stage.setMinHeight(Math.min(minHeight, stage.getHeight()));
                }
                if (oldX >= 0) stage.setX(oldX);
                if (oldY >= 0) stage.setY(oldY);
            });
        });
    }

    public static void sortSongs() {
        List<Map.Entry<String, Song>> entryList = new ArrayList<>(SongConfigWrapper.loadConfig().songs.entrySet());
        entryList.sort(Map.Entry.comparingByValue());
        LinkedHashMap<String, Song> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Song> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        SongConfigWrapper.loadConfig().songs = sortedMap;
    }

    public static void tryMovePlaylistToListTop(Playlist playlist) {
        List<Playlist> playlists = PlaylistConfigWrapper.loadConfig().playlists;
        if (!playlists.getFirst().equals(playlist)) {
            playlists.remove(playlist);
            playlists.addFirst(playlist);
        }
    }

    public static @Nullable String youtubeUrlFromSong(Song song) {
        return song.isFromYoutube() ? "https://youtube.com/watch?v=" + song.getYoutubeId() : null;
    }

    public static String cmdStringFromList(List<String> cmd) {
        StringBuilder builder = new StringBuilder();
        cmd.forEach(entry -> {
            if (entry.contains(" ")) entry = "\"" + entry + "\"";
            builder.append(entry).append(" ");
        });
        return builder.toString();
    }

    public static String formatAsFriendlyText(String name) {
        return name.trim().replace(" ", "_").replace("/", "-").replace("\\", "-").replaceAll("[^a-zA-Z0-9_-]", "");
    }

    public static String audioPathFromSong(Song song) {
        return audioPathFromSong(song, true);
    }
    public static String thumbnailPathFromSong(Song song) {
        return thumbnailPathFromSong(song, true);
    }
    public static String audioPathFromSong(Song song, boolean suffix) {
        return MangoPlayer.APP_FOLDER_PATH + "/assets/audios/" + song.getId() + (suffix ? ".mp3" : "");
    }
    public static String dummyPathFromSong(Song song) {
        return MangoPlayer.APP_FOLDER_PATH + "/assets/dummy/" + song.getId();
    }
    public static String thumbnailPathFromSong(Song song, boolean suffix) {
        return MangoPlayer.APP_FOLDER_PATH + "/assets/thumbnails/" + song.getId() + (suffix ? ".png" : "");
    }
    public static String graphicPathFromPlaylist(Playlist playlist) {
        return MangoPlayer.APP_FOLDER_PATH + "/assets/playlistGraphics/" + playlist.getId() + ".png";
    }

    public static void showCodecErrorMessage() {
        ButtonType ignoreBtn = new ButtonType("Ignore");
        ButtonType learnMoreBtn = new ButtonType("Learn More", ButtonBar.ButtonData.YES);
        Alert alert = new Alert(Alert.AlertType.WARNING, "", ignoreBtn, learnMoreBtn);
        alert.setTitle("FknPlayer | Missing codec");
        alert.setHeaderText("The audio playback failed");
        alert.setContentText("This might be due to a lack of codecs installed on your system");
        alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
        alert.showAndWait();
        if (alert.getResult() == learnMoreBtn) {
            OperatingSystem.loadCurrentOS().open("https://www.oracle.com/java/technologies/javase/products-doc-jdk8-jre8-certconfig.html#:~:text=JavaFX%20Media,12.04%20or%20equivalent.");
        }
    }
    public static void showProcessErrorMessage(boolean conversionProcess, String failedAsset) {
        Platform.runLater(() -> {
            ButtonType issueButton = new ButtonType("Report Issue");
            ButtonType ignoreButton = new ButtonType("Ignore", ButtonBar.ButtonData.YES);
            Alert alert = new Alert(Alert.AlertType.WARNING, "", issueButton, ignoreButton);
            alert.setTitle("FknPlayer | " + (conversionProcess ? "Conversion" : "Download") + " error");
            alert.setHeaderText("An error occurred while " + (conversionProcess ? "converting" : "downloading") + " " + failedAsset);
            alert.setContentText("This may have been caused by a canceled execution of the process.\nIf this is not the case, please report the error to the developer");
            alert.getDialogPane().getStylesheets().add(Finals.STYLESHEET_FORM_APPLICATION_MAIN);
            alert.showAndWait();
            if (alert.getResult() == issueButton) {
                GlobalMenuBarActions.onIssuesMenu();
            }
        });
    }

    public static @Nullable String findExecutableInPath(String command) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isEmpty()) {
            return null;
        }

        String pathSeparator = File.pathSeparator;
        String[] paths = pathEnv.split(pathSeparator);

        String[] extensions = {""}; // Default for UNIX
        if (OperatingSystem.isWindows()) {
            String pathext = System.getenv("PATHEXT");
            if (pathext != null) {
                extensions = pathext.toLowerCase().split(";");
            }
        }

        for (String dir : paths) {
            for (String ext : extensions) {
                File file = new File(dir, command + ext);
                if (file.isFile() && file.canExecute()) {
                    return file.getAbsolutePath();
                }
            }
        }

        return null;
    }
}
