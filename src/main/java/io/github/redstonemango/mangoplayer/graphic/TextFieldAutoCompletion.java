package io.github.redstonemango.mangoplayer.graphic;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A utility class to add automatic completions to {@linkplain TextField JavaFX's TextField}.<br>
 * This class has quite the same usage as {@linkplain org.controlsfx.control.textfield.TextFields#bindAutoCompletion(TextField, Object[]) TextFields.bindAutoCompletion(String, Object[])}, but does not require additionally added module exports. It also provides more functionality than the in-build method as you are able to {@linkplain MetaData#setCompletions(String...) update the possible completions} or {@linkplain MetaData#setMatchHighlightColor(Color) adjust it's appearance}.<br><br>
 * To start with this, use the following setup:
 * <pre>
 * {@code
 * TextFieldAutoCompletion.autoCompletable(textField).[...];
 * }
 * </pre>
 * @author Fabian Krohn
 */
public class TextFieldAutoCompletion {
    /**
     * Library-internal final integer value, defining the entries' padding.
     */
    private static final int padding = 10;
    /**
     * Library-internal {@linkplain Map} containing the information about which {@linkplain TextField TextFields} are auto completable and their {@linkplain MetaData metadata}
     */
    private static final Map<TextField, MetaData> data = new HashMap<>();

    /**
     * Accesses the {@linkplain MetaData auto completable data} of a {@linkplain TextField}.<br>
     * If no data exists <i>(field is not {@linkplain #isRegistered(TextField) registered})</i>, they will be created and therefor make the field auto completable.<br><br>
     * The default usage looks like this:
     * <pre>
     * {@code
     *  // Register as auto completable
     *  TextFieldAutoCompletion.autoCompletable(textField);
     *
     *  // Edit configuration (Automatically registers, if not registered yet)
     *  TextFieldAutoCompletion.autoCompletable(textField)
     *      .setCompletions("text one", "text two", "text three")
     *      .setLineCount(10);
     * }
     * </pre><br>
     * As this library occupies the {@linkplain Node#setOnKeyPressed(EventHandler) onKeyPressed event}, the event should not be used anywhere to avoid callback conflicts.<br>
     * To add a listener that does not conflict with this library, please use the {@linkplain MetaData MetaData}'s {@linkplain MetaData#setOnKeyPressed(Consumer) onKeyPressed configuration}.
     * <br><br>
     * <font color=red>!!! The field has to part of a scene to be accessed correctly. If the field has not been added to a scene yet <i>(e.g.: when using {@linkplain javafx.fxml.FXML @FXML private void initialize()})</i> an {@linkplain IllegalStateException} will be thrown !!!</font>
     * @param textField The {@linkplain TextField} to be accessed <i>(Has to be part of a scene)</i>
     * @return The {@linkplain TextField}'s {@linkplain MetaData MetaData}
     * @see MetaData#setOnKeyPressed(Consumer)
     * @see MetaData#setCompletions(String...)
     * @see MetaData#setLineCount(int)
     * @throws IllegalStateException When the given {@linkplain TextField} is not part of a {@linkplain javafx.scene.Scene Scene}.
     */
    public static MetaData autoCompletable(TextField textField) throws IllegalStateException {
        if (textField.getScene() == null) throw new IllegalStateException("The TextField has to be part of a scene to be completable");
        if (!data.containsKey(textField)) {
            MetaData metaData = new MetaData(textField, 8, false, true, true, Color.DARKORANGE);
            data.put(textField, metaData);

            textField.focusedProperty().addListener(metaData.focusEvent);
            textField.getScene().addEventHandler(MouseEvent.MOUSE_PRESSED, metaData.mouseEvent);
            textField.getScene().getWindow().xProperty().addListener(metaData.posEvent);
            textField.getScene().getWindow().yProperty().addListener(metaData.posEvent);
            textField.textProperty().addListener(metaData.textEvent);
            textField.setOnKeyPressed(metaData.keyEvent);
        }
        return data.get(textField);
    }

    /**
     * Ensures that a {@linkplain TextField} is <b>non-auto completable</b>.<br>
     * Calling this method on a <b>non-auto completable</b> field will do nothing while calling on a {@linkplain #isRegistered(TextField) registered} field unregisters it from the completable list and resets it's {@linkplain MetaData MetaData}.<br>br>
     * <pre>
     *{@code
     *  // Unregister auto completion data (Does nothing yet, as the field is not registered)
     *  TextFieldAutoCompletion.nonAutoCompletable(textField);
     *
     *  // Register field as auto completion
     *  TextFieldAutoCompletion.autoCompletable(textField);
     *
     *  // Unregister auto completion data (Makes the field non auto completable)
     *  TextFieldAutoCompletion.nonAutoCompletable(textField);
     *}
     * </pre><br>
     * Key press events, registered using {@linkplain MetaData#setOnKeyPressed(Consumer) setOnKeyPressed} method will be merged into the {@linkplain TextField}'s data and therefor still work.<br><br>
     * <font color=red>!!! The field has to part of a scene to be accessed correctly. If the field has not been added to a scene yet <i>(e.g.: when using {@linkplain javafx.fxml.FXML @FXML private void initialize()})</i> an {@linkplain IllegalStateException} will be thrown !!!</font>
     * @param textField The {@linkplain TextField} to make <b>non-auto completable</b>.
     * @throws IllegalStateException When the given {@linkplain TextField} is not part of a {@linkplain javafx.scene.Scene}.
     */
    public static void nonAutoCompletable(TextField textField) throws IllegalStateException {
        if (textField.getScene() == null) throw new IllegalStateException("The TextField has to be part of a scene to be completable");
        if (data.containsKey(textField)) {
            MetaData metaData = data.get(textField);

            textField.focusedProperty().removeListener(metaData.focusEvent);
            textField.getScene().removeEventHandler(MouseEvent.MOUSE_PRESSED, metaData.mouseEvent);
            textField.getScene().getWindow().xProperty().removeListener(metaData.posEvent);
            textField.getScene().getWindow().yProperty().removeListener(metaData.posEvent);
            textField.textProperty().removeListener(metaData.textEvent);
            textField.setOnKeyPressed(metaData.onKeyPressed == null ? null : e -> metaData.onKeyPressed.accept(e));

            data.remove(textField);
        }
    }

    /**
     * Checks whether a {@linkplain TextField} is registered as <b>auto completable</b>.
     * @param textField The {@linkplain TextField} to check for
     * @return Whether the field is registered.
     */
    public static boolean isRegistered(TextField textField) {
        return data.containsKey(textField);
    }



    /**
     * Library-internal method that is called when a registered field's text changes.
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, whose text has changed.
     * @param newText The field's new text.
     */
    private static void onTextChange(MetaData metaData, String newText) {
        if (newText.isEmpty()) {
            metaData.popup.hide();
            return;
        }

        List<AnchorPane> matchingCompletions = filterCompletions(metaData, newText);
        if (matchingCompletions.isEmpty()) {
            metaData.popup.hide();
            return;
        }

        metaData.popupBox.getChildren().clear();
        metaData.popupBox.getChildren().addAll(matchingCompletions);
        double x = metaData.textField.localToScreen(0, 0).getX() + 2;
        double y = metaData.textField.localToScreen(0, 0).getY() + metaData.textField.getHeight() + 2;
        metaData.popup.show(metaData.textField, x, y);

        if (matchingCompletions.size() < metaData.lineCount) {
            metaData.popupPane.setPrefHeight(matchingCompletions.size() * (2 * padding + 10) + 10);
        }
        else {
            metaData.popupPane.setPrefHeight(metaData.lineCount * (2 * padding + 10) + 10);
        }
        metaData.popupBox.setMaxWidth(metaData.textField.getWidth() - 4);
        metaData.popupBox.setPrefWidth(metaData.textField.getWidth() - 4);
    }

    /**
     * Library-internal method that is called when an <b>auto complete entry</b> is selected
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, whose entry has been selected.
     * @param index The index of the entry that has been selected.
     * @return Whether the action was successful. <i>(<code>true</code> if <code>index</code> was not <code>-1</code>, <code>false</code> otherwise)</i>
     */
    private static boolean onEntrySelected(MetaData metaData, int index) {
        if (index == -1) return false;

        StringBuilder sb = new StringBuilder();
        for (Node node : ((TextFlow)((Label)((AnchorPane)metaData.popupBox.getChildren().get(index)).getChildren().getFirst()).getGraphic()).getChildren()) {
            if (node instanceof Text) {
                sb.append(((Text) node).getText());
            }
        }
        String completion = sb.toString();

        metaData.textField.setText(completion);
        metaData.textField.positionCaret(completion.length());
        metaData.popup.hide();

        if (metaData.onAutoComplete != null) {
            metaData.onAutoComplete.accept(completion);
        }
        return true;
    }

    /**
     * Library-internal method that filters the completions matching the field's text and returns them as a {@linkplain List} of {@linkplain #buildEntry(MetaData, String, String, int) entries} so they can be used elsewhere
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, this is being filtered.
     * @param sourceText The text that has been entered into the field.
     * @return The matching completions as a {@linkplain List} of {@linkplain #buildEntry(MetaData, String, String, int) entries}.
     */
    private static List<AnchorPane> filterCompletions(MetaData metaData, String sourceText) {
        List<AnchorPane> matchingCompletions = new ArrayList<>();
        metaData.completions.forEach(completion -> {
            if (metaData.caseSensitive) {
                if (completion.contains(sourceText)) matchingCompletions.add(buildEntry(metaData, completion, sourceText, matchingCompletions.size()));
            }
            else {
                if (completion.toLowerCase(Locale.ROOT).contains(sourceText.toLowerCase(Locale.ROOT))) matchingCompletions.add(buildEntry(metaData, completion, sourceText, matchingCompletions.size()));
            }
        });
        return matchingCompletions;
    }

    /**
     * Library-internal method that builds an {@linkplain AnchorPane} containing a {@linkplain Label} with the completion's {@linkplain #buildTextFlow(MetaData, String, String) text}.
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, this is being filtered.
     * @param text The text of the completion, this method builds the entry for.
     * @param filter The filter that has been entered into the field.
     * @param boxIndex The index, this entry is going to have in the popup list.
     * @return The built {@linkplain AnchorPane}.
     */
    private static AnchorPane buildEntry(MetaData metaData, String text, String filter, int boxIndex) {
        Label label = new Label();
        label.setGraphic(buildTextFlow(metaData, text, filter));
        label.setPrefHeight(10);
        label.setMouseTransparent(true);
        AnchorPane pane = new AnchorPane(label);
        pane.setStyle("-fx-background-color: white;");
        pane.setOnMouseEntered(_ -> setSelectedIndex(metaData, boxIndex));
        pane.setOnMouseExited(_ -> setSelectedIndex(metaData, -1));
        pane.setOnMouseClicked(_ -> onEntrySelected(metaData, boxIndex));
        return pane;
    }

    /**
     * Library-internal method that builds a colored {@linkplain TextFlow} <i>(that is then used in the {@linkplain #buildEntry(MetaData, String, String, int) entry}'s label)</i>.
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, this is being filtered.
     * @param text The text of the completion, this method builds the text flow for.
     * @param filter The filter that has been entered into the field.
     * @return The built {@linkplain TextFlow}.
     */
    private static TextFlow buildTextFlow(MetaData metaData, String text, String filter) {
        int filterIndex = text.toLowerCase().indexOf(filter.toLowerCase());
        Text textBefore = new Text(text.substring(0, filterIndex));
        Text textAfter = new Text(text.substring(filterIndex + filter.length()));
        Text textFilter = new Text(text.substring(filterIndex,  filterIndex + filter.length()));
        textFilter.setFill(metaData.matchHighlightColor);
        textFilter.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 12));
        return new TextFlow(textBefore, textFilter, textAfter);
    }

    /**
     * Library-internal method that moves the currently selected index and is called when pressing the <b>arrow keys</b>. <i>(This does not mean, it moves the entry itself, but the selection)</i>
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, whose selection is being moved.
     * @param moveUp Boolean defining the direction, the selection should move <i>(<code>false</code>= move down, <code>true</code>= move up)</i>
     * @return Whether the action was successful, i.e., whether there are selectable indexes visible.
     */
    private static boolean moveSelectedIndex(MetaData metaData, boolean moveUp) {
        int maxIndex = metaData.popupBox.getChildren().size() - 1;
        int currentIndex = getSelectedIndex(metaData);
        int newIndex = currentIndex + (moveUp ? -1 : 1);

        if (newIndex > maxIndex) {
            newIndex = 0;
        }
        else if (newIndex < 0) {
            newIndex = maxIndex;
        }
        if (metaData.isAutoScroll()) scrollToEntry(metaData, metaData.lineCount <= 2 ? newIndex : Math.max(newIndex - 2, 0));
        return setSelectedIndex(metaData, newIndex);
    }

    /**
     * Library-internal method that sets the selected index to a given value. <i>(Means, it selects a completion at index <b><i>x</i></b>)</i>
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, whose selection is being set.
     * @param index The index of the completion to select.
     * @return Whether the index could be selected, i.e., whether it was in bounds of the possible completions
     */
    private static boolean setSelectedIndex(MetaData metaData, int index) {
        metaData.popupBox.getChildren().forEach(child -> child.setStyle("-fx-background-color: white;"));
        if (index >= 0 && index <= metaData.popupBox.getChildren().size() - 1) {
            metaData.popupBox.getChildren().get(index).setStyle("-fx-background-color: silver;");
            return true;
        }
        return false;
    }
    /**
     * Library-internal method that gets the selected index. <i>(Means, it gets the index <b><i>x</i></b> of the currently selected completion)</i>
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, whose selection is being set.
     * @return The currently selected index.
     */
    private static int getSelectedIndex(MetaData metaData) {
        for (int i = 0; i < metaData.popupBox.getChildren().size(); i++) {
            if (metaData.popupBox.getChildren().get(i).getStyle().equals("-fx-background-color: silver;")) return i;
        }
        return -1;
    }

    /**
     * Library-internal method that scrolls the {@linkplain ScrollPane} so the completion at index <b><i>x</i></b> is at the pane's top.
     * @param metaData The {@linkplain MetaData MetaData} belonging to the field, whose pane is being accessed.
     * @param index The index to scroll to.
     */
    private static void scrollToEntry(MetaData metaData, int index) {
        if (index < 0) return;
        if (index > metaData.popupBox.getChildren().size() - 1) return;

        Bounds bounds = metaData.popupPane.getViewportBounds();
        metaData.popupPane.setVvalue(metaData.popupBox.getChildren().get(index).getLayoutY() * (1 / (metaData.popupBox.getHeight() - bounds.getHeight())));
    }


    /**
     * A subclass of {@linkplain TextFieldAutoCompletion} that controls the metadata of a registered {@linkplain TextField}.<br><br>
     * It can be accessed using {@linkplain #autoCompletable(TextField) TextFieldAutoCompletion.autoCompletable(TextField)}:
     * <pre>
     *  {@code
     *  // Access configuration
     *  Set<String> completions = TextFieldAutoCompletion.autoCompletable(textField).getCompletions();
     *
     *  // Modify configuration
     *  TextFieldAutoCompletion.autoCompletable(textField).setCompletions("text one", "text two", "text three");
     *
     *  // Modify configuration (chained)
     *  TextFieldAutoCompletion.autoCompletable(textField).setCompletions("text one", "text two", "text three").setLineCount(10);
     *  }
     * </pre>
     */
    public static class MetaData {
        /**
         * The completion popup's max count of lines that should be shown at once.
         */
        private int lineCount;
        /**
         * Whether the pane should scroll automatically when selecting a new entry using the <b>arrow keys</b>.
         */
        private boolean autoScroll;
        /**
         * Set of <i>(string)</i> completions that should be suggested.
         */
        private Set<String> completions;
        /**
         * Whether the search algorithm should be case-sensitive
         */
        private boolean caseSensitive;
        /**
         * <i>({@linkplain Nullable})</i> consumer called when auto completing the field (Completed text as arg)
         */
        private @Nullable Consumer<String> onAutoComplete;
        /**
         * The color, matching parts in the completion should be highlighted in.
         */
        private Color matchHighlightColor;
        /**
         * <i>({@linkplain Nullable})</i> consumer used to hook additional <i>onKeyPressed</i> logic to the field without overriding the library's one.
         */
        private @Nullable Consumer<? super KeyEvent> onKeyPressed;


        // library internals
        /**
         * Library-internal value storing the metadata owner.
         */
        private final TextField textField;
        /**
         * Library-internal value storing the VBox that contains the single completion entries.
         */
        private final VBox popupBox = new VBox(padding);
        /**
         * Library-internal value storing the ScrollPane that contains the popupBox.
         */
        private final ScrollPane popupPane = new ScrollPane(popupBox);
        /**
         * Library-internal value storing the Popup that contains the popupPane.
         */
        private final Popup popup = new Popup();
        /**
         * Library-internal value storing the action callback that is called when the owner receives/loses focus
         */
        private final ChangeListener<Boolean> focusEvent;
        /**
         * Library-internal value storing the action callback that is called when the scene is clicked with the mouse.
         */
        private final EventHandler<? super MouseEvent> mouseEvent;
        /**
         * Library-internal value storing the action callback that is called when the owner's text changes
         */
        private final ChangeListener<String> textEvent;
        /**
         * Library-internal value storing the action callback that is called when the stage is moved.
         */
        private final ChangeListener<Number> posEvent;
        /**
         * Library-internal value storing the action callback that is called when the owner fires a <b>keyPressEvent</b>.
         */
        private final EventHandler<? super KeyEvent> keyEvent;

        /**
         * Constructor for the metadata object.
         * @param textField This metadata owner <i>(The {@linkplain TextField}, this metadata stores values for)</i>
         * @param lineCount The completion popup's max count of lines that should be shown at once.
         * @param caseSensitive Whether the search algorithm should be case-sensitive.
         * @param autoScroll Whether the popup should scroll automatically when selecting a new entry using the arrow keys.
         * @param escapable Whether the popup can be hidden by pressing <b>ESC</b> key.
         * @param matchHighlightColor The color, matching parts in the completion should be highlighted in.
         */
        public MetaData(TextField textField, int lineCount, boolean caseSensitive, boolean autoScroll, boolean escapable, Color matchHighlightColor) {
            this.textField = textField;
            this.lineCount = lineCount;
            this.completions = new HashSet<>();
            this.caseSensitive = caseSensitive;
            this.autoScroll = autoScroll;
            this.matchHighlightColor = matchHighlightColor;

            popupBox.setPadding(new Insets(padding, padding + 10, padding, padding));
            popupPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            popup.getContent().add(popupPane);
            popup.setHideOnEscape(escapable);

            focusEvent = (_, _, _) -> popup.hide();
            mouseEvent = _ -> popup.hide();
            textEvent = (_, _, newText) -> onTextChange(this, newText);
            posEvent = (_, _, _) -> popup.hide();
            keyEvent = e -> {
                boolean success = false;
                if (e.getCode() == KeyCode.UP) {
                    success = moveSelectedIndex(this, true);
                }
                else if (e.getCode() == KeyCode.DOWN) {
                    success = moveSelectedIndex(this, false);
                }
                else if (e.getCode() == KeyCode.ENTER) {
                    success = onEntrySelected(this, getSelectedIndex(this));
                }

                if (onKeyPressed != null) onKeyPressed.accept(e);
                if (success) e.consume();
            };
        }

        /**
         * Gets the max count of lines, the completion popup should show at once.
         * @return The max count of lines, the completion popup should show at once.
         */
        public int getLineCount() {
            return lineCount;
        }

        /**
         * Sets the max count of lines, the completion popup should show at once.
         * @param lineCount The value to apply.
         * @return This instance.
         */
        public MetaData setLineCount(int lineCount) {
            this.lineCount = lineCount;
            return this;
        }

        /**
         * Sets the strings that should be suggested by the {@linkplain TextField} <i>(As a {@linkplain List})</i>.
         * @param completions The value to apply.
         * @return This instance.
         */
        public MetaData setCompletions(List<String> completions) {
            this.completions = new HashSet<>(completions);
            return this;
        }
        /**
         * Sets the strings that should be suggested by the {@linkplain TextField} <i>(As a vararg)</i>.
         * @param completions The value to apply.
         * @return This instance.
         */
        public MetaData setCompletions(String... completions) {
            this.completions = Arrays.stream(completions).collect(Collectors.toSet());
            return this;
        }
        /**
         * Sets the strings that should be suggested by the {@linkplain TextField} <i>(As a {@linkplain Set})</i>.
         * @param completions The value to apply.
         * @return This instance.
         */
        public MetaData setCompletions(Set<String> completions) {
            this.completions = completions;
            return this;
        }

        /**
         * Gets the strings that should be suggested by the TextField <i>(As a {@linkplain Set})</i>.
         * @return The strings that should be suggested by the TextField <i>(As a {@linkplain Set})</i>.
         */
        public Set<String> getCompletions() {
            return completions;
        }

        /**
         * Checks whether the search algorithm is case-sensitive.
         * @return Whether the search algorithm is case-sensitive.
         */
        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        /**
         * Sets whether the search algorithm is case-sensitive.
         * @param caseSensitive The value to apply.
         * @return This instance.
         */
        public MetaData setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        /**
         * Checks the pane should scroll automatically when selecting a new entry using the <b>arrow keys</b>.
         * @return Whether the pane should scroll automatically when selecting a new entry using the <b>arrow keys</b>.
         */
        public boolean isAutoScroll() {
            return autoScroll;
        }

        /**
         * Sets whether the pane should scroll automatically when selecting a new entry using the <b>arrow keys</b>.
         * @param autoScroll The value to apply.
         * @return This instance.
         */
        public MetaData setAutoScroll(boolean autoScroll) {
            this.autoScroll = autoScroll;
            return this;
        }

        /**
         * Gets the <i>({@linkplain Nullable nullable})</i> consumer called when auto completing the field <i>(Completed text as arg)</i>
         * @return The <i>({@linkplain Nullable nullable})</i> consumer called when auto completing the field.
         */
        public @Nullable Consumer<String> getOnAutoComplete() {
            return onAutoComplete;
        }

        /**
         * Sets the <i>({@linkplain Nullable nullable})</i> consumer called when auto completing the field <i>(Completed text as arg)</i>
         * @param onAutoComplete The value to apply.
         * @return This instance.
         */
        public MetaData setOnAutoComplete(@Nullable Consumer<String> onAutoComplete) {
            this.onAutoComplete = onAutoComplete;
            return this;
        }

        /**
         * Sets whether the popup can be hidden by pressing <b>ESC</b> key.
         * @param escapable The value to apply.
         * @return This instance.
         */
        public MetaData setEscapable(boolean escapable) {
            this.popup.setHideOnEscape(escapable);
            return this;
        }

        /**
         * Gets whether the popup can be hidden by pressing <b>ESC</b> key.
         * @return Whether the popup can be hidden by pressing <b>ESC</b> key.
         */
        public boolean isEscapable() {
            return this.popup.isHideOnEscape();
        }

        /**
         * Gets the color, matching parts in the completion should be highlighted in.
         * @return The color, matching parts in the completion should be highlighted in.
         */
        public Color getMatchHighlightColor() {
            return matchHighlightColor;
        }
        /**
         * Sets the color, matching parts in the completion should be highlighted in.
         * @param matchHighlightColor The value to apply.
         * @return This instance.
         */
        public MetaData setMatchHighlightColor(Color matchHighlightColor) {
            this.matchHighlightColor = matchHighlightColor;
            return this;
        }

        /**
         * Gets the <i>({@linkplain Nullable})</i> consumer used to hook additional <b>onKeyPressed</b> logic to the field without overriding the library's one.
         * @return The <i>({@linkplain Nullable})</i> consumer used to hook additional <b>onKeyPressed</b> logic to the field without overriding the library's one.
         */
        public @Nullable Consumer<? super KeyEvent> getOnKeyPressed() {
            return onKeyPressed;
        }

        /**
         * Sets the <i>({@linkplain Nullable})</i> consumer used to hook additional <b>onKeyPressed</b> logic to the field without overriding the library's one.
         * @param onKeyPressed The value to apply
         * @return This instance.
         */
        public MetaData setOnKeyPressed(@Nullable Consumer<? super KeyEvent> onKeyPressed) {
            this.onKeyPressed = onKeyPressed;
            return this;
        }
    }
}
