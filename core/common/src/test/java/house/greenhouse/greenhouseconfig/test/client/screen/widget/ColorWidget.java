package house.greenhouse.greenhouseconfig.test.client.screen.widget;

import com.mojang.blaze3d.vertex.VertexConsumer;
import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import house.greenhouse.greenhouseconfig.test.client.util.ColorUtil;
import house.greenhouse.greenhouseconfig.test.client.util.MouseUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ColorWidget extends AbstractColorWidget {
    private static final ResourceLocation SELECTOR = GreenhouseConfigTest.asResource("config/selector");
    private static final WidgetSprites DEFAULT_BUTTON = new WidgetSprites(GreenhouseConfigTest.asResource("config/default"), GreenhouseConfigTest.asResource("config/default_disabled"), GreenhouseConfigTest.asResource("config/default"));

    private int maxHV;
    private int maxHS;

    private final EditBox textBox;

    private float currentSlider = 0.0F;
    @Nullable
    private Type currentlyActive;
    private final AbstractButton defaultButton;
    private final TextColor defaultColor;

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
            protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
                graphics.blitSprite(DEFAULT_BUTTON.get(active, isHoveredOrFocused()), getX(), getY(), 12, 12);
                if (isHovered() && !isServerControlled())
                    graphics.renderTooltip(Minecraft.getInstance().font, Component.literal("Reset to Default"), mouseX, mouseY);
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
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
    protected boolean clicked(double mouseX, double mouseY) {
        return MouseUtil.inBounds(mouseX, mouseY, getX() + 2, getY() + 14, 120, 8) ||
                MouseUtil.inBounds(mouseX, mouseY, getX() + 2, getY() + 23, 120, 8) ||
                MouseUtil.inBounds(mouseX, mouseY, getX() + 2, getY() + 32, 120, 8);
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

    private static boolean isAcceptedCharacter(char value) {
        return value >= '0' && value <= '9' || value >= 'a' && value <= 'f' || value >= 'A' && value <= 'F';
    }

    public EditBox getTextBox() {
        return textBox;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderColorBox(graphics, getX(), getY());
        textBox.renderWidget(graphics, mouseX, mouseY, partialTick);
        defaultButton.render(graphics, mouseX, mouseY, partialTick);

        renderHBackground(graphics, getX(), getY() + 14);
        renderSBBackground(graphics, getX(), getY() + 23, FastColor.ARGB32.colorFromFloat(1.0F, v, v, v), maxHV);
        renderSBBackground(graphics, getX(), getY() + 32, 0xFF000000, maxHS);

        renderSlider(graphics, getX(), getY() + 14, h);
        renderSlider(graphics, getX(), getY() + 23, s);
        renderSlider(graphics, getX(), getY() + 32, v);

        if (MouseUtil.inBounds(mouseX, mouseY, getX(), getY(), 122, 45) && isServerControlled())
            graphics.renderTooltip(Minecraft.getInstance().font, Component.literal("This value is controlled by the server."), mouseX, mouseY);
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

        if (isServerControlled())
            graphics.setColor(0.4F, 0.4F, 0.4F, 1.0F);
        graphics.fill(startX, startY, endX, endY, 0xFFFFFFFF);
        if (isServerControlled())
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        startX = startX + 1;
        startY = startY + 1;
        endX = endX - 1;
        endY = endY - 1;

        int colorValue = FastColor.ARGB32.color(255, color.getValue());
        if (isServerControlled())
            colorValue = FastColor.ARGB32.color((int) (FastColor.ARGB32.red(colorValue) * 0.4), (int) (FastColor.ARGB32.green(colorValue) * 0.4), (int) (FastColor.ARGB32.blue(colorValue) * 0.4));

        graphics.fill(startX, startY, endX, endY, 10, colorValue);
    }

    private void renderHBackground(GuiGraphics graphics, int startX, int startY) {
        int endX = startX + 122;
        int endY = startY + 8;

        if (isServerControlled())
            graphics.setColor(0.4F, 0.4F, 0.4F, 1.0F);
        graphics.fill(startX, startY, endX, endY, 0xFFFFFFFF);
        if (isServerControlled())
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        startX = startX + 1;
        startY = startY + 1;
        endY = endY - 2;

        Matrix4f matrix4f = graphics.pose().last().pose();
        VertexConsumer vertex = graphics.bufferSource().getBuffer(RenderType.gui());

        float full = isServerControlled() ? 0.4F : 1.0F;
        int red = FastColor.ARGB32.colorFromFloat(1.0F, full, 0.0F, 0.0F);
        int yellow = FastColor.ARGB32.colorFromFloat(1.0F, full, full, 0.0F);
        int green = FastColor.ARGB32.colorFromFloat(1.0F, 0.0F, full, 0.0F);
        int cyan = FastColor.ARGB32.colorFromFloat(1.0F, 0.0F, full, full);
        int blue = FastColor.ARGB32.colorFromFloat(1.0F, 0.0F, 0.0F, full);
        int magenta = FastColor.ARGB32.colorFromFloat(1.0F, full, 0.0F, full);

        vertex.addVertex(matrix4f, (float)startX, (float)startY, 10.0F).setColor(red);
        vertex.addVertex(matrix4f, (float)startX, (float)endY, 10.0F).setColor(red);
        vertex.addVertex(matrix4f, (float)startX + 20, (float)endY, 10.0F).setColor(yellow);
        vertex.addVertex(matrix4f, (float)startX + 20, (float)startY, 10.0F).setColor(yellow);
        vertex.addVertex(matrix4f, (float)startX + 20, (float)startY, 10.0F).setColor(yellow);
        vertex.addVertex(matrix4f, (float)startX + 20, (float)endY, 10.0F).setColor(yellow);
        vertex.addVertex(matrix4f, (float)startX + 40, (float)endY, 10.0F).setColor(green);
        vertex.addVertex(matrix4f, (float)startX + 40, (float)startY, 10.0F).setColor(green);
        vertex.addVertex(matrix4f, (float)startX + 40, (float)startY, 10.0F).setColor(green);
        vertex.addVertex(matrix4f, (float)startX + 40, (float)endY, 10.0F).setColor(green);
        vertex.addVertex(matrix4f, (float)startX + 60, (float)endY, 10.0F).setColor(cyan);
        vertex.addVertex(matrix4f, (float)startX + 60, (float)startY, 10.0F).setColor(cyan);
        vertex.addVertex(matrix4f, (float)startX + 60, (float)startY, 10.0F).setColor(cyan);
        vertex.addVertex(matrix4f, (float)startX + 60, (float)endY, 10.0F).setColor(cyan);
        vertex.addVertex(matrix4f, (float)startX + 80, (float)endY, 10.0F).setColor(blue);
        vertex.addVertex(matrix4f, (float)startX + 80, (float)startY, 10.0F).setColor(blue);
        vertex.addVertex(matrix4f, (float)startX + 80, (float)startY, 10.0F).setColor(blue);
        vertex.addVertex(matrix4f, (float)startX + 80, (float)endY, 10.0F).setColor(blue);
        vertex.addVertex(matrix4f, (float)startX + 100, (float)endY, 10.0F).setColor(magenta);
        vertex.addVertex(matrix4f, (float)startX + 100, (float)startY, 10.0F).setColor(magenta);
        vertex.addVertex(matrix4f, (float)startX + 100, (float)startY, 10.0F).setColor(magenta);
        vertex.addVertex(matrix4f, (float)startX + 100, (float)endY, 10.0F).setColor(magenta);
        vertex.addVertex(matrix4f, (float)startX + 120, (float)endY, 10.0F).setColor(red);
        vertex.addVertex(matrix4f, (float)startX + 120, (float)startY, 10.0F).setColor(red);
    }

    private void renderSBBackground(GuiGraphics graphics, int startX, int startY, int startColor, int endColor) {
        int endX = startX + 122;
        int endY = startY + 8;

        if (isServerControlled())
            graphics.setColor(0.4F, 0.4F, 0.4F, 1.0F);
        graphics.fill(startX, startY, endX, endY, 0xFFFFFFFF);
        if (isServerControlled())
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        startX = startX + 1;
        startY = startY + 1;
        endX = endX - 1;
        endY = endY - 1;

        Matrix4f matrix4f = graphics.pose().last().pose();
        VertexConsumer vertex = graphics.bufferSource().getBuffer(RenderType.gui());

        float full = isServerControlled() ? 0.4F : 1.0F;

        float startR = (FastColor.ARGB32.red(startColor) / 255.0F) * full;
        float startG = (FastColor.ARGB32.green(startColor) / 255.0F) * full;
        float startB = (FastColor.ARGB32.blue(startColor) / 255.0F) * full;
        float endR = (FastColor.ARGB32.red(endColor) / 255.0F) * full;
        float endG = (FastColor.ARGB32.green(endColor) / 255.0F) * full;
        float endB = (FastColor.ARGB32.blue(endColor) / 255.0F) * full;

        vertex.addVertex(matrix4f, (float)startX, (float)startY, 10.0F).setColor(startR, startG, startB, 1.0F);
        vertex.addVertex(matrix4f, (float)startX, (float)endY, 10.0F).setColor(startR, startG, startB, 1.0F);
        vertex.addVertex(matrix4f, (float)endX, (float)endY, 10.0F).setColor(endR, endG, endB, 1.0F);
        vertex.addVertex(matrix4f, (float)endX, (float)startY, 10.0F).setColor(endR, endG, endB, 1.0F);
    }

    private void renderSlider(GuiGraphics graphics, int startX, int startY, float location) {
        if (isServerControlled())
            graphics.setColor(0.4F, 0.4F, 0.4F, 1.0F);
        graphics.blitSprite(SELECTOR, Mth.clamp((int)(startX + (location * 120) - 1), startX, startX + 120), startY, 20, 3, 8);
        if (isServerControlled())
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void updateColorFromSlider() {
        if (currentlyActive != null) {
            switch (currentlyActive) {
                case HUE -> h = currentSlider;
                case SATURATION -> s = currentSlider;
                case VALUE -> v = currentSlider;
            }
            float[] rgb = ColorUtil.hsvToRgb(h, s, v);
            int c = Mth.color(rgb[0], rgb[1], rgb[2]);
            color = TextColor.fromRgb(c);
            defaultButton.active = !color.equals(defaultColor);
            updateMax();
            textBox.setValue(color.serialize());
        }
    }

    private void updateMax() {
        float[] maxHVRgb = ColorUtil.hsvToRgb(h, 1.0F, v);
        maxHV = FastColor.ARGB32.colorFromFloat(1.0F, maxHVRgb[0], maxHVRgb[1], maxHVRgb[2]);
        float[] maxHSRgb = ColorUtil.hsvToRgb(h, s, 1.0F);
        maxHS = FastColor.ARGB32.colorFromFloat(1.0F, maxHSRgb[0], maxHSRgb[1], maxHSRgb[2]);
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
            narrationElementOutput.add(NarratedElementType.TITLE, "Update Value to " + s);
    }

    private enum Type {
        HUE,
        SATURATION,
        VALUE
    }
}
