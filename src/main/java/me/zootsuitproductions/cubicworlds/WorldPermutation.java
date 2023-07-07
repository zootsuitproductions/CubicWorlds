package me.zootsuitproductions.cubicworlds;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

public class WorldPermutation {
  public final AxisTransformation axisTransformation;
  public final Vector3d topFaceCoordinateOnMainWorld;
  public final Location center;
  private final int radius;
  public final int index;

  public WorldPermutation(Location pasteCenter, int radius, AxisTransformation upFace, Vector3d topFaceCoordinateOnMainWorld, int index) {
    this.center = pasteCenter;
    this.radius = radius;
    this.axisTransformation = upFace;
    this.index = index;
    this.topFaceCoordinateOnMainWorld = topFaceCoordinateOnMainWorld;
  }

  public BlockData unrotateBlockData(BlockData blockData) {
    if (blockData instanceof Stairs) {
      Stairs stairs = (Stairs) blockData;
      return TransformationUtils.unrotateStairs(stairs, axisTransformation);
    }

    return blockData;
  }

  public BlockData rotateBlockData(BlockData blockData, WorldPermutation from) {
    if (blockData instanceof Stairs) {
      Stairs stairs = (Stairs) blockData;
      return rotateStairsFrom(stairs, from);
    }

    return blockData;
  }

  private Stairs rotateStairsFrom(Stairs stairs1, WorldPermutation from) {
    Stairs stairs = (Stairs) stairs1.clone();

    Vector3d v1 = axisTransformation.apply(from.axisTransformation.unapply(TransformationUtils.getHorizontalStairsVector(stairs)));
    Vector3d v2 = axisTransformation.apply(from.axisTransformation.unapply(TransformationUtils.getVerticalStairsVector(stairs)));

    if (v1.y == 0) {
      //v1 is the horizontal component
      stairs.setFacing(TransformationUtils.getBlockFaceFromVector(v1));
      stairs.setHalf(TransformationUtils.getHalfFromVector(v2));
    } else {
      //v2 is horizontal
      stairs.setFacing(TransformationUtils.getBlockFaceFromVector(v2));
      stairs.setHalf(TransformationUtils.getHalfFromVector(v1));
    }
    return stairs;
  }


  //this doesnt need to clamp for going to opposite side
  public Location getMinecraftWorldLocationOnOtherCube(WorldPermutation other, Location playerLoc, boolean isNewPermOnOppositeSide, Player player) {
    Location eyeLocation = playerLoc.add(0,1.62,0);
    Vector3d cubeWorldCoordinateOfPlayerEyes = getCubeWorldCoordinate(eyeLocation);

    Vector3d localCoordOnClosestFace = other.getLocalCoordinateFromWorldCoordinate(cubeWorldCoordinateOfPlayerEyes);
    localCoordOnClosestFace = localCoordOnClosestFace.sub(0, 1.62, 0);
    Location actualWorldLocationToTeleportTo = other.getLocationFromRelativeCoordinate(localCoordOnClosestFace);

    float newYaw = other.convertYawToNewCubePermutation(eyeLocation.getYaw(), this, isNewPermOnOppositeSide, player);
    actualWorldLocationToTeleportTo.setYaw(newYaw);

    actualWorldLocationToTeleportTo.setPitch(other.convertPitchFromOtherCubeRotation(eyeLocation.getPitch(), eyeLocation.getYaw(), this));

    return actualWorldLocationToTeleportTo;
  }

  public Vector rotateVectorToOtherCube(Vector vector, WorldPermutation other) {
    Vector3d v3 = other.axisTransformation.apply(axisTransformation.unapply(new Vector3d(vector.getX(), vector.getY(), vector.getZ())));

    if (v3.y < 0.45) {
      v3.y = 0.45;
    }

    return new Vector(v3.x, v3.y, v3.z);
  }

  public boolean isLocationOffOfFaceRadius(Location loc) {
    Vector3d relative = getRelativeCoordinate(loc);
    //potential for reading which face to rotate to from whether its pos or neg x or z.
    return (Math.abs(relative.x) >= radius || Math.abs(relative.z) >= radius || relative.y <= 0);
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

  public BlockData rotateBlockDataFromMainWorld(BlockData blockData) {
    if (blockData instanceof Stairs) {
      Stairs stairs = (Stairs) blockData;
//      stairs.get
    } else if (blockData instanceof Directional) {
      Directional directional = (Directional) blockData;

    }
    return blockData;
  }

  public Location getLocationOnThisPermFromCubeWorldCoordinate(Vector3d cubeWorldCoordinate, World world) {
    Vector3d localCoordinate = axisTransformation.apply(cubeWorldCoordinate);
    return new Location(world, localCoordinate.x + center.getBlockX(), localCoordinate.y + center.getBlockY(), localCoordinate.z + center.getBlockZ());
  }

  public static float clampDegrees(float degrees) {
    if (degrees > 180) {
      return degrees - 360;
    }
    return degrees;
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

  private float getYawFromVector(Vector3d vector, Player p) {
    double radians = Math.atan2(-vector.x, vector.z);
    return (float) (radians * (180 / Math.PI));
  }

  private Vector3d getVectorFromYaw(float yaw) {
    double radians = Math.PI * (yaw/180);
    return new Vector3d(-Math.sin(radians), 0, Math.cos(radians));
  }

  public float convertPitchFromOtherCubeRotation(float pitch, float yaw, WorldPermutation previous) {
    Vector3d originalYawAxis = previous.getLocalYawAxisFacing(yaw);
    Vector3d vectorOfFaceSwitchingTo = previous.axisTransformation.apply(topFaceCoordinateOnMainWorld).normalize();

    //if you're facing in the direction of the cube ur switching to
    if (vectorOfFaceSwitchingTo.x == originalYawAxis.x && vectorOfFaceSwitchingTo.z == originalYawAxis.z) {
      return pitch - 90; //rotate head forward
    }
    //if you're looking opposite direction
    else if (vectorOfFaceSwitchingTo.y == 0 && (vectorOfFaceSwitchingTo.x == -originalYawAxis.x || vectorOfFaceSwitchingTo.z == -originalYawAxis.z)) {
      return pitch + 90; //rotate head back
    }

    return -pitch;
  }

  public float convertYawToNewCubePermutation(float yaw, WorldPermutation newPerm, boolean isNewPermutationOnOppositeSide, Player p) {
    if (isNewPermutationOnOppositeSide) {
      Vector3d lookVector = getVectorFromYaw(yaw);
      Vector3d yaxAxisWorld = axisTransformation.unapply(lookVector);
      Vector3d newPermLookVector = newPerm.axisTransformation.apply(yaxAxisWorld);
      float newPermYaw = clampDegrees(getYawFromVector(newPermLookVector, p) + 180);
      p.sendMessage("look " + lookVector);
      p.sendMessage("yaxAxisWorld " + yaxAxisWorld);
      p.sendMessage("newPermLookVector " + newPermLookVector);
      p.sendMessage("newPermYaw " + newPermYaw);
      return newPermYaw;
    }

    Vector3d yaxAxisWorld = newPerm.getWorldYawAxisFacing(yaw);
    Vector3d localYawAxis = axisTransformation.apply(yaxAxisWorld);
    Vector3d originalYawAxis = newPerm.getLocalYawAxisFacing(yaw);

    float yawOnNewCube = getYawFromAxisDirectionFacing(localYawAxis);

    Vector3d vectorOfFaceSwitchingTo = newPerm.axisTransformation.apply(topFaceCoordinateOnMainWorld).normalize();

    if (vectorOfFaceSwitchingTo.x == originalYawAxis.x && vectorOfFaceSwitchingTo.z == originalYawAxis.z) {
      return yawOnNewCube;
    }

    return clampDegrees(yawOnNewCube + 180);
  }

  public Vector3d getWorldYawAxisFacing(float yaw) {
    return axisTransformation.unapply(WorldPermutation.getLocalYawAxisFacing(yaw));
  }


  public Vector3d getLocalCoordinateFromWorldCoordinate(Vector3d worldCoordinate) {
    return axisTransformation.apply(worldCoordinate);
  }

  public Location getLocationFromRelativeCoordinate(Vector3d vector) {
    return new Location(center.getWorld(), center.getBlockX() + 0.5 + vector.x, center.getBlockY() + 0.5 + vector.y, center.getBlockZ() + 0.5 + vector.z);
  }

  private Vector3d getLocationRelativeToThisPermutation(Location loc) {
    Vector3d toReturn = new Vector3d(
        loc.getX() - (center.getBlockX() + 0.5),
        loc.getY() - (center.getBlockY() + 0.5),
        loc.getZ() - (center.getBlockZ() + 0.5));
     return toReturn;
  }

  public Vector3d getCubeWorldCoordinate(Location loc) {
    return axisTransformation.unapply(getLocationRelativeToThisPermutation(loc));
  }

  public static Location translateLocation(Location loc, int xTrans, int yTrans, int zTrans) {
    return new Location(loc.getWorld(), loc.getBlockX() + xTrans, loc.getBlockY() + yTrans, loc.getBlockZ() + zTrans);
  }

  private float getDegreesBetweenRotations(float r1, float r2) {
    // Calculate the difference between the two rotations
    float rotationDifference = r2 - r1;

    // Normalize the rotation difference to be within the range [-180, 180]
    while (rotationDifference > 180) {
      rotationDifference -= 360;
    }
    while (rotationDifference < -180) {
      rotationDifference += 360;
    }

    // Return the rotation difference in degrees
    return rotationDifference;
  }

  public float getYawForSeamlessSwitch(Vector3d directionOfClosestFace, float playerYaw) {
    //this wont work for upside down face
    float yawInDirectionOfNewFace = getYawFromAxisDirectionFacing(directionOfClosestFace);

    if (Math.abs(getDegreesBetweenRotations(yawInDirectionOfNewFace, playerYaw)) > 90) {
        float oppositeDirection = getYawFromAxisDirectionFacing(new Vector3d(directionOfClosestFace.x * -1, directionOfClosestFace.y * -1, directionOfClosestFace.z * -1));
        return oppositeDirection;
    }

    return yawInDirectionOfNewFace;
  }

}
