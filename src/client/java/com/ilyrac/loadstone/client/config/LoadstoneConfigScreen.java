package com.ilyrac.loadstone.client.config;

import com.ilyrac.loadstone.client.ClientLoaderCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class LoadstoneConfigScreen extends Screen {
    private final Screen parent;
    private final LoadstoneConfig config;

    // Layout tracking variables so text rendering matches button placement
    private int hudHeaderY;
    private int loaderHeaderY;
    private int commandSectionY;
    private boolean showCommands;

    public LoadstoneConfigScreen(Screen parent) {
        super(Component.literal("Loadstone Settings"));
        this.parent = parent;
        this.config = LoadstoneConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        // 1. DYNAMIC WIDTH
        int widgetW = Math.clamp(this.width / 3, 150, 200);
        int widgetH = 20;
        int cx = this.width / 2;
        int buttonX = cx - (widgetW / 2);

        // 2. DYNAMIC SPACING
        int spacingY = Math.min(24, this.height / 14);

        // 3. DYNAMIC STARTING Y
        int currentY = Math.max(20, this.height / 12);

        // ==========================================
        // TOP SECTION: HUD SETTINGS
        // ==========================================

        this.hudHeaderY = currentY;
        currentY += 15; // Add space below header text for the first button

        // 1. HUD Opacity
        this.addRenderableWidget(CycleButton.builder(
                        val -> Component.literal(val.name),
                        config.hudOpacity)
                .withValues(LoadstoneConfig.HudOpacity.values())
                .withTooltip(_ -> Tooltip.create(Component.literal(
                        "Controls the transparency of the Loadstone HUD.\n• Hide: Completely removes the HUD.\n• 50% - 75%: Semi-transparent.\n• 100%: Fully solid."
                )))
                .create(buttonX, currentY, widgetW, widgetH, Component.literal("HUD Opacity"), (_, value) -> config.hudOpacity = value));
        currentY += spacingY;

        // 2. HUD Size
        this.addRenderableWidget(CycleButton.builder(
                        val -> Component.literal(val.name),
                        config.hudSize)
                .withValues(LoadstoneConfig.HudSize.values())
                .withTooltip(_ -> Tooltip.create(Component.literal(
                        "Scales the size of the Loadstone HUD.\n• 50% - 75%: Shrinks the UI for a cleaner screen.\n• 100%: Default vanilla size."
                )))
                .create(buttonX, currentY, widgetW, widgetH, Component.literal("HUD Size"), (_, value) -> config.hudSize = value));
        currentY += spacingY;

        // 3. HUD Location
        this.addRenderableWidget(CycleButton.builder(
                        val -> Component.literal(val.name),
                        config.hudLocation)
                .withValues(LoadstoneConfig.HudLocation.values())
                .withTooltip(_ -> Tooltip.create(Component.literal(
                        "Moves the HUD around your screen.\nSelect a corner or edge to anchor the information box."
                )))
                .create(buttonX, currentY, widgetW, widgetH, Component.literal("HUD Location"), (_, value) -> config.hudLocation = value));

        // Add extra padding before the next section
        currentY += spacingY + 5;

        // ==========================================
        // BOTTOM SECTION: LOADER VISUALS
        // ==========================================

        this.loaderHeaderY = currentY;
        currentY += 15; // Space for header

        // 1. Tinted Loader
        this.addRenderableWidget(CycleButton.onOffBuilder(config.tintedLoaders)
                .withTooltip(_ -> Tooltip.create(Component.literal(
                        "Color-codes the Loadstone blocks.\n• On: Colors match the tier (Gray, Aqua, Purple).\n• Off: Uses the default gray stone texture."
                )))
                .create(buttonX, currentY, widgetW, widgetH, Component.literal("Tinted Loader"), (_, value) -> config.tintedLoaders = value));
        currentY += spacingY;

        // 2. Interaction Particles
        this.addRenderableWidget(CycleButton.onOffBuilder(config.interactionParticles)
                .withTooltip(_ -> Tooltip.create(Component.literal(
                        "Spawns particles when you click or use the Loadstone.\n• On: Visual feedback enabled.\n• Off: No interaction effects for better performance."
                )))
                .create(buttonX, currentY, widgetW, widgetH, Component.literal("Interaction Particles"), (_, value) -> config.interactionParticles = value));
        currentY += spacingY;

        // 3. Ambient Particles
        this.addRenderableWidget(CycleButton.builder(
                        val -> Component.literal(val.name),
                        config.ambientParticles)
                .withValues(LoadstoneConfig.AmbientMode.values())
                .withTooltip(_ -> Tooltip.create(Component.literal(
                        "Controls the ambient particle effects around an active Loadstone.\n• Off: No ambient particles.\n• Low: Occasional floating particles.\n• High: Constant ambient particle field."
                )))
                .create(buttonX, currentY, widgetW, widgetH, Component.literal("Ambient Particles"), (_, value) -> config.ambientParticles = value));

        currentY += spacingY + 5;

        // ==========================================
        // ADMIN COMMAND DOCUMENTATION CARD
        // ==========================================
        this.commandSectionY = currentY;
        // Safely check if there is enough vertical screen real-estate to draw the commands
        // We need about 45 pixels of space above the "Done" button.
        this.showCommands = (this.commandSectionY + 45 < this.height - 15);

        // ==========================================
        // DONE NAVIGATION
        // ==========================================
        // Force the Done button to always anchor perfectly to the bottom of the screen
        this.addRenderableWidget(Button.builder(Component.literal("Done"), _ -> {
            LoadstoneConfig.save();

            if (this.minecraft.level != null) {
                ClientLoaderCache.snapshot().keySet().forEach(pos -> {
                    BlockState state = this.minecraft.level.getBlockState(pos);
                    this.minecraft.level.sendBlockUpdated(pos, state, state, 3);
                });
            }

            this.minecraft.gui.setScreen(this.parent);
        }).bounds(cx - 60, this.height - 24, 120, 20).build());
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;

        // Title Header (Always at the top)
        graphics.centeredText(this.font, this.title, cx, Math.max(5, this.hudHeaderY - 15), 0xFFFFFFFF);

        // Section Headers (Using the exact coordinates saved during init)
        graphics.centeredText(this.font,
                Component.literal("HUD Settings").withStyle(ChatFormatting.UNDERLINE),
                cx, this.hudHeaderY, 0xFFAAAAAA);

        graphics.centeredText(this.font,
                Component.literal("Loader Visuals").withStyle(ChatFormatting.UNDERLINE),
                cx, this.loaderHeaderY, 0xFFAAAAAA);

        // Admin Commands (Only render if they won't overlap the "Done" button)
        if (this.showCommands) {
            int textLineSpacing = Math.min(10, (this.height - 15 - this.commandSectionY) / 4);

            // Main header line
            graphics.centeredText(this.font,
                    Component.literal("Operator Commands").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD),
                    cx, this.commandSectionY, 0xFFFFFFFF);

            // Sub-header explaining it needs Cheats / Admin privileges
            graphics.centeredText(this.font,
                    Component.literal("(Requires Cheats / Admin)").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
                    cx, this.commandSectionY + textLineSpacing, 0xFFFFFFFF);

            // Commands shifted down to accommodate the sub-header
            graphics.centeredText(this.font,
                    Component.literal("• /loadstone list ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("- View active chunks.").withStyle(ChatFormatting.GRAY)),
                    cx, this.commandSectionY + (textLineSpacing * 2), 0xFFFFFFFF);

            graphics.centeredText(this.font,
                    Component.literal("• /loadstone activate <x y z> <tier> ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("- Set or upgrade/downgrade a zone.").withStyle(ChatFormatting.GRAY)),
                    cx, this.commandSectionY + (textLineSpacing * 3), 0xFFFFFFFF);

            graphics.centeredText(this.font,
                    Component.literal("• /loadstone deactivate [all | <x y z>] ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("- Unload specific or all tickets.").withStyle(ChatFormatting.GRAY)),
                    cx, this.commandSectionY + (textLineSpacing * 4), 0xFFFFFFFF);
        }
    }

    @Override
    public void onClose() {
        LoadstoneConfig.save();
        this.minecraft.gui.setScreen(this.parent);
    }
}