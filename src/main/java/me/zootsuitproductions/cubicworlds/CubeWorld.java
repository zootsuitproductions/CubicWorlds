package me.zootsuitproductions.cubicworlds;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class CubeWorld {



  Map<Player, CubePermutation> currentPermutationOfPlayer = new HashMap<>();

  public Coordinate fromWorldLocGetCoordinateRelativeToTheFirstCubePermutationInTheListFromLocation(Location loc, CubePermutation perm) {
    return new Coordinate(
        loc.getX() - perm.center.getBlockX(),
        loc.getY() - perm.center.getBlockY(),
        loc.getZ() - perm.center.getBlockZ()
    );
  }

  //angle needed to rotate center.



  Map<Location, CubePermutation> faceUpPermutations = new HashMap<Location, CubePermutation>();

  //map of player to which cube permutation they are on
  // convert from coordinate in one perm to another. do this by
  //whichever is closer



  public CubeWorld(Location center, Location pastedCenter, int radius) {
    Location upVector = new Location(center.getWorld(), 0,1,0);
    CubePermutation face1Up = new CubePermutation(center,pastedCenter,radius, upVector);
    faceUpPermutations.put(upVector, face1Up);
    
    CubePermutation face2Up = new CubePermutation(face1Up, CubePermutation.translateLocation(pastedCenter, 25,0,0), upVector);
    //face 0 pos x goes to face 1,
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
