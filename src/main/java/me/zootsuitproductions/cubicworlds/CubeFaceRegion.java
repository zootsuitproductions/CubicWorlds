package me.zootsuitproductions.cubicworlds;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

// represents a face on the cubic world
public class CubeFaceRegion {
  private final Location copyCenter;
  public final Location center;

  private final AxisTransformation transformation;

  private Map<Location, Block> sidewaysBlocks;

  private void copyRotateAndPaste(Vector3d worldCoordinate) {
    Vector3d rotatedCoordinate = transformation.apply(worldCoordinate);

    Block copyBlock = new Location(copyCenter.getWorld(),
        copyCenter.getBlockX() + worldCoordinate.x,
        copyCenter.getBlockY() + worldCoordinate.y,
        copyCenter.getBlockZ()  + worldCoordinate.z).getBlock();

    BlockData blockData = copyBlock.getBlockData();

    Block pasteBlock =  new Location(copyCenter.getWorld(),
        center.getBlockX() + rotatedCoordinate.x,
        center.getBlockY() + rotatedCoordinate.y,
        center.getBlockZ()  + rotatedCoordinate.z).getBlock();

    //change grass to moss. if its not facing up. need to check that

    if (copyBlock.getType() == Material.GRASS_BLOCK) {
      //for everything except the top face, replace grass blocks with moss
//      if (!transformation.equals(AxisTransformation.TOP)) {
        pasteBlock.setType(Material.MOSS_BLOCK);
        sidewaysBlocks.put(pasteBlock.getLocation(), copyBlock);
        return;
//      }
    }

    //add a live set up phase where the admin can set how high each side is on the world. use block commands to do it


    pasteBlock.setBlockData(TransformationUtils.rotateBlockData(blockData, transformation));
  }

  public CubeFaceRegion(Location centerToCopy, Location centerOfPaste, int radius, int xSliceToFindRadius, AxisTransformation transformation, Map<Location, Block> offSidesActualBlocks) {
    center = centerOfPaste;
    copyCenter = centerToCopy;
    this.transformation = transformation;
    this.sidewaysBlocks = offSidesActualBlocks;

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

    //loop in a cube above the center y
    for (int y = 0; y <= radius; y++) {
      for (int x = -radius; x <= radius; x++) {
        for (int z = -radius; z <= radius; z++) {
          copyRotateAndPaste(new Vector3d(x,y,z));

        }
      }
    }
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
