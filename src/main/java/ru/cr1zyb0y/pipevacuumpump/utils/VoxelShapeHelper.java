package ru.cr1zyb0y.pipevacuumpump.utils;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class VoxelShapeHelper
{
    public static VoxelShape[] getRotatedShapes(Direction from, VoxelShape source)
    {
        VoxelShape shapeUp = rotateShapeVertically(from, Direction.AxisDirection.POSITIVE, source);
        VoxelShape shapeDown = rotateShapeVertically(from, Direction.AxisDirection.NEGATIVE, source);
        VoxelShape shapeNorth = rotateShapeHorizontally(from, Direction.NORTH, source);
        VoxelShape shapeEast = rotateShapeHorizontally(from, Direction.EAST, source);
        VoxelShape shapeSouth = rotateShapeHorizontally(from, Direction.SOUTH, source);
        VoxelShape shapeWest = rotateShapeHorizontally(from, Direction.WEST, source);
        return new VoxelShape[] { shapeUp, shapeDown, shapeSouth, shapeWest, shapeNorth, shapeEast };
    }

    public static VoxelShape rotateShapeVertically(Direction from, Direction.AxisDirection axisDirection, VoxelShape shape)
    {
        // Based on the input horizontal direction (NORTH, EAST, SOUTH, WEST), and on the destination vertical axis direction (POSITIVE, NEGATIVE),
        // we need to rotate the voxel shape to the correct orientation.
        final VoxelShape[] output = {VoxelShapes.empty()};

        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> output[0] =
                VoxelShapes.union(output[0], VoxelShapes.cuboid(
                        minY, axisDirection == Direction.AxisDirection.POSITIVE ? 1-maxZ : minZ, minX,
                        maxY, axisDirection == Direction.AxisDirection.POSITIVE ? 1-minZ : maxZ, maxX)));

        return output[0];
    }

    public static VoxelShape rotateShapeHorizontally(Direction from, Direction to, VoxelShape shape)
    {
        VoxelShape[] buffer = new VoxelShape[] { shape, VoxelShapes.empty() };

        int times = (to.getHorizontal() - from.getHorizontal() + 4) % 4;

        for (int i = 0; i < times; i++)
        {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] =
                    VoxelShapes.union(buffer[1], VoxelShapes.cuboid(1-maxZ, minY, minX, 1-minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0];
    }
}
