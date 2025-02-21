package ru.cr1zyb0y.pipevacuumpump.blocksentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import ru.cr1zyb0y.pipevacuumpump.RegistryManager;
import ru.cr1zyb0y.pipevacuumpump.blocks.MachineFluidPipePumpBlock;

public class MachineFluidPipePumpBlockEntity extends PowerAcceptorBlockEntity
{
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

        if (world == null || world.isClient)
        {
            return;
        }

        if (this._machinePipeFluidPump != null)
        {
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
                useEnergy(getEuPerTick(energyCost));

                if (!isActive)
                {
                    this._machinePipeFluidPump.setActive(true, world, pos);
                }
            }
            else if (isActive)
            {
                this._machinePipeFluidPump.setActive(false, world, pos);
            }
        }
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
}
