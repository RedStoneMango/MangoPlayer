package io.github.redstonemango.mangoplayer.graphic.controller.useAnalyser;

import io.github.redstonemango.mangoplayer.graphic.ComboBoxSearching;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import io.github.redstonemango.mangoplayer.graphic.controller.interfaces.IInitializable;
import io.github.redstonemango.mangoplayer.graphic.controller.songManager.SongListScene;
import io.github.redstonemango.mangoplayer.logic.Utilities;
import io.github.redstonemango.mangoplayer.logic.config.PlaylistConfigWrapper;
import io.github.redstonemango.mangoplayer.logic.config.SongConfigWrapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This is a standalone class, not an inheritor of IInitializable to enable us to do an async load
public class AnalyserController {

    private boolean initialized = false;

    @FXML private TabPane tabPane;

    @FXML private TableView<SongDataRepresentation> songTable;
    @FXML private TableColumn<SongDataRepresentation, String> songNameColumn;
    @FXML private TableColumn<SongDataRepresentation, Image> songThumbnailColumn;
    @FXML private TableColumn<SongDataRepresentation, String> songListenColumn;
    @FXML private TableColumn<SongDataRepresentation, String> songUseColumn;
    @FXML private TableColumn<SongDataRepresentation, String> songDurationColumn;
    @FXML private ComboBox<SongDataRepresentation> songJumpBox;

    @FXML private TableView<PlaylistDataRepresentation> playlistTable;
    @FXML private TableColumn<PlaylistDataRepresentation, String> playlistNameColumn;
    @FXML private TableColumn<PlaylistDataRepresentation, Image> playlistGraphicColumn;
    @FXML private TableColumn<PlaylistDataRepresentation, String> playlistPlayCountColumn;
    @FXML private TableColumn<PlaylistDataRepresentation, String> playlistTimeColumn;
    @FXML private TableColumn<PlaylistDataRepresentation, String> playlistSongCountColumn;
    @FXML private TableColumn<PlaylistDataRepresentation, String> playlistTotalDurationColumn;
    @FXML private ComboBox<PlaylistDataRepresentation> playlistJumpBox;

    // This is a standalone method, not an inheritor of IInitializable to enable us to do an async load
    public void init() {
        tabPane.getSelectionModel().select(1);
        tabPane.getScene().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) onBack();
        });

        initSongTab();
        initPlaylistTab();

        initialized = true;
    }

    private void initSongTab() {
        ComboBoxSearching.apply(songJumpBox, SongDataRepresentation.class);
        Utilities.applyComboBoxCellFactory(songJumpBox, songData -> {
            BorderPane pane = new BorderPane();
            Label label = new Label(songData.nameProperty().get());
            BorderPane.setAlignment(label, Pos.CENTER_LEFT);
            label.setTextFill(Color.BLACK);
            pane.setCenter(label);
            ImageView image = new ImageView(songData.thumbnailProperty().get());
            image.setPreserveRatio(true);
            image.setPickOnBounds(true);
            image.setFitHeight(20);
            BorderPane.setMargin(image, new Insets(0, 5, 0, 0));
            pane.setLeft(image);
            return pane;
        });

        List<SongDataRepresentation> boxOptions = ComboBoxSearching.getOptions(songJumpBox, SongDataRepresentation.class);
        Property<SongDataRepresentation> boxSelection = ComboBoxSearching.selectionProperty(songJumpBox, SongDataRepresentation.class);
        assert boxOptions != null;
        assert boxSelection != null;
        boxSelection.addListener((_, _, selection) -> {
            if (selection == null) return;
            songTable.scrollTo(selection);
            songTable.getSelectionModel().select(selection);
            songTable.requestFocus();
            boxSelection.setValue(null);
        });

        songNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        songThumbnailColumn.setCellValueFactory(cellData -> cellData.getValue().thumbnailProperty());
        songListenColumn.setCellValueFactory(cellData -> cellData.getValue().listenCountProperty());
        songUseColumn.setCellValueFactory(cellData -> cellData.getValue().useCountProperty());
        songDurationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
        songThumbnailColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<SongDataRepresentation, Image> call(TableColumn<SongDataRepresentation, Image> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            ImageView imageView = new ImageView(item);
                            imageView.setPreserveRatio(true);
                            imageView.setPickOnBounds(true);
                            imageView.setFitHeight(50);
                            FlowPane pane = new FlowPane(imageView);
                            pane.setAlignment(Pos.CENTER);
                            pane.setPrefHeight(50);
                            pane.setPrefWidth(50);
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
        AnalyserScene scene = (AnalyserScene) tabPane.getScene();
        songTable.getItems().addAll(scene.getSongData());
        boxOptions.addAll(scene.getSongData());

        applyComparator(songUseColumn);
        applyComparator(songDurationColumn);
        applyComparator(songListenColumn);

        songListenColumn.setSortType(TableColumn.SortType.DESCENDING);
        songTable.getSortOrder().add(songListenColumn);
        songTable.sort();
    }

    private void initPlaylistTab() {
        ComboBoxSearching.apply(playlistJumpBox, PlaylistDataRepresentation.class);
        Utilities.applyComboBoxCellFactory(playlistJumpBox, songData -> {
            BorderPane pane = new BorderPane();
            Label label = new Label(songData.nameProperty().get());
            BorderPane.setAlignment(label, Pos.CENTER_LEFT);
            label.setTextFill(Color.BLACK);
            pane.setCenter(label);
            ImageView image = new ImageView(songData.graphicProperty().get());
            image.setPreserveRatio(true);
            image.setPickOnBounds(true);
            image.setFitHeight(20);
            BorderPane.setMargin(image, new Insets(0, 5, 0, 0));
            pane.setLeft(image);
            return pane;
        });

        List<PlaylistDataRepresentation> boxOptions = ComboBoxSearching.getOptions(playlistJumpBox, PlaylistDataRepresentation.class);
        Property<PlaylistDataRepresentation> boxSelection = ComboBoxSearching.selectionProperty(playlistJumpBox, PlaylistDataRepresentation.class);
        assert boxOptions != null;
        assert boxSelection != null;
        boxSelection.addListener((_, _, selection) -> {
            if (selection == null) return;
            playlistTable.scrollTo(selection);
            playlistTable.getSelectionModel().select(selection);
            playlistTable.requestFocus();
            boxSelection.setValue(null);
        });

        playlistNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        playlistGraphicColumn.setCellValueFactory(cellData -> cellData.getValue().graphicProperty());
        playlistPlayCountColumn.setCellValueFactory(cellData -> cellData.getValue().playedSongCountProperty());
        playlistTimeColumn.setCellValueFactory(cellData -> cellData.getValue().totalPlayTimeProperty());
        playlistSongCountColumn.setCellValueFactory(cellData -> cellData.getValue().songCountProperty());
        playlistTotalDurationColumn.setCellValueFactory(cellData -> cellData.getValue().totalDurationProperty());
        playlistGraphicColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<PlaylistDataRepresentation, Image> call(TableColumn<PlaylistDataRepresentation, Image> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            ImageView imageView = new ImageView(item);
                            imageView.setPreserveRatio(true);
                            imageView.setPickOnBounds(true);
                            imageView.setFitHeight(50);
                            FlowPane pane = new FlowPane(imageView);
                            pane.setAlignment(Pos.CENTER);
                            pane.setPrefHeight(50);
                            pane.setPrefWidth(50);
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
        applyComparator(playlistSongCountColumn);
        applyComparator(playlistPlayCountColumn);
        applyComparator(playlistTimeColumn);

        AnalyserScene scene = (AnalyserScene) tabPane.getScene();
        playlistTable.getItems().addAll(scene.getPlaylistData());
        boxOptions.addAll(scene.getPlaylistData());

        playlistPlayCountColumn.setSortType(TableColumn.SortType.DESCENDING);
        playlistTable.getSortOrder().add(playlistPlayCountColumn);
        playlistTable.sort();
    }

    @FXML
    private void onTabChange() {
        if (tabPane.getTabs().getFirst().isSelected() && initialized) {
            onBack();
        }
    }

    @FXML
    private void onBack() {
        SongListScene scene = SongListScene.createViewingScene(((AnalyserScene) tabPane.getScene()).getMainWindowController());
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.setTitle("MangoPlayer | Song manager");
        Utilities.prepareAndShowStage(stage, scene, scene.getLoader());
    }

    private static final Pattern LEADING_INT_PATTERN = Pattern.compile("^(\\d+)");

    private static void applyComparator(TableColumn<?, String> column) {
        column.setComparator((s1, s2) -> {
            if (s1 == null) return -1;
            if (s2 == null) return 1;

            String n1 = normalize(s1);
            String n2 = normalize(s2);

            if (n1.equals(n2)) return 0;

            boolean isTime1 = isLikelyTime(n1);
            boolean isTime2 = isLikelyTime(n2);

            if (isTime1 && isTime2) {
                long t1 = parseTimeToMillis(n1);
                long t2 = parseTimeToMillis(n2);
                return Long.compare(t1, t2);
            }

            Integer i1 = extractLeadingInteger(n1);
            Integer i2 = extractLeadingInteger(n2);

            if (i1 != null && i2 != null) {
                return Integer.compare(i1, i2);
            }

            return n1.compareToIgnoreCase(n2);
        });
    }

    private static String normalize(String input) {
        input = input.trim();
        if (input.equalsIgnoreCase("no songs") || input.equalsIgnoreCase("so songs")) {
            return "0 songs";
        }
        return input;
    }

    private static boolean isLikelyTime(String input) {
        int colonCount = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ':') colonCount++;
            else if (!Character.isDigit(input.charAt(i)) && input.charAt(i) != ':') {
                return false; // Early exit: invalid character
            }
        }
        return colonCount >= 1;
    }

    private static long parseTimeToMillis(String timeString) {
        String[] parts = timeString.split(":");
        long total = 0;
        int[] multipliers = {1, 1000, 60000, 3600000, 86400000}; // ms, sec, min, hr, day
        int index = parts.length - 1;

        for (int i = 0; i < parts.length && i < multipliers.length; i++) {
            try {
                int unit = Integer.parseInt(parts[index - i]);
                total += unit * (long) multipliers[i];
            } catch (NumberFormatException e) {
                break; // stop parsing on first invalid part
            }
        }

        return total;
    }

    private static Integer extractLeadingInteger(String input) {
        Matcher m = LEADING_INT_PATTERN.matcher(input);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
