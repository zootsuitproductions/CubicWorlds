package me.zootsuitproductions.cubicworlds;

import static me.zootsuitproductions.cubicworlds.CubicWorlds.creatingWorldStateFileName;
import static me.zootsuitproductions.cubicworlds.FileUtils.deleteFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

public class CubeWorld {
  private final int radius;
  Map<UUID, WorldPermutation> currentPermutationOfPlayer = new HashMap<>();
  Map<UUID, Long> playerLastMoveTime = new HashMap<>();
  Vector3d[] cubeFaceCenters = new Vector3d[6];
  WorldPermutation[] worldPermutations = new WorldPermutation[6];

  //if this is changed, need to also change the getClosestPerm function
  public static int spacing = 100;

  public static int mainCubeZPos = 500;
  public static int mainCubeXPos = 500;

  private final Location mainCubeCenter;

  private final World world;
  private final Plugin plugin;
  private List<PlayerTimePosition> playerTimePositions = new ArrayList<>();

  public static final AxisTransformation[] transformations = new AxisTransformation[] {
      AxisTransformation.TOP,
      AxisTransformation.FRONT,
      AxisTransformation.BOTTOM,
      AxisTransformation.BACK,
      AxisTransformation.LEFT,
      AxisTransformation.RIGHT
  };

  private void setupCubeWorld() {
    WECubeWorldPaster worldPaster = new WECubeWorldPaster(radius, cubeCenterLocations);

    if (CubicWorlds.shouldCreateNewCubeWorld()) {
      worldPaster.pasteWorldAtLocation(mainCubeCenter, plugin);
      System.out.println("Creating new cube world");
      deleteFile(creatingWorldStateFileName);
    }

  }

  private void beginCheckingForPlayerTeleportations() {
    Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
      @Override
      public void run() {
        switchPlayerPermutationsIfNecessaryRepeatingTask();
      }
    }, 20L, 5L);
  }

  public void switchPlayerPermutationsIfNecessaryRepeatingTask() {
    world.getPlayers().forEach(p ->
    {
      if (shouldPlayerBeTeleportedToNewFace(p)) {
        playerTimePositions.add(new PlayerTimePosition(p, p.getLocation(), System.currentTimeMillis()));
      }
    });

    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < playerTimePositions.size(); i++) {
          PlayerTimePosition pTimePos = playerTimePositions.get(i);
          Player p = pTimePos.getPlayer();

          Location displacement = p.getLocation().subtract(pTimePos.getLocation());

          int ticks = 1;

          Vector velocity = new Vector(displacement.getX() / ticks,
              displacement.getY() / ticks, displacement.getZ() / ticks);

          teleportToClosestFace(p, velocity, plugin);
        }

        playerTimePositions.clear();
      }
    }, 1);
  }

  public void setBlockOnAllPermsExcept(BlockData blockData, Vector3d cubeWorldCoordinate, WorldPermutation dontSetOnThisOne) {
    int skipIndex = dontSetOnThisOne.index;
    for (int i = 0; i < worldPermutations.length; i++) {
      if (i == skipIndex) {
        continue;
      }

      Location loc = worldPermutations[i].getLocationOnThisPermFromCubeWorldCoordinate(cubeWorldCoordinate, world);

      loc.getBlock().setBlockData(blockData);

    }
  }

  //put them in a grid not a line

  public WorldPermutation getClosestPermutation(Location location) {
    //this only works for thousand block seperated worlds

    Location relativeLoc = location.subtract(mainCubeCenter);

    int x = (int) Math.round(relativeLoc.getBlockX() / 100.0);
    int z = (int) Math.round(relativeLoc.getBlockZ() / 100.0);

    System.out.println("actual: " + location);
    System.out.println("relative: " + relativeLoc);
    System.out.println("x: " + x);
    System.out.println("z: " + z);

    int worldIndex = z;

    if (x == 1) {
      switch (z) {
        case 0:
          worldIndex = 3;
          break;
        case 1:
          worldIndex = 4;
          break;
        default:
          worldIndex = 5;
          break;
      }
    }


//    worldIndex = Math.round((float) (x - mainCubeXPos) / (float) spacing);

    return worldPermutations[worldIndex];
  }

  private void setCurrentPlayerPerms() {
    Bukkit.getServer().getOnlinePlayers().forEach(player -> {
      setCurrentPermutationOfPlayer(player);
    });
  }

  public void setCurrentPermutationOfPlayer(Player player) {
    currentPermutationOfPlayer.put(player.getUniqueId(), getClosestPermutation(player.getLocation()));
  }

  private final Vector[] cubeCenterScaledPositions = new Vector[] {
      new Vector(0,0,0),
      new Vector(0,0,1),
      new Vector(0,0,2),
      new Vector(1,0,0),
      new Vector(1,0,1),
      new Vector(1,0,2),
  };

  private final Location[] cubeCenterLocations = new Location[6];


  public CubeWorld(Location pasteCenter, int radius, int spaceBetween, Plugin plugin) {
    this.radius = radius;
    this.mainCubeCenter = pasteCenter;
    int spaceBetweenCubeRotationsInWorld = spaceBetween;
    this.world = pasteCenter.getWorld();
    this.plugin = plugin;

    world.setSpawnLocation(WorldPermutation.translateLocation(pasteCenter, 0, radius,0));

    cubeFaceCenters[0] = new Vector3d(0, radius,0);
    worldPermutations[0] = new WorldPermutation(pasteCenter, radius, AxisTransformation.TOP, cubeFaceCenters[0], 0);

    cubeCenterLocations[0] = mainCubeCenter;

    ChunkUtils.forceLoadChunksAroundLocation(pasteCenter, radius);

    for (int i = 1; i < transformations.length; i++) {
      cubeFaceCenters[i] = transformations[i].unapply(cubeFaceCenters[0]);

      cubeCenterLocations[i] = WorldPermutation.translateLocation(mainCubeCenter,
          cubeCenterScaledPositions[i].getBlockX() * spaceBetweenCubeRotationsInWorld,
          cubeCenterScaledPositions[i].getBlockY() * spaceBetweenCubeRotationsInWorld,
          cubeCenterScaledPositions[i].getBlockZ() * spaceBetweenCubeRotationsInWorld)
      ;
      //uncomment if you want to keep all chunks loaded
      Location center = WorldPermutation.translateLocation(pasteCenter, i * spaceBetweenCubeRotationsInWorld,0,0);
//      ChunkUtils.forceLoadChunksAroundLocation(center, radius);


      worldPermutations[i] = new WorldPermutation(
          worldPermutations[0],
          cubeCenterLocations[i],
//          WorldPermutation.translateLocation(pasteCenter, i * spaceBetweenCubeRotationsInWorld,0,0),
          transformations[i],
          cubeFaceCenters[i], i);
    }


    setupCubeWorld();
    setCurrentPlayerPerms();
    beginCheckingForPlayerTeleportations();
  }


  public void rotTimer(Player p, Vector velocity, Plugin plugin, float rotateToThisYaw, Location rotatedLocation, WorldPermutation currentRot, WorldPermutation closestFace) {

    int ticksToRotateOver = 1;

//    float degreeDifference = (rotateToThisYaw - p.getLocation().getYaw());
//    if (degreeDifference > 180) {
//      degreeDifference = 360 - degreeDifference;
//    } else if (degreeDifference < -180) {
//      degreeDifference = 360 + degreeDifference;
//    }

//    float degreesToRotatePerTick = degreeDifference / ticksToRotateOver;

    new BukkitRunnable() {

      int counter = 0;
      @Override
      public void run() {
        Location pLoc = p.getLocation();

        if (counter == ticksToRotateOver) {
          currentPermutationOfPlayer.put(p.getUniqueId(), closestFace);
          cancel();
          return;
        }

        if (counter == ticksToRotateOver - 1) {
          Location rotatedLocation = currentRot.getMinecraftWorldLocationOnOtherCube(closestFace, p.getLocation());

          p.teleport(rotatedLocation);
          Vector rotatedVelocity = currentRot.rotateVectorToOtherCube(velocity, closestFace);

          p.setVelocity(rotatedVelocity);
        }

        counter ++;
      }
    }.runTaskTimer(plugin, 0, 1);
  }

  private float getYawForSeamlessSwitch(
      WorldPermutation currentCube, WorldPermutation cubeToTeleportTo, float playerYaw) {

    Vector3d faceVectorOfFaceAboutToSwitchTo = currentCube.axisTransformation.apply(
        cubeToTeleportTo.topFaceCoordinateOnMainWorld);


    float potentialYaw = WorldPermutation.getYawFromAxisDirectionFacing(faceVectorOfFaceAboutToSwitchTo.div(radius));

    if (Math.round(playerYaw / 90) * 90 != potentialYaw) {
      potentialYaw = potentialYaw - 180;
    }

    return potentialYaw;
  }

  private Location getMinecraftWorldLocationOnOtherCube(WorldPermutation currentCube, WorldPermutation cubeToTeleportTo, Location playerLoc) {
    Location eyeLocation = playerLoc.add(0,1.62,0);
    Vector3d cubeWorldCoordinateOfPlayerEyes = currentCube.getCubeWorldCoordinate(eyeLocation);

    Vector3d localCoordOnClosestFace = cubeToTeleportTo.getLocalCoordinateFromWorldCoordinate(cubeWorldCoordinateOfPlayerEyes);
    localCoordOnClosestFace = localCoordOnClosestFace.sub(0, 1.62, 0);
    Location actualWorldLocationToTeleportTo = cubeToTeleportTo.getLocationFromRelativeCoordinate(localCoordOnClosestFace);


    float newYaw = cubeToTeleportTo.convertYawFromOtherCubeRotation(eyeLocation.getYaw(), currentCube);
    actualWorldLocationToTeleportTo.setYaw(newYaw);

    actualWorldLocationToTeleportTo.setPitch(cubeToTeleportTo.convertPitchFromOtherCubeRotation(eyeLocation.getPitch(), eyeLocation.getYaw(), currentCube));

    return actualWorldLocationToTeleportTo;
  }

  public boolean shouldPlayerBeTeleportedToNewFace(Player player) {
    Location loc = player.getLocation();

    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(player.getUniqueId(), worldPermutations[0]);

    Block blockUnder = loc.subtract(0,1,0).getBlock();

    //zombie block on head to make normal blocks over the edge gravity\\

    //block in direction of looking.

    //space for their entire body:



    return currentRot.isLocationOffOfFaceRadius(loc) /*&& blockUnder.getBlockData().getMaterial() == Material.AIR*/;
//
//    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLocation);
//    WorldPermutation closestFace = findClosestFaceToCubeWorldCoordinate(cubeWorldCoordinateOfPlayer);
//
//    return (closestFace != currentRot);
  }



  public boolean teleportToClosestFace(Player player, Vector velocity, Plugin plugin) {
    UUID uuid = player.getUniqueId();

    Location loc = player.getLocation();

    Location eyeLocation = loc.add(0,1.62,0);


    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(uuid, worldPermutations[0]);

    int index = currentRot.index;

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLocation);
    WorldPermutation closestFace = findClosestFaceToCubeWorldCoordinate(cubeWorldCoordinateOfPlayer);

    Vector3d directionOfClosestFace = currentRot.axisTransformation.apply(closestFace.topFaceCoordinateOnMainWorld).normalize();
    Location behindLoc = player.getLocation().subtract(directionOfClosestFace.x,directionOfClosestFace.y,directionOfClosestFace.z);


    if ((closestFace == currentRot) || (behindLoc.getBlock().getBlockData().getMaterial() != Material.AIR)) return false;


    Location actualWorldLocationToTeleportTo = getMinecraftWorldLocationOnOtherCube(currentRot, closestFace, loc);

    float desiredYawForSeamlessSwitch = getYawForSeamlessSwitch(currentRot, closestFace, player.getLocation().getYaw());

    player.sendMessage("yaw" + desiredYawForSeamlessSwitch);
    rotTimer(player, velocity, plugin, desiredYawForSeamlessSwitch, actualWorldLocationToTeleportTo, currentRot, closestFace);


    return true;

  }

//  private WorldPermutation findClosestPermutationFromMCWorldCoordinate(Location worldLocation) {
//
//  }

  private WorldPermutation findClosestFaceToCubeWorldCoordinate(Vector3d mainCubeWorldCoordinate) {

    int closestFaceIndex = 0;
    double closestDistance = mainCubeWorldCoordinate.distance(cubeFaceCenters[0]);

    for (int i = 1; i < cubeFaceCenters.length; i++) {
      double distance = mainCubeWorldCoordinate.distance(cubeFaceCenters[i]);
      if (distance < closestDistance) {
        closestDistance = distance;
        closestFaceIndex = i;
      }
    }
    return worldPermutations[closestFaceIndex];
  }

}
