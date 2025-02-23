package ru.cr1zyb0y.pipevacuumpump.blocksentity;

import alexiil.mc.lib.attributes.CombinableAttribute;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.fluid.world.FluidWorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import ru.cr1zyb0y.pipevacuumpump.RegistryManager;
import ru.cr1zyb0y.pipevacuumpump.blocks.MachineFluidPipePumpBlock;

public class MachineFluidPipePumpBlockEntity extends PowerAcceptorBlockEntity
{
    private FluidVolume storedFluid = FluidVolumeUtil.EMPTY;

    private MachineFluidPipePumpBlock _machinePipeFluidPump;

    public MachineFluidPipePumpBlockEntity(BlockPos pos, BlockState state)
    {
        super(RegistryManager.FLUID_PIPE_PUMP_BLOCK_ENTITY, pos, state);
        Block block = state.getBlock();
        if (block instanceof MachineFluidPipePumpBlock) {
            _machinePipeFluidPump = (MachineFluidPipePumpBlock) block;
        }
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
            if (!storedFluid.isEmpty()) {
                return;
            }
        }
        FluidVolume drained = FluidWorldUtil.drain(getWorld(), getPos().offset(facing), Simulation.ACTION);
        if (!drained.isEmpty()) {
            storedFluid = drained;
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
        storedFluid = FluidVolume.fromTag(tag.getCompound("fluid"));
    }

    @Override
    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(tag, lookup);
        tag.put("fluid", storedFluid.toTag());
    }

}
