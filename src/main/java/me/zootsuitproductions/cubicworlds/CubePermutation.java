package me.zootsuitproductions.cubicworlds;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CubePermutation {
  public final Location center;
  private int radius;
  private Location upFaceCenter;
  private CubeFaceRegion[] faces;

  private static final int EDGE_LEEWAY = 3;
  // to calculate which faces go to which, add a y rotation func and have a cardinal location on each of the face axes
  // vector type beat. rotate that vector and then get the cube permutation stored at that vector in a mpa


  private Coordinate convertCoordFromThisScheme() {

  }

//  private Coordinate rotateHelper(Coordinate coord, int xRot, int yRot, int zRot) {
//    double x = coord.getX();
//    double y = coord.getY();
//    double z = coord.getZ();
//
//    double newX = x;
//    double newY = y;
//    double newZ = z;
//
//    double centerX = coord.getX();
//    double centerY = coord.getY();
//    double centerZ = coord.getZ();
//
//    //function object representing transformation. look this up.
//
//    //should be a way to save these, update the equation so i dont have to check if statesments every time. set the axis
//    if ((zRot) == 90) {
//      newX = -y;
//      newY = x;
//    } else if (zRot == 270 || zRot == -90) {
//      newX = y;
//      newY = -x;
//    } else if (zRot == 180) {
//      newX = -x;
//      newY = -y;
//    }
//
//    if (xRot == 90) {
//      newZ = -y;
//      newY = z;
//    } else if (xRot == 270 || xRot == -90) {
//      newZ = y;
//      newY = -(z - centerZ) + centerY;
//    } else if (xRot == 180) {
//      newZ = -(z - centerZ) + centerZ;
//      newY = -(y - centerY) + centerY;
//    }
//
//    if (yRot == 90) {
//      newZ = -(x - centerX) + centerZ;
//      newX = (z - centerZ) + centerX;
//    } else if (yRot == 270 || yRot == -90) {
//      newZ = (x - centerX) + centerZ;
//      newX = -(z - centerZ) + centerX;
//    } else if (yRot == 180) {
//      newZ = -(z - centerZ) + centerZ;
//      newX = -(x - centerX) + centerX;
//    }
//
//    return new Location(point.getWorld(), newX, newY, newZ);
//  }

  public CubePermutation(CubePermutation main, Location pasteCenter, Location upFace) {
    radius = main.radius;
    this.center = pasteCenter;

    for (int x = -2 * radius - 1; x <= 2 * radius + 1; x++) {
      for (int y = -2 * radius - 1; y <= 2 * radius + 1; y++) {
        for (int z = -2 * radius - 1; z <= 2 * radius + 1; z++) {
          Block copyBlock = translateLocation(main.center, x, y, z).getBlock();

          Location pasteBeforeRot = translateLocation(pasteCenter, x, y, z);
          CubeFaceRegion.rotateLocation(pasteBeforeRot, 90, 0,0, pasteCenter).getBlock().setBlockData(copyBlock.getBlockData());

        }
      }
    }
  }

  public Location localCoordinateToAbsolute(int x, int y, int z) {
    return new Location(center.getWorld(), center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
  }

//  public static GetRotationBetweenPoints(Location point1, Location point2, Location center) {
//
//  }

  public CubePermutation(Location centerInWorld, Location pasteCenter, int radius, Location upFaceVector) {
    this.center = pasteCenter;
    this.radius = radius;
    this.upFaceCenter = translateLocation(pasteCenter, 0, radius, 0);

    this.faces = new CubeFaceRegion[6];

    faces[0] = new CubeFaceRegion(centerInWorld,
        this.upFaceCenter,
        radius, 0, 0,0);
    faces[1] = new CubeFaceRegion(translateLocation(centerInWorld, -2*radius + 1, 0, 0),
        translateLocation(pasteCenter, -radius, 0, 0),
        radius, 0, 0,90);
    faces[2] = new CubeFaceRegion(translateLocation(centerInWorld, 4*radius + 2, 0, 0),
        translateLocation(pasteCenter, 0, -radius, 0),
        radius, 0, 0,180);
    faces[3] = new CubeFaceRegion(translateLocation(centerInWorld, 2*radius - 1, 0, 0),
        translateLocation(pasteCenter, radius, 0, 0),
        radius, 0, 0,-90);
    faces[4] = new CubeFaceRegion(translateLocation(centerInWorld, 0, 0, 2*radius - 1),
        translateLocation(pasteCenter, 0, 0, radius),
        radius, 0, -90,0);
    faces[5] = new CubeFaceRegion(translateLocation(centerInWorld, 0, 0, -2*radius + 1),
        translateLocation(pasteCenter, 0, 0, -radius),
        radius, 0, 90,0);
  }

/*  public Location convertLocationToDifferentPermutation(Location loc, int fromFace) {

  }*/

  public static Location translateLocation(Location loc, int xTrans, int yTrans, int zTrans) {
    return new Location(loc.getWorld(), loc.getBlockX() + xTrans, loc.getBlockY() + yTrans, loc.getBlockZ() + zTrans);
  }


  public Coordinate rotatePoint(Coordinate coordinate, int degreesZ) {
    switch (degreesZ) {
      case 90: return new Coordinate(coordinate.getY(), -coordinate.getX(), coordinate.getZ());
      case 180: return new Coordinate(-coordinate.getX(), -coordinate.getY(), coordinate.getZ());
      case 270: return new Coordinate(-coordinate.getY(), coordinate.getX(), coordinate.getZ());
      default: return coordinate;
    }
  }

  public boolean PlayerIsOnFace(Player p) {
    int x = p.getLocation().getBlockX();
    int z = p.getLocation().getBlockZ();
    return (x <= center.getX() + (radius + EDGE_LEEWAY))
        && (x >= center.getX() - (radius + EDGE_LEEWAY))
        && (z <= center.getZ() + (radius + EDGE_LEEWAY))
        && (z >= center.getZ() - (radius + EDGE_LEEWAY));
  }

  public Location getPosXEdgeCoord() {
    return new Location(
        center.getWorld(),
        center.getBlockX() + radius,
        center.getBlockY() + radius,
        center.getBlockZ());
  }

  public Location getNegXEdgeCoord() {
    return new Location(
        center.getWorld(),
        center.getBlockX() - radius,
        center.getBlockY() + radius,
        center.getBlockZ());
  }


}
