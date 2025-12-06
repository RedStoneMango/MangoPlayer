package io.github.redstonemango.mangoplayer.back;

import io.github.redstonemango.mangoplayer.front.controller.songOrderSpecification.SongOrderSpecificationController;
import io.github.redstonemango.mangoplayer.front.controller.waitScreen.WaitScreenScene;
import io.github.redstonemango.mangoutils.MangoIO;
import io.github.redstonemango.mangoutils.OperatingSystem;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
            final File resultFile;
            if (!file.getName().endsWith(".zip")) resultFile = new File(file.getAbsolutePath() + ".zip");
            else resultFile = file;


            try {
                Stage stage = new Stage();
                stage.setTitle("MangoPlayer | Song order specification");
                stage.setX(MangoPlayer.primaryStage.getX() + 50);
                stage.setY(MangoPlayer.primaryStage.getY() + 50);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(MangoPlayer.primaryStage);
                FXMLLoader loader = new FXMLLoader(PlaylistExporting.class.getResource(
                        "/io/github/redstonemango/mangoplayer/fxml/util/songOrderSpecification.fxml"));
                Scene scene = new Scene(loader.load());
                Utilities.prepareAndShowStage(stage, scene, loader);
                SongOrderSpecificationController controller = loader.getController();
                controller.setAction(type -> resumeExport(playlist, resultFile, type));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void resumeExport(Playlist playlist, File targetFile, SongOrderSpecificationController.SongOrderType orderType) {
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
        latestSongNumberLength.set(Math.max(2, latestSongNumberLength.get()));
        if (targetFile.exists()) {
            targetFile.delete();
        }

        System.out.println("Exporting playlist '" + playlist.getName()
                + "' (ID is '" + playlist.getId() + "') to file '" + targetFile.getAbsolutePath()
                + "' using the temporary directory '" + tempFolder.getAbsolutePath() + "' and ordering specs of type "
                + orderType + ":");

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
                List<String> songs = new ArrayList<>();

                playlist.getSongs().forEach(song -> {
                    String songname = Utilities.formatAsFriendlyText(song.getName()) + ".mp3";
                    if (orderType.numeric()) {
                        StringBuilder songNumber = new StringBuilder(String.valueOf(playlist.getSongs().indexOf(song) + 1));
                        while (songNumber.length() < latestSongNumberLength.get()) {
                            songNumber.insert(0, "0");
                        }
                        songname = songNumber + "--" + songname;
                    }
                    songs.add(songname); // Preserves order in ArrayList
                    song.exportToFile(new File(tempFolder.getAbsolutePath(), songname), playlist.getName());
                });

                if (orderType.m3u8()) writeM3u8(tempFolder, playlist, songs);
                if (orderType.wpl()) writeWpl(tempFolder, playlist, songs);

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

    private static void writeM3u8(File directory, Playlist playlist, List<String> relativeFiles) {
        try {
            File manifestFile = new File(directory, "playlist.m3u8");
            System.out.println("Writing .m3u8 manifest file as '" + manifestFile.getAbsolutePath() + "'...");

            StringBuilder content = new StringBuilder("""
                    #EXTM3U
                    # Exported playlist '""");
            content.append(playlist.getName()).append("""
                    '
                    # By MangoPlayer (https://github.com/RedStoneMango/MangoPlayer/)
                    
                    
                    """);

            relativeFiles.forEach(file -> content.append(file).append("\n"));

            manifestFile.createNewFile();
            Files.writeString(manifestFile.toPath(), content);

            System.out.println("Done writing .m3u8 manifest file");
        } catch (IOException e) {
            Utilities.showErrorScreen("Create .m3u8 manifest file", String.valueOf(e), false);
        }
    }
    private static void writeWpl(File directory, Playlist playlist, List<String> relativeFiles) {
        try {
            File manifestFile = new File(directory, "playlist.wpl");
            System.out.println("Writing .wpl manifest file as '" + manifestFile.getAbsolutePath() + "'...");

            Document dom;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();

            // Create Structure
            dom.appendChild(dom.createComment("By MangoPlayer (https://github.com/RedStoneMango/MangoPlayer/)"));
            Element rootElement = dom.createElement("smil");
            dom.appendChild(rootElement);
            Element headElement = dom.createElement("head");
            rootElement.appendChild(headElement);
            Element bodyElement = dom.createElement("body");
            rootElement.appendChild(bodyElement);
            Element sequenceElement = dom.createElement("seq");
            bodyElement.appendChild(sequenceElement);

            // Set title
            Element titleElement = dom.createElement("title");
            titleElement.appendChild(dom.createTextNode(playlist.getName()));
            headElement.appendChild(titleElement);

            // Set Generator meta
            Element genMetaElement = dom.createElement("meta");
            genMetaElement.setAttribute("name", "Generator");
            genMetaElement.setAttribute("content", "MangoPlayer");
            headElement.appendChild(genMetaElement);
            // Set Count meta
            Element countMetaElement = dom.createElement("meta");
            countMetaElement.setAttribute("name", "ItemCount");
            countMetaElement.setAttribute("content", String.valueOf(relativeFiles.size()));
            headElement.appendChild(countMetaElement);

            // Insert songs
            relativeFiles.forEach(file -> {
                Element mediaElement = dom.createElement("media");
                mediaElement.setAttribute("src", file);
                sequenceElement.appendChild(mediaElement);
            });

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.VERSION, "1.0");

                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(manifestFile)));

            } catch (TransformerException | IOException e) {
                Utilities.showErrorScreen("Create .wpl manifest file", String.valueOf(e), false);
                return;
            }

            System.out.println("Done writing .wpl manifest file");
        } catch (ParserConfigurationException e) {
            Utilities.showErrorScreen("Configure .wpl manifest file", String.valueOf(e), false);
        }
    }
}
