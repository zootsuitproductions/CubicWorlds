package me.zootsuitproductions.cubicworlds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3d;

public class CopyAndRotateEntireCubeOperation implements ISetBlocksOverTimeOperation {
  private final Location copyCenter;
  public final Location pasteCenter;
  private final int radius;
  private final AxisTransformation transformation;
  private final int blocksPerTick;
  private final Plugin plugin;
  private final ISetBlocksOverTimeOperation nextOperation;
  int currentX;
  int currentY;
  int currentZ;

  CopyAndRotateEntireCubeOperation(Location centerToCopy, Location centerOfPaste, int radius, AxisTransformation transformation, int blocksPerTick, Plugin plugin, ISetBlocksOverTimeOperation nextOp) {
    pasteCenter = centerOfPaste;
    copyCenter = centerToCopy;
    this.transformation = transformation;
    this.radius = radius;
    this.blocksPerTick = blocksPerTick;
    this.plugin = plugin;
    this.nextOperation = nextOp;
  }

  public void apply() {
    System.out.println("applying new opp");
    System.out.println(transformation);

    System.out.println("copy center: " + copyCenter);
    System.out.println("paste center: " + pasteCenter);
    applyCube();
  }

  private void applyCube() {
    currentY = -radius;
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

  private void copyRotateAndPaste(Vector3d worldCoordinate) {
    Vector3d rotatedCoordinate = transformation.apply(worldCoordinate);

    Block copyBlock = new Location(copyCenter.getWorld(),
        copyCenter.getBlockX() + worldCoordinate.x,
        copyCenter.getBlockY() + worldCoordinate.y,
        copyCenter.getBlockZ()  + worldCoordinate.z).getBlock();

    BlockData blockData = copyBlock.getBlockData();

    System.out.println(copyBlock.getLocation() + ", " + blockData.getMaterial() );

    Block pasteBlock =  new Location(pasteCenter.getWorld(),
        pasteCenter.getBlockX() + rotatedCoordinate.x,
        pasteCenter.getBlockY() + rotatedCoordinate.y,
        pasteCenter.getBlockZ()  + rotatedCoordinate.z).getBlock();

    pasteBlock.setBlockData(TransformationUtils.rotateBlockData(blockData, transformation));

    if (blockData.getMaterial() == Material.AIR || blockData.getMaterial() == Material.LAVA) {

      //used to disable world water flowing
      pasteBlock.setBiome(Biome.THE_VOID);
    }

    //do a check to see if its face up. and unfreeze the water
  }
}
