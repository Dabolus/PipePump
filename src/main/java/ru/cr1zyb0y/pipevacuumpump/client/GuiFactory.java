package ru.cr1zyb0y.pipevacuumpump.client;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import reborncore.common.screen.BuiltScreenHandler;

// Heavily based on (aka copy-pasted from) techreborn.client.GuiFactory
public interface GuiFactory<T extends BlockEntity> extends HandledScreens.Provider<BuiltScreenHandler, HandledScreen<BuiltScreenHandler>> {
    HandledScreen<BuiltScreenHandler> create(int syncId, PlayerEntity playerEntity, T blockEntity);

    @Override
    default HandledScreen<BuiltScreenHandler> create(BuiltScreenHandler builtScreenHandler, PlayerInventory playerInventory, Text text) {
        PlayerEntity playerEntity = playerInventory.player;
        //noinspection unchecked
        T blockEntity = (T) builtScreenHandler.getBlockEntity();
        return create(builtScreenHandler.syncId, playerEntity, blockEntity);
    }
}
