package me.zootsuitproductions.cubicworlds;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FaceFlattener {

  private final World world;
  private final Location center;

  private final int radius;

  private List<Material> aboveGroundBlocks = new ArrayList<>();

  //algo: get ground level, make all edges ground level the desired level. then, move in a ring inwards,

  FaceFlattener(Location center, int radius, Player player) {
    aboveGroundBlocks.add(Material.AIR);
    aboveGroundBlocks.add(Material.GRASS);
    aboveGroundBlocks.add(Material.TALL_GRASS);
    aboveGroundBlocks.add(Material.OAK_LOG);
    aboveGroundBlocks.add(Material.OAK_LEAVES);
    aboveGroundBlocks.add(Material.SPRUCE_LOG);
    aboveGroundBlocks.add(Material.SPRUCE_LEAVES);
    aboveGroundBlocks.add(Material.BIRCH_LOG);
    aboveGroundBlocks.add(Material.BIRCH_LEAVES);
    aboveGroundBlocks.add(Material.ACACIA_LOG);
    aboveGroundBlocks.add(Material.ACACIA_LEAVES);
    aboveGroundBlocks.add(Material.DARK_OAK_LOG);
    aboveGroundBlocks.add(Material.DARK_OAK_LEAVES);

    this.world = center.getWorld();
    this.center = center;
    this.radius = radius;
//    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"/world " + world.getName());


    for (int x = -radius; x <= radius; x ++) {
      for (int z = -radius; z <= radius; z ++) {
//      	int shift = getNumberToOffsetYLevelSlice(x, center.getBlockY(), z);


        //find max and min on the ring and make them within the distanceToEdge of the center.getBlockY value

        int currentY = getGroundLevel(x,z);

        int shift = center.getBlockY() - currentY;

//        int maxY - currentY;

//        if (center.getBlockY() - currentY < )

        int xDistanceToEdge = radius - Math.abs(x);
        int zDistanceToEdge = radius - Math.abs(z);


        //ring number
        int distanceToEdge = Math.min(xDistanceToEdge,zDistanceToEdge);

        int maxY = center.getBlockY() + distanceToEdge + 1;
        int minY = center.getBlockY() - distanceToEdge - 1;


        player.sendMessage("_______"
        );
        player.sendMessage("distanceToEdge" + distanceToEdge );
        player.sendMessage("og shift" + shift);

        player.sendMessage("maxShift" + maxY);

        player.sendMessage("mixShift" + minY);

        if (shift < 0) {
          shift = Math.max(shift, minY);
        } else {
          shift = Math.min(shift, maxY);
        }

        player.sendMessage("newshift" + shift);
        shiftSectionBy(shift, x, z);
//
//        if (distanceToEdge == 0) {
//          clampSectionBetweenYs(x,z,minAllowedHeight, maxAllowedHeight);
//          shiftSectionBy(shift, x, z);
//        }
////
//      	if (shift < 0) {
//      		shift += distanceToEdge;
//
//          if (shift > 0) shift = 0;
//      	} else {
//          shift -= distanceToEdge;
//
//          if (shift < 0) shift = 0;
//        }



        player.sendMessage("ring number of " + x + ", " + z + ": " + distanceToEdge);
//        while (getBlockTypeAtRelativeLocation(x,center.getBlockY(),z) != Material.AIR) {
//          //lower the 1 block section
//          //raise this 1 block vertical slice:
//          Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"/pos1 " );
//
//        }
      }
    }
  }

//  private void clampSectionBetweenYs(int x, int z, int minY, int maxY) {
//    if (getNumberToOffsetYLevelSlice(x, center.getBlockY(), z);)
//  }

  private Material getBlockTypeAtRelativeLocation(int x, int y, int z) {
    return new Location(world, center.getBlockX() + x, y, center.getBlockZ() + z).getBlock()
        .getType();
  }

  private int findYGroundFromAbove(int x, int currentY, int z) {
    for (int i = 0; i < aboveGroundBlocks.size(); i++) {
      if (getBlockTypeAtRelativeLocation(x, currentY, z) == aboveGroundBlocks.get(i)) {
        //still above ground
        return findYGroundFromAbove(x, currentY - 1, z);
      }
    }

    //we have hit ground:
    return currentY;
  }

  private int findYGroundFromBelow(int x, int currentY, int z) {
    for (int i = 0; i < aboveGroundBlocks.size(); i++) {
      if (getBlockTypeAtRelativeLocation(x, currentY, z) == aboveGroundBlocks.get(i)) {
        ///we have hit ground:
        return currentY - 1; //return the y of the block below this air/above ground block (to get the ground level)
      }
    }

    //still below ground
    return findYGroundFromAbove(x, currentY + 1, z);

  }

  private int getGroundLevel(int x, int z) {
    for (int i = 0; i < aboveGroundBlocks.size(); i++) {
      if (getBlockTypeAtRelativeLocation(x, center.getBlockY(), z) == aboveGroundBlocks.get(i)) {
        //still above ground
        return findYGroundFromAbove(x, center.getBlockY() - 1, z);
      }
    }

    return findYGroundFromBelow(x, center.getBlockY() + 1, z);
  }

  private int getNumberToOffsetYLevelSlice(int x,  int desiredGroundLevel, int z) {
    for (int i = 0; i < aboveGroundBlocks.size(); i++) {
      if (getBlockTypeAtRelativeLocation(x, desiredGroundLevel, z) == aboveGroundBlocks.get(i)) {
        //still above ground
        return center.getBlockY() - findYGroundFromAbove(x, desiredGroundLevel - 1, z);
      }
    }

    return center.getBlockY() - findYGroundFromBelow(x, desiredGroundLevel + 1, z);
  }

  private void shiftSectionBy(int verticalShift, int x, int z) {
    if (verticalShift == 0) return;

    if (verticalShift < 0) {
      //shift down, start from bottom up
      for (int y = center.getBlockY() - radius + verticalShift; y < center.getBlockY() + radius - verticalShift; y++) {
        new Location(world, center.getBlockX() + x, y, center.getBlockZ() + z).getBlock().setBlockData(new Location(world, center.getBlockX() + x, y - verticalShift, center.getBlockZ() + z).getBlock()
            .getBlockData());
      }
    } else {
        //shift up, start from top down
      for (int y = center.getBlockY() + radius + verticalShift; y > center.getBlockY() - radius - verticalShift; y--) {
        new Location(world, center.getBlockX() + x, y, center.getBlockZ() + z).getBlock().setBlockData(new Location(world, center.getBlockX() + x, y - verticalShift, center.getBlockZ() + z).getBlock()
            .getBlockData());
      }
    }

  }
}
