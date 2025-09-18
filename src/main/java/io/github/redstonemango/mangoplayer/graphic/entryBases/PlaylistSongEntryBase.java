package io.github.redstonemango.mangoplayer.graphic.entryBases;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class PlaylistSongEntryBase extends BorderPane {

    protected final HBox hBox;
    protected final FlowPane flowPane;
    protected final Label sortLabel;
    protected final ImageView sortImageView;
    protected final Tooltip unsortableTooltip;
    protected final Text text;
    protected final FlowPane flowPane0;
    protected final ImageView imageView;
    protected final AnchorPane anchorPane;
    protected final Label nameLabel;
    protected final AnchorPane anchorPane0;
    protected final Button deleteButton;
    protected final ImageView imageView0;
    protected final Button button;
    protected final ImageView imageView1;

    public PlaylistSongEntryBase() {

        hBox = new HBox();
        flowPane = new FlowPane();
        sortLabel = new Label();
        sortImageView = new ImageView();
        unsortableTooltip = new Tooltip();
        text = new Text();
        flowPane0 = new FlowPane();
        imageView = new ImageView();
        anchorPane = new AnchorPane();
        nameLabel = new Label();
        anchorPane0 = new AnchorPane();
        deleteButton = new Button();
        imageView0 = new ImageView();
        button = new Button();
        imageView1 = new ImageView();

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(60.0);
        setPrefWidth(600.0);
        setStyle("-fx-border-color: gray; -fx-border-width: 1;");

        BorderPane.setAlignment(hBox, javafx.geometry.Pos.CENTER);

        flowPane.setAlignment(javafx.geometry.Pos.CENTER);
        flowPane.setPrefHeight(58.0);
        flowPane.setPrefWidth(23.0);

        sortLabel.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        sortLabel.setCursor(Cursor.MOVE);

        sortImageView.setFitHeight(10.0);
        sortImageView.setFitWidth(16.0);
        sortImageView.setPickOnBounds(true);
        sortImageView.setPreserveRatio(true);
        sortImageView.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/sort_entry_on.png").toExternalForm()));
        sortLabel.setGraphic(sortImageView);

        unsortableTooltip.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);

        text.setFill(Color.RED);
        text.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        text.setStrokeWidth(0.0);
        text.setText("Sorting is not possible while filtering/playing");
        unsortableTooltip.setGraphic(text);
        unsortableTooltip.setFont(new Font(15.0));
        sortLabel.setTooltip(unsortableTooltip);

        flowPane0.setAlignment(javafx.geometry.Pos.CENTER);
        flowPane0.setColumnHalignment(javafx.geometry.HPos.CENTER);
        flowPane0.setPrefHeight(58.0);
        flowPane0.setPrefWidth(61.0);

        imageView.setFitHeight(41.0);
        imageView.setFitWidth(48.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        imageView.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/thumbnail_fallback.png").toExternalForm()));
        setLeft(hBox);

        BorderPane.setAlignment(anchorPane, javafx.geometry.Pos.CENTER);
        anchorPane.setPrefHeight(200.0);
        anchorPane.setPrefWidth(200.0);

        nameLabel.setLayoutY(-1.0);
        nameLabel.setPrefHeight(60.0);
        nameLabel.setPrefWidth(386.0);
        nameLabel.setText("<Song name here>");
        nameLabel.setFont(new Font(18.0));
        setCenter(anchorPane);

        BorderPane.setAlignment(anchorPane0, javafx.geometry.Pos.CENTER);
        anchorPane0.setPrefHeight(60.0);
        anchorPane0.setPrefWidth(125.0);

        deleteButton.setFocusTraversable(false);
        deleteButton.setLayoutY(8.0);
        deleteButton.setMnemonicParsing(false);
        deleteButton.setOnAction(this::onDelete);
        deleteButton.setPrefHeight(45.0);
        deleteButton.setPrefWidth(45.0);

        imageView0.setFitHeight(35.0);
        imageView0.setFitWidth(35.0);
        imageView0.setPickOnBounds(true);
        imageView0.setPreserveRatio(true);
        imageView0.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/delete.png").toExternalForm()));
        deleteButton.setGraphic(imageView0);

        button.setFocusTraversable(false);
        button.setLayoutX(57.0);
        button.setLayoutY(7.0);
        button.setMnemonicParsing(false);
        button.setOnAction(this::onPlayThis);
        button.setPrefHeight(45.0);
        button.setPrefWidth(45.0);

        imageView1.setFitHeight(35.0);
        imageView1.setFitWidth(35.0);
        imageView1.setPickOnBounds(true);
        imageView1.setPreserveRatio(true);
        imageView1.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/play_this.png").toExternalForm()));
        button.setGraphic(imageView1);
        setRight(anchorPane0);

        flowPane.getChildren().add(sortLabel);
        hBox.getChildren().add(flowPane);
        flowPane0.getChildren().add(imageView);
        hBox.getChildren().add(flowPane0);
        anchorPane.getChildren().add(nameLabel);
        anchorPane0.getChildren().add(deleteButton);
        anchorPane0.getChildren().add(button);

    }

    protected abstract void onDelete(javafx.event.ActionEvent actionEvent);

    protected abstract void onPlayThis(javafx.event.ActionEvent actionEvent);

}
