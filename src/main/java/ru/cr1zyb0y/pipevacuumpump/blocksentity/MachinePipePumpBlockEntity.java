package ru.cr1zyb0y.pipevacuumpump.blocksentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.minecraft.world.World;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;

import ru.cr1zyb0y.pipevacuumpump.RegistryManager;
import ru.cr1zyb0y.pipevacuumpump.blocks.MachinePipePumpBlock;

public class MachinePipePumpBlockEntity extends PowerAcceptorBlockEntity
{
    private MachinePipePumpBlock _machinePipePump;

    public MachinePipePumpBlockEntity(BlockPos pos, BlockState state)
    {
        super(RegistryManager.PIPE_PUMP_BLOCK_ENTITY, pos, state);
        Block block = state.getBlock();
        if (block instanceof MachinePipePumpBlock) {
            _machinePipePump = (MachinePipePumpBlock) block;
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

        if (this._machinePipePump != null)
        {
            boolean isActive = this._machinePipePump.isActive(state);

            //Redstone signal turn off engine
            if(world.isReceivingRedstonePower(getPos()))
            {
                if(isActive)
                {
                    this._machinePipePump.setActive(false, world, pos);
                }

                return;
            }

            //Consume energy
            int _energyCost = this._machinePipePump.getEnergyCost();
            long energyCost = getEuPerTick(_energyCost);
            if (getEnergy() > energyCost)
            {
                useEnergy(getEuPerTick(energyCost));

                if (!isActive)
                {
                    this._machinePipePump.setActive(true, world, pos);
                }
            }
            else if (isActive)
            {
                this._machinePipePump.setActive(false, world, pos);
            }
        }
    }

    // this is capacity
    @Override
    public long getBaseMaxPower() { return getBaseMaxInput() * 8L; } // 256 1024 4096 16384

    // return max input
    @Override
    public long getBaseMaxInput() {
        int energyCost = this._machinePipePump != null ? this._machinePipePump.getEnergyCost() : 1;
        return energyCost * 32L;
    } // 32 128 512 2048

    // we are not generating energy
    @Override
    public long getBaseMaxOutput() { return 0L; }

    // we are not generating energy
    @Override
    public boolean canProvideEnergy(final Direction direction) { return false; }
}
