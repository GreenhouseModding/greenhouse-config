package house.greenhouse.greenhouseconfig.test.client.screen.widget;

import house.greenhouse.greenhouseconfig.impl.client.gui.GradientRectRenderState;
import house.greenhouse.greenhouseconfig.impl.client.gui.GradientRectRenderState.GradientDirection;
import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import house.greenhouse.greenhouseconfig.test.client.util.ColorUtil;
import house.greenhouse.greenhouseconfig.test.client.util.MouseUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ColorWidget extends AbstractColorWidget {
	private static final ResourceLocation SELECTOR = GreenhouseConfigTest.asResource("config/selector");
	private static final WidgetSprites DEFAULT_BUTTON = new WidgetSprites(GreenhouseConfigTest.asResource("config/default"), GreenhouseConfigTest.asResource("config/default_disabled"), GreenhouseConfigTest.asResource("config/default"));
	private final EditBox textBox;
	private final AbstractButton defaultButton;
	private final TextColor defaultColor;
	private int maxHV;
	private int maxHS;
	private float currentSlider = 0.0F;
	@Nullable
	private Type currentlyActive;
	private boolean resetActive = true;

	public ColorWidget(int x, int y, TextColor color, TextColor defaultColor) {
		super(x, y, 122, 45, Component.literal("Color Input"));
		defaultButton = new AbstractButton(x + 110, y, 12, 12, Component.literal("")) {
			@Override
			public void onPress() {
				setColor(defaultColor);
				textBox.setValue(defaultColor.serialize());
				setDirty(true);
			}

			@Override
			public void renderString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int color) {}

			@Override
			protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
				super.renderWidget(graphics, mouseX, mouseY, partialTick);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DEFAULT_BUTTON.get(active, isHoveredOrFocused()), getX(), getY(), 12, 12);
				if (isHovered() && !isServerControlled())
					graphics.renderTooltip(
							Minecraft.getInstance().font,
							List.of(ClientTooltipComponent.create(Component.literal("Reset to Default").getVisualOrderText())),
							mouseX,
							mouseY,
							DefaultTooltipPositioner.INSTANCE,
							null
					);
			}

			@Override
			protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
				narrationElementOutput.add(NarratedElementType.POSITION, Component.literal("Reset to Default Color"));
			}
		};
		textBox = new EditBox(Minecraft.getInstance().font, getX() + 15, getY(), 92, 12, Component.literal(color.serialize()));
		textBox.setFilter(s1 -> {
			var chars = s1.toCharArray();
			if (chars.length < 1) {
				textBox.setValue("#");
				return false;
			}
			if (chars.length > 7)
				return false;
			for (int i = 0; i < chars.length; ++i) {
				if (i == 0 && chars[i] != '#')
					return false;
				if (i != 0 && !isAcceptedCharacter(chars[i]))
					return false;
			}
			return true;
		});
		textBox.setResponder(s1 -> {
			if (textBox.canConsumeInput() && s1.length() == 7) {
				setColor(TextColor.parseColor(s1).getOrThrow());
				setDirty(true);
			}
		});
		this.defaultColor = defaultColor;
		defaultButton.active = !color.equals(defaultColor);
		setColor(color);
		textBox.setValue(color.serialize());
	}

	private static boolean isAcceptedCharacter(char value) {
		return value >= '0' && value <= '9' || value >= 'a' && value <= 'f' || value >= 'A' && value <= 'F';
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		textBox.setX(x + 15);
		defaultButton.setX(x + 110);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		textBox.setY(y);
		defaultButton.setY(y);
	}

	@Override
	public void setColor(TextColor color) {
		super.setColor(color);
		defaultButton.active = !color.equals(defaultColor);
		updateMax();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (textBox.mouseClicked(mouseX, mouseY, button)) {
			currentlyActive = null;
			currentSlider = 0.0F;
			textBox.setFocused(true);
			return true;
		}
		textBox.setFocused(false);
		if (defaultButton.mouseClicked(mouseX, mouseY, button)) {
			currentlyActive = null;
			currentSlider = 0.0F;
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return MouseUtil.inBounds(mouseX, mouseY, getX(), getY(), 120, 40);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		resetActive = false;
		if (MouseUtil.inBounds(mouseX, mouseY, getX() + 2, getY() + 14, 120, 8)) {
			setFocused(true);
			currentSlider = h;
			currentlyActive = Type.HUE;
		} else if (MouseUtil.inBounds(mouseX, mouseY, getX() + 2, getY() + 23, 120, 8)) {
			setFocused(true);
			currentSlider = s;
			currentlyActive = Type.SATURATION;
		} else if (MouseUtil.inBounds(mouseX, mouseY, getX() + 2, getY() + 32, 120, 8)) {
			setFocused(true);
			currentSlider = v;
			currentlyActive = Type.VALUE;
		}
		resetActive = true;
		onDrag(mouseX, mouseY, 0.0, 0.0);
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
		if (isFocused() && !textBox.canConsumeInput()) {
			double originMouse = Mth.clampedLerp(getX(), getX() + 120, currentSlider);
			float diff = (float) ((mouseX - originMouse) / 120);
			currentSlider = Mth.clamp(currentSlider + diff, 0.0F, 1.0F);
			updateColorFromSlider();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (textBox.canConsumeInput())
			return textBox.keyPressed(keyCode, scanCode, modifiers);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (textBox.canConsumeInput())
			return textBox.charTyped(codePoint, modifiers);
		return super.charTyped(codePoint, modifiers);
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		if (isFocused() && !textBox.canConsumeInput()) {
			setFocused(false);
			setDirty(true);
		}
	}

	public EditBox getTextBox() {
		return textBox;
	}

	@Override
	protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderColorBox(graphics, getX(), getY());
		textBox.renderWidget(graphics, mouseX, mouseY, partialTick);
		defaultButton.render(graphics, mouseX, mouseY, partialTick);

		renderHBackground(graphics, getX(), getY() + 14);
		renderSBBackground(graphics, getX(), getY() + 23, ARGB.colorFromFloat(1.0F, v, v, v), maxHV);
		renderSBBackground(graphics, getX(), getY() + 32, 0xFF000000, maxHS);

		renderSlider(graphics, getX(), getY() + 14, h);
		renderSlider(graphics, getX(), getY() + 23, s);
		renderSlider(graphics, getX(), getY() + 32, v);

		if (MouseUtil.inBounds(mouseX, mouseY, getX(), getY(), 122, 45) && isServerControlled())
			graphics.renderTooltip(
					Minecraft.getInstance().font,
					List.of(ClientTooltipComponent.create(Component.literal("This value is controlled by the server.").getVisualOrderText())),
					mouseX,
					mouseY,
					DefaultTooltipPositioner.INSTANCE,
					null
			);
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!resetActive) {
			currentlyActive = null;
			currentSlider = 0.0F;
		}
	}

	@Override
	protected boolean isValidClickButton(int button) {
		return !isServerControlled() && super.isValidClickButton(button);
	}

	@Override
	public void setServerControlled(boolean serverControlled) {
		super.setServerControlled(serverControlled);
		defaultButton.active = !serverControlled;
		textBox.active = !serverControlled;
		textBox.setEditable(!serverControlled);
	}

	private void renderColorBox(GuiGraphics graphics, int startX, int startY) {
		int endX = startX + 12;
		int endY = startY + 12;

		graphics.fill(startX, startY, endX, endY, isServerControlled() ? 0xFF666666 : 0xFFFFFFFF);

		startX = startX + 1;
		startY = startY + 1;
		endX = endX - 1;
		endY = endY - 1;

		int colorValue = ARGB.color(255, color.getValue());
		if (isServerControlled())
			colorValue = ARGB.color((int) (ARGB.red(colorValue) * 0.4), (int) (ARGB.green(colorValue) * 0.4), (int) (ARGB.blue(colorValue) * 0.4));

		graphics.fill(startX, startY, endX, endY, colorValue);
	}

	private void renderHBackground(GuiGraphics graphics, int startX, int startY) {
		int endX = startX + 122;
		int endY = startY + 8;

		graphics.fill(startX, startY, endX, endY, isServerControlled() ? 0xFF666666 : 0xFFFFFFFF);

		int finalStartX = startX + 1;
		int finalStartY = startY + 1;
		int finalEndY = endY - 2;

		float full = isServerControlled() ? 0.4F : 1.0F;
		int red = ARGB.colorFromFloat(1.0F, full, 0.0F, 0.0F);
		int yellow = ARGB.colorFromFloat(1.0F, full, full, 0.0F);
		int green = ARGB.colorFromFloat(1.0F, 0.0F, full, 0.0F);
		int cyan = ARGB.colorFromFloat(1.0F, 0.0F, full, full);
		int blue = ARGB.colorFromFloat(1.0F, 0.0F, 0.0F, full);
		int magenta = ARGB.colorFromFloat(1.0F, full, 0.0F, full);

		GradientRectRenderState.fillGradient(graphics, finalStartX, finalStartY, finalStartX + 20, finalEndY, red, yellow, GradientDirection.LEFT_TO_RIGHT);
		GradientRectRenderState.fillGradient(graphics, finalStartX + 20, finalStartY, finalStartX + 40, finalEndY, yellow, green, GradientDirection.LEFT_TO_RIGHT);
		GradientRectRenderState.fillGradient(graphics, finalStartX + 40, finalStartY, finalStartX + 60, finalEndY, green, cyan, GradientDirection.LEFT_TO_RIGHT);
		GradientRectRenderState.fillGradient(graphics, finalStartX + 60, finalStartY, finalStartX + 80, finalEndY, cyan, blue, GradientDirection.LEFT_TO_RIGHT);
		GradientRectRenderState.fillGradient(graphics, finalStartX + 80, finalStartY, finalStartX + 100, finalEndY, blue, magenta, GradientDirection.LEFT_TO_RIGHT);
		GradientRectRenderState.fillGradient(graphics, finalStartX + 100, finalStartY, finalStartX + 120, finalEndY, magenta, red, GradientDirection.LEFT_TO_RIGHT);
	}

	private void renderSBBackground(GuiGraphics graphics, int startX, int startY, int startColor, int endColor) {
		int endX = startX + 122;
		int endY = startY + 8;

		graphics.fill(startX, startY, endX, endY, isServerControlled() ? 0xFF666666 : 0xFFFFFFFF);

		int finalStartX = startX + 1;
		int finalStartY = startY + 1;
		int finalEndX = endX - 1;
		int finalEndY = endY - 1;

		int full = isServerControlled() ? 0xFF666666 : 0xFFFFFFFF;

		int startColorFull = ARGB.multiply(startColor, full);
		int endColorFull = ARGB.multiply(endColor, full);

		GradientRectRenderState.fillGradient(graphics, finalStartX, finalStartY, finalEndX, finalEndY, startColorFull, endColorFull, GradientDirection.LEFT_TO_RIGHT);
	}

	private void renderSlider(GuiGraphics graphics, int startX, int startY, float location) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECTOR, Mth.clamp((int) (startX + (location * 120) - 1), startX, startX + 120), startY, 3, 8, isServerControlled() ? 0xFF666666 : 0xFFFFFFFF);
	}

	private void updateColorFromSlider() {
		if (currentlyActive != null) {
			switch (currentlyActive) {
				case HUE -> h = currentSlider;
				case SATURATION -> s = currentSlider;
				case VALUE -> v = currentSlider;
			}
			float[] rgb = ColorUtil.hsvToRgb(h, s, v);
			int c = ARGB.colorFromFloat(1.0F, rgb[0], rgb[1], rgb[2]);
			color = TextColor.fromRgb(c);
			defaultButton.active = !color.equals(defaultColor);
			updateMax();
			textBox.setValue(color.serialize());
		}
	}

	private void updateMax() {
		float[] maxHVRgb = ColorUtil.hsvToRgb(h, 1.0F, v);
		maxHV = ARGB.colorFromFloat(1.0F, maxHVRgb[0], maxHVRgb[1], maxHVRgb[2]);
		float[] maxHSRgb = ColorUtil.hsvToRgb(h, s, 1.0F);
		maxHS = ARGB.colorFromFloat(1.0F, maxHSRgb[0], maxHSRgb[1], maxHSRgb[2]);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
		if (textBox.canConsumeInput())
			textBox.updateWidgetNarration(narrationElementOutput);
		else if (defaultButton.isHovered())
			textBox.updateWidgetNarration(narrationElementOutput);
		else if (currentlyActive == Type.HUE)
			narrationElementOutput.add(NarratedElementType.TITLE, "Update Hue to " + h);
		else if (currentlyActive == Type.SATURATION)
			narrationElementOutput.add(NarratedElementType.TITLE, "Update Saturation to " + s);
		else if (currentlyActive == Type.VALUE)
			narrationElementOutput.add(NarratedElementType.TITLE, "Update Value to " + v);
	}

	private enum Type {
		HUE,
		SATURATION,
		VALUE
	}
}
