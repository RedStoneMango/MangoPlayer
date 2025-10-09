package io.github.redstonemango.mangoplayer.front.entryBases;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;

public abstract class ManagerSongEntryBase extends BorderPane {

    protected final FlowPane flowPane;
    protected final ImageView thumbnailView;
    protected final AnchorPane anchorPane;
    protected final Label nameLabel;
    protected final AnchorPane anchorPane0;
    protected final ToggleButton actionButton;
    protected final ImageView imageView;
    protected final Button deleteButton;
    protected final ImageView imageView0;

    public ManagerSongEntryBase() {

        flowPane = new FlowPane();
        thumbnailView = new ImageView();
        anchorPane = new AnchorPane();
        nameLabel = new Label();
        anchorPane0 = new AnchorPane();
        actionButton = new ToggleButton();
        imageView = new ImageView();
        deleteButton = new Button();
        imageView0 = new ImageView();

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(60.0);
        setPrefWidth(600.0);
        setStyle("-fx-border-color: gray;");

        BorderPane.setAlignment(flowPane, javafx.geometry.Pos.CENTER);
        flowPane.setAlignment(javafx.geometry.Pos.CENTER);
        flowPane.setColumnHalignment(javafx.geometry.HPos.CENTER);
        flowPane.setPrefHeight(58.0);
        flowPane.setPrefWidth(61.0);

        thumbnailView.setFitHeight(41.0);
        thumbnailView.setFitWidth(48.0);
        thumbnailView.setPickOnBounds(true);
        thumbnailView.setPreserveRatio(true);
        thumbnailView.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/thumbnail_fallback.png").toExternalForm()));
        setLeft(flowPane);

        BorderPane.setAlignment(anchorPane, javafx.geometry.Pos.CENTER);
        anchorPane.setPrefHeight(200.0);
        anchorPane.setPrefWidth(200.0);

        nameLabel.setLayoutY(-1.0);
        nameLabel.setPrefHeight(60.0);
        nameLabel.setPrefWidth(404.0);
        nameLabel.setText("<Song name here>");
        nameLabel.setFont(new Font(18.0));
        setCenter(anchorPane);

        BorderPane.setAlignment(anchorPane0, javafx.geometry.Pos.CENTER);
        anchorPane0.setPrefHeight(60.0);
        anchorPane0.setPrefWidth(125.0);

        actionButton.setFocusTraversable(false);
        actionButton.setLayoutX(57.0);
        actionButton.setLayoutY(8.0);
        actionButton.setMnemonicParsing(false);
        actionButton.setOnAction(this::onAction);
        actionButton.setPrefHeight(45.0);
        actionButton.setPrefWidth(53.0);

        imageView.setFitHeight(35.0);
        imageView.setFitWidth(35.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        imageView.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/edit.png").toExternalForm()));
        actionButton.setGraphic(imageView);

        deleteButton.setFocusTraversable(false);
        deleteButton.setLayoutX(-5.0);
        deleteButton.setLayoutY(8.0);
        deleteButton.setMnemonicParsing(false);
        deleteButton.setOnAction(this::onDelete);
        deleteButton.setPrefHeight(45.0);
        deleteButton.setPrefWidth(53.0);

        imageView0.setFitHeight(35.0);
        imageView0.setFitWidth(35.0);
        imageView0.setPickOnBounds(true);
        imageView0.setPreserveRatio(true);
        imageView0.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/delete.png").toExternalForm()));
        deleteButton.setGraphic(imageView0);
        setRight(anchorPane0);

        flowPane.getChildren().add(thumbnailView);
        anchorPane.getChildren().add(nameLabel);
        anchorPane0.getChildren().add(actionButton);
        anchorPane0.getChildren().add(deleteButton);

    }

    protected abstract void onAction(javafx.event.ActionEvent actionEvent);

    protected abstract void onDelete(javafx.event.ActionEvent actionEvent);

}
