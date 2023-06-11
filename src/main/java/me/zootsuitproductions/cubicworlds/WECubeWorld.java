package me.zootsuitproductions.cubicworlds;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class WECubeWorld {
  public final int xRadius;
  public final int yRadius;
  public final int zRadius;
  private int currentFace = 0;

  WECubeWorld(int xRadius, int yRadius, int zRadius) {
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

  static int spacing = 50;

  public void pasteWorldAtLocation(Location center, Plugin plugin) {
    List<String> commands = new ArrayList<>();
    List<Integer> delays = new ArrayList<>();

    commands.add("/world " + center.getWorld().getName());
    delays.add(0);

    for (int i = 0; i < 6; i++) {
      commands.add("/schem load face" + i);
      delays.add(20);

      Vector rotation = faceRotationValues[i];
      commands.add("/rotate " + rotation.getBlockY() + " " + rotation.getBlockX() + " "
          + rotation.getBlockZ());

      delays.add(10);

      commands.add(
          "/pos1 " + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ());
      delays.add(0);

      commands.add("/paste -a");
      delays.add(5);
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
            delays.add(20);

            Vector rotation = permRotationValues[currentRotation];
            commands.add("/rotate " + rotation.getBlockY() + " " + rotation.getBlockX() + " "
                + rotation.getBlockZ());
            delays.add(10);

            commands.add("/pos1 " + (center.getBlockX() + currentRotation * spacing) + ","
                + (center.getBlockY()) + "," + (center.getBlockZ()));
            delays.add(0);

            commands.add("/paste -a");
            delays.add(5);
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