package me.zootsuitproductions.cubicworlds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.joml.Vector3d;

public class CubeWorld {
  Map<UUID, CubeRotation> currentPermutationOfPlayer = new HashMap<>();
  AxisTransformation[] transformations = new AxisTransformation[] {
      AxisTransformation.TOP,
      AxisTransformation.FRONT,
      AxisTransformation.BOTTOM,
      AxisTransformation.BACK,
      AxisTransformation.LEFT,
      AxisTransformation.RIGHT
  };

  Vector3d[] faceCenters = new Vector3d[6];
  CubeRotation[] cubeRotations = new CubeRotation[6];

  public CubeWorld(Location center, Location pasteCenter, int radius) {

    int spaceBetweenCubeRotationsInWorld = radius*5;

    faceCenters[0] = new Vector3d(0, radius,0);
//    faceCenters[1] = new Vector3d(radius, 0,0);
//    faceCenters[2] = new Vector3d(0, -radius,0);
//    faceCenters[3] = new Vector3d(-radius, 0,0);
//    faceCenters[4] = new Vector3d(0, 0, -radius);
//    faceCenters[5] = new Vector3d(0, 0, radius);

    cubeRotations[0] = new CubeRotation(center, pasteCenter, radius, AxisTransformation.TOP, faceCenters[0]);

    System.out.println(faceCenters[0]);

    for (int i = 1; i < transformations.length; i++) {
      faceCenters[i] = transformations[i].apply(faceCenters[0]);
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

  public void teleportToClosestFace(Player player) {
    Location loc = player.getLocation();
    CubeRotation currentRot = currentPermutationOfPlayer.getOrDefault(player.getUniqueId(), cubeRotations[0]);
    Vector3d cubeWorldCoordinateOfPlayer = currentRot.getCubeWorldCoordinate(loc);

    //get eye position
    cubeWorldCoordinateOfPlayer = cubeWorldCoordinateOfPlayer.add(0, 0.62, 0);
    CubeRotation closestFace = findClosestCubeRotationToCoordinate(cubeWorldCoordinateOfPlayer);




    Vector3d localCoordOnClosestFace = closestFace.getLocalCoordinateFromWorldCoordinate(cubeWorldCoordinateOfPlayer);

    //subtract one so the player's new viewport will align
    localCoordOnClosestFace = localCoordOnClosestFace.sub(0, 1.62, 0);


    Location actualWorldLocationToTeleportTo = closestFace.getLocationFromRelativeCoordinate(localCoordOnClosestFace);

    float newYaw = closestFace.convertYawFromOtherCubeRotation(loc.getYaw(), currentRot);
    actualWorldLocationToTeleportTo.setYaw(newYaw);

    //this wont work if you dig straight down to switch1!!!!
    //!!!

    //rotate the sun too!!!
    actualWorldLocationToTeleportTo.setPitch(loc.getPitch() - 90f);

    player.teleport(actualWorldLocationToTeleportTo);


      //CHANGE THIS:::
      currentPermutationOfPlayer.put(player.getUniqueId(), closestFace);


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
