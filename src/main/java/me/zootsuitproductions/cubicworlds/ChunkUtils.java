package me.zootsuitproductions.cubicworlds;

import org.bukkit.Location;
import org.bukkit.World;

public class ChunkUtils {

  public static void forceLoadChunksAroundLocation(Location location, int radius) {

    int chunkRadius = radius / 16; // Convert radius to chunk radius

    // Calculate the chunk coordinates for the center location
    int centerChunkX = location.getBlockX() >> 4;
    int centerChunkZ = location.getBlockZ() >> 4;

    // Iterate over the chunks within the specified radius
    for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
      for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
        int chunkX = centerChunkX + dx;
        int chunkZ = centerChunkZ + dz;

        // Force load the chunk
        location.getWorld().setChunkForceLoaded(chunkX, chunkZ, true);
      }
    }
  }
}




