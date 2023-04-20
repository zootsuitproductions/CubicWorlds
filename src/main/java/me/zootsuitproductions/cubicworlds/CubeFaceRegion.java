package me.zootsuitproductions.cubicworlds;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class CubeFaceRegion {
  public CubeFaceRegion(Location centerToCopy, Location centerOfPaste, int radius, int xSliceToFindRadius, int xRot, int zRot) {
    World world = centerToCopy.getWorld();

    int centerX = centerToCopy.getBlockX();
    //make the Y center of the world the lowest land on the Positive X Edge of the face
    int centerY = centerToCopy.getBlockY();
    int centerZ = centerToCopy.getBlockZ();


    for (int y = -radius; y < 0; y++) {
      for (int x = -(radius + y); x <= (radius + y); x++) {
        for (int z = -(radius + y); z <= (radius + y); z++) {
          Block copyBlock = new Location(world, centerX + x, centerY + y, centerZ + z).getBlock();

          Location beforeRotatingPasteLoc = new Location(world, centerOfPaste.getBlockX() + x, centerOfPaste.getBlockY() + y, centerOfPaste.getBlockZ() + z);
          Location pasteLoc = rotateLocation(beforeRotatingPasteLoc, xRot, zRot, 0, centerOfPaste);

          pasteLoc.getBlock().setBlockData(copyBlock.getBlockData());
        }
      }
    }
    //loop in a cube above the center y
    //   should be  y = 0
    for (int y = 0; y <= radius; y++) {
      for (int x = -radius; x <= radius; x++) {
        for (int z = -radius; z <= radius; z++) {
          Block copyBlock = new Location(world, centerX + x, centerY + y, centerZ + z).getBlock();

          Location beforeRotatingPasteLoc = new Location(world, centerOfPaste.getBlockX() + x, centerOfPaste.getBlockY() + y, centerOfPaste.getBlockZ() + z);
          Location pasteLoc = rotateLocation(beforeRotatingPasteLoc, xRot, zRot, 0, centerOfPaste);

          pasteLoc.getBlock().setBlockData(copyBlock.getBlockData());
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

//
//  public CubeFaceRegion(Location centerInWorld, int radius, int xSliceToFindRadius) {
//    int size = 2 * radius + 1;
//
//    this.blocks = new Block[size][size][size];
//    this.offset = radius;
//    this.radius = radius;
//    this.center = centerInWorld;
//
//    World world = centerInWorld.getWorld();
//
//    int centerX = centerInWorld.getBlockX();
//    //make the Y center of the world the lowest land on the Positive X Edge of the face
//    int centerY = findLowestPointOnXSlice(xSliceToFindRadius);
//    int centerZ = centerInWorld.getBlockZ();
//
//    for (int x = -radius; x <= radius; x++) {
//      for (int y = -radius; y <= radius; y++) {
//        for (int z = -radius; z <= radius; z++) {
//          Block block = new Location(world, centerX + x, centerY + y, centerZ + z).getBlock();
//          blocks[x+offset][y+offset][z+offset] = ifOutsideOfReversePyramidTurnIntoAir(block, x, y, z);
//        }
//      }
//    }
//  }

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
/*
  public void pasteAtLocation(Location location) {
    this.center = location;

    World world = location.getWorld();

    int centerX = location.getBlockX();
    int centerY = location.getBlockY();
    int centerZ = location.getBlockZ();

    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        for (int z = -radius; z <= radius; z++) {
          BlockData data = getBlockAtRelativeCoords(x, y, z).getBlockData();

          if (data.getMaterial() != Material.AIR) {
            Block blockToSet = new Location(world, centerX + x, centerY + y, centerZ + z).getBlock();
            blockToSet.setBlockData(data);
          }
        }
      }
    }
  }*/
/*
  public CubeFaceRegion rotateZ(int degreesX, int degreesZ) {
    Block[][][] newBlocks = new Block[blocks.length][blocks[0].length][blocks[0][0].length];

    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        for (int z = -radius; z <= radius; z++) {
          int transformedX = x;
          int transformedY = y;

          if (degreesZ == 90) {
            transformedX = y;
            transformedY = -x;
          } else if (degreesZ == 180) {
            transformedX = -x;
            transformedY = -y;
          } else if (degreesZ == -90 || degreesZ == 270) {
            transformedX = -y;
            transformedY = x;
          }
          newBlocks[transformedX+offset][transformedY+offset][z+offset] = this.getBlockAtRelativeCoords(x,y,z);
        }
      }
    }
    return new CubeFaceRegion(newBlocks, this.offset, this.radius);
  }

  public CubeFaceRegion rotateX(int degreesX) {
    Block[][][] newBlocks = new Block[blocks.length][blocks[0].length][blocks[0][0].length];

    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        for (int z = -radius; z <= radius; z++) {
          int transformedY = y;
          int transformedZ = z;

          if (degreesX == 90) {
            transformedY = -z;
            transformedZ = y;
          } else if (degreesX == 180) {
            transformedY = -y;
            transformedZ = -z;
          } else if (degreesX == -90 || degreesX == 270) {
            transformedY = z;
            transformedZ = -y;
          }
          newBlocks[x+offset][transformedY+offset][transformedZ+offset] = this.getBlockAtRelativeCoords(x,y,z);
        }
      }
    }
    return new CubeFaceRegion(newBlocks, this.offset, this.radius);
  }

  private Block getBlockAtRelativeCoords(int x, int y, int z) {
    return blocks[x+offset][y+offset][z+offset];
  }*/

}
