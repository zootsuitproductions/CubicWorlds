package me.zootsuitproductions.cubicworlds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

public class CubeWorld {
  private final int radius;
  Map<UUID, WorldPermutation> currentPermutationOfPlayer = new HashMap<>();
  Map<UUID, Boolean> playerIsReadyToTeleport = new HashMap<>();
  Vector3d[] cubeFaceCenters = new Vector3d[6];
  WorldPermutation[] worldPermutations = new WorldPermutation[6];

  public CubeWorld(Location center, Location pasteCenter, int radius) {
    this.radius = radius;
    int spaceBetweenCubeRotationsInWorld = radius*5 + 30;

    cubeFaceCenters[0] = new Vector3d(0, radius,0);
    worldPermutations[0] = new WorldPermutation(center, pasteCenter, radius, AxisTransformation.TOP, cubeFaceCenters[0]);

    for (int i = 1; i < AxisTransformation.transformations.length; i++) {
      cubeFaceCenters[i] = AxisTransformation.transformations[i].unapply(cubeFaceCenters[0]);

      worldPermutations[i] = new WorldPermutation(
          worldPermutations[0],
          WorldPermutation.translateLocation(pasteCenter, i * spaceBetweenCubeRotationsInWorld,0,0),
          AxisTransformation.transformations[i],
          cubeFaceCenters[i]);
    }
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

  public static void loadChunkRadius(Location loc, int radius) {
    Chunk centerChunk = loc.getChunk();
    int centerX = centerChunk.getX();
    int centerZ = centerChunk.getZ();
    for (int x = centerX - radius; x <= centerX + radius; x++) {
      for (int z = centerZ - radius; z <= centerZ + radius; z++) {
        loc.getWorld().loadChunk(x, z);
      }
    }
  }

//  private void acc

  public void rotTimer(Player p, Vector velocity, Plugin plugin, float rotateToThisYaw, Location rotatedLocation, WorldPermutation currentRot, WorldPermutation closestFace) {

    //do head rotation in movement event!!!!
    //change thje loookm direction if falling
    

//    loadChunkRadius(rotatedLocation, 2);

//    GameMode currentMode = p.getGameMode();
//    p.setGameMode(GameMode.SPECTATOR);

    //create an invisible armor stand and have player spectate it
//    ArmorStand a = (ArmorStand) p.getWorld().spawnEntity(pLoc, EntityType.ARMOR_STAND);
//    a.setGravity(false);
//    a.setVisible(false);
//    p.setSpectatorTarget(a);

    int ticksToRotateOver = 5;

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

          startTimerTillPlayerCanChangeFacesAgain(p.getUniqueId(), plugin);
          cancel();
          return;
        }

//        Location aLoc = a.getLocation();

//        pLoc.setYaw(pLoc.getYaw() + degreesToRotatePerTick);

        //can update this dynamically to compensate for mouse movement

        //make it a natural movement

//        p.sendMessage("rot " + counter + ": " + pLoc.getYaw());
//        p.sendMessage("deg per Tick: " + degreesToRotatePerTick);
//        p.teleport(pLoc);

        if (counter == ticksToRotateOver - 1) {
          Location rotatedLocation = getMinecraftWorldLocationOnOtherCube(currentRot, closestFace, p.getLocation());
//          p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,20, 2));

          p.teleport(rotatedLocation);
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

    Location eyeLocation = loc.add(0,1.62,0);

    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(player.getUniqueId(), worldPermutations[0]);

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLocation);
    WorldPermutation closestFace = findClosestCubeRotationToCoordinate(cubeWorldCoordinateOfPlayer);

    return (closestFace != currentRot);
  }

  public boolean teleportToClosestFace(Player player, Vector velocity, Plugin plugin) {
    UUID uuid = player.getUniqueId();

    Location loc = player.getLocation();

    Location eyeLocation = loc.add(0,1.62,0);

    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(uuid, worldPermutations[0]);

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLocation);
    WorldPermutation closestFace = findClosestCubeRotationToCoordinate(cubeWorldCoordinateOfPlayer);

    if (closestFace == currentRot) return false;

    //player will be teleported now, so disable until done teleporting
    playerIsReadyToTeleport.put(uuid, false);

    Location actualWorldLocationToTeleportTo = getMinecraftWorldLocationOnOtherCube(currentRot, closestFace, loc);

    float desiredYawForSeamlessSwitch = getYawForSeamlessSwitch(currentRot, closestFace);

    rotTimer(player, velocity, plugin, desiredYawForSeamlessSwitch, actualWorldLocationToTeleportTo, currentRot, closestFace);

    return true;

  }

  private WorldPermutation findClosestCubeRotationToCoordinate(Vector3d coordinate) {
    int closestFaceIndex = 0;
    double closestDistance = coordinate.distance(cubeFaceCenters[0]);

    for (int i = 1; i < cubeFaceCenters.length; i++) {
      double distance = coordinate.distance(cubeFaceCenters[i]);
      if (distance < closestDistance) {
        closestDistance = distance;
        closestFaceIndex = i;
      }
    }
    return worldPermutations[closestFaceIndex];
  }

}
