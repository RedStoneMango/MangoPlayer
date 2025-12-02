package io.github.redstonemango.mangoplayer.front.controller.textDisplay;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import io.github.redstonemango.mangoplayer.front.controller.interfaces.IInitializable;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDisplayController implements IInitializable {

    @FXML private Label titleLabel;
    @FXML private WebView textView;

    @Override
    public void init() {
        titleLabel.getScene().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                onExit();
            }
        });
        if (titleLabel.getScene() instanceof TextDisplay scene) {
            titleLabel.setText(scene.getHeader());
            String darkCss = """
                    <style>
                      body {
                        background-color: #373e43;
                        color: #e0e0e0;
                      }
                      a {
                        color: #bb86fc;
                      }
                      pre, code {
                        background-color: #373e43;
                        color: #cfcfcf;
                      }
                    </style>
                    """;
            String htmlText = markdownToHtml(scene.getMarkdown());
            textView.getEngine().setUserDataDirectory(new File(MangoPlayer.APP_FOLDER_PATH + "/internalData/"));
            textView.getEngine().loadContent(darkCss + htmlText, "text/html");
            textView.setContextMenuEnabled(false);

            textView.getEngine().getLoadWorker().stateProperty().addListener((_, _, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    textView.getEngine().executeScript(
                            """
                            document.querySelectorAll('a').forEach(function(anchor) {
                                anchor.addEventListener('click', function(event) {
                                    var href = this.getAttribute('href');
                                    if (!href) return;
                                    event.preventDefault();

                                    if (href.startsWith('#')) {
                                        var target = document.getElementById(href.substring(1));
                                        if (target) {
                                            target.scrollIntoView({ behavior: 'smooth' });
                                        }
                                    }
                                });
                            });
                            """
                    );
                }
            });
        }
        else {
            throw new IllegalStateException("Expected scene to be in instance of " + TextDisplay.class.getName() + " but got " + titleLabel.getScene().getClass().getName() + " instead");
        }
    }

    @FXML
    private void onExit() {
        titleLabel.getScene().getWindow().hide();
    }

    public static String markdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node doc = parser.parse(preprocessMarkdown(markdown));
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(doc);
    }

    private static String preprocessMarkdown(String markdown) {
        Pattern headerPattern = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = headerPattern.matcher(markdown);

        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String hashes = matcher.group(1);
            String headerText = matcher.group(2).trim();

            int level = hashes.length();
            String id = generateId(headerText);

            String replacement = String.format("<h%d id=\"%s\">%s</h%d>%n", level, id, headerText, level);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String generateId(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")   // remove special chars
                .replaceAll("\\s+", "-")           // spaces to dashes
                .replaceAll("-+", "-")             // multiple dashes to one
                .trim();
    }
}
