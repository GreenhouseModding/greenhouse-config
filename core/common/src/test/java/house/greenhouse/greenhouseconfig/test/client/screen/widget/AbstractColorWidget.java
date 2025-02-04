package house.greenhouse.greenhouseconfig.test.client.screen.widget;

import house.greenhouse.greenhouseconfig.test.client.util.ColorUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FastColor;

public abstract class AbstractColorWidget extends AbstractWidget implements ServerControllable {
    protected float h;
    protected float s;
    protected float v;

    private boolean serverControlled = false;

    protected TextColor color;
    private boolean dirty = false;

    public AbstractColorWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public void setColor(TextColor color) {
        this.color = color;
        int c = color.getValue();
        float[] hsv = ColorUtil.rgbToHsv(FastColor.ARGB32.red(c), FastColor.ARGB32.green(c), FastColor.ARGB32.blue(c));
        h = hsv[0];
        s = hsv[1];
        v = hsv[2];
    }

    public boolean isDirty() {
        return dirty;
    }

    public TextColor getColor() {
        setDirty(false);
        return color;
    }

    protected void setDirty(boolean value) {
        dirty = value;
    }

    @Override
    public boolean isServerControlled() {
        return serverControlled;
    }

    @Override
    public void setServerControlled(boolean serverControlled) {
        this.serverControlled = serverControlled;
    }
}
