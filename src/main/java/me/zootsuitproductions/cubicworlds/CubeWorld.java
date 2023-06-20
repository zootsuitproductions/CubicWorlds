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
    }, 20L, 1L);
  }

  public void switchPlayerPermutationsIfNecessaryRepeatingTask() {
    world.getPlayers().forEach(p ->
    {
      if (shouldPlayerBeTeleportedToNewFace(p)) {
        teleportToClosestFace(p, plugin);
      }
    });
  }

  public void setBlockOnAllPermsExcept(BlockData blockData, Vector3d cubeWorldCoordinate, WorldPermutation dontSetOnThisOne) {
    int skipIndex = dontSetOnThisOne.index;
    for (int i = 0; i < worldPermutations.length; i++) {
      if (i == skipIndex) {
        continue;
      }

      Location loc = worldPermutations[i].getLocationOnThisPermFromCubeWorldCoordinate(cubeWorldCoordinate, world);

      System.out.println(loc.toVector());
      loc.getBlock().setBlockData(blockData);

    }
  }

  //put them in a grid not a line

  public WorldPermutation getClosestPermutation(Location location) {
    //this only works for thousand block seperated worlds

    Location copy = location.clone();

    Location relativeLoc = copy.subtract(mainCubeCenter);

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


  private float calculateYawToRotatePerTick(float currentYaw, float finalYaw, int ticksToRotateOver) {
    float yawPerTick;
    float difference = finalYaw - currentYaw;

    if (difference > 180) {
      difference -= 360;
    } else if (difference < -180) {
      difference += 360;
    }

    yawPerTick = difference / (ticksToRotateOver+1);
    return yawPerTick;
  }

  public void rotTimer(Player p, Plugin plugin, WorldPermutation currentRot, WorldPermutation closestFace, float yawForSeamlessSwitch, Vector3d directionOfNewFace) {

    int ticksToRotateOver = 8;

    //todo: ROTATION

//    float degreeDifference = (rotateToThisYaw - p.getLocation().getYaw());
//    if (degreeDifference > 180) {
//      degreeDifference = 360 - degreeDifference;
//    } else if (degreeDifference < -180) {
//      degreeDifference = 360 + degreeDifference;
//    }

//    float degreesToRotatePerTick = degreeDifference / ticksToRotateOver;

    new BukkitRunnable() {
      int counter = 0;
      Location lastPlayerPosition = p.getLocation();
      float yawPerTick = calculateYawToRotatePerTick(p.getLocation().getYaw(), yawForSeamlessSwitch, ticksToRotateOver);

      Vector newVelo;
      @Override
      public void run() {
        p.sendMessage("c" + counter);
        yawPerTick = calculateYawToRotatePerTick(p.getLocation().getYaw(), yawForSeamlessSwitch, ticksToRotateOver - counter);

        if (counter > 0) {
          Location displacement = p.getLocation().subtract(lastPlayerPosition);
          lastPlayerPosition = p.getLocation();

          int ticks = 1;
          Vector velocity = new Vector(displacement.getX() / ticks,displacement.getY() / ticks, displacement.getZ() / ticks);

          p.sendMessage("vel " + velocity);

          //add velocity in the outward direction too, to move one block out
//


          if (counter == 1) {
            Vector velocityTowardNewFace = new Vector(directionOfNewFace.x * 0.1, 0, directionOfNewFace.z * 0.1);
            newVelo = velocity.add(new Vector(0,0.1,0)).add(velocityTowardNewFace);
            if (newVelo.getY() > 0) {
              newVelo.setY(0);
            }

          }
          Location pLoc = p.getLocation();
          p.sendMessage("yawpertick " + yawPerTick);
          pLoc.setYaw(pLoc.getYaw() + yawPerTick);
          p.teleport(pLoc);
          p.setVelocity(newVelo);

          if (counter >= ticksToRotateOver) {
            Location rotatedLocation = currentRot.getMinecraftWorldLocationOnOtherCube(closestFace, p.getLocation());
            Vector rotatedVelocity = currentRot.rotateVectorToOtherCube(velocity, closestFace);
            p.teleport(rotatedLocation);
            p.setVelocity(rotatedVelocity);

            currentPermutationOfPlayer.put(p.getUniqueId(), closestFace);
            cancel();
            return;
          }
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

    if (currentRot == null) return false;
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


  public boolean teleportToClosestFace(Player player, Plugin plugin) {
    UUID uuid = player.getUniqueId();

    Location loc = player.getLocation();

    Location eyeLocation = loc.add(0,1.62,0);

    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(uuid, worldPermutations[0]);

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLocation);
    WorldPermutation closestFace = findClosestFaceToCubeWorldCoordinate(cubeWorldCoordinateOfPlayer);

    Vector3d directionOfClosestFace = currentRot.axisTransformation.apply(closestFace.topFaceCoordinateOnMainWorld).normalize();

    //make sure the player has at least 1 block of space in the opposite direction as the face
    //they are switching to, to make room for the players legs when they teleport
    Location behindLoc = player.getLocation().subtract(directionOfClosestFace.x,directionOfClosestFace.y,directionOfClosestFace.z);

    if ((closestFace == currentRot) /*|| (behindLoc.getBlock().getBlockData().getMaterial() != Material.AIR)*/) return false;

    currentPermutationOfPlayer.put(uuid, null);
    //null used to indicate teleportation in progress

    float yawForSeamlessSwitch = currentRot.getYawForSeamlessSwitch(directionOfClosestFace, player.getLocation().getYaw());

    rotTimer(player,plugin, currentRot, closestFace, yawForSeamlessSwitch, directionOfClosestFace);


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
