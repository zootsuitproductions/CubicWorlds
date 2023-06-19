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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

    WECubeWorldCreator worldCreator = new WECubeWorldCreator(radius,radius,radius);

    if (CubicWorlds.shouldCreateNewCubeWorld()) {
      worldCreator.pasteWorldAtLocation(mainCubeCenter, plugin);
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


          Location loc2 = p.getLocation();

          if (teleportToClosestFace(p, velocity, plugin)) {
          }
        }

        playerTimePositions.clear();
      }
    }, 1);
  }

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

  //put them in a grid not a line


  public WorldPermutation getClosestPermutation(Location location) {
    //this only works for thousand block seperated worlds

    int x = location.getBlockX();
    int z = location.getBlockZ();

    int worldIndex = z;


//
//    if (x == 1) {
//      switch (z) {
//        case 0:
//          worldIndex = 3;
//          break;
//        case 1:
//          worldIndex = 4;
//          break;
//        default:
//          worldIndex = 5;
//          break;
//      }
//    }


//
    worldIndex = Math.round((float) (x - mainCubeXPos) / (float) spacing);
//
//    int thousandsPlace = (x / 1000) % 10; // Get the thousands place digit
//
//    if (permNumber > 5 || permNumber < 0) {
//
//      //find the closest conventionally
//      System.out.println("too far away");
//      return worldPermutations[0];
//    }
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

  public void setCurrentPermutationOfPlayerByLocation(Player player, Location loc) {
    currentPermutationOfPlayer.put(player.getUniqueId(), getClosestPermutation(loc));
  }

  private final Vector[] cubeCenterPositions = new Vector[] {
      new Vector(0,0,0),
      new Vector(0,0,1),
      new Vector(0,0,2),
      new Vector(1,0,0),
      new Vector(1,0,1),
      new Vector(1,0,2),
  };


  public CubeWorld(Location pasteCenter, int radius, int spaceBetween, Plugin plugin) {
    this.radius = radius;
    this.mainCubeCenter = pasteCenter;
    int spaceBetweenCubeRotationsInWorld = spaceBetween;
    this.world = pasteCenter.getWorld();
    this.plugin = plugin;

    world.setSpawnLocation(WorldPermutation.translateLocation(pasteCenter, 0, radius,0));

    cubeFaceCenters[0] = new Vector3d(0, radius,0);
    worldPermutations[0] = new WorldPermutation(pasteCenter, radius, AxisTransformation.TOP, cubeFaceCenters[0], 0);

    ChunkUtils.forceLoadChunksAroundLocation(pasteCenter, radius);

    for (int i = 1; i < transformations.length; i++) {
      cubeFaceCenters[i] = transformations[i].unapply(cubeFaceCenters[0]);

      //uncomment if you want to keep all chunks loaded
//      Location center = WorldPermutation.translateLocation(pasteCenter, i * spaceBetweenCubeRotationsInWorld,0,0);
//      ChunkUtils.forceLoadChunksAroundLocation(center, radius);

      worldPermutations[i] = new WorldPermutation(
          worldPermutations[0],
          WorldPermutation.translateLocation(pasteCenter, i * spaceBetweenCubeRotationsInWorld,0,0),
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
//          p.setGameMode(currentMode);
//          a.remove();
          currentPermutationOfPlayer.put(p.getUniqueId(), closestFace);
//          p.setVelocity(new Vector(0,0,0));
//          startTimerTillPlayerCanChangeFacesAgain(p.getUniqueId(), plugin);
          cancel();
          return;
        }


//        pLoc.setYaw(pLoc.getYaw() + degreesToRotatePerTick);

        //can update this dynamically to compensate for mouse movement

        //make it a natural movement

//        p.sendMessage("rot " + counter + ": " + pLoc.getYaw());
//        p.sendMessage("deg per Tick: " + degreesToRotatePerTick);

        //rotate the velocity vector instead
//        p.teleport(pLoc);

        if (counter == ticksToRotateOver - 1) {
          Location rotatedLocation = getMinecraftWorldLocationOnOtherCube(currentRot, closestFace, p.getLocation());


          Vector yawVector = currentRot.getYawVector(p.getLocation());
          Vector newLookVector = currentRot.rotateVectorToOtherCube(yawVector,closestFace);

          //need better testing workflow
          p.sendMessage("og: " + yawVector);
          p.sendMessage("rotated: " + newLookVector);



          p.teleport(rotatedLocation);
          Vector rotatedVelocity = currentRot.rotateVectorToOtherCube(velocity, closestFace);


          //only add if block in front or inside player (to account for head height
          //check on the original if there is a block directly under or nah!!!!
//add this: new Vector(0,0.3,0)
          p.setVelocity(rotatedVelocity);


          //moss over the edge when creating:
          //set all grass to moss, then do a check for moss with only air above it

          //TODO: TAKE INTO ACCOUNT IF THEY ARE CROUCHED HEAD POSITION

          //also crawling


          //check if the block in the same direction as the face ur transitioning to is air


          //get the closest face top vector on this cube
//          Vector3d directionOfClosestFace = currentRot.axisTransformation.apply(closestFace.topFaceCoordinateOnMainWorld).normalize();
//          p.sendMessage("ur happy" + directionOfClosestFace);
//
//
//          Location behindLoc = p.getLocation().subtract(directionOfClosestFace.x,directionOfClosestFace.y,directionOfClosestFace.z);
//          p.sendMessage("behind loc" + behindLoc.toVector());
//          p.sendMessage("p loc" + p.getLocation().toVector());
//
////
//          if (p.getLocation().subtract(directionOfClosestFace.x,directionOfClosestFace.y,directionOfClosestFace.z).getBlock().getBlockData().getMaterial() != Material.AIR) {
//
//
//            p.setVelocity(rotatedVelocity.add(new Vector(0,0.3,0)));
//          }

          //if there is a block in front of them or inside of them, effectively do an auto jump
          //by applying velocity to y


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

    System.out.println("the yaw" + potentialYaw);
    if (Math.round(playerYaw / 90) * 90 != potentialYaw) {
      potentialYaw = potentialYaw - 180;
    }

    return potentialYaw;
  }


  //do this


  private Location getMinecraftWorldLocationOnOtherCube(WorldPermutation currentCube, WorldPermutation cubeToTeleportTo, Location playerLoc) {
    Location eyeLocation = playerLoc.add(0,1.62,0);
    Vector3d cubeWorldCoordinateOfPlayerEyes = currentCube.getCubeWorldCoordinate(eyeLocation);

    Vector3d localCoordOnClosestFace = cubeToTeleportTo.getLocalCoordinateFromWorldCoordinate(cubeWorldCoordinateOfPlayerEyes);
    localCoordOnClosestFace = localCoordOnClosestFace.sub(0, 1.62, 0);
    Location actualWorldLocationToTeleportTo = cubeToTeleportTo.getLocationFromRelativeCoordinate(localCoordOnClosestFace);


    float newYaw = cubeToTeleportTo.convertYawFromOtherCubeRotation(eyeLocation.getYaw(), currentCube);
    actualWorldLocationToTeleportTo.setYaw(newYaw);

    //if player looking towards axis do this
//    actualWorldLocationToTeleportTo.setPitch(eyeLocation.getPitch() - 90f);


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
