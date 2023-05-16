package me.zootsuitproductions.cubicworlds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3d;

public class CopyAndRotateCubeFaceOperation implements ISetBlocksOverTimeOperation {
  private final Location copyCenter;
  public final Location center;
  private final int radius;

  private final AxisTransformation transformation;

  private final int blocksPerTick;
  private final Plugin plugin;
  private final ISetBlocksOverTimeOperation nextOperation;
  int currentX;

  int currentY;
  int currentZ;

  CopyAndRotateCubeFaceOperation(Location centerToCopy, Location centerOfPaste, int radius, AxisTransformation transformation, int blocksPerTick, Plugin plugin, ISetBlocksOverTimeOperation nextOp) {
    center = centerOfPaste;
    copyCenter = centerToCopy;
    this.transformation = transformation;
    this.radius = radius;
    this.blocksPerTick = blocksPerTick;
    this.plugin = plugin;
    this.nextOperation = nextOp;
  }

  public void apply() {
    System.out.println("applying new opp");
    applyPyramid();
  }


  private void applyPyramid() {
    currentY = -radius;
    currentX = -(radius + currentY);
    currentZ = -(radius + currentY);

    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
          if (copyPyramid()) {
            Bukkit.getScheduler().cancelTasks(plugin);
            applyCube();
          }
        },
        0L, 1L);
  }

  private void applyCube() {
    currentY = 0;
    currentX = -radius;
    currentZ = -radius;

    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
          if (copyCube()) {
            Bukkit.getScheduler().cancelTasks(plugin);
            if (nextOperation != null) {
              nextOperation.apply();
            }
          }
        },
        0L, 1L);
  }

  private boolean copyCube() {
    int clearedThisTick = 0;

    //loop in a cube above the center y
    for (currentY = currentY; currentY <= radius; currentY++) {
      for (currentX = currentX; currentX <= radius; currentX++) {
        for (currentZ = currentZ; currentZ <= radius; currentZ++) {
          if (clearedThisTick >= blocksPerTick) return false;

          copyRotateAndPaste(new Vector3d(currentX,currentY,currentZ));
          clearedThisTick++;
        } currentZ = -radius;
      } currentX = -radius;
    }

    return true;
  }

  private boolean copyPyramid() {
    int clearedThisTick = 0;

    //loop in an inverted pyramid below the center y
    for (currentY = currentY; currentY < 0; currentY++) {
      for (currentX = currentX; currentX <= (radius + currentY); currentX++) {
        for (currentZ = currentZ; currentZ <= (radius + currentY); currentZ++) {

          if (clearedThisTick >= blocksPerTick) return false;

          copyRotateAndPaste(new Vector3d(currentX,currentY,currentZ));
          clearedThisTick++;
        }
        currentZ = -(radius + (currentY));
      }
      currentX = -(radius + (currentY + 1)); //use the next y level
      currentZ = -(radius + (currentY + 1)); //use the next y level
    }

    return true;
  }


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

    System.out.println(pasteBlock.getLocation().toString());
    pasteBlock.setBlockData(TransformationUtils.rotateBlockData(blockData, transformation));
  }
}
