package io.github.redstonemango.mangoplayer.graphic.entryBases;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public abstract class DownloadResultEntryBase extends BorderPane {

    protected final AnchorPane anchorPane;
    protected final Button button;
    protected final ImageView imageView;
    protected final AnchorPane anchorPane0;
    protected final Label titleLabel;
    protected final Label urlLabel;

    public DownloadResultEntryBase() {

        anchorPane = new AnchorPane();
        button = new Button();
        imageView = new ImageView();
        anchorPane0 = new AnchorPane();
        titleLabel = new Label();
        urlLabel = new Label();

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(60.0);
        setPrefWidth(471.0);
        setStyle("-fx-border-color: gray;");

        BorderPane.setAlignment(anchorPane, javafx.geometry.Pos.CENTER);
        anchorPane.setPrefHeight(58.0);
        anchorPane.setPrefWidth(63.0);

        button.setFocusTraversable(false);
        button.setLayoutX(2.0);
        button.setLayoutY(10.0);
        button.setMnemonicParsing(false);
        button.setOnAction(this::select);
        button.setPrefHeight(40.0);
        button.setPrefWidth(40.0);

        imageView.setFitHeight(27.0);
        imageView.setFitWidth(25.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        imageView.setImage(new Image(getClass().getResource("/io/github/redstonemango/mangoplayer/images/select.png").toExternalForm()));
        button.setGraphic(imageView);
        setRight(anchorPane);

        BorderPane.setAlignment(anchorPane0, javafx.geometry.Pos.CENTER);
        anchorPane0.setPrefHeight(200.0);
        anchorPane0.setPrefWidth(200.0);

        titleLabel.setLayoutX(8.0);
        titleLabel.setPrefHeight(32.0);
        titleLabel.setPrefWidth(382.0);
        titleLabel.setText("<VIDEO TITLE>");
        titleLabel.setFont(new Font(19.0));

        urlLabel.setLayoutX(9.0);
        urlLabel.setLayoutY(30.0);
        urlLabel.setOnMouseClicked(this::onLinkOpen);
        urlLabel.setPrefHeight(28.0);
        urlLabel.setPrefWidth(381.0);
        urlLabel.setText("<VIDEO URL>");
        urlLabel.setTextFill(javafx.scene.paint.Color.BLUE);
        urlLabel.setUnderline(true);
        urlLabel.setFont(new Font(12.0));
        urlLabel.setCursor(Cursor.HAND);
        setCenter(anchorPane0);

        anchorPane.getChildren().add(button);
        anchorPane0.getChildren().add(titleLabel);
        anchorPane0.getChildren().add(urlLabel);

    }

    protected abstract void select(javafx.event.ActionEvent actionEvent);

    protected abstract void onLinkOpen(javafx.scene.input.MouseEvent mouseEvent);

}
