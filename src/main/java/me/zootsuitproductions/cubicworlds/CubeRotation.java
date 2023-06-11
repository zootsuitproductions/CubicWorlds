package me.zootsuitproductions.cubicworlds;

import static me.zootsuitproductions.cubicworlds.AxisTransformation.transformations;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.joml.Vector2i;
import org.joml.Vector3d;

public class CubeRotation {
  public final AxisTransformation axisTransformation;
  public final Vector3d topFaceCoordinateOnMainWorld;

  public final Location center;
  private int radius;
  private final CubeFaceRegion[] faces = new CubeFaceRegion[6];
  private final Location[] faceCenters = new Location[6];


  public CubeRotation(Location centerInWorld, Location pasteCenter, int radius, AxisTransformation upFace, Vector3d topFaceCoordinateOnMainWorld) {
    this.center = pasteCenter;
    this.radius = radius;
    this.axisTransformation = upFace;
    this.topFaceCoordinateOnMainWorld = topFaceCoordinateOnMainWorld;

    faceCenters[0] = translateLocation(pasteCenter, 0, radius, 0);

    faceCenters[1] = translateLocation(pasteCenter, -radius, 0, 0);

    faceCenters[2] = translateLocation(pasteCenter, 0, -radius, 0);

    faceCenters[3] = translateLocation(pasteCenter, radius, 0, 0);

    faceCenters[4] = translateLocation(pasteCenter, 0, 0, radius);

    faceCenters[5] = translateLocation(pasteCenter, 0, 0, -radius);


  }

  public CubeRotation(CubeRotation mainCube, Location pasteCenter, AxisTransformation axisTransformation, Vector3d topFaceCoordinateOnMainWorld) {
    radius = mainCube.radius;
    this.center = pasteCenter;
    this.axisTransformation = axisTransformation;
    this.topFaceCoordinateOnMainWorld = topFaceCoordinateOnMainWorld;

    for (int x = -2 * radius - 1; x <= 2 * radius + 1; x++) {
      for (int y = -2 * radius - 1; y <= 2 * radius + 1; y++) {
        for (int z = -2 * radius - 1; z <= 2 * radius + 1; z++) {
          Vector3d localCoordinateSource = new Vector3d(x, y, z);
          BlockData copyBlockData = mainCube.getBlockLocationFromRelativeCoordinate(localCoordinateSource).getBlock().getBlockData();

          Vector3d localCoordinateDest = axisTransformation.unapply(localCoordinateSource); //this is rotated
          Location worldDestination = getBlockLocationFromRelativeCoordinate(localCoordinateDest);

          worldDestination.getBlock().setBlockData(CubeFaceRegion.rotateBlockData(copyBlockData, axisTransformation));
        }
      }
    }
  }

  public static Vector3d getLocalYawAxisFacing(float yaw) {
    switch ((int) Math.round(yaw/90.0)) {
      case 0:
        return new Vector3d(0,-1,1);
      case 1:
        return new Vector3d(-1,-1,0);
      case -1:
        return new Vector3d(1,-1,0);
      default:
        return new Vector3d(0,-1,-1);
    }
  }

  public static float getYawFromAxisDirectionFacing(Vector3d facingDirection) {
    if (facingDirection.z == 1) {
      return 0;
    } else if (facingDirection.z == -1) {
      return 180;
    } else if (facingDirection.x == 1) {
      return -90;
    } else {
      return 90;
    }
  }

  public float convertYawFromOtherCubeRotation(float yaw, CubeRotation other) {
    Vector3d yaxAxisWorld = other.getWorldYawAxisFacing(yaw);
    System.out.println("world yaw axis: " + yaxAxisWorld);

    Vector3d localYawAxis = axisTransformation.unapply(yaxAxisWorld);

    System.out.println("local yaw axis: " + localYawAxis);


    //yaw doesnt face y so the matrix rotation fucks it
    //take into account pitch
    //if pitch > 45
    return getYawFromAxisDirectionFacing(localYawAxis);
  }

  public Vector3d getWorldYawAxisFacing(float yaw) {
    return axisTransformation.apply(CubeRotation.getLocalYawAxisFacing(yaw));
  }


  public Vector3d getLocalCoordinateFromWorldCoordinate(Vector3d worldCoordinate) {
    return axisTransformation.unapply(worldCoordinate);
  }

  public Location[] getFaceCenters() {
    return this.faceCenters.clone();
  }

  public Vector3d translateLocalCoordinateToThisCubeRotation(Vector3d localSource, AxisTransformation sourceAxisTransformation) {
    Vector3d coordinateFromMainCube = sourceAxisTransformation.unapply(localSource);
    return this.axisTransformation.apply(coordinateFromMainCube);
  }

  public Location getLocationFromRelativeCoordinate(Vector3d vector) {
    System.out.println("center coordinate x + : " + center.getBlockX() + 0.0001);
    return new Location(center.getWorld(), center.getBlockX() + 0.5 + vector.x, center.getBlockY() + 0.5 + vector.y, center.getBlockZ() + 0.5 + vector.z);
  }

  public Location getBlockLocationFromRelativeCoordinate(Vector3d vector) {
    return new Location(center.getWorld(), center.getBlockX() + vector.x, center.getBlockY() + vector.y, center.getBlockZ() + vector.z);
  }

//  public Location getBlockLocationFromRelativeCoordinate(Vector3d vector) {
//    return new Location(center.getWorld(), center.getBlockX() + vector.x, center.getY() + vector.y, center.getZ() + vector.z);
//  }

  //fix the world gen getting fucked from not rotating center block. TEST

  private Vector3d getLocationRelativeToThisPermutation(Location loc) {
    Vector3d toReturn = new Vector3d(
        loc.getX() - (center.getBlockX() + 0.5),
        loc.getY() - (center.getBlockY() + 0.5),
        loc.getZ() - (center.getBlockZ() + 0.5));


    //THE ROUNDING IS WHAT FUCKED IT
    System.out.println("local coord " + Math.round(toReturn.x) + " " + Math.round(toReturn.y) + " " + Math.round(toReturn.z));

    return toReturn;
  } //maybe make block

  public Vector3d getCubeWorldCoordinate(Location loc) {
    System.out.println("coordinate before applying transform: " + getLocationRelativeToThisPermutation(loc));
    System.out.println("coordinate before applying transform: " + getLocationRelativeToThisPermutation(loc));
    return axisTransformation.apply(getLocationRelativeToThisPermutation(loc));
  }

  public static Location translateLocation(Location loc, int xTrans, int yTrans, int zTrans) {
    return new Location(loc.getWorld(), loc.getBlockX() + xTrans, loc.getBlockY() + yTrans, loc.getBlockZ() + zTrans);
  }

}