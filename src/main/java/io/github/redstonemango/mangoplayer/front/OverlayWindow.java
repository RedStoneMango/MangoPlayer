package io.github.redstonemango.mangoplayer.front;

import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for displaying an overlay window. In most cases, such a window can be treated like the normal {@linkplain Window} <i>(or its inheritor "{@linkplain Stage}")</i> but with the mayor difference, this window will always be visible to the user, even when focusing another app.<br><br>
 * As this is an inheritor of {@linkplain Popup} <i>(which inherits from {@linkplain Window})</i>, these classes' documentations should be referenced for further infos on certain methods.<br>
 * The Main differences between this class and the known JFX ones are:
 * <ol>
 *     <li>This window does not support scenes, so {@linkplain #setScene(Scene)} is not to be used at all, as it will throw an {@linkplain UnsupportedOperationException}. Instead {@linkplain #setRoot(Parent)} should be called to change the window content.</li>
 *     <li>While many developers use a {@linkplain Scene} inheritor to store view-dependent data, this class does not support scenes. As a workaround, the {@linkplain #metaData} function exists, allowing the dev to store java objects inside this window.</li>
 *     <li>Sizing methods ({@linkplain #setWidth(double)} and {@linkplain #setHeight(double)}) do not affect the window. The window itself is also not resizable by the user, instead it will always fit its {@linkplain Scene}'s size.</li>
 *     <li>Though this is a {@linkplain Popup} inheritor, {@linkplain #autoHideProperty() auto hide} and {@linkplain #hideOnEscapeProperty() hide on escape} methods will throw {@linkplain UnsupportedOperationException UnsupportedOperationExceptions}.</li>
 * </ol>
 * @author RedStoneMango
 */
public class OverlayWindow extends Popup {

    /**
     * String property storing the name of this window.
     * @see #nameProperty()
     */
    private final SimpleStringProperty name;
    /**
     * Object property storing the icon of this window.
     * @see #iconProperty()
     */
    private final ObjectProperty<Image> icon;
    /**
     * Object property storing the root node of this window.
     * @see #rootProperty()
     */
    private final ObjectProperty<Parent> root;
    /**
     * Object property storing the context menu factory of this window. The factory is a {@linkplain Supplier}, supplying a {@linkplain ContextMenu} to be shown when context clicking the title bar.<br>
     * The factory is called every time the menu is being shown what allows the user to change the menu content in real time based on their own back.
     * @see #rootProperty()
     */
    private final ObjectProperty<Supplier<ContextMenu>> contextMenuFactory;
    /**
     * Boolean property storing whether the window is titled.<br>
     * A titled window contains a top bar with the closing button, the window name, its icon, etc. An untitled window consists only of the stage content without any further decorations.
     * @see #titledProperty()
     */
    private final BooleanProperty titled;
    /**
     * Map storing identifier-{@linkplain MetaData data} pairs. These can be accessed using {@linkplain #loadMetaData(String, Class)} / {@linkplain #addMetaData(String, MetaData)} methods and can be used to store java objects inside this window, as a workaround for the scene-inheritor-method, which is not possible here as this class does not support scenes.
     * @see #loadMetaData(String, Class)
     * @see #addMetaData(String, MetaData)
     * @see #getMetaData()
     */
    private final Map<String, MetaData<?>> metaData;

    /**
     * The {@linkplain Label} displaying this window's name.
     * @see #name
     */
    private final Label nameLabel;
    /**
     * The {@linkplain ImageView} displaying this window's icon.
     * @see #icon
     */
    private final ImageView iconView;

    /**
     * Internal variable storing the x offset between cursor and window while dragging.
     */
    private double moveOffsetX = 0;
    /**
     * Internal variable storing the y offset between cursor and window while dragging.
     */
    private double moveOffsetY = 0;
    /**
     * Internal variable storing the currently showing {@linkplain ContextMenu}. This value might also be <code>null</code>.
     */
    private @Nullable ContextMenu showingContextMenu;

    /**
     * Parameterless constructor for this class.
     * @see #OverlayWindow(Parent, String, Image)
     */
    public OverlayWindow() {
        this(createEmptyRoot(), null, null);
    }
    /**
     * Constructor for this class, taking the scene as argument
     * @param root The {@linkplain Scene} for this window.
     * @see #OverlayWindow(Parent, String, Image)
     */
    public OverlayWindow(Parent root) {
        this(root, null, null);
    }
    /**
     * Constructor for this class, taking the name as argument
     * @param name The name for this window. This may also be <code>null</code>.
     * @see #OverlayWindow(Parent, String, Image)
     */
    public OverlayWindow(@Nullable String name) {
        this(createEmptyRoot(), name, null);
    }
    /**
     * Constructor for this class, taking the scene and name as arguments
     * @param root The {@linkplain Scene} for this window.
     * @param name The name for this window. This may also be <code>null</code>.
     * @see #OverlayWindow(Parent, String, Image)
     */
    public OverlayWindow(Parent root, @Nullable String name) {
        this(root, name, null);
    }
    /**
     * Constructor for this class, taking the icon as argument
     * @param icon The icon for this window as an {@linkplain Image} instance.
     * @see #OverlayWindow(Parent, String, Image)
     */
    public OverlayWindow(@Nullable Image icon) {
        this(createEmptyRoot(), null, icon);
    }
    /**
     * Constructor for this class, taking the scene and icon as arguments
     * @param root The {@linkplain Scene} for this window.
     * @param icon The icon for this window as an {@linkplain Image} instance.
     * @see #OverlayWindow(Parent, String, Image)
     */
    public OverlayWindow(Parent root, @Nullable Image icon) {
        this(root, null, icon);
    }
    /**
     * Constructor for this class, taking the name and icon as arguments
     * @param name The name for this window. This may also be <code>null</code>.
     * @param icon The icon for this window as an {@linkplain Image} instance.
     * @see #OverlayWindow(Parent, String, Image)
     */
    public OverlayWindow(@Nullable String name, @Nullable Image icon) {
        this(createEmptyRoot(), name, icon);
    }
    /**
     * Constructor for this class, taking the name and icon as arguments
     * @param name The name for this window. This may also be <code>null</code>.
     * @param icon The icon for this window as an {@linkplain Image} instance.
     * @see #OverlayWindow(Parent, String, Image)
     */
    public OverlayWindow(Parent root, @Nullable String name, @Nullable Image icon) {
        this(root, name, icon, false);
    }
    /**
     * Constructor for this class, taking the scene, name and icon as arguments
     * @param root The {@linkplain Scene} for this window.
     * @param name The name for this window. This may also be <code>null</code>.
     * @param icon The icon for this window as an {@linkplain Image} instance.
     * @param suppressBackground Whether to stop the class from setting the window's background color to white. This should be <code>false</code> in most cases, but when using custom stylesheets applying background color changes, this value is recommended to be set to <code>true</code>
     */
    public OverlayWindow(Parent root, @Nullable String name, @Nullable Image icon, boolean suppressBackground) {
        this.name = new SimpleStringProperty(name);
        this.icon = new SimpleObjectProperty<>(icon);
        this.root = new SimpleObjectProperty<>(root);
        this.contextMenuFactory = new SimpleObjectProperty<>(() -> null);
        this.titled = new SimpleBooleanProperty(true);
        this.metaData = new HashMap<>();

        setAutoHide(false);
        setHideOnEscape(false);
        setIcon(icon);

        VBox layer = new VBox();

        BorderPane top = new BorderPane();
        top.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                if (showingContextMenu == null) {
                    showingContextMenu = contextMenuFactory.get().get();
                    if (showingContextMenu == null) return; // if factory returns 'null', cancel
                    showingContextMenu.show(this, e.getScreenX(), e.getScreenY());
                }
                else {
                    showingContextMenu.hide();
                    showingContextMenu = null;
                }
            }
        });
        top.setPrefHeight(30);
        top.setStyle("-fx-background-color: dimgray;");
        top.setOnMousePressed(e -> {
            moveOffsetX = e.getScreenX() - getX();
            moveOffsetY = e.getScreenY() - getY();
        });
        top.setOnMouseDragged(e -> {
            setX(e.getScreenX() - moveOffsetX);
            setY(e.getScreenY() - moveOffsetY);
        });
        Label exitLabel = new Label("x");
        exitLabel.setMouseTransparent(true);
        exitLabel.setTextFill(Color.WHITE);
        exitLabel.setPadding(new Insets(0, 7, 3.45, 0));
        Circle exitBackground = new Circle(10, Color.GRAY);
        exitBackground.setOnMouseClicked(_ -> {
            WindowEvent event = new WindowEvent(this, WindowEvent.WINDOW_HIDING);
            if (getOnCloseRequest() != null) getOnCloseRequest().handle(event);
            if (!event.isConsumed()) {
                hide();
            }
        });
        exitBackground.setOnMouseExited(_ -> exitBackground.setFill(Color.GRAY));
        exitBackground.setOnMouseEntered(_ -> exitBackground.setFill(Color.DARKGRAY));
        exitBackground.setTranslateX(-3.5);
        StackPane exitPane = new StackPane();
        exitPane.resize(30, 30);
        exitPane.getChildren().addAll(exitBackground, exitLabel);
        top.setRight(exitPane);

        nameLabel = new Label(name == null ? "" : name);
        layer.setAlignment(Pos.CENTER);
        nameLabel.setPadding(new Insets(0, 20, 3, 20));
        nameLabel.setTextFill(Color.LIGHTGRAY);
        top.setCenter(nameLabel);

        iconView = new ImageView();
        iconView.setPreserveRatio(true);
        iconView.setFitWidth(20);
        iconView.setFitWidth(20);
        iconView.setY(20);
        iconView.setImage(icon);
        Label iconContainer = new Label();
        iconContainer.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        iconContainer.setGraphic(iconView);
        iconContainer.setPadding(new Insets(5, 0, 0, 5));
        top.setLeft(iconContainer);

        if (!suppressBackground) {
           root.setStyle("-fx-background-color: white;");
        }

        layer.getChildren().addAll(top, root);
        getContent().add(layer);

        this.name.addListener((_, _, newName) -> nameLabel.setText(newName));
        this.icon.addListener((_, _, newIcon) -> {
            iconView.setImage(newIcon);
            setIcon(newIcon);
        });
        this.root.addListener((_, _, newRoot) -> {
            layer.getChildren().removeLast();
            layer.getChildren().addLast(newRoot);
        });
        this.titled.addListener((_, _, isTitled) -> {
            if (isTitled) {
                if (!layer.getChildren().contains(top)) layer.getChildren().addFirst(top);
            }
            else {
                layer.getChildren().remove(top);
            }
        });
        autoHideProperty().addListener((_, _, _) -> {
            setAutoHide(false);
            throw new UnsupportedOperationException();
        });
        consumeAutoHidingEventsProperty().addListener((_, _, _) -> {
            setConsumeAutoHidingEvents(false);
            throw new UnsupportedOperationException();
        });
        hideOnEscapeProperty().addListener((_, _, _) -> {
            setHideOnEscape(false);
            throw new UnsupportedOperationException();
        });
    }

    /**
     * Library-internal method creating a blank {@linkplain Parent}.<br>
     * This is used for creating empty windows in the constructors, that do not take a scene argument, like {@linkplain #OverlayWindow()};
     * @return The blank parent.
     */
    private static Parent createEmptyRoot() {
        return new Pane();
    }

    /**
     * Gets the value of the property {@linkplain #name}.
     * @return The value of the property {@linkplain #name}.
     */
    public @Nullable String getName() {
        return name.get();
    }
    /**
     * Gets the {@linkplain #name} property.
     * @return The {@linkplain #name} property.
     */
    public SimpleStringProperty nameProperty() {
        return name;
    }
    /**
     * Sets the value of the property {@linkplain #name}.
     * @param name The new value of the property {@linkplain #name}.
     */
    public void setName(@Nullable String name) {
        this.name.set(name);
    }

    /**
     * Gets the value of the property {@linkplain #icon}.
     * @return The value of the property {@linkplain #icon}.
     */
    public @Nullable Image getIcon() {
        return icon.get();
    }
    /**
     * Gets the {@linkplain #icon} property.
     * @return The {@linkplain #icon} property.
     */
    public ObjectProperty<Image> iconProperty() {
        return icon;
    }
    /**
     * Sets the value of the property {@linkplain #icon}.
     * @param icon The new value of the property {@linkplain #icon}.
     */
    public void setIcon(@Nullable Image icon) {
        this.icon.set(icon);
    }

    /**
     * Gets the value of the property {@linkplain #root}.
     * @return The value of the property {@linkplain #root}.
     */
    public Parent getRoot() {
        return root.get();
    }
    /**
     * Gets the {@linkplain #root} property.
     * @return The {@linkplain #root} property.
     */
    public ObjectProperty<Parent> rootProperty() {
        return root;
    }
    /**
     * Sets the value of the property {@linkplain #root}.
     * @param root The new value of the property {@linkplain #root}.
     */
    public void setRoot(Parent root) {
        this.root.set(root);
    }

    /**
     * Gets the value of the property {@linkplain #contextMenuFactory}.
     * @return The value of the property {@linkplain #contextMenuFactory}.
     */
    public Supplier<ContextMenu> getContextMenuFactory() {
        return contextMenuFactory.get();
    }
    /**
     * Gets the {@linkplain #contextMenuFactory} property.
     * @return The {@linkplain #contextMenuFactory} property.
     */
    public ObjectProperty<Supplier<ContextMenu>> contextMenuFactoryProperty() {
        return contextMenuFactory;
    }
    /**
     * Sets the value of the property {@linkplain #contextMenuFactory}.
     * @param contextMenuFactory The new value of the property {@linkplain #contextMenuFactory}.
     */
    public void setContextMenuFactory(Supplier<ContextMenu> contextMenuFactory) {
        this.contextMenuFactory.set(contextMenuFactory);
    }

    /**
     * Gets the value of the property {@linkplain #titled}.
     * @return The value of the property {@linkplain #titled}.
     */
    public boolean isTitled() {
        return titled.get();
    }
    /**
     * Gets the {@linkplain #titled} property.
     * @return The {@linkplain #titled} property.
     */
    public BooleanProperty titledProperty() {
        return titled;
    }
    /**
     * Sets the value of the property {@linkplain #titled}.
     * @param titled The new value of the property {@linkplain #titled}.
     */
    public void setTitled(boolean titled) {
        this.titled.set(titled);
    }

    /**
     * Gets an unmodifiable view of the {@linkplain #metaData} map.<br>
     * When trying to access a specific metadata, consider using {@linkplain #loadMetaData(String, Class)} instead.
     * @return An unmodifiable view of the {@linkplain #metaData} map.
     */
    public Map<String, MetaData<?>> getMetaData() {
        return Collections.unmodifiableMap(metaData);
    }
    /**
     * Loads an existing {@linkplain MetaData MetaData} entry based on its identifier and the type of value stored in the metadata.<br>
     * The method returns the data if it exists and holds the given datatype. Otherwise <code>null</code> will be returned.
     * @param identifier The identifier of the metadata to load. Based on this value, the data will be resolved.
     * @param clazz The class of the object held by the metadata that is to be accessed.
     * @return The loaded metadata, or <code>null</code> if the identifier does not exist / the class does not match.
     * @param <T> The type of java object held by the metadata.
     */
    public <T> @Nullable MetaData<T> loadMetaData(String identifier, Class<T> clazz) {
        MetaData<?> raw = metaData.get(identifier);
        if (raw == null) return null;
        if (clazz.isInstance(raw.value)) {
            @SuppressWarnings("unchecked")
            MetaData<T> result = (MetaData<T>) raw;
            return result;
        }
        return null;
    }
    /**
     * Checks whether a {@linkplain MetaData MetaData} with a given identifier exists.
     * @param identifier The identifier to check for.
     * @return Whether a metadata with the given identifier exists.
     */
    public boolean hasMetaData(String identifier) {
        return metaData.containsKey(identifier);
    }
    /**
     * Checks whether a given {@linkplain MetaData MetaData} exists.
     * @param metaData The metaData to check for.
     * @return Whether the given metaData exists.
     */
    public boolean hasMetaData(MetaData<?> metaData) {
        return this.metaData.containsValue(metaData);
    }
    /**
     * Adds a {@linkplain MetaData MetaData} to the window by taking an identifier and a {@linkplain MetaData MetaData&lt;?&gt;} object.<br>
     * If the action was successful (there was no older data with the same identifier), <code>true</code> will be returned, <code>false</code> otherwise.
     * @param identifier The identifier to save the data under.
     * @param metaData The metadata to save.
     * @return Whether the action was successful.
     * @see #addMetaData(String, Object)
     */
    public boolean addMetaData(String identifier, MetaData<?> metaData) {
        if (this.metaData.containsKey(identifier)) return false;
        this.metaData.put(identifier, metaData);
        return true;
    }
    /**
     * Adds a {@linkplain MetaData MetaData} to the window by taking an identifier and the value to be stored in this object.<br>
     * If the action was successful (there was no older data with the same identifier), <code>true</code> will be returned, <code>false</code> otherwise.
     * @param identifier The identifier to save the data under.
     * @param metaDataContent The value to be stored inside the new metadata.
     * @return Whether the action was successful.
     * @implNote Basically this is a shortcut for calling {@linkplain #addMetaData(String, MetaData)}, as:<pre>
     *   {@code
     *    overlayWindow.addMetaData(identifier, metaData);
     *    // The above is equal to the below
     *    overlayWindow.addMetaData(identifier, new MetaData<>(metaDataContent));
     *   }
     * </pre>
     * @see #addMetaData(String, MetaData)
     */
    public boolean addMetaData(String identifier, Object metaDataContent) {
        return addMetaData(identifier, new MetaData<>(metaDataContent));
    }
    /**
     * Removes a {@linkplain MetaData MetaData} from this window based on its identifier.<br>
     * If the data has existed before, <code>true</code> will be returned, <code>false</code> otherwise.
     * @param identifier The identifier of the metadata to remove.
     * @return Whether the data has existed before.
     * @see #removeMetaData(String, MetaData)
     */
    public boolean removeMetaData(String identifier) {
        return metaData.remove(identifier) != null;
    }
    /**
     * Removes a {@linkplain MetaData MetaData} from this window, but only if the identifier and the data instance itself match.<br>
     * If the data was removed successfully, <code>true</code> will be returned, <code>false</code> otherwise.
     * @param identifier The identifier of the metadata to remove.
     * @return Whether the action was successful.
     */
    public boolean removeMetaData(String identifier, MetaData<?> metaData) {
        return this.metaData.remove(identifier, metaData);
    }

    /**
     * Class that can be used to store java objects inside a {@linkplain OverlayWindow}, as a workaround for the scene-inheritor-method, which is not possible here as the window does not support scenes.
     * @param <T> The type of java object stored in this class.
     */
    public static class MetaData <T> {
        /**
         * The value stored in this instance.
         */
        private T value;
        /**
         * Constructor for this class, taking a value to store as argument.
         * @param value The value to store.
         */
        public MetaData(T value) {
            this.value = value;
        }
        /**
         * Returns the current value.
         * @return The current value.
         */
        public T get() {
            return value;
        }
        /**
         * Changes the value
         * @param value The new value
         */
        public void set(T value) {
            this.value = value;
        }
    }
}
