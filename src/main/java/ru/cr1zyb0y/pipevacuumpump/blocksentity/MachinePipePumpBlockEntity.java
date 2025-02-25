package ru.cr1zyb0y.pipevacuumpump.blocksentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.minecraft.world.World;

import alexiil.mc.lib.attributes.CombinableAttribute;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.ItemExtractable;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;

import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;
import reborncore.common.screen.builder.ScreenHandlerBuilder;
import ru.cr1zyb0y.pipevacuumpump.RegistryManager;
import ru.cr1zyb0y.pipevacuumpump.blocks.MachinePipePumpBlock;

public class MachinePipePumpBlockEntity extends PowerAcceptorBlockEntity implements BuiltScreenHandlerProvider
{
    private ItemStack storedItems = ItemStack.EMPTY;
    private int _lastTick;

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

        if (world == null || world.isClient || this._machinePipePump == null)
        {
            return;
        }

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
            useEnergy(energyCost);

            if (!isActive)
            {
                this._machinePipePump.setActive(true, world, pos);
            }
        }
        else if (isActive)
        {
            this._machinePipePump.setActive(false, world, pos);
        }

        // No energy: do not try to extract items
        if (!isActive)
        {
            return;
        }

        // Wait ticks based on machine tier before extracting items
        _lastTick++;
        if(_lastTick < this._machinePipePump.getEngineTickSpeed())
        {
            return;
        }
        _lastTick = 0;

        Direction facing = state.get(MachinePipePumpBlock.FACING);
        if (!storedItems.isEmpty()) {
            ItemInsertable insertable = getNeighbourAttribute(ItemAttributes.INSERTABLE, facing.getOpposite());
            storedItems = insertable.attemptInsertion(storedItems, Simulation.ACTION);
            if (!storedItems.isEmpty()) {
                return;
            }
        }
        ItemExtractable extractable = getNeighbourAttribute(ItemAttributes.EXTRACTABLE, facing);
        ItemStack extracted = extractable.attemptAnyExtraction(1, Simulation.ACTION);
        if (!extracted.isEmpty()) {
            storedItems = extracted;
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
        int energyCost = this._machinePipePump != null ? this._machinePipePump.getEnergyCost() : 1;
        return energyCost * 32L;
    } // 32 128 512 2048

    // we are not generating energy
    @Override
    public long getBaseMaxOutput() { return 0L; }

    // we are not generating energy
    @Override
    public boolean canProvideEnergy(final Direction direction) { return false; }

    @Override
    public BuiltScreenHandler createScreenHandler(int syncID, final PlayerEntity player) {
        return new ScreenHandlerBuilder("machine_pipe_pump").player(player.getInventory()).inventory().hotbar().addInventory()
                .blockEntity(this).slot(0, 84, 43).energySlot(7, 8, 72)
                .syncEnergyValue().sync(PacketCodecs.VAR_LONG, this::getBaseMaxPower, /* no-op */ v -> {})
                .addInventory().create(this, syncID);
    }
}
