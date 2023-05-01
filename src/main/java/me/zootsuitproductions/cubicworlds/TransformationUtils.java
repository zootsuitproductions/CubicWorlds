package me.zootsuitproductions.cubicworlds;

import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Stairs;
import org.joml.Vector3d;

public class TransformationUtils {

    private Bisected.Half flip(Bisected.Half half) {
        if (half == Bisected.Half.TOP) {
            return Bisected.Half.BOTTOM;
        } else {
            return Bisected.Half.TOP;
        }
    }
//    private Stairs rotateStairs(Stairs stairs, AxisTransformation transformation) {
//
//        //if the rotation is in the same direction as it is facing, flip the facing by 180
//        //if its the opposite direction, flip the half
//        //
//        // we can do it
//        //
//
//        if (transformation == AxisTransformation.BOTTOM)
//            stairs.setHalf(flip(stairs.getHalf()));
//        }
//    }

    public static BlockData rotateBlockData(BlockData blockData, AxisTransformation transformation) {

        if (blockData instanceof Stairs) {
            Stairs stairs = (Stairs) blockData;

        }
        if (blockData instanceof Orientable) {
            Orientable orientable = (Orientable) blockData;

            try {
                orientable.setAxis(TransformationUtils.rotateAxis(orientable.getAxis(), transformation));

                return orientable;
            } catch (Exception e) {
            }
        }

        if (blockData instanceof Directional) {
            Directional directional = (Directional) blockData;

            try {
                BlockFace newDirection = TransformationUtils.rotateBlockFace(
                        directional.getFacing(), transformation);
                directional.setFacing(newDirection);

                return directional;
            } catch (Exception e) {
            }

        }

        if (blockData instanceof Rotatable) {
            Rotatable rotatable = (Rotatable) blockData;

            try {
                BlockFace newDirection = TransformationUtils.rotateBlockFace(
                        rotatable.getRotation(), transformation);
                rotatable.setRotation(newDirection);

                return rotatable;
            } catch (Exception e) {
            }

        }
        return blockData;
    }

    public static Vector3d getVectorFromBlockFace(BlockFace blockFace) {
        switch (blockFace) {
            case UP:
                return new Vector3d(0,1,0);
            case DOWN:
                return new Vector3d(0,-1,0);
            case NORTH:
                return new Vector3d(0,0,-1);
            case SOUTH:
                return new Vector3d(0,0,1);
            case EAST:
                return new Vector3d(1,0,0);
            default:
                return new Vector3d(-1,0,0);
        }
    }

    public static BlockFace getBlockFaceFromVector(Vector3d vector) {
        if (vector.x > 0) {
            return BlockFace.EAST;
        } else if (vector.x < 0) {
            return BlockFace.WEST;
        } else if (vector.y > 0) {
            return BlockFace.UP;
        } else if (vector.y < 0) {
            return BlockFace.DOWN;
        } else if (vector.z > 0) {
            return BlockFace.SOUTH;
        } else {
            return BlockFace.NORTH;
        }
    }

    public static BlockFace rotateBlockFace(BlockFace blockFace, AxisTransformation transformation) {
        return getBlockFaceFromVector(
                transformation.apply(
                        getVectorFromBlockFace(blockFace)));
    }

    public static Axis rotateAxis(Axis axis, AxisTransformation transformation) {
        return getAxisFromVector(
                transformation.apply(
                        getVectorFromAxis(axis)));
    }

    public static Vector3d getVectorFromAxis(Axis axis) {
        switch (axis) {
            case X:
                return new Vector3d(1,0,0);
            case Y:
                return new Vector3d(0,1,0);
            default:
                return new Vector3d(0,0,1);
        }
    }

    public static Axis getAxisFromVector(Vector3d vector3d) {
        if (vector3d.x != 0) {
            return Axis.X;
        } else if (vector3d.y != 0) {
            return Axis.Y;
        } else {
            return Axis.Z;
        }
    }

}
