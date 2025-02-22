package ru.cr1zyb0y.pipevacuumpump.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import reborncore.api.ToolManager;
import reborncore.api.blockentity.IMachineGuiHandler;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.blocks.BlockMachineBase;
import reborncore.common.misc.ModSounds;
import ru.cr1zyb0y.pipevacuumpump.utils.VoxelShapeHelper;
import techreborn.init.TRBlockSettings;

public class MachinePipePumpBlockBase extends BlockMachineBase
{
    public static final DirectionProperty FACING = Properties.FACING;

    protected int _energyCost;
    protected int _pumpSpeedTick;
    protected VoxelShape[] _blockShapes;

    public MachinePipePumpBlockBase(int energyCost, int pumpSpeedTick)
    {
        super(TRBlockSettings.machine(), true);
        this.setDefaultState(
                this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(ACTIVE, false));
        _energyCost = energyCost;
        _pumpSpeedTick = pumpSpeedTick;
        _blockShapes = VoxelShapeHelper.getRotatedShapes(Direction.NORTH, getBaseShape());
    }

    private VoxelShape getBaseShape()
    {
        return VoxelShapes.union
        (
            VoxelShapes.cuboid(0f, 0f, 0.624f, 1.0f, 1.0f, 1.0f),
            VoxelShapes.cuboid(0.25f, 0.25f, 0.062f, 0.75f, 0.75f, 0.75f)
        );
    }

    //Now is no gui, later we need add GUI with "vacuuming progress" and upgrades
    @Override
    public IMachineGuiHandler getGui()
    {
        return null;
    }

    //Get pump item speed
    public int getEngineTickSpeed()
    {
        return _pumpSpeedTick;
    }

    //Base cost per tick
    public int getEnergyCost() { return _energyCost; }

    //make block valid view
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx)
    {
        Direction dir = state.get(FACING);
        return dir == Direction.UP || dir == Direction.DOWN
            ? _blockShapes[dir == Direction.UP ? 0 : 1]
            : _blockShapes[dir.getHorizontal() + 2];
    }

    @Override
    public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, BlockHitResult hitResult)
    {
		ItemStack tool = playerIn.getStackInHand(Hand.MAIN_HAND);
		if (!tool.isEmpty() && ToolManager.INSTANCE.canHandleTool(tool)) {
			if (ToolManager.INSTANCE.handleTool(tool, pos, worldIn, playerIn, hitResult.getSide(), false)) {
				if (playerIn.isSneaking()) {
					ItemStack drop = new ItemStack(this);
					dropStack(worldIn, pos, drop);
					worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), ModSounds.BLOCK_DISMANTLE,
							SoundCategory.BLOCKS, 0.6F, 1F);
					if (!worldIn.isClient) {
						worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
					}
					return ActionResult.SUCCESS;
				}
			}
		}
		return ActionResult.PASS;
	}

    // These are needed to allow vertical placement, as block machine base only allows horizontal placement by default
    @Override
    public void setFacing(Direction facing, World world, BlockPos pos) {
        world.setBlockState(pos, world.getBlockState(pos).with(FACING, facing));
    }

    @Override
    public Direction getFacing(BlockState state) {
        return state.get(FACING);
    }

    @Override
    public void setActive(Boolean active, World world, BlockPos pos) {
        Direction facing = world.getBlockState(pos).get(FACING);
        BlockState state = world.getBlockState(pos).with(ACTIVE, active).with(FACING, facing);
        world.setBlockState(pos, state, 3);
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
        // Place the pump in the direction the player is looking at, similarly to
        // how hoppers are placed, to make it easier to place it attached to a pipe
        setFacing(placer.getFacing(), worldIn, pos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }
}
