package me.zootsuitproductions.cubicworlds;

import java.util.Map;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.material.MaterialData;
import org.joml.Vector3d;

public class CubeFaceRegion {

  private final Location copyCenter;
  public final Location center;

  private final AxisTransformation transformation;

  //put in utils later:
  private static Vector3d getVectorFromBlockFace(BlockFace blockFace) {
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

  private static BlockFace getBlockFaceFromVector(Vector3d vector) {
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

  private static BlockFace rotateBlockFace(BlockFace blockFace, AxisTransformation transformation) {
    //fix this

    return getBlockFaceFromVector(
        transformation.unapply(
            getVectorFromBlockFace(blockFace)));
  }

  private static Axis rotateAxis(Axis axis, AxisTransformation transformation) {
    return getAxisFromVector(
        transformation.unapply(
            getVectorFromAxis(axis)));
  }

  private static Vector3d getVectorFromAxis(Axis axis) {
    switch (axis) {
      case X:
        return new Vector3d(1,0,0);
      case Y:
        return new Vector3d(0,1,0);
      default:
        return new Vector3d(0,0,1);
    }
  }

  private static Axis getAxisFromVector(Vector3d vector3d) {
    if (vector3d.x != 0) {
      return Axis.X;
    } else if (vector3d.y != 0) {
      return Axis.Y;
    } else {
      return Axis.Z;
    }
  }


  public static BlockData rotateBlockData(BlockData blockData, AxisTransformation transformation) {

    if (blockData instanceof Orientable) {
      Orientable orientable = (Orientable) blockData;

      try {
        orientable.setAxis(rotateAxis(orientable.getAxis(), transformation));

        return orientable;
      } catch (Exception e) {
      }
    }

    if (blockData instanceof Directional) {
      Directional directional = (Directional) blockData;

      try {
        BlockFace newDirection = rotateBlockFace(
            directional.getFacing(), transformation);
        directional.setFacing(newDirection);

        return directional;
      } catch (Exception e) {
      }

    }

    //changing pos x edge twice is messed up
    //changing anything twice is fucked.

    return blockData;
//
//     BlockData rotatedLogData = new Location(loc.getWorld(), 330, 159, 176).getBlock().getBlockData();
//     copyBlock.setBlockData(rotatedLogData);


    //just have the block in the world and copy from that
    //save the blockdata of rotated log in the persistent storage
    //set it to the saved copy and then set the material of that block data.


//    if (copyBlock.getBlockData().
//
//            hasProperty(BlockStateProperties.AXIS)) {
//      Direction.Axis axis = logState.get(BlockStateProperties.AXIS);
//      // Use the axis variable as needed
//    }


    //log axis y
    //half
//    if (blockData instanceof Rotatable) {
//      Rotatable rotatable = (Rotatable) blockData;
//      System.out.println(rotatable.getRotation());
//      try {
//        BlockFace newDirection = rotateBlockFace(
//                rotatable.getRotation(), transformation);
//        System.out.println(newDirection);
//
//        rotatable.setRotation(newDirection);
//        copyBlock.setBlockData(rotatable);
//      } catch (Exception e) {
//        System.out.println("error: " + e.getMessage());
//      }
//    } else if (blockData instanceof Directional) {
//
//      Directional directional = (Directional) blockData;
//      System.out.println(directional.getFacing());
//
//      try {
//        BlockFace newDirection = rotateBlockFace(
//                directional.getFacing(), transformation);
//        directional.setFacing(newDirection);
//        System.out.println(newDirection);
//        copyBlock.setBlockData(directional);
//      } catch (Exception e) {
//
//        System.out.println("error: " + e.getMessage());
//      }
//
//    }
  }

  private void copyRotateAndPaste(Vector3d worldCoordinate) {
    Vector3d rotatedCoordinate = transformation.unapply(worldCoordinate);

    Block copyBlock = new Location(copyCenter.getWorld(),
        copyCenter.getBlockX() + worldCoordinate.x,
        copyCenter.getBlockY() + worldCoordinate.y,
        copyCenter.getBlockZ()  + worldCoordinate.z).getBlock();

    BlockData blockData = copyBlock.getBlockData();

    Block pasteBlock =  new Location(copyCenter.getWorld(),
        center.getBlockX() + rotatedCoordinate.x,
        center.getBlockY() + rotatedCoordinate.y,
        center.getBlockZ()  + rotatedCoordinate.z).getBlock();

    System.out.println("mat: " + blockData.getMaterial());

    //change grass to moss. if its not facing up. need to check that

    pasteBlock.setBlockData(rotateBlockData(blockData, transformation));
//
//
//    if (blockData instanceof Rotatable) {
//      Rotatable rotatable = (Rotatable) blockData;
//      rotatable.setRotation(BlockFace.EAST);
//
//
//
//      System.out.println("mat rotate: " + rotatable.getMaterial());
//      try {
//        BlockFace newDirection = rotateBlockFace(
//            rotatable.getRotation(), transformation);
//
//        //set up the workflOWWWW
//
//        //is this failing??
//
//
//        //i need debugger
////
//
////        System.out.println("material: "+ directional.getMaterial() + " , facing: " + directional.getFacing() + ", new facing: " + newDirection);
//////        c.setFacing(BlockFace.EAST); // Change the direction as needed
//        rotatable.setRotation(newDirection);
//        copyBlock.setBlockData(rotatable);
//      } catch (Exception e) {
//        System.out.println("error: " + e.getMessage());
//      }
//
////      pasteBlock.setBlockData(newDirection?!?snakd);
//
//    } else if (blockData instanceof Directional) {
//
//      Directional directional = (Directional) blockData;
//
//      System.out.println("mat directional: " + directional.getMaterial());
////      directional.setFacing(BlockFace.EAST);
////      copyBlock.setBlockData(directional);
////
////      System.out.println("type: "  + copyBlock.getType());
////      System.out.println("FACING: "  + ((Directional) copyBlock.getBlockData()).getFacing());
////      System.out.println("faces: "  + ((Directional) copyBlock.getBlockData()).getFaces());
//
//      try {
//        BlockFace newDirection = rotateBlockFace(
//            directional.getFacing(), transformation);
////
////        System.out.println("material: "+ directional.getMaterial() + " , facing: " + directional.getFacing() + ", new facing: " + newDirection);
//////        c.setFacing(BlockFace.EAST); // Change the direction as needed
//        directional.setFacing(newDirection);
//        copyBlock.setBlockData(directional);
//      } catch (Exception e) {
//
//        System.out.println("error: " + e.getMessage());
//      }
//
//      pasteBlock.setBlockData(directional);
//    } else {
//      pasteBlock.setBlockData(blockData);
//    }


  }

  private static int lastCopyCenter = 0;
  private static int materialIndex = 0;
  private static Material[] mats = new Material[] {Material.BLUE_STAINED_GLASS,
      Material.RED_STAINED_GLASS,
      Material.WHITE_STAINED_GLASS,
      Material.GREEN_STAINED_GLASS,
      Material.YELLOW_STAINED_GLASS,
      Material.PURPLE_STAINED_GLASS
  };
  private static Material getNextStainedGlassColor(int copyCenterX) {
    if (copyCenterX == lastCopyCenter) {
      return  mats[materialIndex];
    } else {
      materialIndex ++;
      lastCopyCenter = copyCenterX;
      return mats[materialIndex];
    }
  }

  public CubeFaceRegion(Location centerToCopy, Location centerOfPaste, int radius, int xSliceToFindRadius, /*int xRot, int zRot,*/ AxisTransformation transformation) {
    center = centerOfPaste;
    copyCenter = centerToCopy;
    this.transformation = transformation;

    World world = centerToCopy.getWorld();

    int centerX = centerToCopy.getBlockX();
    //make the Y center of the world the lowest land on the Positive X Edge of the face
    int centerY = centerToCopy.getBlockY();
    int centerZ = centerToCopy.getBlockZ();


    //loop in an inverted pyramid below the center y
    for (int y = -radius; y < 0; y++) {
      for (int x = -(radius + y); x <= (radius + y); x++) {
        for (int z = -(radius + y); z <= (radius + y); z++) {
          copyRotateAndPaste(new Vector3d(x,y,z));
        }
      }
    }

    //THE PROBLEM ISNT THE PLACEMENT, ITS THE WORLD. THIS ROTATION FUNCTION IS WRONG


    //loop in a cube above the center y
    for (int y = 0; y <= radius; y++) {
      for (int x = -radius; x <= radius; x++) {
        for (int z = -radius; z <= radius; z++) {
          copyRotateAndPaste(new Vector3d(x,y,z));

        }
      }
    }
  }

  public static Location rotateLocation(Location point, int xRot, int zRot, int yRot, Location center) {
    int x = point.getBlockX();
    int y = point.getBlockY();
    int z = point.getBlockZ();

    int newX = x;
    int newY = y;
    int newZ = z;

    int centerX = center.getBlockX();
    int centerY = center.getBlockY();
    int centerZ = center.getBlockZ();

    if ((zRot) == 90) {
      newX = -(y - centerY) + centerX;
      newY = (x - centerX) + centerY;
    } else if (zRot == 270 || zRot == -90) {
      newX = (y - centerY) + centerX;
      newY = -(x - centerX) + centerY;
    } else if (zRot == 180) {
      newX = -(x - centerX) + centerX;
      newY = -(y - centerY) + centerY;
    }

    if (xRot == 90) {
      newZ = -(y - centerY) + centerZ;
      newY = (z - centerZ) + centerY;
    } else if (xRot == 270 || xRot == -90) {
      newZ = (y - centerY) + centerZ;
      newY = -(z - centerZ) + centerY;
    } else if (xRot == 180) {
      newZ = -(z - centerZ) + centerZ;
      newY = -(y - centerY) + centerY;
    }

    if (yRot == 90) {
      newZ = -(x - centerX) + centerZ;
      newX = (z - centerZ) + centerX;
    } else if (yRot == 270 || yRot == -90) {
      newZ = (x - centerX) + centerZ;
      newX = -(z - centerZ) + centerX;
    } else if (yRot == 180) {
      newZ = -(z - centerZ) + centerZ;
      newX = -(x - centerX) + centerX;
    }

    return new Location(point.getWorld(), newX, newY, newZ);
  }


  private Block ifOutsideOfReversePyramidTurnIntoAir(Block block, int x, int y, int z) {
    if (y < 0 && Math.abs(x) >= -y  && Math.abs(z) >= -y) {
      block.setType(Material.AIR);
    }

    return block;
  }

  private int findLowestPointOnXSlice(int x, int radius, Location center) {
    int lowestPoint = 170; //arbitrary high point in the world

    for (int z = -radius; z <= radius; z++) {
      for (int y = lowestPoint; y > 40; y--) {
        Block block = new Location(center.getWorld(), x, center.getBlockY() + y, center.getBlockZ() + z).getBlock();
        if (!block.getType().isAir()) {
          lowestPoint = y;
          break;
        }
      }
    }
    return lowestPoint;
  }

}