package ru.cr1zyb0y.pipevacuumpump.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import reborncore.client.gui.GuiBase;
import reborncore.client.gui.widget.GuiButtonExtended;
import reborncore.client.gui.GuiBuilder;
import reborncore.common.screen.BuiltScreenHandler;
import ru.cr1zyb0y.pipevacuumpump.blocksentity.MachinePipePumpBlockEntity;

public class GuiMachinePipePump extends GuiBase<BuiltScreenHandler> {

    private final MachinePipePumpBlockEntity blockEntity;

    public GuiMachinePipePump(int syncID, final PlayerEntity player, final MachinePipePumpBlockEntity blockEntity) {
        super(player, blockEntity, blockEntity.createScreenHandler(syncID, player));
        this.blockEntity = blockEntity;
    }

    @Override
    protected void drawBackground(DrawContext drawContext, final float partialTicks, final int mouseX, final int mouseY) {
        super.drawBackground(drawContext, partialTicks, mouseX, mouseY);
        final Layer layer = Layer.BACKGROUND;

        // Battery slot
        drawSlot(drawContext, 8, 72, layer);
    }

    @Override
    protected void drawForeground(DrawContext drawContext, final int mouseX, final int mouseY) {
        super.drawForeground(drawContext, mouseX, mouseY);
        final Layer layer = Layer.FOREGROUND;

        // builder.drawProgressBar(drawContext, this, (int)blockEntity.getEnergy(), (int)blockEntity.getBaseMaxPower(), 105, 47, mouseX, mouseY, GuiBuilder.ProgressDirection.RIGHT, layer);
        builder.drawMultiEnergyBar(drawContext, this, 9, 19, (int) blockEntity.getEnergy(), (int) blockEntity.getMaxStoredPower(), mouseX, mouseY, 0, layer);
    }
}
