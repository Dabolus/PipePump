package ru.cr1zyb0y.pipevacuumpump.blocksentity;

import alexiil.mc.lib.attributes.CombinableAttribute;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.fluid.world.FluidWorldUtil;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.fluid.FluidValue;
import reborncore.common.fluid.container.FluidInstance;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;
import reborncore.common.screen.builder.ScreenHandlerBuilder;
import reborncore.common.util.Tank;
import ru.cr1zyb0y.pipevacuumpump.RegistryManager;
import ru.cr1zyb0y.pipevacuumpump.blocks.MachineFluidPipePumpBlock;
import techreborn.blockentity.machine.multiblock.IndustrialGrinderBlockEntity;

public class MachineFluidPipePumpBlockEntity extends PowerAcceptorBlockEntity implements BuiltScreenHandlerProvider
{
    private FluidVolume storedFluid = FluidVolumeUtil.EMPTY;
    public final Tank tank;

    private void syncTankContent() {
        Fluid fluid = storedFluid.getRawFluid();
        if (fluid != null) {
            this.tank.setFluid(fluid);
            this.tank.setFluidAmount(FluidValue.fromMillibuckets(storedFluid.amount().asLong(1000)));
        }
    }

    private MachineFluidPipePumpBlock _machinePipeFluidPump;

    public MachineFluidPipePumpBlockEntity(BlockPos pos, BlockState state)
    {
        super(RegistryManager.FLUID_PIPE_PUMP_BLOCK_ENTITY, pos, state);
        Block block = state.getBlock();
        if (block instanceof MachineFluidPipePumpBlock) {
            _machinePipeFluidPump = (MachineFluidPipePumpBlock) block;
        }
        this.tank = new Tank("vacuum_fluid_pump", FluidValue.BUCKET);
        this.checkTier();
    }

    //Consuming energy
    @Override
    public void tick(World world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity)
    {
        super.tick(world, pos, state, blockEntity);

        if (world == null || world.isClient || this._machinePipeFluidPump == null)
        {
            return;
        }

        boolean isActive = this._machinePipeFluidPump.isActive(state);

        //Redstone signal turn off engine
        if(world.isReceivingRedstonePower(getPos()))
        {
            if(isActive)
            {
                this._machinePipeFluidPump.setActive(false, world, pos);
            }

            return;
        }

        //Consume energy
        int _energyCost = this._machinePipeFluidPump.getEnergyCost();
        long energyCost = getEuPerTick(_energyCost);
        if (getEnergy() > energyCost)
        {
            useEnergy(energyCost);

            if (!isActive)
            {
                this._machinePipeFluidPump.setActive(true, world, pos);
            }
        }
        else if (isActive)
        {
            this._machinePipeFluidPump.setActive(false, world, pos);
        }

        // No energy: do not try to extract fluid
        if (!isActive)
        {
            return;
        }

        Direction facing = state.get(MachineFluidPipePumpBlock.FACING);
        if (!storedFluid.isEmpty()) {
            FluidInsertable insertable = getNeighbourAttribute(FluidAttributes.INSERTABLE, facing.getOpposite());
            storedFluid = insertable.attemptInsertion(storedFluid, Simulation.ACTION);
            this.syncTankContent();
            if (!storedFluid.isEmpty()) {
                return;
            }
        }
        FluidVolume drained = FluidWorldUtil.drain(getWorld(), getPos().offset(facing), Simulation.ACTION);
        if (!drained.isEmpty()) {
            storedFluid = drained;
            this.syncTankContent();
        }
    }

    public <T> T getNeighbourAttribute(CombinableAttribute<T> attr, Direction dir) {
        return attr.get(getWorld(), getPos().offset(dir), SearchOptions.inDirection(dir));
    }

    // this is capacity
    @Override
    public long getBaseMaxPower() { return getBaseMaxInput() * 8L; } // 256 1024 4096 16384

    // return max input
    @Override
    public long getBaseMaxInput() {
        int energyCost = this._machinePipeFluidPump != null ? this._machinePipeFluidPump.getEnergyCost() : 1;
        return energyCost * 32L;
    } // 32 128 512 2048

    // we are not generating energy
    @Override
    public long getBaseMaxOutput() { return 0L; }

    // we are not generating energy
    @Override
    public boolean canProvideEnergy(final Direction direction) { return false; }


    @Override
    public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(tag, lookup);
        tank.read(tag, lookup);
        storedFluid = FluidVolume.fromTag(tag.getCompound("fluid"));
    }

    @Override
    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(tag, lookup);
        tank.write(tag, lookup);
        tag.put("fluid", storedFluid.toTag());
    }

    @Nullable
    @Override
    public Tank getTank() {
        return tank;
    }

    @Override
    public BuiltScreenHandler createScreenHandler(int syncID, final PlayerEntity player) {
        // fluidSlot first to support automation and shift-click
        return new ScreenHandlerBuilder("machine_fluid_pipe_pump").player(player.getInventory()).inventory().hotbar().addInventory()
                .blockEntity(this).fluidSlot(1, 34, 35).slot(0, 84, 43).energySlot(7, 8, 72)
                .sync(tank)
                .syncEnergyValue().sync(PacketCodecs.VAR_LONG, this::getBaseMaxPower, /* no-op */ v -> {})
                .addInventory().create(this, syncID);
    }
}
