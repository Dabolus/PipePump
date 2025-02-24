package ru.cr1zyb0y.pipevacuumpump.client;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

import java.util.Objects;

import ru.cr1zyb0y.pipevacuumpump.blocksentity.GuiType;
import ru.cr1zyb0y.pipevacuumpump.blocksentity.MachineFluidPipePumpBlockEntity;
import ru.cr1zyb0y.pipevacuumpump.blocksentity.MachinePipePumpBlockEntity;
import ru.cr1zyb0y.pipevacuumpump.gui.GuiMachineFluidPipePump;
import ru.cr1zyb0y.pipevacuumpump.gui.GuiMachinePipePump;

// Heavily based on (aka copy-pasted from) techreborn.client.ClientGuiType
@SuppressWarnings("unused")
public record ClientGuiType<T extends BlockEntity>(GuiType<T> guiType, GuiFactory<T> guiFactory) {
    public static final ClientGuiType<MachinePipePumpBlockEntity> MACHINE_PIPE_PUMP = register(GuiType.MACHINE_PIPE_PUMP, GuiMachinePipePump::new);
    public static final ClientGuiType<MachineFluidPipePumpBlockEntity> MACHINE_FLUID_PIPE_PUMP = register(GuiType.MACHINE_FLUID_PIPE_PUMP, GuiMachineFluidPipePump::new);

    public static <T extends BlockEntity> ClientGuiType<T> register(GuiType<T> type, GuiFactory<T> factory) {
        return new ClientGuiType<>(type, factory);
    }

    public ClientGuiType(GuiType<T> guiType, GuiFactory<T> guiFactory) {
        this.guiType = Objects.requireNonNull(guiType);
        this.guiFactory = Objects.requireNonNull(guiFactory);

        HandledScreens.register(guiType.getScreenHandlerType(), guiFactory());
    }
}
