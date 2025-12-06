package io.github.redstonemango.mangoplayer.front.controller.songOrderSpecification;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SongOrderSpecificationController {

    @FXML private CheckBox m3u8;
    @FXML private CheckBox wpl;
    @FXML private CheckBox numeric;

    private Consumer<SongOrderType> action;

    @FXML
    private void onCancel() {
        m3u8.getScene().getWindow().hide();
    }
    @FXML
    private void onDone() {
        action.accept(new SongOrderType(
                m3u8.isSelected(),
                wpl.isSelected(),
                numeric.isSelected()
        ));
        onCancel();
    }

    public void setAction(Consumer<SongOrderType> action) {
        this.action = action;
    }
    public Consumer<SongOrderType> getAction() {
        return action;
    }

    public record SongOrderType(boolean m3u8, boolean wpl, boolean numeric) {
        @Override
        public @NotNull String toString() {
            return String.format("{m3u8:%s,wpl:%s,numeric:%s}", m3u8, wpl, numeric);
        }
    }
}
