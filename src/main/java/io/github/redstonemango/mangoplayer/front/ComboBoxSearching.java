package io.github.redstonemango.mangoplayer.front;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class that enhances a JavaFX {@link ComboBox} with live filtering/searching
 * functionality based on user input. It supports dynamic item filtering while typing
 * and maintains selection tracking.
 *
 * @author RedStoneMango
 */
public class ComboBoxSearching {

    /**
     * Internal map associating each {@link ComboBox} instance with its corresponding
     * {@link TypedObjects} data, allowing the tracking of original options and selected item.
     */
    private static final Map<ComboBox<?>, TypedObjects<?>> optionsMap = new IdentityHashMap<>();

    /**
     * Applies search and filtering behavior to the given {@link ComboBox}.
     *
     * @param comboBox the ComboBox to enhance with search behavior
     * @param type     the class type of items in the ComboBox, which must implement {@link ISearchComparable}
     * @param <T>      the type of items in the ComboBox
     */
    public static <T extends ISearchComparable> void apply(ComboBox<T> comboBox, Class<T> type) {
        ObservableList<T> data = comboBox.getItems();
        ObjectProperty<T> selection = new SimpleObjectProperty<>();
        optionsMap.put(comboBox, new TypedObjects<>(data, selection, type));

        comboBox.setEditable(false);
        comboBox.setFocusTraversable(false);
        comboBox.setValue(null);
        comboBox.getSelectionModel().select(null);
        comboBox.showingProperty().addListener((_, _, isShowing) -> {
            comboBox.setEditable(isShowing);
            if (isShowing) {
                comboBox.setItems(data);
                comboBox.getEditor().setText(Objects.requireNonNullElse(selection.getValue(), "").toString());
                comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
                Platform.runLater(comboBox::requestFocus);
            } else {
                T item = comboBox.getSelectionModel().getSelectedItem();
                selection.setValue(item);
                comboBox.getEditor().setText(null);
            }
        });
        selection.addListener((_, _, newSelection) -> comboBox.getSelectionModel().select(newSelection));

        comboBox.setOnShowing(_ -> {
            Skin<?> skin = comboBox.getSkin();
            if (skin instanceof ComboBoxListViewSkin<?> cbSkin) {
                @SuppressWarnings("unchecked")
                ListView<String> listView = (ListView<String>) cbSkin.getPopupContent();
                listView.setMaxWidth(comboBox.getWidth());
                listView.setPrefWidth(comboBox.getWidth());
                listView.setMaxHeight(200);
                listView.setPrefHeight(200);
                listView.setMinHeight(200);
            }
        });

        comboBox.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<>() {

            private boolean moveCaretToPos = false;
            private int caretPos;

            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                    caretPos = -1;
                    if (comboBox.getEditor().getText() != null) {
                        moveCaret(comboBox.getEditor().getText().length());
                    }
                    return;
                } else if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                    if (comboBox.getEditor().getText() != null) {
                        moveCaretToPos = true;
                        caretPos = comboBox.getEditor().getCaretPosition();
                    }
                } else if (event.getCode() == KeyCode.ENTER) {
                    return;
                }

                if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT ||
                        event.getCode().equals(KeyCode.SHIFT) || event.getCode().equals(KeyCode.CONTROL) ||
                        event.isControlDown() || event.isMetaDown() ||
                        event.getCode() == KeyCode.HOME || event.getCode() == KeyCode.END ||
                        event.getCode() == KeyCode.TAB) {
                    return;
                }

                ObservableList<T> list = FXCollections.observableArrayList();
                for (T aData : data) {
                    if (aData != null && comboBox.getEditor().getText() != null &&
                            aData.matches(comboBox.getEditor().getText())) {
                        list.add(aData);
                    }
                }

                String t = comboBox.getEditor().getText() != null ? comboBox.getEditor().getText() : "";

                comboBox.setItems(list);
                comboBox.getEditor().setText(t);
                if (!moveCaretToPos) {
                    caretPos = -1;
                }
                moveCaret(t.length());
            }

            private void moveCaret(int textLength) {
                if (caretPos == -1) {
                    comboBox.getEditor().positionCaret(textLength);
                } else {
                    comboBox.getEditor().positionCaret(caretPos);
                }
                moveCaretToPos = false;
            }
        });
    }

    /**
     * Retrieves the full unfiltered list of options the ComboBox can display, ready to be
     * populated during runtime.
     *
     * @param box  the ComboBox to query
     * @param type the class type of items in the ComboBox
     * @param <T>  the type of items
     * @return the original options list, or {@code null} if none is found or type doesn't match
     */
    public static <T extends ISearchComparable> @Nullable ObservableList<T> getOptions(ComboBox<T> box, Class<T> type) {
        TypedObjects<?> raw = optionsMap.get(box);
        if (raw != null && raw.type.equals(type)) {
            @SuppressWarnings("unchecked")
            ObservableList<T> typedList = (ObservableList<T>) raw.options();
            return typedList;
        }
        return null;
    }

    /**
     * Returns a read-only property that tracks the current selection in the ComboBox.
     *
     * @param box  the ComboBox to observe
     * @param type the class type of items in the ComboBox
     * @param <T>  the type of items
     * @return a read-only property reflecting the selected item, or {@code null} if not available
     */
    public static <T extends ISearchComparable> @Nullable ObjectProperty<T> selectionProperty(ComboBox<T> box, Class<T> type) {
        TypedObjects<?> raw = optionsMap.get(box);
        if (raw != null && raw.type.equals(type)) {
            @SuppressWarnings("unchecked")
            ObjectProperty<T> typedProp = (ObjectProperty<T>) raw.selectionProperty();
            return typedProp;
        }
        return null;
    }

    /**
     * Gets the current selection for the given ComboBox if search behavior has been applied.
     *
     * @param box  the ComboBox to check
     * @param type the expected type of item
     * @param <T>  the type of items
     * @return the currently selected item, or {@code null} if not available
     */
    public static <T extends ISearchComparable> @Nullable T getSelection(ComboBox<T> box, Class<T> type) {
        TypedObjects<?> raw = optionsMap.get(box);
        if (raw != null && raw.type.equals(type)) {
            @SuppressWarnings("unchecked")
            T typedVal = (T) raw.selectionProperty.getValue();
            return typedVal;
        }
        return null;
    }

    /**
     * Sets a new selection for the given ComboBox if search behavior has been applied.
     *
     * @param box  the ComboBox to check
     * @param type the expected type of item
     * @param selectedItem the item to select
     * @param <T>  the type of items
     * @return whether search behavior has been applied to the ComboBox and whether the given type matches the one
     * used when behavior has been applied.
     */
    public static <T extends ISearchComparable> boolean setSelection(ComboBox<T> box, Class<T> type, T selectedItem) {
        TypedObjects<?> raw = optionsMap.get(box);
        if (raw != null && raw.type.equals(type)) {
            @SuppressWarnings("unchecked")
            ObjectProperty<T> typedProp = (ObjectProperty<T>) raw.selectionProperty();
            typedProp.setValue(selectedItem);
            return true;
        }
        return false;
    }

    /**
     * Interface to be implemented by objects that support matching against user-typed input.
     * Items in the ComboBox must implement this interface to enable search functionality.<br>
     * <br>
     * It is also recommended that items implement the {@link #toString()} method in order to
     * specify the text shown in the filter field.<br>
     * <br>
     * Custom CellFactories have to be applied manually.
     */
    public interface ISearchComparable {

        /**
         * Checks if the current item matches the given typed text.
         *
         * @param typedText the text entered by the user
         * @return {@code true} if this item matches the text; {@code false} otherwise
         */
        boolean matches(String typedText);
    }

    /**
     * Container record that stores metadata associated with a ComboBox.
     *
     * @param <T> the type of item stored in the ComboBox
     * @param options           the original full list of options for the ComboBox
     * @param selectionProperty  a wrapper holding the currently selected item
     * @param type              the class type of the items
     */
    public record TypedObjects<T extends ISearchComparable>(
            ObservableList<T> options,
            ObjectProperty<T> selectionProperty,
            Class<T> type) {
    }
}
