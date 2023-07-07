package me.zootsuitproductions.cubicworlds;

import static me.zootsuitproductions.cubicworlds.CubicWorlds.creatingWorldStateFileName;
import static me.zootsuitproductions.cubicworlds.FileUtils.deleteFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
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

    ChunkUtils.forceLoadChunksAroundLocation(cubeCenterLocations[0],radius);

    for (int i = 1; i < faceTransformations.length; i++) {
      cubeFaceCenters[i] = faceTransformations[i].unapply(cubeFaceCenters[0]);

      cubeCenterLocations[i] = WorldPermutation.translateLocation(cubeCenterLocations[0],
          cubeCenterPositionsInTermsOfSpacing[i].getBlockX() * spacing,
          cubeCenterPositionsInTermsOfSpacing[i].getBlockY() * spacing,
          cubeCenterPositionsInTermsOfSpacing[i].getBlockZ() * spacing);

      ChunkUtils.forceLoadChunksAroundLocation(cubeCenterLocations[i],radius);

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
    }, 20L, 2L);
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

  public void updateBlockDataAt(Location location, BlockData newData) {
    WorldPermutation perm = getClosestPermutation(location);
    Vector3d cubeWorldCoord = perm.getWorldCoordinate(location);

    setBlockOnAllPermsExcept(newData, cubeWorldCoord, perm);
  }

  public void setBlockOnAllPermsExcept(BlockData blockData, Vector3d cubeWorldCoordinate, WorldPermutation dontSetOnThisOne) {
    int skipIndex = dontSetOnThisOne.index;
    System.out.println(skipIndex);

    //i know, it has impossible shit. need to save vectors of unrotated instead of the data
    for (int i = 0; i < worldPermutations.length; i++) {
      if (i == skipIndex) {
        continue;
      }

      WorldPermutation perm = worldPermutations[i];

      Location loc = perm.getLocationOnThisPermFromCubeWorldCoordinate(cubeWorldCoordinate, world);

      loc.getBlock().setBlockData(perm.rotateBlockData(blockData, dontSetOnThisOne));

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

  public void translatePlayerMovementToNewPerm(Player p, Plugin plugin, WorldPermutation currentRot, WorldPermutation newPerm, boolean isPermOnOppositeSide) {
    currentPermutationOfPlayer.put(p.getUniqueId(), null);

    final Location lastPlayerPosition = p.getLocation();
    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
      @Override
      public void run() {
        Vector velocity = p.getLocation().subtract(lastPlayerPosition).toVector(); //because its over 1 tick, the displacement is the velocity (blocks per tick)

        Location rotatedLocation = currentRot.getMinecraftWorldLocationOnOtherCube(newPerm, p.getLocation(), isPermOnOppositeSide, p);
        Vector rotatedVelocity = currentRot.rotateVectorToOtherCube(velocity, newPerm);

        p.teleport(rotatedLocation);
        p.setVelocity(rotatedVelocity);
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,40,5, true, false));
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
          @Override
          public void run() {
            currentPermutationOfPlayer.put(p.getUniqueId(), newPerm);
          }
        }, 20L);
      }
    }, 1L);
  }

  //return null if shouldnt be teleported
  public WorldPermutation getFacePlayerShouldTeleportTo(Player player) {
    WorldPermutation currentRot = currentPermutationOfPlayer.getOrDefault(player.getUniqueId(), worldPermutations[0]);

    //get direction
    if (currentRot == null) return null;

    Location loc = player.getLocation();
    Location eyeLoc = loc.add(0,1.62,0);

    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(eyeLoc);
    WorldPermutation closestFace = findClosestFaceToCubeWorldCoordinate(cubeWorldCoordinateOfPlayer);

    if ((closestFace != currentRot)) {
      return closestFace;
    }
    return null;
  }

  public void teleportPlayerToNewPerm(Player player, WorldPermutation newPerm) {
    UUID uuid = player.getUniqueId();

    WorldPermutation currentPerm = currentPermutationOfPlayer.getOrDefault(uuid, worldPermutations[0]);
    Vector3d directionOfClosestFace = currentPerm.axisTransformation.apply(newPerm.topFaceCoordinateOnMainWorld).normalize();

    if (directionOfClosestFace.y < 0) { //going to other side of the world
      translatePlayerMovementToNewPerm(player, plugin, currentPerm, newPerm, true);
      return;
    }

    float yawForSeamlessSwitch = currentPerm.getYawForSeamlessSwitch(directionOfClosestFace, player.getLocation().getYaw());

    if (Math.abs(calculateDegreeDifference(yawForSeamlessSwitch, player.getLocation().getYaw())) <= 40) {
      translatePlayerMovementToNewPerm(player, plugin, currentPerm, newPerm, false);
    } else {
      Vector oppositeDirection = new Vector(-1 * directionOfClosestFace.x, 1, -1 * directionOfClosestFace.z);
      player.setVelocity(oppositeDirection);
    }
  }
  private WorldPermutation findClosestFaceToCubeWorldCoordinate(Vector3d mainCubeWorldCoordinate) {

    //find the distance and use that for performance increases (schedule individual player checks based on how close they are.
    //also remember to reset it on respawn and player join, cancel on player dea
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
