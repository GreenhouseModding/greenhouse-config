package house.greenhouse.greenhouseconfig.test.client.screen.widget;

import house.greenhouse.greenhouseconfig.test.client.util.ColorUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;

public abstract class AbstractColorWidget extends AbstractWidget implements ServerControllable {
	protected float h;
	protected float s;
	protected float v;
	protected TextColor color;
	private boolean serverControlled = false;
	private boolean dirty = false;

	public AbstractColorWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	public boolean isDirty() {
		return dirty;
	}

	protected void setDirty(boolean value) {
		dirty = value;
	}

	public TextColor getColor() {
		setDirty(false);
		return color;
	}

	public void setColor(TextColor color) {
		this.color = color;
		int c = color.getValue();
		float[] hsv = ColorUtil.rgbToHsv(ARGB.red(c), ARGB.green(c), ARGB.blue(c));
		h = hsv[0];
		s = hsv[1];
		v = hsv[2];
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
