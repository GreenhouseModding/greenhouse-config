package house.greenhouse.greenhouseconfig.test.client.screen;

import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import house.greenhouse.greenhouseconfig.test.client.screen.widget.ColorWidget;
import house.greenhouse.greenhouseconfig.test.config.TestConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class GreenhouseConfigTestScreen extends Screen {
    private static final Component SAVED_CONFIG = Component.literal("Saved Config!");
    private final Screen previousScreen;

    private final TestConfigBuilder builder;

    private final ColorWidget splitCommonColorWidget;
    private final ColorWidget splitClientColorWidget;
    private final AbstractButton saveConfigButton;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private int savedMessageTime = 0;
    @Nullable
    private Component errorMessage;

    public GreenhouseConfigTestScreen(Screen previousScreen) {
        super(Component.literal("Greenhouse Config Test Configuration"));
        this.previousScreen = previousScreen;
        TestConfig currentConfig = GreenhouseConfigTest.CONFIG.getUnsynced();
        builder = new TestConfigBuilder(currentConfig);
        splitCommonColorWidget = new ColorWidget(0, 0, builder.color, TestConfig.DEFAULT.color());
        splitClientColorWidget = new ColorWidget(0, 0, builder.clientColor, TestConfig.DEFAULT.clientValues().color());
        saveConfigButton = Button.builder(Component.literal("Save Config"), button -> save()).build();
        saveConfigButton.active = false;
    }

    protected void init() {
        if (Minecraft.getInstance().getCurrentServer() != null && !Minecraft.getInstance().isLocalServer())
            splitCommonColorWidget.setServerControlled(true);
        layout.setHeaderHeight(44);
        layout.addTitleHeader(Component.literal("Greenhouse Config Test Configuration"), font);
        LinearLayout contents = layout.addToContents(LinearLayout.vertical().spacing(2));
        contents.addChild(layoutLine("Server Color", splitCommonColorWidget));
        contents.addChild(layoutLine("Client Color", splitClientColorWidget));
        LinearLayout footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(saveConfigButton);
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).build());
        layout.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            addRenderableWidget(widget);
        });
        layout.arrangeElements();
    }

    private LinearLayout layoutLine(String name, AbstractWidget widget) {
        LinearLayout layout = LinearLayout.horizontal();
        StringWidget stringWidget = new StringWidget(Component.literal(name), font);
        stringWidget.alignLeft();
        stringWidget.setWidth(80);
        layout.addChild(stringWidget);
        layout.addChild(widget);
        layout.arrangeElements();
        return layout;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (savedMessageTime > 0) {
            float timeMultiplier = savedMessageTime - partialTick;
            int alpha = (int)(timeMultiplier * 255.0F / 20.0F);
            if (alpha > 255)
                alpha = 255;
            graphics.drawStringWithBackdrop(font, errorMessage == null ? SAVED_CONFIG : errorMessage, (int) ((float) width / 2 - ((float) Minecraft.getInstance().font.width(SAVED_CONFIG) / 2)), height - 40, 0, FastColor.ARGB32.color(alpha, 255,  errorMessage == null ? 255 : 0, errorMessage == null ? 255 : 0));
        }
    }

    @Override
    public void tick() {
        if (savedMessageTime > 0) {
            --savedMessageTime;
            if (savedMessageTime == 0)
                errorMessage = null;
        }

        if (splitCommonColorWidget.isDirty()) {
            TextColor color = splitCommonColorWidget.getColor();
            updateColor(builder::color, color);
        }
        if (splitClientColorWidget.isDirty()) {
            TextColor color = splitClientColorWidget.getColor();
            updateColor(builder::clientColor, color);
        }
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (splitCommonColorWidget.getTextBox().canConsumeInput()) {
            if (overwrite)
                splitCommonColorWidget.getTextBox().setValue(text);
            else
                splitCommonColorWidget.getTextBox().insertText(text);
        }
        if (splitClientColorWidget.getTextBox().canConsumeInput()) {
            if (overwrite)
                splitClientColorWidget.getTextBox().setValue(text);
            else
                splitClientColorWidget.getTextBox().insertText(text);
        }
    }

    private void updateColor(Consumer<TextColor> colorSetter, TextColor color) {
        colorSetter.accept(color);
        saveConfigButton.active = !builder.equals(GreenhouseConfigTest.CONFIG.getUnsynced());
    }

    private void save() {
        GreenhouseConfigTest.CONFIG.saveConfig(builder.build());
        GreenhouseConfigTest.CONFIG.reloadConfig(s -> {
            errorMessage = Component.literal("Error refreshing config! Check logs.");
            GreenhouseConfigTest.LOG.error(s);
        });
        if (Minecraft.getInstance().getConnection() != null)
            GreenhouseConfigTest.CONFIG.queryConfig();
        savedMessageTime = 60;
        saveConfigButton.active = false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previousScreen);
    }

    /**
     * A builder for the split config.
     */
    public static class TestConfigBuilder {
        protected int silly;
        protected LateHolder<Enchantment> favoriteEnchantment;
        protected LateHolderSet<Block> redBlocks;
        protected LateHolderSet<Biome> greenBiomes;
        protected TextColor color;
        protected TextColor clientColor;

        protected TestConfigBuilder(TestConfig config) {
            this.silly = config.silly();
            this.favoriteEnchantment = config.favoriteEnchantment();
            this.redBlocks = config.redBlocks();
            this.greenBiomes = config.greenBiomes();
            this.color = config.color();
            this.clientColor = config.clientValues().color();
        }

        public TestConfigBuilder silly(int silly) {
            this.silly = silly;
            return this;
        }

        public TestConfigBuilder favoriteEnchantment(LateHolder<Enchantment> enchantment) {
            this.favoriteEnchantment = enchantment;
            return this;
        }

        public TestConfigBuilder color(TextColor color) {
            this.color = color;
            return this;
        }

        public TestConfigBuilder clientColor(TextColor color) {
            this.clientColor = color;
            return this;
        }

        public boolean equals(TestConfig original) {
            return original.silly() == silly && original.favoriteEnchantment().equals(favoriteEnchantment) && original.redBlocks().equals(redBlocks) && original.greenBiomes().equals(greenBiomes) && original.color().getValue() == color.getValue() && original.clientValues().color().getValue() == clientColor.getValue();
        }

        public TestConfig build() {
            return new TestConfig(silly, favoriteEnchantment, redBlocks, greenBiomes, color, new TestConfig.ClientConfigValues(clientColor));
        }
    }
}
