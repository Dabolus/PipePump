package ru.cr1zyb0y.pipevacuumpump.blocksentity;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import reborncore.api.blockentity.IMachineGuiHandler;
import reborncore.common.network.BlockPosPayload;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;

// Heavily based on (aka copy-pasted from) techreborn.blockentity.GuiType
public record GuiType<T extends BlockEntity>(Identifier identifier, ScreenHandlerType<BuiltScreenHandler> screenHandlerType) implements IMachineGuiHandler {
	public static final GuiType<MachinePipePumpBlockEntity> MACHINE_PIPE_PUMP = register("vacuum_pump");
	public static final GuiType<MachineFluidPipePumpBlockEntity> MACHINE_FLUID_PIPE_PUMP = register("vacuum_fluid_pump");

	private static <T extends BlockEntity> GuiType<T> register(String path) {
		var id = Identifier.of("pipe_vacuum_pump", path);
		var screenHandlerType = Registry.register(Registries.SCREEN_HANDLER, id, new ExtendedScreenHandlerType<>(getScreenHandlerFactory(id), ScreenHandlerData.PACKET_CODEC));
		return new GuiType<>(id, screenHandlerType);
	}

	private static ExtendedScreenHandlerType.ExtendedFactory<BuiltScreenHandler, ScreenHandlerData> getScreenHandlerFactory(Identifier identifier) {
		return (syncId, playerInventory, payload) -> {
			if (!payload.isWithinDistance(playerInventory.player, 16)) {
				throw new IllegalStateException("Player cannot use this block entity as its too far away");
			}

			final BlockEntity blockEntity = playerInventory.player.getWorld().getBlockEntity(payload.pos());
			BuiltScreenHandler screenHandler = ((BuiltScreenHandlerProvider) blockEntity).createScreenHandler(syncId, playerInventory.player);

			//noinspection unchecked
			screenHandler.setType((ScreenHandlerType<BuiltScreenHandler>) Registries.SCREEN_HANDLER.get(identifier));
			return screenHandler;
		};
	}

	public T getBlockEntity(ServerPlayNetworking.Context context, BlockPosPayload posPayload, BlockEntityType<T> blockEntityType) {
		if (!posPayload.canUse(context.player(), screenHandler -> screenHandler.getType() == screenHandlerType)) {
			throw new IllegalStateException("Player cannot use this block entity");
		}

		return posPayload.getBlockEntity(blockEntityType, context.player());
	}

	@Override
	public void open(PlayerEntity player, BlockPos pos, World world) {
		if (!world.isClient) {
			//This is awful
			player.openHandledScreen(new ExtendedScreenHandlerFactory<ScreenHandlerData>() {
				@Override
				public ScreenHandlerData getScreenOpeningData(ServerPlayerEntity player) {
					return new ScreenHandlerData(pos);
				}

				@Override
				public Text getDisplayName() {
					return Text.literal("What is this for?");
				}

				@Override
				public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
					final BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
					BuiltScreenHandler screenHandler = ((BuiltScreenHandlerProvider) blockEntity).createScreenHandler(syncId, player);
					screenHandler.setType(screenHandlerType);
					return screenHandler;
				}
			});
		}
	}

	record ScreenHandlerData(BlockPos pos) implements BlockPosPayload {
		public static final PacketCodec<RegistryByteBuf, ScreenHandlerData> PACKET_CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC, ScreenHandlerData::pos,
			ScreenHandlerData::new
		);
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public ScreenHandlerType<BuiltScreenHandler> getScreenHandlerType() {
		return screenHandlerType;
	}
}
