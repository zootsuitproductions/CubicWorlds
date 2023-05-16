package me.zootsuitproductions.cubicworlds;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class WorldPermutation {
  public final AxisTransformation axisTransformation;
  public final Vector3d topFaceCoordinateOnMainWorld;
  public final Location center;
  private int radius;
  private final CubeFaceRegion[] faces = new CubeFaceRegion[6];
  private Location[] faceCenters;
  private Map<Location, Block> sidewaysBlocks = new HashMap<>();

  private ISetBlocksOverTimeOperation createOperation;

  private ISetBlocksOverTimeOperation clearBlocksAroundCubeWorld(Location center, int blocksPerTick, int cubeRadius,
      int clearUntilRadius, Plugin plugin, ISetBlocksOverTimeOperation nextOperation) {
    World world = center.getWorld();

    int maxHeight = center.getWorld().getMaxHeight();
    int minHeight = center.getWorld().getMinHeight();

    int centerX = center.getBlockX();
    int centerY = center.getBlockY();
    int centerZ = center.getBlockZ();

    ISetBlocksOverTimeOperation leftVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX - clearUntilRadius,
            minHeight,
            centerZ - clearUntilRadius),
        new Location(
            world,
            centerX - cubeRadius,
            maxHeight,
            centerZ + clearUntilRadius),
        blocksPerTick, plugin, nextOperation);

    ISetBlocksOverTimeOperation rightVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX + cubeRadius,
            minHeight,
            centerZ - clearUntilRadius),
        new Location(
            world,
            centerX + clearUntilRadius,
            maxHeight,
            centerZ + clearUntilRadius),
        blocksPerTick,
        plugin, leftVoid);

    ISetBlocksOverTimeOperation frontVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX - cubeRadius,
            minHeight,
            centerZ + cubeRadius),
        new Location(
            world,
            centerX + cubeRadius,
            maxHeight,
            centerZ + clearUntilRadius),
        blocksPerTick, plugin, rightVoid);

    ISetBlocksOverTimeOperation backVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX - cubeRadius,
            minHeight,
            centerZ - clearUntilRadius),
        new Location(
            world,
            centerX + cubeRadius,
            maxHeight,
            centerZ - cubeRadius),
        blocksPerTick, plugin, frontVoid);

    ISetBlocksOverTimeOperation bottomVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX - clearUntilRadius,
            minHeight,
            centerZ - clearUntilRadius),
        new Location(
            world,
            centerX + clearUntilRadius,
            CubicWorlds.clampValueToRange(centerY - 2 * cubeRadius, minHeight, maxHeight),
            centerZ + clearUntilRadius),
        blocksPerTick,
        plugin, backVoid);

    //test it
    return bottomVoid;
  }

  //main world perm
  public WorldPermutation(List<Location> faceLocations, int radius, Vector3d topFaceCoordinateOnMainWorld, Plugin plugin) {
    this.center = faceLocations.get(0).clone().subtract(0,radius,0);
    this.radius = radius;
    this.axisTransformation = AxisTransformation.TOP;
    this.topFaceCoordinateOnMainWorld = topFaceCoordinateOnMainWorld;

    faceCenters = new Location[] {
        translateLocation(center, 0, radius, 0),
        translateLocation(center, -radius, 0, 0),
        translateLocation(center, 0, -radius, 0),
        translateLocation(center, radius, 0, 0),
        translateLocation(center, 0, 0, radius),
        translateLocation(center, 0, 0, -radius)
    };

    AxisTransformation[] transformations = new AxisTransformation[] {
        AxisTransformation.TOP,
        AxisTransformation.FRONT,
        AxisTransformation.BOTTOM,
        AxisTransformation.BACK,
        AxisTransformation.LEFT,
        AxisTransformation.RIGHT
    };

    ISetBlocksOverTimeOperation prev = null;
    for (int i = 1; i < faceCenters.length; i++) {
      System.out.println(transformations[i]);
      ISetBlocksOverTimeOperation temp = new CopyAndRotateCubeFaceOperation(
          faceLocations.get(i),
          faceCenters[i],
          radius,
          transformations[i],
          1000,
          plugin,
          prev);
      prev = temp;

    }

//    prev.apply();
    clearBlocksAroundCubeWorld(faceLocations.get(0), 1000, radius, radius + 100, plugin, prev).apply();

    createOperation = prev;

  }

  public ISetBlocksOverTimeOperation getCreateOperation() {
    return createOperation;
  }

  public WorldPermutation(Location centerInWorld, Location pasteCenter, int radius, AxisTransformation upFace, Vector3d topFaceCoordinateOnMainWorld) {
    this.center = pasteCenter;
    this.radius = radius;
    this.axisTransformation = upFace;
    this.topFaceCoordinateOnMainWorld = topFaceCoordinateOnMainWorld;

    faceCenters[0] = translateLocation(pasteCenter, 0, radius, 0);
    faces[0] = new CubeFaceRegion(
        centerInWorld,
        faceCenters[0],
        radius, 0, AxisTransformation.transformations[0], sidewaysBlocks);

    faceCenters[1] = translateLocation(pasteCenter, -radius, 0, 0);
    faces[1] = new CubeFaceRegion(
        translateLocation(centerInWorld, -2*radius - 1, 0, 0),
        faceCenters[1],
        radius, 0, AxisTransformation.transformations[1], sidewaysBlocks);

    faceCenters[2] = translateLocation(pasteCenter, 0, -radius, 0);
    faces[2] = new CubeFaceRegion(
        translateLocation(centerInWorld, 4*radius + 2, 0, 0),
        faceCenters[2],
        radius, 0, AxisTransformation.transformations[2], sidewaysBlocks);

    faceCenters[3] = translateLocation(pasteCenter, radius, 0, 0);
    faces[3] = new CubeFaceRegion(
        translateLocation(centerInWorld, 2*radius + 1, 0, 0),
        faceCenters[3],
        radius, 0, AxisTransformation.transformations[3], sidewaysBlocks);

    faceCenters[4] = translateLocation(pasteCenter, 0, 0, radius);
    faces[4] = new CubeFaceRegion(
        translateLocation(centerInWorld, 0, 0, 2*radius + 1),
        faceCenters[4],
        radius, 0, AxisTransformation.transformations[4], sidewaysBlocks);

    faceCenters[5] = translateLocation(pasteCenter, 0, 0, -radius);
    faces[5] = new CubeFaceRegion(
        translateLocation(centerInWorld, 0, 0, -2*radius - 1),
        faceCenters[5],
        radius, 0, AxisTransformation.transformations[5], sidewaysBlocks);

  }

  public WorldPermutation(WorldPermutation mainCube, Location pasteCenter, AxisTransformation axisTransformation, Vector3d topFaceCoordinateOnMainWorld) {
    radius = mainCube.radius;
    this.center = pasteCenter;
    this.axisTransformation = axisTransformation;
    this.topFaceCoordinateOnMainWorld = topFaceCoordinateOnMainWorld;

    for (int x = -2 * radius - 1; x <= 2 * radius + 1; x++) {
      for (int y = -2 * radius - 1; y <= 2 * radius + 1; y++) {
        for (int z = -2 * radius - 1; z <= 2 * radius + 1; z++) {
          Vector3d localCoordinateSource = new Vector3d(x, y, z);
          BlockData copyBlockData = mainCube.getBlockLocationFromRelativeCoordinate(localCoordinateSource).getBlock().getBlockData();

          Vector3d localCoordinateDest = axisTransformation.apply(localCoordinateSource); //this is rotated
          Location worldDestination = getBlockLocationFromRelativeCoordinate(localCoordinateDest);

          worldDestination.getBlock().setBlockData(TransformationUtils.rotateBlockData(copyBlockData, axisTransformation));
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

  public Location[] getFaceCenters() {
    return this.faceCenters.clone();
  }

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
