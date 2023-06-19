package me.zootsuitproductions.cubicworlds;

import static me.zootsuitproductions.cubicworlds.FileUtils.createAndWriteFile;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class WECubeWorldSaver {

  public final int xRadius;
  public final int yRadius;
  public final int zRadius;

  WECubeWorldSaver(int radius) {
    this.xRadius = radius;
    this.yRadius = radius;
    this.zRadius = radius;
  }

  public void saveFacesAroundLocation(Location centerToCopyFrom, Plugin plugin) {
    List<String> commands = new ArrayList<>();
    List<Integer> delays = new ArrayList<>();

    for (int i = 0; i < 6; i++) {
      commands.add("/world " + centerToCopyFrom.getWorld().getName());
      delays.add(20*5);
      commands.add("/sel convex");
      delays.add(0);

      int x = centerToCopyFrom.getBlockX() + xRadius * 2 * i;
      int y = centerToCopyFrom.getBlockY();
      int z = centerToCopyFrom.getBlockZ();

      if (i == 3) {
        x = centerToCopyFrom.getBlockX() - xRadius * 2;
      } else if (i == 4) {
        x = centerToCopyFrom.getBlockX();
        z = (centerToCopyFrom.getBlockZ() - zRadius * 2);
      } else if (i == 5) {
        x = centerToCopyFrom.getBlockX();
        z = (centerToCopyFrom.getBlockZ() + zRadius * 2);
      }

      int minY = clampValueToWorldHeight(y - yRadius, centerToCopyFrom.getWorld());
      int maxY = clampValueToWorldHeight(y + yRadius, centerToCopyFrom.getWorld());

      commands.add("/sel convex");
      delays.add(0);

      commands.add("/pos1 " + x + "," + minY + "," + z);
      delays.add(0);

      commands.add("/pos2 " + (x - xRadius) + "," + y + "," + (z - zRadius));
      delays.add(0);
      commands.add("/pos2 " + (x - xRadius) + "," + y + "," + (z + zRadius));
      delays.add(0);
      commands.add("/pos2 " + (x + xRadius) + "," + y + "," + (z - zRadius));
      delays.add(0);
      commands.add("/pos2 " + (x + xRadius) + "," + y + "," + (z + zRadius));
      delays.add(0);

      commands.add("/pos2 " + (x - xRadius) + "," + maxY + "," + (z - zRadius));
      delays.add(0);
      commands.add("/pos2 " + (x - xRadius) + "," + maxY + "," + (z + zRadius));
      delays.add(0);
      commands.add("/pos2 " + (x + xRadius) + "," + maxY + "," + (z - zRadius));
      delays.add(0);
      commands.add("/pos2 " + (x + xRadius) + "," + maxY + "," + (z + zRadius));
      delays.add(0);

      commands.add("/copy");
      delays.add(0);
      commands.add("/schem save face" + i + " -f");
      delays.add(0);
    }

    createAndWriteFile(CubicWorlds.creatingWorldStateFileName, "true");

    commands.add("stop");
    delays.add(0);

    new TimedCommandExecutor(plugin, commands, delays).execute();
  }

  private static int clampValueToWorldHeight(int value, World world) {
    if (value > world.getMaxHeight()) {
      return world.getMaxHeight();
    } else if (value < world.getMinHeight()) {
      return world.getMinHeight();
    }
    return value;
  }
}
