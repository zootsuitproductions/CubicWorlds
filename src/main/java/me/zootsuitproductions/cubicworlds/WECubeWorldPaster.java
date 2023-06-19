package me.zootsuitproductions.cubicworlds;

import static me.zootsuitproductions.cubicworlds.FileUtils.createAndWriteFile;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class WECubeWorldPaster {
  public final int xRadius;
  public final int yRadius;
  public final int zRadius;

  private final Location[] cubeCenters;

  WECubeWorldPaster(int radius, Location[] cubeCenters) {
    this.xRadius = radius;
    this.yRadius = radius;
    this.zRadius = radius;
    this.cubeCenters = cubeCenters;
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

  public void pasteWorldAtLocation(Location mainCubeCenter, Plugin plugin) {
    List<String> commands = new ArrayList<>();
    List<Integer> delays = new ArrayList<>();

    commands.add("/world " + mainCubeCenter.getWorld().getName());
    delays.add(0);

    for (int i = 0; i < 6; i++) {
      System.out.println("pasting the " + i + "th permutation");
      commands.add("/schem load face" + i);
      delays.add(20*2);

      Vector rotation = faceRotationValues[i];
      commands.add("/rotate " + rotation.getBlockY() + " " + rotation.getBlockX() + " "
          + rotation.getBlockZ());

      delays.add(10*1);

      commands.add(
          "/pos1 " + mainCubeCenter.getBlockX() + "," + mainCubeCenter.getBlockY() + "," + mainCubeCenter.getBlockZ());
      delays.add(0);

      commands.add("/paste -a");
      delays.add(5*3);
    }

    for (int currentRotation = 1; currentRotation < permRotationValues.length; currentRotation++) {
      Location permCenter = cubeCenters[currentRotation];
      int x = permCenter.getBlockX();
      int y = permCenter.getBlockY();
      int z = permCenter.getBlockZ();

      for (int i = -2; i <= 2; i += 4) {
        for (int j = -2; j <= 2; j += 4) {
          for (int k = -2; k <= 2; k += 4) {
            commands.add("/pos1 " + (mainCubeCenter.getBlockX()) + ","
                + (mainCubeCenter.getBlockY()) + "," + (mainCubeCenter.getBlockZ()));
            delays.add(0);

            commands.add("/pos2 " + (mainCubeCenter.getBlockX() + i * xRadius) + ","
                + (mainCubeCenter.getBlockY() + k * yRadius) + "," + (mainCubeCenter.getBlockZ()
                + j * zRadius));
            delays.add(0);

            commands.add("/copy -be");
            delays.add(10*1);

            Vector rotation = permRotationValues[currentRotation];
            commands.add("/rotate " + rotation.getBlockY() + " " + rotation.getBlockX() + " "
                + rotation.getBlockZ());
            delays.add(10*1);


            //paste location of cube perm
            //todo: make not on a line: put the cubes in a 2x3 grid. update the function for finding closest cube
            //based on this^^

            //todo: then do the gravity properly
            //bruh its here


            //paste it with center at coordinates (rotates around this point)
            commands.add("/pos1 " + x + ","
                + y + "," + z);
//            commands.add("/pos1 " + (mainCubeCenter.getBlockX() + currentRotation * CubeWorld.spacing) + ","
//                + (mainCubeCenter.getBlockY()) + "," + (mainCubeCenter.getBlockZ()));
            delays.add(0);

            commands.add("/paste -a");
            delays.add(5*3);
          }
        }
      }

      new TimedCommandExecutor(plugin, commands, delays).execute();
    }
  }

}
