package me.zootsuitproductions.cubicworlds;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class WECubeWorld {
  private final int xRadius;
  private final int yRadius;
  private final int zRadius;


  private List<Location> faceCenters = new ArrayList<Location>();

  WECubeWorld(int xRadius, int yRadius, int zRadius) {
    this.xRadius = xRadius;
    this.yRadius = yRadius;
    this.zRadius = zRadius;
  }

  private void setPyramidOfAirFromBottom(Location faceCenter) {
    for (int relativeY = 0; relativeY >= -yRadius; relativeY--) {
      WorldEditCommandExecutor commandExecutor = new WorldEditCommandExecutor(Bukkit.getWorlds().get(0).getName());

      commandExecutor.setPos1(
          faceCenter.getBlockX() + xRadius - relativeY,
          -yRadius,
          faceCenter.getBlockZ() - zRadius);
      commandExecutor.setPos2(
          faceCenter.getBlockX() + xRadius - relativeY,
          yRadius - relativeY,
          faceCenter.getBlockZ() + zRadius);
      commandExecutor.setAir();
    }
  }

  public void addFace(Location faceCenter) {
    faceCenters.add(faceCenter);

    setPyramidOfAirFromBottom(faceCenter);
//
//    int minHeight = faceCenter.getWorld().getMinHeight();
//    int maxHeight = faceCenter.getWorld().getMaxHeight();
//
//    int x1 = faceCenter.getBlockX() - xRadius;
//    int y1 = clampValueToRange(faceCenter.getBlockY() - cubeWorldRadius, minHeight,maxHeight);
//    int z1 = faceCenter.getBlockZ() - zRadius;
//
//    int x2 = faceCenter.getBlockX() + xRadius;
//    int y2 = clampValueToRange(faceCenter.getBlockY() + cubeWorldRadius, minHeight,maxHeight);
//    int z2 = faceCenter.getBlockZ() + zRadius;
//
//
//    p.performCommand("/pos1 " + x1 + "," + y1 + "," + z1);
//    p.performCommand("/pos2 " + x2 + "," + y2 + "," + z2);
//    p.performCommand("/copy -b -e");
//    p.performCommand("/schem save face" + currentFace + " -f");
//
//
//    if (faceCenters.size() >= 5) {
//      createAndWriteFile(creatingWorldStateFileName, "true");
//      Bukkit.shutdown();
//    }
  }
}
