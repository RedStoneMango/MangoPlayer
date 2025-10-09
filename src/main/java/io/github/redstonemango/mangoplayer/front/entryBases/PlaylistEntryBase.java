package io.github.redstonemango.mangoplayer.front.entryBases;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;

public abstract class PlaylistEntryBase extends BorderPane {

    protected final AnchorPane anchorPane;
    protected final Label nameLabel;
    protected final Label songCountLabel;
    protected final AnchorPane anchorPane0;
    protected final Button button;
    protected final ImageView imageView;
    protected final Button button0;
    protected final ImageView imageView0;
    protected final FlowPane flowPane;
    protected final ImageView graphicView;

    public PlaylistEntryBase() {

        anchorPane = new AnchorPane();
        nameLabel = new Label();
        songCountLabel = new Label();
        anchorPane0 = new AnchorPane();
        button = new Button();
        imageView = new ImageView();
        button0 = new Button();
        imageView0 = new ImageView();
        flowPane = new FlowPane();
        graphicView = new ImageView();

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(60.0);
        setPrefWidth(578.0);
        setStyle("-fx-border-color: gray;");

        BorderPane.setAlignment(anchorPane, javafx.geometry.Pos.CENTER);
        anchorPane.setPrefHeight(58.0);
        anchorPane.setPrefWidth(395.0);

        nameLabel.setLayoutX(7.0);
        nameLabel.setLayoutY(8.0);
        nameLabel.setPrefHeight(30.0);
        nameLabel.setPrefWidth(376.0);
        nameLabel.setText("<PLALIST NAME HERE>");
        nameLabel.setFont(new Font(18.0));

        songCountLabel.setLayoutX(10.0);
        songCountLabel.setLayoutY(34.0);
        songCountLabel.setPrefHeight(29.0);
        songCountLabel.setPrefWidth(369.0);
        songCountLabel.setText("X songs");
        songCountLabel.setFont(new Font(12.0));
        setCenter(anchorPane);

        BorderPane.setAlignment(anchorPane0, javafx.geometry.Pos.CENTER);
        anchorPane0.setPrefHeight(58.0);
        anchorPane0.setPrefWidth(129.0);

        button.setFocusTraversable(false);
        button.setLayoutY(9.0);
        button.setMnemonicParsing(false);
        button.setOnAction(this::onDelete);
        button.setPrefHeight(41.0);
        button.setPrefWidth(51.0);

        imageView.setFitHeight(33.0);
        imageView.setFitWidth(33.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        imageView.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/delete.png").toExternalForm()));
        button.setGraphic(imageView);

        button0.setFocusTraversable(false);
        button0.setLayoutX(65.0);
        button0.setLayoutY(9.0);
        button0.setMnemonicParsing(false);
        button0.setOnAction(this::onOpen);
        button0.setPrefHeight(41.0);
        button0.setPrefWidth(51.0);

        imageView0.setFitHeight(33.0);
        imageView0.setFitWidth(33.0);
        imageView0.setPickOnBounds(true);
        imageView0.setPreserveRatio(true);
        imageView0.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/select.png").toExternalForm()));
        button0.setGraphic(imageView0);
        setRight(anchorPane0);

        BorderPane.setAlignment(flowPane, javafx.geometry.Pos.CENTER);
        flowPane.setAlignment(javafx.geometry.Pos.CENTER);
        flowPane.setColumnHalignment(javafx.geometry.HPos.CENTER);
        flowPane.setPrefHeight(58.0);
        flowPane.setPrefWidth(61.0);

        graphicView.setFitHeight(41.0);
        graphicView.setFitWidth(48.0);
        graphicView.setPickOnBounds(true);
        graphicView.setPreserveRatio(true);
        graphicView.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/no_song_playing.png").toExternalForm()));
        setLeft(flowPane);

        anchorPane.getChildren().add(nameLabel);
        anchorPane.getChildren().add(songCountLabel);
        anchorPane0.getChildren().add(button);
        anchorPane0.getChildren().add(button0);
        flowPane.getChildren().add(graphicView);

    }

    protected abstract void onDelete(javafx.event.ActionEvent actionEvent);

    protected abstract void onOpen(javafx.event.ActionEvent actionEvent);

}
