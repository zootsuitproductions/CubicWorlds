package me.zootsuitproductions.cubicworlds;

import static me.zootsuitproductions.cubicworlds.CubicWorlds.creatingWorldStateFileName;
import static me.zootsuitproductions.cubicworlds.FileUtils.deleteFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

public class CubeWorld {
  private final int radius;
  Map<UUID, WorldPermutation> currentPermutationOfPlayer = new HashMap<>();
  Vector3d[] cubeFaceCenters = new Vector3d[6];
  WorldPermutation[] worldPermutations = new WorldPermutation[6];

  public static int spacing = 100;
  public static int mainCubeZPos = 500;
  public static int mainCubeXPos = 500;

  private final World world;
  private final Plugin plugin;

  public static final AxisTransformation[] faceTransformations = new AxisTransformation[] {
      AxisTransformation.TOP,
      AxisTransformation.FRONT,
      AxisTransformation.BOTTOM,
      AxisTransformation.BACK,
      AxisTransformation.LEFT,
      AxisTransformation.RIGHT
  };

  private final Vector[] cubeCenterPositionsInTermsOfSpacing = new Vector[] {
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
    spacing = spaceBetween;
    this.world = pasteCenter.getWorld();
    this.plugin = plugin;

    world.setSpawnLocation(WorldPermutation.translateLocation(pasteCenter, 0, radius,0));

    cubeFaceCenters[0] = new Vector3d(0, radius,0);
    worldPermutations[0] = new WorldPermutation(pasteCenter, radius, AxisTransformation.TOP, cubeFaceCenters[0], 0);

    cubeCenterLocations[0] = pasteCenter;

    for (int i = 1; i < faceTransformations.length; i++) {
      cubeFaceCenters[i] = faceTransformations[i].unapply(cubeFaceCenters[0]);

      cubeCenterLocations[i] = WorldPermutation.translateLocation(cubeCenterLocations[0],
          cubeCenterPositionsInTermsOfSpacing[i].getBlockX() * spacing,
          cubeCenterPositionsInTermsOfSpacing[i].getBlockY() * spacing,
          cubeCenterPositionsInTermsOfSpacing[i].getBlockZ() * spacing);

      worldPermutations[i] = new WorldPermutation(
          cubeCenterLocations[i],
          radius,
          faceTransformations[i],
          cubeFaceCenters[i], i);
    }

    setupCubeWorld();
    setCurrentPlayerPerms();
    teleportPlayersIfNecessaryRepeatingTask();
  }

  private void setupCubeWorld() {
    WECubeWorldPaster worldPaster = new WECubeWorldPaster(radius, cubeCenterLocations);

    if (CubicWorlds.shouldCreateNewCubeWorld()) {
      System.out.println("Creating new cube world. This will take about 1.5 minutes");
      worldPaster.pasteWorldAtLocation(cubeCenterLocations[0], plugin);
      deleteFile(creatingWorldStateFileName);
    }

  }

  private void teleportPlayersIfNecessaryRepeatingTask() {
    Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
      @Override
      public void run() {
        teleportPlayersIfNecessary();
      }
    }, 20L, 1L);
  }

  public void teleportPlayersIfNecessary() {
    world.getPlayers().forEach(p ->
    {
      WorldPermutation permToTeleportTo = getFacePlayerShouldTeleportTo(p);
      if (permToTeleportTo != null) {
        teleportPlayerToNewPerm(p, permToTeleportTo);
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

      loc.getBlock().setBlockData(blockData);

    }
  }

  public WorldPermutation getClosestPermutation(Location location) {
    Location copy = location.clone();

    Location relativeLoc = copy.subtract(cubeCenterLocations[0]);

    int x = (int) Math.round(relativeLoc.getBlockX() / (double) spacing);
    int z = (int) Math.round(relativeLoc.getBlockZ() / (double) spacing);

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

  public float calculateDegreeDifference(float rotation1, float rotation2) {
    float difference = rotation1 - rotation2;

    if (difference > 180) {
      difference -= 360;
    } else if (difference < -180) {
      difference += 360;
    }

    return difference;
  }

  private float calculateYawToRotatePerTick(float currentYaw, float finalYaw, int ticksToRotateOver) {
    float yawPerTick;
    float difference = calculateDegreeDifference(finalYaw, currentYaw);

    yawPerTick = difference / (ticksToRotateOver + 1);
    return yawPerTick;
  }

  public void teleportPlayerToNewCubePermutationASAP(Player p, Plugin plugin, WorldPermutation currentPerm, WorldPermutation newPerm) {
    Location lastPlayerPosition = p.getLocation();
    new BukkitRunnable() {
      @Override
      public void run() {
        Location pLoc = p.getLocation();
        Location displacement = pLoc.subtract(lastPlayerPosition);

        int ticks = 1;
        Vector velocity = new Vector(displacement.getX() / ticks,displacement.getY() / ticks, displacement.getZ() / ticks);

        Location rotatedLocation = currentPerm.getMinecraftWorldLocationOnOtherCube(newPerm, p.getLocation(), true, p);
        Vector rotatedVelocity = currentPerm.rotateVectorToOtherCube(velocity, newPerm);
        p.teleport(rotatedLocation);
        p.setVelocity(rotatedVelocity);

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
          @Override
          public void run() {
            currentPermutationOfPlayer.put(p.getUniqueId(), newPerm);
          }
        }, 20L);
      }
    }.runTaskLater(plugin, 1);
  }

  public void rotTimer(Player p, Plugin plugin, WorldPermutation currentRot, WorldPermutation closestFace, float yawForSeamlessSwitch, Vector3d directionOfNewFace, boolean shouldBoost) {

    int ticksToRotateOver = 3;

    new BukkitRunnable() {
      int counter = 0;
      Location lastPlayerPosition = p.getLocation();
      float yawPerTick = calculateYawToRotatePerTick(p.getLocation().getYaw(), yawForSeamlessSwitch, ticksToRotateOver);

      Vector newVelo;
      @Override
      public void run() {
        yawPerTick = calculateYawToRotatePerTick(p.getLocation().getYaw(), yawForSeamlessSwitch, ticksToRotateOver - counter);

        if (counter > 0) {
          Location displacement = p.getLocation().subtract(lastPlayerPosition);
          lastPlayerPosition = p.getLocation();

          int ticks = 1;
          Vector velocity = new Vector(displacement.getX() / ticks,displacement.getY() / ticks, displacement.getZ() / ticks);
          //add velocity in the outward direction too, to move one block out
//


          if (counter == 1) {
            Vector velocityTowardNewFace = new Vector(0,0,0);
            //todo instead of doing the boost thing, check if the player's outward velocity is enough to avoid issues
            //if it isn't, bounce them back


//            if (shouldBoost) {
//              p.sendMessage("boosting");
//              velocityTowardNewFace = new Vector(directionOfNewFace.x * 0.2, 0, directionOfNewFace.z * 0.2);
//
//            }

            //this isnt just down
            newVelo = velocity.add(new Vector(0,0,0)).add(velocityTowardNewFace);
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
            Location rotatedLocation = currentRot.getMinecraftWorldLocationOnOtherCube(closestFace, p.getLocation(), false, p);
            Vector rotatedVelocity = currentRot.rotateVectorToOtherCube(velocity, closestFace);
            p.teleport(rotatedLocation);
            p.setVelocity(rotatedVelocity);

            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
              @Override
              public void run() {
                currentPermutationOfPlayer.put(p.getUniqueId(), closestFace);
              }
            }, 8L);
            cancel();
            return;
          }
        }
        counter ++;
      }
    }.runTaskTimer(plugin, 0, 1);
  }

  //return null if shouldnt be teleported
  public WorldPermutation getFacePlayerShouldTeleportTo(Player player) {
    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(player.getUniqueId(), worldPermutations[0]);

    if (currentRot == null) return null;

    Location loc = player.getLocation();
    Location eyeLoc = loc.add(0,1.62,0);

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLoc);
    WorldPermutation closestFace = findClosestFaceToCubeWorldCoordinate(cubeWorldCoordinateOfPlayer);

    if ((currentRot.isLocationOffOfFaceRadius(eyeLoc)) && (closestFace != currentRot)) {
      return closestFace;
    }
    return null;
  }

  public void teleportPlayerToNewPerm(Player player, WorldPermutation newPerm) {
    UUID uuid = player.getUniqueId();

    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(uuid, worldPermutations[0]);

    Vector3d directionOfClosestFace = currentRot.axisTransformation.apply(newPerm.topFaceCoordinateOnMainWorld).normalize();
    Vector3d clone = new Vector3d(directionOfClosestFace.x, directionOfClosestFace.y, directionOfClosestFace.z);

    if (directionOfClosestFace.y < 0) { //going to other side of the world
      teleportPlayerToNewCubePermutationASAP(player, plugin, currentRot, newPerm);
      currentPermutationOfPlayer.put(uuid, null);
      return;
    }

    float yawForSeamlessSwitch = currentRot.getYawForSeamlessSwitch(directionOfClosestFace, player.getLocation().getYaw());

    if (Math.abs(calculateDegreeDifference(yawForSeamlessSwitch, player.getLocation().getYaw())) <= 5) {

      Location behindLoc = player.getLocation().subtract(directionOfClosestFace.x,directionOfClosestFace.y,directionOfClosestFace.z);
      boolean shouldBoost = (behindLoc.getBlock().getBlockData().getMaterial() != Material.AIR);

      rotTimer(player,plugin, currentRot, newPerm, yawForSeamlessSwitch, directionOfClosestFace, shouldBoost);
      currentPermutationOfPlayer.put(uuid, null);
    } else {
      Vector oppositeDirection = new Vector(clone.x, -1, clone.z).multiply(-1);
      player.sendMessage("directiopn" + oppositeDirection);

      player.setVelocity(oppositeDirection);
      //calculate vector to send player back. it isnt just the opposite of direction of closest face
      //take into account the direction the player is looking
    }


  }
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
