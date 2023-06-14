package me.zootsuitproductions.cubicworlds;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class WorldPermutation {
  public final AxisTransformation axisTransformation;
  public final Vector3d topFaceCoordinateOnMainWorld;
  public final Location center;
  private int radius;

  public final int index;

  public WorldPermutation(Location pasteCenter, int radius, AxisTransformation upFace, Vector3d topFaceCoordinateOnMainWorld, int index) {
    this.center = pasteCenter;
    this.radius = radius;
    this.axisTransformation = upFace;
    this.index = index;
    this.topFaceCoordinateOnMainWorld = topFaceCoordinateOnMainWorld;
  }

  public WorldPermutation(WorldPermutation mainCube, Location pasteCenter, AxisTransformation axisTransformation, Vector3d topFaceCoordinateOnMainWorld, int index) {
    radius = mainCube.radius;
    this.center = pasteCenter;
    this.axisTransformation = axisTransformation;
    this.index = index;
    this.topFaceCoordinateOnMainWorld = topFaceCoordinateOnMainWorld;
  }

  public Vector rotateVectorToOtherCube(Vector vector, WorldPermutation other) {
    Vector3d v3 = other.axisTransformation.apply(axisTransformation.unapply(new Vector3d(vector.getX(), vector.getY(), vector.getZ())));
    return new Vector(v3.x, v3.y, v3.z);
  }

  public boolean isLocationOffOfFaceRadius(Location loc) {
    Vector3d relative = getRelativeCoordinate(loc);
    //potential for reading which face to rotate to from whether its pos or neg x or z.
    return (Math.abs(relative.x) > radius || Math.abs(relative.z) > radius || relative.y < 0);
  }

  private Vector3d getRelativeCoordinate(Location loc) {
    return new Vector3d(
        loc.getBlockX() - center.getBlockX(),
        loc.getBlockY() - center.getBlockY(),
        loc.getBlockZ() - center.getBlockZ());
  }

  public Vector3d getWorldCoordinate(Location loc) {
    return axisTransformation.unapply(getRelativeCoordinate(loc));
  }

  public Location getLocationOnThisPermFromCubeWorldCoordinate(Vector3d cubeWorldCoordinate, World world) {
    Vector3d localCoordinate = axisTransformation.apply(cubeWorldCoordinate);
    System.out.println("LOCAL COORD: " + localCoordinate);
    return new Location(world, localCoordinate.x + center.getBlockX(), localCoordinate.y + center.getBlockY(), localCoordinate.z + center.getBlockZ());
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

  public static Vector3d convertYawPitchToVector(float yaw, float pitch) {
    double yawRadian = Math.toRadians(yaw);
    double pitchRadian = Math.toRadians(pitch);

    double x = -Math.sin(yawRadian);
    double y = -Math.sin(pitchRadian);
    double z = Math.cos(yawRadian);

    // Calculate the magnitude (length) of the vector
    double magnitude = Math.sqrt(x * x + y * y + z * z);

    // Normalize the vector by dividing each component by the magnitude
    x /= magnitude;
    y /= magnitude;
    z /= magnitude;

    return new Vector3d(x, y, z);
  }

  //move these to utils class

  public static Location setLookDirectionToVector(Location location, Vector3d vector3d) {
    float yaw = (float) Math.toDegrees(Math.atan2(-vector3d.x, vector3d.z));
    float pitch = (float) Math.toDegrees(Math.asin(-vector3d.y));

    location.setYaw(yaw);
    location.setPitch(pitch);
    return location;
  }

  public static void debug(String string, Vector3d vector3d) {
    float yaw = (float) Math.toDegrees(Math.atan2(-vector3d.x, vector3d.z));
    float pitch = (float) Math.toDegrees(Math.asin(-vector3d.y));
    System.out.println(string + ": yaw " + yaw + ", pitch " + pitch);
  }

  public static float getYaw(Vector3d vector3d) {
    return (float) Math.toDegrees(Math.atan2(-vector3d.x, vector3d.z));
  }

  public static float getPitch(Vector3d vector3d) {
    return (float) (float) Math.toDegrees(Math.asin(-vector3d.y));
  }

  public Vector3d convertLookingVectorFromOtherCubeRotation(Vector3d lookDirectionOnOther, WorldPermutation other) {
    System.out.println("helloIN FUNC");

    System.out.println("currentWorld vector : " + lookDirectionOnOther);
    Vector3d mainCubeWorldLookDirection = other.axisTransformation.unapply(lookDirectionOnOther);

    float y = getYaw(lookDirectionOnOther);
    float y1 =  getYaw(lookDirectionOnOther);
    float y3 =  getPitch(lookDirectionOnOther);
    float y4 = getPitch(lookDirectionOnOther);

    float y5 = getYaw(mainCubeWorldLookDirection);
    float y6 = getYaw(mainCubeWorldLookDirection);
    float ya = getPitch(mainCubeWorldLookDirection);

    System.out.println("main world vector: " + mainCubeWorldLookDirection);
    Vector3d newWorldLookDirection = axisTransformation.apply(mainCubeWorldLookDirection);

    float y33= getYaw(newWorldLookDirection);
    float ay = getYaw(newWorldLookDirection);
    float y1a = getPitch(newWorldLookDirection);

    System.out.println("new!! world vector: " + newWorldLookDirection);

    return newWorldLookDirection;
  }

  public float convertYawFromOtherCubeRotation(float yaw, WorldPermutation other) {
    Vector3d yaxAxisWorld = other.getWorldYawAxisFacing(yaw);
    System.out.println("world yaw axis: " + yaxAxisWorld);

    Vector3d localYawAxis = axisTransformation.apply(yaxAxisWorld);

    System.out.println("local yaw axis: " + localYawAxis);

    //yaw doesnt face y so the matrix rotation fucks it
    //take into account pitch
    //if pitch > 45
    return getYawFromAxisDirectionFacing(localYawAxis);
  }

  public Vector3d getWorldYawAxisFacing(float yaw) {
    return axisTransformation.unapply(WorldPermutation.getLocalYawAxisFacing(yaw));
  }


  public Vector3d getLocalCoordinateFromWorldCoordinate(Vector3d worldCoordinate) {
    return axisTransformation.apply(worldCoordinate);
  }
//
//  public Location[] getFaceCenters() {
//    return this.faceCenters.clone();
//  }

  public Vector3d translateLocalCoordinateToThisCubeRotation(Vector3d localSource, AxisTransformation sourceAxisTransformation) {
    Vector3d coordinateFromMainCube = sourceAxisTransformation.apply(localSource);
    return this.axisTransformation.unapply(coordinateFromMainCube);
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
    return axisTransformation.unapply(getLocationRelativeToThisPermutation(loc));
  }

  public static Location translateLocation(Location loc, int xTrans, int yTrans, int zTrans) {
    return new Location(loc.getWorld(), loc.getBlockX() + xTrans, loc.getBlockY() + yTrans, loc.getBlockZ() + zTrans);
  }

}
