package ru.cr1zyb0y.pipevacuumpump.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import alexiil.mc.lib.attributes.item.impl.EmptyItemExtractable;
import reborncore.api.blockentity.IMachineGuiHandler;
import ru.cr1zyb0y.pipevacuumpump.blocksentity.GuiType;
import ru.cr1zyb0y.pipevacuumpump.blocksentity.MachinePipePumpBlockEntity;

import java.util.List;

public class MachinePipePumpBlock extends MachinePipePumpBlockBase implements AttributeProvider
{
    public MachinePipePumpBlock(int energyCost, int pumpSpeedTick)
    {
        super(energyCost, pumpSpeedTick);
    }

    // Create block entity
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
    {
        return new MachinePipePumpBlockEntity(pos, state);
    }

    // Make tooltip for block
    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options)
    {
        super.appendTooltip(stack, context, tooltip, options);
        if(Screen.hasShiftDown())
        {
            tooltip.add(Text.translatable("pipe_vacuum_pump.tooltip.vacuum_pump_block").formatted(Formatting.GOLD));
        }
        else
        {
            tooltip.add(Text.translatable("pipe_vacuum_pump.tooltip.hold_shift").formatted(Formatting.BLUE));
        }

        tooltip.add(Text.translatable("pipe_vacuum_pump.tooltip.speed",
                Formatting.GOLD, "1", getEngineTickSpeed()).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("pipe_vacuum_pump.tooltip.consumption",
                Formatting.GOLD, getEnergyCost()).formatted(Formatting.GRAY));
    }

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        Direction facing = state.get(FACING);
        if (to.getSearchDirection() == facing) {
            to.offer(EmptyItemExtractable.SUPPLIER);
        }
    }

    @Override
    public IMachineGuiHandler getGui()
    {
        return GuiType.MACHINE_PIPE_PUMP;
    }
}
