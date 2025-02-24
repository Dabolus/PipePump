package ru.cr1zyb0y.pipevacuumpump.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import reborncore.client.gui.GuiBase;
import reborncore.common.fluid.container.FluidInstance;
import reborncore.common.screen.BuiltScreenHandler;
import ru.cr1zyb0y.pipevacuumpump.blocksentity.MachineFluidPipePumpBlockEntity;

public class GuiMachineFluidPipePump extends GuiBase<BuiltScreenHandler> {

    private final MachineFluidPipePumpBlockEntity blockEntity;

    public GuiMachineFluidPipePump(int syncID, final PlayerEntity player, final MachineFluidPipePumpBlockEntity blockEntity) {
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
        FluidInstance fluidInstance = blockEntity.tank.getFluidInstance();
        builder.drawTank(drawContext, this, 53, 25, mouseX, mouseY, fluidInstance, blockEntity.tank.getFluidValueCapacity(), blockEntity.tank.isEmpty(), layer);
        builder.drawMultiEnergyBar(drawContext, this, 9, 19, (int) blockEntity.getEnergy(), (int) blockEntity.getMaxStoredPower(), mouseX, mouseY, 0, layer);
    }
}
