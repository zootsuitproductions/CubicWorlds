package me.zootsuitproductions.cubicworlds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

public class CubeWorld {
  private final int radius;
  Map<UUID, WorldPermutation> currentPermutationOfPlayer = new HashMap<>();
  Map<UUID, Boolean> playerIsReadyToTeleport = new HashMap<>();
  Map<UUID, Long> playerLastMoveTime = new HashMap<>();
  Vector3d[] cubeFaceCenters = new Vector3d[6];
  WorldPermutation[] worldPermutations = new WorldPermutation[6];

  //if this is changed, need to also change the getClosestPerm function
  public static int spacing = 1000;

  private final World world;

  public static final AxisTransformation[] transformations = new AxisTransformation[] {
      AxisTransformation.TOP,
      AxisTransformation.FRONT,
      AxisTransformation.BOTTOM,
      AxisTransformation.BACK,
      AxisTransformation.LEFT,
      AxisTransformation.RIGHT
  };

  public void setBlockOnAllPermsExcept(BlockData blockData, Vector3d cubeWorldCoordinate, WorldPermutation dontSetOnThisOne) {
    //this is a bit redundant/inefficient. for the og perm it updates it even though it doesnt need to.
    int skipIndex = dontSetOnThisOne.index;
    for (int i = 0; i < worldPermutations.length; i++) {
      if (i == skipIndex) {
        continue;
      }

      Location loc = worldPermutations[i].getLocationOnThisPermFromCubeWorldCoordinate(cubeWorldCoordinate, world);

      loc.getBlock().setBlockData(blockData);

      System.out.println("cube world coordinate: " + cubeWorldCoordinate);
      System.out.println("VNEWsetting air at: " + loc);

    }
  }

  public WorldPermutation getClosestPermutation(Location location) {
    //this only works for thousand block seperated worlds

    int number = location.getBlockX();

    int thousandsPlace = (number / 1000) % 10; // Get the thousands place digit

    if (thousandsPlace > 5 || thousandsPlace < 0) {

      //find the closest conventionally
      System.out.println("too far away");
      return worldPermutations[0];
    }
    System.out.println(location);
    System.out.println("new perm: " + worldPermutations[thousandsPlace].index);
    return worldPermutations[thousandsPlace];
  }

  private void setCurrentPlayerPerms() {
    Bukkit.getServer().getOnlinePlayers().forEach(player -> {
      setCurrentPermutationOfPlayer(player);
    });
  }

  public void setCurrentPermutationOfPlayer(Player player) {
    currentPermutationOfPlayer.put(player.getUniqueId(), getClosestPermutation(player.getLocation()));
  }


  public CubeWorld(Location pasteCenter, int radius, int spaceBetween) {
    this.radius = radius;
    int spaceBetweenCubeRotationsInWorld = spaceBetween;
    this.world = pasteCenter.getWorld();

    world.setSpawnLocation(WorldPermutation.translateLocation(pasteCenter, 0, radius,0));


    cubeFaceCenters[0] = new Vector3d(0, radius,0);
    worldPermutations[0] = new WorldPermutation(pasteCenter, radius, AxisTransformation.TOP, cubeFaceCenters[0], 0);

    ChunkUtils.forceLoadChunksAroundLocation(pasteCenter, radius);

    for (int i = 1; i < transformations.length; i++) {
      cubeFaceCenters[i] = transformations[i].unapply(cubeFaceCenters[0]);

      Location center = WorldPermutation.translateLocation(pasteCenter, i * spaceBetweenCubeRotationsInWorld,0,0);
      ChunkUtils.forceLoadChunksAroundLocation(center, radius);

      worldPermutations[i] = new WorldPermutation(
          worldPermutations[0],
          WorldPermutation.translateLocation(pasteCenter, i * spaceBetweenCubeRotationsInWorld,0,0),
          transformations[i],
          cubeFaceCenters[i], i);
    }


    setCurrentPlayerPerms();
  }

  public void startTimerTillPlayerCanChangeFacesAgain(UUID playerID, Plugin plugin) {
    // Run the task after a 10-second delay and repeat every 20 ticks (1 second)

    new BukkitRunnable() {
      @Override
      public void run() {
        playerIsReadyToTeleport.put(playerID, true);
        cancel();

      }
    }.runTaskTimer(plugin, 20, 20);
  }


  public void rotTimer(Player p, Vector velocity, Plugin plugin, float rotateToThisYaw, Location rotatedLocation, WorldPermutation currentRot, WorldPermutation closestFace) {

    p.sendMessage("velocity: " + velocity);
    int ticksToRotateOver = 1;

    float degreeDifference = (rotateToThisYaw - p.getLocation().getYaw());
    if (degreeDifference > 180) {
      degreeDifference = 360 - degreeDifference;
    } else if (degreeDifference < -180) {
      degreeDifference = 360 + degreeDifference;
    }

    float degreesToRotatePerTick = degreeDifference / ticksToRotateOver;

    p.sendMessage("currentYaw: " + p.getLocation().getYaw());
    p.sendMessage("rotateToThisyaw: " + rotateToThisYaw);
    p.sendMessage("degreeDifference: " + degreeDifference);


    new BukkitRunnable() {

      int counter = 0;
      @Override
      public void run() {
        Location pLoc = p.getLocation();

        if (counter == ticksToRotateOver) {
//          p.setGameMode(currentMode);
//          a.remove();
          currentPermutationOfPlayer.put(p.getUniqueId(), closestFace);
          p.setVelocity(new Vector(0,0,0));
//          startTimerTillPlayerCanChangeFacesAgain(p.getUniqueId(), plugin);
          cancel();
          return;
        }


        pLoc.setYaw(pLoc.getYaw() + degreesToRotatePerTick);

        //can update this dynamically to compensate for mouse movement

        //make it a natural movement

//        p.sendMessage("rot " + counter + ": " + pLoc.getYaw());
//        p.sendMessage("deg per Tick: " + degreesToRotatePerTick);

        //rotate the velocity vector instead
        p.teleport(pLoc);

        if (counter == ticksToRotateOver - 1) {
          Location rotatedLocation = getMinecraftWorldLocationOnOtherCube(currentRot, closestFace, p.getLocation());

          p.teleport(rotatedLocation);
          Vector rotatedVelocity = currentRot.rotateVectorToOtherCube(velocity, closestFace);
          rotatedVelocity.setY(0);

          p.setVelocity(rotatedVelocity.multiply(0.3));

        }

        counter ++;
      }
    }.runTaskTimer(plugin, 0, 1);
  }

  private float getYawForSeamlessSwitch(
      WorldPermutation currentCube, WorldPermutation cubeToTeleportTo) {
    Vector3d faceVectorOfFaceAboutToSwitchTo = currentCube.axisTransformation.apply(
        cubeToTeleportTo.topFaceCoordinateOnMainWorld);
    return WorldPermutation.getYawFromAxisDirectionFacing(faceVectorOfFaceAboutToSwitchTo.div(radius));
  }

  private Location getMinecraftWorldLocationOnOtherCube(WorldPermutation currentCube, WorldPermutation cubeToTeleportTo, Location playerLoc) {
    Location eyeLocation = playerLoc.add(0,1.62,0);
    Vector3d cubeWorldCoordinateOfPlayerEyes = currentCube.getCubeWorldCoordinate(eyeLocation);

    Vector3d localCoordOnClosestFace = cubeToTeleportTo.getLocalCoordinateFromWorldCoordinate(cubeWorldCoordinateOfPlayerEyes);
    localCoordOnClosestFace = localCoordOnClosestFace.sub(0, 1.62, 0);
    Location actualWorldLocationToTeleportTo = cubeToTeleportTo.getLocationFromRelativeCoordinate(localCoordOnClosestFace);

    float newYaw = cubeToTeleportTo.convertYawFromOtherCubeRotation(eyeLocation.getYaw(), currentCube);
    actualWorldLocationToTeleportTo.setYaw(newYaw);

    //if player looking towards axis do this
    actualWorldLocationToTeleportTo.setPitch(eyeLocation.getPitch() - 90f);

    return actualWorldLocationToTeleportTo;

  }

  public boolean shouldPlayerBeTeleportedToNewFace(Player player) {
    Location loc = player.getLocation();

    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(player.getUniqueId(), worldPermutations[0]);

    return currentRot.isLocationOffOfFaceRadius(player.getLocation());
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

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLocation);
    WorldPermutation closestFace = findClosestFaceToCubeWorldCoordinate(cubeWorldCoordinateOfPlayer);

    if (closestFace == currentRot) return false;

    //player will be teleported now, so disable until done teleporting
    playerIsReadyToTeleport.put(uuid, false);

    Location actualWorldLocationToTeleportTo = getMinecraftWorldLocationOnOtherCube(currentRot, closestFace, loc);

    float desiredYawForSeamlessSwitch = getYawForSeamlessSwitch(currentRot, closestFace);

    rotTimer(player, velocity, plugin, desiredYawForSeamlessSwitch, actualWorldLocationToTeleportTo, currentRot, closestFace);




    return true;

  }

//  private WorldPermutation findClosestPermutationFromMCWorldCoordinate(Location worldLocation) {
//
//  }

  private WorldPermutation findClosestFaceToCubeWorldCoordinate(Vector3d mainCubeWorldCoordinate) {
    //      AxisTransformation.TOP,
    //      AxisTransformation.FRONT,
    //      AxisTransformation.BOTTOM,
    //      AxisTransformation.BACK,
    //      AxisTransformation.LEFT,
    //      AxisTransformation.RIGHT


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
