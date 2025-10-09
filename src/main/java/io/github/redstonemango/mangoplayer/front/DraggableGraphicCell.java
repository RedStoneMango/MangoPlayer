package io.github.redstonemango.mangoplayer.front;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Utility class for implementing a drag-and-drop-based sorting behavior in a {@link ListView} by using a custom implementation of the {@link ListCell} that can be set using the view's {@link ListView#setCellFactory(Callback)} method
 * @param <T> The type of element stored by the {@link ListCell}
 * @author Fabian Krohn
 */
public class DraggableGraphicCell<T> extends ListCell<T> {
    /**
     * An instance of {@link GraphicData} containing the lastest result of the {@link #graphicFunction}.
     */
    private GraphicData graphicData;
    /**
     * A {@link Function} supplying an instance of {@link GraphicData} based on a list item.
     * @see #getGraphicFunction()
     */
    private Function<T, GraphicData> graphicFunction;
    /**
     * A {@link Function} supplying an {@link Image} based on an item in the list. The image will be used as {@linkplain Dragboard#getDragView() drag view} <i>(The image to be displayed next to the cursor while dragging)</i>.<br>
     * This value may be <code>null</code> if the user decides to use the system-default drag view.
     * @see #getGraphicFunction()
     */
    private @Nullable Function<T, Image> dragViewFunction;
    /**
     * A {@link Function} supplying a {@link Boolean} that represents whether scrolling should be possible. The function has the current {@link ListView} as parameter.
     * @see #getAllowSortingFunction()
     */
    private Function<ListView<T>, Boolean> allowSortingFunction;
    /**
     * The instance of the current cell. This is being queried in internal lambda expressions to compare the drag source to this cell.
     */
    private final DraggableGraphicCell<T> thisCell;
    /**
     * This class's {@link EventListener}.
     * @see #getEvents()
     */
    private EventListener events;
    /**
     * The {@link LineData} object containing information on how to draw the indicator line.<br>
     * @see #getLineData()
     */
    private LineData lineData;
    /**
     * Internal value storing the current instance of {@link Line} that is being displayed as the indicator line.
     */
    private @Nullable Line currentLine = null;
    /**
     * Interna value storing whether sorting is currently allowed. This value is received using the {@link #allowSortingFunction} and is updated in every {@linkplain EventListener#onDragDetected(MouseEvent) drag start}.
     */
    private boolean allowSorting = true;
    private boolean upperPart = true;
    /**
     * A final {@link DataFormat} for transferring the dragged item inside a {@link Dragboard}.
     */
    private static final DataFormat DRAGGED_ITEM = new DataFormat("application/x-java-serialized-object");


    /**
     * Constructor for this class. This takes a {@link Function} supplying an instance of {@link GraphicData} based on a list item.<br>
     * For more information on that function's use, refer to the {@linkplain  GraphicData GraphicData-documentation}.
     * @param graphicFunction The function supplying {@link GraphicData} based on an item in the list.
     * @see #DraggableGraphicCell(Function, Function, Function, EventListener, LineData)
     */
    public DraggableGraphicCell(Function<T, GraphicData> graphicFunction) {
        this(graphicFunction, null, _ -> true, new EventAdapter(){}, new LineData(Color.RED));
    }

    /**
     * Constructor for this class. This takes a {@link Function} supplying an instance of {@link GraphicData} based on a list item and an implementation of {@link EventListener} to respond to various actions executed by this class.<br>
     * For more information on that function's use, refer to the {@linkplain  GraphicData GraphicData-documentation}.
     * @param graphicFunction The function supplying {@link GraphicData} based on an item in the list.
     * @param events An implementation of {@link EventListener}.
     * @see #DraggableGraphicCell(Function, Function, Function, EventListener, LineData)
     */
    public DraggableGraphicCell(Function<T, GraphicData> graphicFunction, EventListener events) {
        this(graphicFunction, null, _ -> true, events, new LineData(Color.RED));
    }

    /**
     * Constructor for this class. This takes a {@link Function} supplying an instance of {@link GraphicData} based on a list item and a {@link LineData} object containing information on how to draw the indicator line.<br>
     * For more information on that function's use, refer to the {@linkplain  GraphicData GraphicData-documentation}.
     * @param graphicFunction The function supplying {@link GraphicData} based on an item in the list.
     * @param lineData The {@link LineData} object containing information on how to draw the indicator line.
     * @see #DraggableGraphicCell(Function, Function, Function, EventListener, LineData)
     */
    public DraggableGraphicCell(Function<T, GraphicData> graphicFunction, LineData lineData) {
        this(graphicFunction, null, _ -> true, new EventAdapter() {}, lineData);
    }

    /**
     * Constructor for this class. This takes a {@link Function} supplying an instance of {@link GraphicData} based on a list item and another function supplying an {@link Image} to be used as drag view.<br>
     * For more information on that function's use, refer to the {@linkplain  GraphicData GraphicData-documentation}.
     * @param graphicFunction The function supplying {@link GraphicData} based on an item in the list.
     * @param dragViewFunction The function supplying an {@link Image} based on an item in the list. The image will be used as {@linkplain Dragboard#getDragView() drag view} <i>(The image to be displayed next to the cursor while dragging)</i>
     * @see #DraggableGraphicCell(Function, Function, Function, EventListener, LineData)
     */
    public DraggableGraphicCell(Function<T, GraphicData> graphicFunction, @Nullable Function<T, Image> dragViewFunction) {
        this(graphicFunction, dragViewFunction, _ -> true, new EventAdapter() {}, new LineData(Color.RED));
    }

    /**
     * Constructor for this class. This takes a {@link Function} supplying an instance of {@link GraphicData} based on a list item, another function supplying an {@link Image} to be used as drag view and a function stating whether sorting shall be allowed (by taking this list view's instance).<br>
     * For more information on that function's use, refer to the {@linkplain  GraphicData GraphicData-documentation}.
     * @param graphicFunction The function supplying {@link GraphicData} based on an item in the list.
     * @param dragViewFunction The function supplying an {@link Image} based on an item in the list. The image will be used as {@linkplain Dragboard#getDragView() drag view} <i>(The image to be displayed next to the cursor while dragging)</i>
     * @param allowSortingFunction The function stating whether sorting shall be allowed (by taking this list view's instance)
     * @see #DraggableGraphicCell(Function, Function, Function, EventListener, LineData)
     */
    public DraggableGraphicCell(Function<T, GraphicData> graphicFunction, @Nullable Function<T, Image> dragViewFunction, Function<ListView<T>, Boolean> allowSortingFunction) {
        this(graphicFunction, dragViewFunction, allowSortingFunction, new EventAdapter() {}, new LineData(Color.RED));
    }

    /**
     * Constructor for this class. This takes a {@link Function} supplying an instance of {@link GraphicData} based on a list item, another function supplying an {@link Image} to be used as drag view, a function stating whether sorting shall be allowed (by taking this list view's instance), an implementation of {@link EventListener} to respond to various actions executed by this class and a {@link LineData} object containing information on how to draw the indicator line.<br>
     * For more information on the first function's use, refer to the {@linkplain  GraphicData GraphicData-documentation}.
     * @param graphicFunction The function supplying {@link GraphicData} based on an item in the list.
     * @param dragViewFunction The function supplying an {@link Image} based on an item in the list. The image will be used as {@linkplain Dragboard#getDragView() drag view} <i>(The image to be displayed next to the cursor while dragging)</i>
     * @param allowSortingFunction The function stating whether sorting shall be allowed (by taking this list view's instance)
     * @param events The implementation of {@link EventListener}, allowing the developer to respond to various action executed by this class.
     * @param lineData The {@link LineData} object containing information on how to draw the indicator line.
     * @see #DraggableGraphicCell(Function)
     * @see #DraggableGraphicCell(Function, LineData)
     * @see #DraggableGraphicCell(Function, Function, Function, EventListener, LineData)
     * @see #DraggableGraphicCell(Function, Function)
     * @see #DraggableGraphicCell(Function, EventListener)
     */
    public DraggableGraphicCell(Function<T, GraphicData> graphicFunction, @Nullable Function<T, Image> dragViewFunction, Function<ListView<T>, Boolean> allowSortingFunction, EventListener events, LineData lineData) {
        this.allowSortingFunction = allowSortingFunction;
        thisCell = this;
        this.graphicFunction = graphicFunction;
        this.events = events;
        this.lineData = lineData;
        this.dragViewFunction = dragViewFunction;

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        setOnDragDetected(event -> {
            if (getItem() == null || graphicData == null) return;
            allowSorting = allowSortingFunction.apply(getListView());
            if (!allowSorting) return;

            Node intersectedNode = event.getPickResult().getIntersectedNode();
            if (!graphicData.canDragAtNode(intersectedNode)) return;

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(DRAGGED_ITEM, getItem());
            dragboard.setContent(content);

            if (thisCell.dragViewFunction != null) {
                Image dragImage = thisCell.dragViewFunction.apply(getItem());
                if (dragImage != null) {
                    dragboard.setDragView(dragImage);
                }
            }

            event.consume();
            events.onDragDetected(event);
        });

        setOnDragOver(event -> {
            if (!allowSorting) return;
            if (event.getGestureSource() != thisCell && event.getDragboard().hasContent(DRAGGED_ITEM) && getIndex() < getListView().getItems().size()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            boolean expectedUpperPart = isUpperPart(event, thisCell);
            if (expectedUpperPart != upperPart) {
                upperPart = expectedUpperPart;
                updateLine();
            }
            event.consume();
            events.onDragOver(event);
        });

        setOnDragEntered(event -> {
            allowSorting = allowSortingFunction.apply(getListView());
            if (!allowSorting) return;
            updateLine();
            @SuppressWarnings("unchecked")
            T draggedItem = (T) event.getDragboard().getContent(DRAGGED_ITEM);
            ObservableList<T> items = getListView().getItems();
            int draggedIdx = items.indexOf(draggedItem);
            if (draggedIdx != getIndex() && event.getDragboard().hasContent(DRAGGED_ITEM) && getIndex() < getListView().getItems().size()) {
                setOpacity(0.5);
            }
            events.onDragEntered(event);
        });

        setOnDragExited(event -> {
            if (!allowSorting) return;
            getChildren().removeIf(node -> node == currentLine);
            if (event.getGestureSource() != thisCell && event.getDragboard().hasContent(DRAGGED_ITEM) && getIndex() < getListView().getItems().size()) {
                setOpacity(1);
                currentLine = null;
            }
            events.onDragExited(event);
        });

        setOnDragDropped(event -> {
            if (!allowSorting) return;
            if (getIndex() > getListView().getItems().size()) return;


            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(DRAGGED_ITEM)) {
                @SuppressWarnings("unchecked")
                T draggedItem = (T) db.getContent(DRAGGED_ITEM);

                ObservableList<T> items = getListView().getItems();
                int draggedIdx = items.indexOf(draggedItem);
                int thisIdx = getIndex();

                if (thisIdx < draggedIdx) { // Item moved upwards
                    thisIdx++;
                }

                if (draggedIdx >= 0 && thisIdx >= 0) {
                    items.remove(draggedIdx);
                    int targetIndex = thisIdx - 1;
                    if (!upperPart) targetIndex ++;
                    items.add(targetIndex, draggedItem);
                    success = true;
                    getListView().getSelectionModel().select(draggedItem);
                }
            }

            event.setDropCompleted(success);
            event.consume();
            events.onDragDropped(event);
        });

        setOnDragDone(event -> {
            if (!allowSorting) return;
            event.consume();
            events.onDragDone(event);
        });
    }

    /**
     * Utility method that checks whether a {@link DragEvent} happens in the upper or the lower part of a {@link ListCell} <i>(Above or below it's half)</i>.<br>
     * Returns <code>true</code> if the event happens in the upper part, <code>false</code> otherwise.
     * @param event The {@link DragEvent} to check for.
     * @param cell The {@link ListCell} to check for. This has to be a part of a visible scene in order for this method to work.
     * @return <code>true</code> if the {@link DragEvent}'s cursor is above the {@link ListCell}'s half, <code>false</code> otherwise.
     */
    private static boolean isUpperPart(DragEvent event, ListCell<?> cell) {
        double mouseY = event.getScreenY();
        double cellY = cell.localToScreen(0, 0).getY();
        double cellHalfY = cellY + cell.getHeight() / 2;
        return mouseY < cellHalfY;
    }

    /**
     * Updates the indicator line. This method is to be called every time the line needs tp refresh, i.e., when the drag enters the cell or the hovered part changes (cf. {@link #isUpperPart(DragEvent, ListCell)}).<br>
     * This method takes the current value of {@link #upperPart} for the calculations, but does not automatically update it. The value is expected to be correct when the method is called.
     * @implNote This method works by first removing the last existing calculator line from the cell's children, then setting the new Line's Y coordinates to either <code>0</code> (if we are in the upper part) or <code>this.getHeight()</code> (if we are in the lower part) and finally adding the new line to the cell's children.
     */
    private void updateLine() {
        getChildren().removeIf(node -> node == currentLine);
        currentLine = new Line();
        currentLine.setStartX(0);
        currentLine.setEndX(getWidth());
        if (upperPart) {
            currentLine.setStartY(0);
            currentLine.setEndY(0);
        }
        else {
            currentLine.setStartY(getHeight());
            currentLine.setEndY(getHeight());
        }
        lineData.applyFor(currentLine);
        getChildren().add(currentLine);
    }

    /**
     * Override for {@link Cell#updateItem(Object, boolean)}. This implementation calls the {@link #graphicFunction} and sets the cell's front to its {@linkplain GraphicData#graphic front}
     * @param item The item inside the cell.
     * @param empty Whether the cell is empty.
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setPadding(new Insets(0));
        if (empty || item == null) {
            setGraphic(null);
        } else {
            graphicData = graphicFunction.apply(item);
            setGraphic(graphicData.getGraphic());
        }

        events.onItemUpdate(this, item, empty);
    }

    /**
     * Gets the current value of {@link #lineData}
     * @return The current value of {@link #lineData}
     * @see #setLineData(LineData)
     */
    public LineData getLineData() {
        return lineData;
    }
    /**
     * Sets a new value for {@link #lineData}
     * @param lineData The new value for {@link #lineData}
     * @see #getLineData()
     */
    public void setLineData(LineData lineData) {
        this.lineData = lineData;
    }
    /**
     * Gets the current value of {@link #graphicFunction}
     * @return The current value of {@link #graphicFunction}
     * @see #setGraphicFunction(Function)
     */
    public Function<T, GraphicData> getGraphicFunction() {
        return graphicFunction;
    }
    /**
     * Sets a new value for {@link #graphicFunction}
     * @param graphicFunction The new value for {@link #graphicFunction}
     * @see #getGraphicFunction()
     */
    public void setGraphicFunction(Function<T, GraphicData> graphicFunction) {
        this.graphicFunction = graphicFunction;
    }
    /**
     * Gets the current value of {@link #events}
     * @return The current value of {@link #events}
     * @see #setEvents(EventListener)
     */
    public EventListener getEvents() {
        return events;
    }
    /**
     * Sets a new value for {@link #events}
     * @param events The new value for {@link #events}
     * @see #getEvents()
     */
    public void setEvents(EventListener events) {
        this.events = events;
    }
    /**
     * Gets the current value of {@link #dragViewFunction}
     * @return The current value of {@link #dragViewFunction}
     * @see #setDragViewFunction(Function)
     */
    public @Nullable Function<T, Image> getDragViewFunction() {
        return dragViewFunction;
    }
    /**
     * Sets a new value for {@link #dragViewFunction}
     * @param dragViewFunction The new value for {@link #dragViewFunction}
     * @see #getDragViewFunction()
     */
    public void setDragViewFunction(@Nullable Function<T, Image> dragViewFunction) {
        this.dragViewFunction = dragViewFunction;
    }
    /**
     * Gets the current value of {@link #allowSortingFunction}
     * @return The current value of {@link #allowSortingFunction}
     * @see #setAllowSortingFunction(Function)
     */
    public Function<ListView<T>, Boolean> getAllowSortingFunction() {
        return allowSortingFunction;
    }
    /**
     * Sets a new value for {@link #allowSorting}
     * @param allowSortingFunction The new value for {@link #allowSorting}
     * @see #getAllowSortingFunction()
     */
    public void setAllowSortingFunction(Function<ListView<T>, Boolean> allowSortingFunction) {
        this.allowSortingFunction = allowSortingFunction;
    }

    /**
     * A holder class containing the graphical information relevant for this class, being:
     * <ul>
     *     <li><u>Graphic:</u><br>The main front to be displayed. This is the Node that will be shown by the {@link DraggableGraphicCell} and has to be present inside every instance of this class.</li>
     *     <li><u>Drag Source:</u><br>The node to start the drag and drop process. If this value is present, a front can only be dragged when the pressed element is equal to this value. If the value is not present, the front can be dragged always, no matter where it was pressed.</li>
     * </ul>
     * This class is usually passed to the {@link DraggableGraphicCell} by using the cell's {@linkplain #graphicFunction}.
     */
    public static class GraphicData {
        /**
         * The main front to be displayed. This is the Node that will be shown by the {@link DraggableGraphicCell} and has to be present inside every instance of this class.
         */
        private final @NotNull Node graphic;
        /**
         * The node to start the drag and drop process. If this value is present, a front can only be dragged when the pressed element is equal to this value. If the value is not present, the front can be dragged always, no matter where it was pressed.<br>
         * In most cases you will want this to be a child of {@link #graphic}, so you can specify some form of drag button and therefore control the dragging better.
         */
        private final @Nullable Node dragSource;

        /**
         * Constructor for this class. It takes the {@link #graphic} and initializes the {@link #dragSource} to <code>null</code>, resulting in the possibility to drag the cell at every position.
         * @param graphic The front to be displayed by the cell.
         * @see #GraphicData(Node, Node)
         */
        public GraphicData(@NotNull Node graphic) {
            this(graphic, null);
        }
        /**
         * Constructor for this class. It takes the {@link #graphic} and {@link #dragSource}.
         * @param graphic The front to be displayed by the cell.
         * @param dragSource The node to start the dragging process <i>(refer to {@linkplain #dragSource this doc})</i>
         * @see #GraphicData(Node, Node)
         */
        public GraphicData(@NotNull Node graphic, @Nullable Node dragSource) {
            this.graphic = graphic;
            this.dragSource = dragSource;
        }

        /**
         * Returns the value of {@link #dragSource}.
         * @return The value of {@link #dragSource}.
         */
        @Nullable
        public Node getDragSource() {
            return dragSource;
        }

        /**
         * Checks whether the user would be able to drag a cell when using a given node as the source:
         * <ul>
         *     <li>If {@link #dragSource} is not present, this method will return whether the given node is part of the front itself <i>(intersects with it)</i></li>
         *     <li>If {@link #dragSource} is present, this method will return whether the given node is the drag source.</li>
         * </ul>
         * @param node The node that was used as the source.
         * @return Whether the user would be able to drag a cell.
         */
        public boolean canDragAtNode(Node node) {
            return dragSource == null ? node.intersects(graphic.getLayoutBounds()) : node == dragSource;
        }

        /**
         * Returns the value of {@link #graphic}.
         * @return The value of {@link #graphic}.
         */
        @NotNull
        public Node getGraphic() {
            return graphic;
        }
    }

    /**
     * A holder class containing information related to a {@link Line} object's appearance, being:
     * <ul>
     *     <li><u>Color:</u><br>The color, the line shall be drawn in. This color is represented by a {@link Paint} object.</li>
     *     <li><u>Width:</u><br>The width, the line shall be drawn with (default is <code>2D</code>).</li>
     * </ul>
     */
    public static class LineData {
        /**
         * The color, the line shall be drawn in. This color is represented by a {@link Paint} object (default is <code>Color.RED</code>).
         */
        private final Paint color;
        /**
         * The width, the line shall be drawn with (default is <code>2D</code>).
         */
        private final double width;

        /**
         * Constructor for this class. It takes the {@link #color} and initializes the {@link #width} to <code>2D</code>.
         * @param color The color to draw the line in.
         * @see #LineData(double)
         * @see #LineData(Paint, double)
         */
        public LineData(Paint color) {
            this(color, 2);
        }
        /**
         * Constructor for this class. It takes the {@link #width} and initializes the {@link #color} to <code>Color.RED</code>.
         * @param width The width to draw the line with.
         * @see #LineData(Paint)
         * @see #LineData(Paint, double)
         */
        public LineData(double width) {
            this(Color.RED, width);
        }
        /**
         * Constructor for this class. It takes the {@link #color} and {@link #width}.
         * @param color The color to draw the line in.
         * @param width The width to draw the line with.
         * @see #LineData(Paint)
         * @see #LineData(double)
         */
        public LineData(Paint color, double width) {
            this.color = color;
            this.width = width;
        }

        /**
         * Helper method applying the stored data to a {@link Line} object.
         * @param line The object to apply the data to.
         */
        public void applyFor(Line line) {
            line.setStroke(color);
            line.setStrokeWidth(width);
        }
        /**
         * Returns the value of {@link #width}.
         * @return The value of {@link #width}.
         */
        public double getWidth() {
            return width;
        }
        /**
         * Returns the value of {@link #color}.
         * @return The value of {@link #color}.
         */
        public Paint getColor() {
            return color;
        }
    }

    /**
     * A listener interface for handling drag-and-drop and item update events within the {@link DraggableGraphicCell} class.
     * Implementations of this interface can respond to various stages of the drag process
     * and updates to UI cell items.
     */
    public interface EventListener {

        /**
         * Called when a drag gesture is detected.
         *
         * @param event the {@link MouseEvent} associated with the drag detection
         */
        void onDragDetected(MouseEvent event);

        /**
         * Called when a drag is currently over a target node.
         *
         * @param event the {@link DragEvent} representing the drag over state
         */
        void onDragOver(DragEvent event);

        /**
         * Called when the drag enters a target node.
         *
         * @param event the {@link DragEvent} triggered when the drag enters the node
         */
        void onDragEntered(DragEvent event);

        /**
         * Called when the drag exits a target node.
         *
         * @param event the {@link DragEvent} triggered when the drag exits the node
         */
        void onDragExited(DragEvent event);

        /**
         * Called when the drag is dropped onto a target node.
         *
         * @param event the {@link DragEvent} representing the drop action
         */
        void onDragDropped(DragEvent event);

        /**
         * Called when a drag-and-drop gesture is completed, regardless of success.
         *
         * @param event the {@link DragEvent} triggered upon completion of the drag operation
         */
        void onDragDone(DragEvent event);

        /**
         * Called when the item in a cell is updated.
         *
         * @param cell  the {@link Cell} whose item has changed
         * @param item  the new item contained in the cell
         * @param empty true if the cell is empty, false otherwise
         */
        void onItemUpdate(Cell<?> cell, Object item, boolean empty);
    }

    /**
     * An abstract adapter class for receiving drag-and-drop and item update events within the {@link DraggableGraphicCell} class.
     * The methods in this class are empty. This class exists as a convenience for creating
     * listener objects by extending this class and overriding only the methods of interest.
     */
    public abstract static class EventAdapter implements EventListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDragDetected(MouseEvent event) {}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDragOver(DragEvent event) {}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDragEntered(DragEvent event) {}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDragExited(DragEvent event) {}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDragDropped(DragEvent event) {}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDragDone(DragEvent event) {}

        /**
         * {@inheritDoc}
         */
        @Override
        public void onItemUpdate(Cell<?> cell, Object item, boolean empty) {}
    }
}
