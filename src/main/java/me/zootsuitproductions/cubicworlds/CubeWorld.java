package me.zootsuitproductions.cubicworlds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Vector3d;
import org.joml.sampling.BestCandidateSampling.Cube;

import static me.zootsuitproductions.cubicworlds.CubeRotation.convertYawPitchToVector;

public class CubeWorld {
  Map<UUID, CubeRotation> currentPermutationOfPlayer = new HashMap<>();
  Map<UUID, Boolean> playerIsReadyToTeleport = new HashMap<>();
  Map<UUID, CubeRotation> previousPermutationOfPlayer = new HashMap<>();
  public static AxisTransformation[] transformations = new AxisTransformation[] {
      AxisTransformation.TOP,
      AxisTransformation.FRONT,
      AxisTransformation.BOTTOM,
      AxisTransformation.BACK,
      AxisTransformation.LEFT,
      AxisTransformation.RIGHT
  };

  Vector3d[] faceCenters = new Vector3d[6];
  CubeRotation[] cubeRotations = new CubeRotation[6];

  private final int radius;

  public CubeWorld(Location center, Location pasteCenter, int radius) {
    this.radius = radius;
    int spaceBetweenCubeRotationsInWorld = radius*5 + 30;

    faceCenters[0] = new Vector3d(0, radius,0);
//    faceCenters[1] = new Vector3d(radius, 0,0);
//    faceCenters[2] = new Vector3d(0, -radius,0);
//    faceCenters[3] = new Vector3d(-radius, 0,0);
//    faceCenters[4] = new Vector3d(0, 0, -radius);
//    faceCenters[5] = new Vector3d(0, 0, radius);

    cubeRotations[0] = new CubeRotation(center, pasteCenter, radius, AxisTransformation.TOP, faceCenters[0]);

    System.out.println(faceCenters[0]);

    for (int i = 1; i < transformations.length; i++) {
      faceCenters[i] = transformations[i].unapply(faceCenters[0]);
      System.out.println("face index: " + i);

      //look at this

      System.out.println(faceCenters[i]);

      cubeRotations[i] = new CubeRotation(
          cubeRotations[0],
          CubeRotation.translateLocation(pasteCenter, i * spaceBetweenCubeRotationsInWorld,0,0),
          transformations[i],
          faceCenters[i]);
    }
  }

  public void undoFaceSwitch(Player p) {
    p.teleport(previousLoc.subtract(0,1.62,0));
    currentPermutationOfPlayer.put(p.getUniqueId(), previousPermutationOfPlayer.get(p.getUniqueId()));
  }

  //NEGATIVE Z is off
  //and negative x
  //postives work

  Location previousLoc;

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


  public void rotTimer(Player p, Plugin plugin, float rotateToThisYaw, Location rotatedLocation, CubeRotation currentRot, CubeRotation closestFace) {

//    loadChunkRadius(rotatedLocation, 2);

//    GameMode currentMode = p.getGameMode();
//    p.setGameMode(GameMode.SPECTATOR);

    //create an invisible armor stand and have player spectate it
//    ArmorStand a = (ArmorStand) p.getWorld().spawnEntity(pLoc, EntityType.ARMOR_STAND);
//    a.setGravity(false);
//    a.setVisible(false);
//    p.setSpectatorTarget(a);

    int ticksToRotateOver = 20;

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

          p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,20, 2));

          previousPermutationOfPlayer.put(p.getUniqueId(), currentRot);
          currentPermutationOfPlayer.put(p.getUniqueId(), closestFace);

          startTimerTillPlayerCanChangeFacesAgain(p.getUniqueId(), plugin);
          cancel();
          return;
        }

//        Location aLoc = a.getLocation();

        pLoc.setYaw(pLoc.getYaw() + degreesToRotatePerTick);

        //can update this dynamically to compensate for mouse movement

        //make it a natural movement

        p.sendMessage("rot " + counter + ": " + pLoc.getYaw());
        p.sendMessage("deg per Tick: " + degreesToRotatePerTick);

        p.teleport(pLoc);

        if (counter == ticksToRotateOver - 1) {
          Location rotatedLocation = getMinecraftWorldLocationOnOtherCube(currentRot, closestFace, p.getLocation());

          p.teleport(rotatedLocation);
        }

        counter ++;
      }
    }.runTaskTimer(plugin, 0, 1);
  }

  private float getYawForSeamlessSwitch(CubeRotation currentCube, CubeRotation cubeToTeleportTo) {
    Vector3d faceVectorOfFaceAboutToSwitchTo = currentCube.axisTransformation.apply(
        cubeToTeleportTo.topFaceCoordinateOnMainWorld);
    return CubeRotation.getYawFromAxisDirectionFacing(faceVectorOfFaceAboutToSwitchTo.div(radius));
  }

  private Location getMinecraftWorldLocationOnOtherCube(CubeRotation currentCube, CubeRotation cubeToTeleportTo, Location playerLoc) {
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

    CubeRotation currentRot = currentPermutationOfPlayer.getOrDefault(player.getUniqueId(), cubeRotations[0]);

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLocation);
    CubeRotation closestFace = findClosestCubeRotationToCoordinate(cubeWorldCoordinateOfPlayer);

    return (closestFace != currentRot);
  }

  public boolean teleportToClosestFace(Player player, Plugin plugin) {
    UUID uuid = player.getUniqueId();

    Location loc = player.getLocation();
    previousLoc = loc;

    Location eyeLocation = loc.add(0,1.62,0);

    CubeRotation currentRot = currentPermutationOfPlayer.getOrDefault(uuid, cubeRotations[0]);

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLocation);
    CubeRotation closestFace = findClosestCubeRotationToCoordinate(cubeWorldCoordinateOfPlayer);

    if (closestFace == currentRot) return false;

    //player will be teleported now, so disable until done teleporting
    playerIsReadyToTeleport.put(uuid, false);

    Location actualWorldLocationToTeleportTo = getMinecraftWorldLocationOnOtherCube(currentRot, closestFace, loc);

    float desiredYawForSeamlessSwitch = getYawForSeamlessSwitch(currentRot, closestFace);

    rotTimer(player, plugin, desiredYawForSeamlessSwitch, actualWorldLocationToTeleportTo, currentRot, closestFace);

    return true;

  }

  private CubeRotation findClosestCubeRotationToCoordinate(Vector3d coordinate) {
    int closestFaceIndex = 0;
    double closestDistance = coordinate.distance(faceCenters[0]);

    for (int i = 1; i < faceCenters.length; i++) {
      double distance = coordinate.distance(faceCenters[i]);
      if (distance < closestDistance) {
        closestDistance = distance;
        closestFaceIndex = i;
      }
    }
    return cubeRotations[closestFaceIndex];
  }

 /* private CubePermutation[] worldPermutations = new CubePermutation[6];
  private int radius;

  public CubeWorld(Location face1PosXEdgeCenter, Location pasteCenter, int faceRadius) {
    radius = faceRadius;
    createCubeWorldFromEdge(face1PosXEdgeCenter, pasteCenter);

    Location permutation1Location = new Location(pasteCenter.getWorld(), pasteCenter.getBlockX(), pasteCenter.getBlockY(), pasteCenter.getBlockZ() + 3 * faceRadius);
    createFirstRotationWorld(face1PosXEdgeCenter, permutation1Location);
  }

  public int GetCurrentFaceOfPlayer(Player player) {
    for (int i = 0; i < worldPermutations.length; i++) {
      if (worldPermutations[i].PlayerIsOnFace(player)) {
        return i;
      }
    }

    return -1;
  }

  public Location getFacePosXEdgeCoord(int face) {
    return worldPermutations[face].getPosXEdgeCoord();
  }

  public Location getFaceNegXEdgeCoord(int face) {
    return worldPermutations[face].getNegXEdgeCoord();
  }

  public int getRadius() {
    return radius;
  }

  private void createFirstRotationWorld(Location edgeCenter, Location pasteCenter) {
    int lowestGroundYValue = findLowestPointOnXSlice(edgeCenter);

    Location uprightFaceCenter = new Location(edgeCenter.getWorld(), edgeCenter.getBlockX() + radius,
        lowestGroundYValue, edgeCenter.getZ());

    Location cube2Center = new Location(edgeCenter.getWorld(), edgeCenter.getBlockX() - radius,
        lowestGroundYValue, edgeCenter.getZ());



    Location pastedFace1Center = new Location(pasteCenter.getWorld(),

        pasteCenter.getBlockX(),
        pasteCenter.getBlockY() + radius,
        pasteCenter.getZ());

    Location face2Center = new Location(pasteCenter.getWorld(),

        pasteCenter.getBlockX() - radius,
        pasteCenter.getBlockY(),
        pasteCenter.getZ());

    copyAndPasteRegion(cube2Center, face2Center, -90);
    copyAndPasteRegion(uprightFaceCenter, pastedFace1Center, 0);



    worldPermutations[1] = new CubePermutation(pasteCenter, radius, 1);

  }

  private void createCubeWorldFromEdge(Location edgeCenter, Location pasteCenter) {
    int lowestGroundYValue = findLowestPointOnXSlice(edgeCenter);

    Location cube1Center = new Location(edgeCenter.getWorld(), edgeCenter.getBlockX() - radius,
        lowestGroundYValue, edgeCenter.getZ());

    Location cube2Center = new Location(edgeCenter.getWorld(), edgeCenter.getBlockX() + radius,
        lowestGroundYValue, edgeCenter.getZ());


    Location face1Center = new Location(pasteCenter.getWorld(),

        pasteCenter.getBlockX(),
        pasteCenter.getBlockY() + radius,
        pasteCenter.getZ());

    Location face2Center = new Location(pasteCenter.getWorld(),

        pasteCenter.getBlockX() + radius,
        pasteCenter.getBlockY(),
        pasteCenter.getZ());

    copyAndPasteRegion(cube1Center, face1Center, 0);
    copyAndPasteRegion(cube2Center, face2Center, 90);


    worldPermutations[0] = new CubePermutation(pasteCenter, radius, 0);
  }

  private int findLowestPointOnXSlice(Location playerPositionOnSlice) {
    int x = playerPositionOnSlice.getBlockX();
    int zCenter = playerPositionOnSlice.getBlockZ();

    int lowestPoint = playerPositionOnSlice.getBlockY() - 1; //subtract height of player viewpoint

    for (int z = zCenter - radius; z <= zCenter + radius; z++) {
      for (int y = lowestPoint; y > 40; y--) {
        Block block = new Location(playerPositionOnSlice.getWorld(), x, y, z).getBlock();
        if (!block.getType().isAir()) {
          lowestPoint = y;
          break;
        }
      }
    }
    return lowestPoint;
  }

  private void copyAndPasteRegion(Location center, Location destination,
      int rotationDeg) {

    World world = center.getWorld();

    int centerX = center.getBlockX();
    int centerY = center.getBlockY();
    int centerZ = center.getBlockZ();

    int destX = destination.getBlockX();
    int destY = destination.getBlockY();
    int destZ = destination.getBlockZ();

    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        for (int z = -radius; z <= radius; z++) {
          Block originalBlock = new Location(world, centerX + x, centerY + y,
              centerZ + z).getBlock();

          int pastedX;
          int pasteY;
          if (rotationDeg == 90) {
            pastedX = destX + y;
            pasteY = destY - x;
          } else if (rotationDeg == -90) {
            pastedX = destX - y;
            pasteY = destY + x;
          } else {
            pastedX = destX + x;
            pasteY = destY + y;
          }
          Block pastedBlock = new Location(world, pastedX, pasteY, destZ + z).getBlock();

          BlockData data = originalBlock.getBlockData();
          if (data instanceof org.bukkit.material.Tree) {
            org.bukkit.material.Tree treeData = (org.bukkit.material.Tree) data;
            BlockFace face = treeData.getDirection();

            //do stuff...
            // Use the face variable to determine the direction the block is facing
          }
          pastedBlock.setBlockData(data);
        }
      }
    }
  }*/
}
