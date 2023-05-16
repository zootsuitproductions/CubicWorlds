package me.zootsuitproductions.cubicworlds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

public class SetBlocksOverTimeOperation implements ISetBlocksOverTimeOperation {

  int currrentX;
  int currentY;
  int currentZ;

  private final World world;
  private final int minCornerX;
  private final int minCornerY;
  private final int minCornerZ;

  private final int maxCornerX;
  private final int maxCornerY;
  private final int maxCornerZ;
  private final int blocksPerTick;

  private ISetBlocksOverTimeOperation nextOperation;


  private final Plugin plugin;

  SetBlocksOverTimeOperation(Location minCorner, Location maxCorner, int blocksPerTick, Plugin plugin, ISetBlocksOverTimeOperation nextOperation) {
    this.world = minCorner.getWorld();

    this.plugin = plugin;
    this.nextOperation = nextOperation;

    this.minCornerX = minCorner.getBlockX();
    this.minCornerY = minCorner.getBlockY();
    this.minCornerZ = minCorner.getBlockZ();

    currrentX = minCornerX;
    currentY = minCornerY;
    currentZ = minCornerZ;

    this.maxCornerX = maxCorner.getBlockX();
    this.maxCornerY = maxCorner.getBlockY();
    this.maxCornerZ = maxCorner.getBlockZ();

    this.blocksPerTick = blocksPerTick;

  }

  public void apply() {
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
          if (clearCurrentTickBlockQuotaAndReturnWhenDoneWithSection()) {
            Bukkit.getScheduler().cancelTasks(plugin);
            if (nextOperation != null) {
              nextOperation.apply();
            }
          }
        },
        0L, 1L);
  }


  private boolean clearCurrentTickBlockQuotaAndReturnWhenDoneWithSection() {
    int clearedThisTick = 0;

    for (currrentX = currrentX; currrentX <= maxCornerX; currrentX ++) {
      for (currentY = currentY; currentY <= maxCornerY; currentY ++) {
        for (currentZ = currentZ; currentZ <= maxCornerZ; currentZ ++) {
          // Stop clearing blocks for this tick if fulfilled quota
          if (clearedThisTick >= blocksPerTick) return false;

          world.getBlockAt(currrentX, currentY, currentZ).setType(Material.AIR);
          clearedThisTick++;
        } currentZ = minCornerZ;
      } currentY = minCornerY;
    }

    return true;
  }

}
