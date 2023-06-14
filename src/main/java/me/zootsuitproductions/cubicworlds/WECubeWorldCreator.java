package me.zootsuitproductions.cubicworlds;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class WECubeWorldCreator {
  public final int xRadius;
  public final int yRadius;
  public final int zRadius;
  private int currentFace = 0;

  WECubeWorldCreator(int xRadius, int yRadius, int zRadius) {
    this.xRadius = xRadius;
    this.yRadius = yRadius;
    this.zRadius = zRadius;
  }

  private final Vector[] faceRotationValues = new Vector[] {
      new Vector(0,0,0),
      new Vector(0,0,90),
      new Vector(0,0,180),
      new Vector(0,0,-90),
      new Vector(90,0,0),
      new Vector(-90,0,0)
  };

  private final Vector[] permRotationValues = new Vector[] {
      new Vector(0,0,0),
      new Vector(0,0,-90),
      new Vector(0,0,180),
      new Vector(0,0,90),
      new Vector(-90,0,0),
      new Vector(90,0,0)
  };



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

    CubicWorlds.createAndWriteFile(CubicWorlds.creatingWorldStateFileName, "true");

    commands.add("stop");
    delays.add(0);

    new TimedCommandExecutor(plugin, commands, delays).execute();
  }


  public void pasteWorldAtLocation(Location center, Plugin plugin) {
    List<String> commands = new ArrayList<>();
    List<Integer> delays = new ArrayList<>();

    commands.add("/world " + center.getWorld().getName());
    delays.add(0);

    for (int i = 0; i < 6; i++) {
      System.out.println("pasting the " + i + "th permutation");
      commands.add("/schem load face" + i);
      delays.add(20*6);

      Vector rotation = faceRotationValues[i];
      commands.add("/rotate " + rotation.getBlockY() + " " + rotation.getBlockX() + " "
          + rotation.getBlockZ());

      delays.add(10*6);

      commands.add(
          "/pos1 " + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ());
      delays.add(0);

      commands.add("/paste -a");
      delays.add(5*6);
    }

    for (int currentRotation = 1; currentRotation < permRotationValues.length; currentRotation++) {
      for (int i = -2; i <= 2; i += 4) {
        for (int j = -2; j <= 2; j += 4) {
          for (int k = -2; k <= 2; k += 4) {
            commands.add("/pos1 " + (center.getBlockX()) + ","
                + (center.getBlockY()) + "," + (center.getBlockZ()));
            delays.add(0);

            commands.add("/pos2 " + (center.getBlockX() + i * xRadius) + ","
                + (center.getBlockY() + k * yRadius) + "," + (center.getBlockZ()
                + j * zRadius));
            delays.add(0);

            commands.add("/copy -be");
            delays.add(20*3);

            Vector rotation = permRotationValues[currentRotation];
            commands.add("/rotate " + rotation.getBlockY() + " " + rotation.getBlockX() + " "
                + rotation.getBlockZ());
            delays.add(10*3);

            commands.add("/pos1 " + (center.getBlockX() + currentRotation * CubeWorld.spacing) + ","
                + (center.getBlockY()) + "," + (center.getBlockZ()));
            delays.add(0);

            commands.add("/paste -a");
            delays.add(5*3);
          }
        }
      }

      new TimedCommandExecutor(plugin, commands, delays).execute();
    }
  }

  private static int clampValueToWorldHeight(int value, World world) {
    if (value > world.getMaxHeight()) {
      return world.getMaxHeight();
    } else if (value < world.getMinHeight()) {
      return world.getMinHeight();
    }
    return value;
  }

  public void addFace(Location faceCenter) {
    if (currentFace > 5) {
      return;
    }

    WECommandExecutor commandExecutor = new WECommandExecutor(faceCenter.getWorld().getName());
    commandExecutor.setSelectionConvex();

    int x = faceCenter.getBlockX();
    int y = faceCenter.getBlockY();
    int z = faceCenter.getBlockZ();

    int minY = clampValueToWorldHeight(y - yRadius, faceCenter.getWorld());
    int maxY = clampValueToWorldHeight(y + yRadius, faceCenter.getWorld());

    commandExecutor.setPos1(x, minY, z); //bottom of inverted pyramid. it will copy from here

    commandExecutor.setPos2(x - xRadius, y, z - zRadius);
    commandExecutor.setPos2(x - xRadius, y, z + zRadius);
    commandExecutor.setPos2(x + xRadius, y, z - zRadius);
    commandExecutor.setPos2(x + xRadius, y, z + zRadius);

    commandExecutor.setPos2(x - xRadius, maxY, z - zRadius);
    commandExecutor.setPos2(x - xRadius, maxY, z + zRadius);
    commandExecutor.setPos2(x + xRadius, maxY, z - zRadius);
    commandExecutor.setPos2(x + xRadius, maxY, z + zRadius);

    commandExecutor.copy();
    commandExecutor.saveSchematic("face" + currentFace);

    currentFace ++;

    if (currentFace > 5) {
      CubicWorlds.createAndWriteFile(CubicWorlds.creatingWorldStateFileName, "true");
      Bukkit.shutdown();
    }
  }
}
