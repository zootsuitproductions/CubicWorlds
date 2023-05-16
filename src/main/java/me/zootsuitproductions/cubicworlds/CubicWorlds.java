package me.zootsuitproductions.cubicworlds;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.io.File;
import java.io.IOException;
import java.util.*;

import com.sk89q.worldedit.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class CubicWorlds extends JavaPlugin implements Listener {

  Map<UUID, Long> playerLastMoveTime = new HashMap<UUID, Long>();
  Map<UUID, Integer> playerCurrentFace = new HashMap<UUID, Integer>();

  private FileConfiguration dataConfig;
  private File dataFile;

  private CubeWorld cube;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    WorldCreator worldCreator = new WorldCreator("air_world");
    worldCreator.generator(new ChunkGen());
    World world = worldCreator.createWorld();

    // Create or load data file
    dataFile = new File(getDataFolder(), "data.yml");
    if (!dataFile.exists()) {
      saveResource("data.yml", false);
    }
    dataConfig = YamlConfiguration.loadConfiguration(dataFile);

    // Load variable from file
//        cube = dataConfig.getInt("myVariable", 0);
  }


  @Override
  public void onDisable() {
    // Save variable to file
    dataConfig.set("cube", cube);
    try {
      dataConfig.save(dataFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

//    @EventHandler
//    public void onServerSave(ServerSaveEvent event) {
//        // Save variable to file when the server is being saved
//        saveCubeData();
//    }

  @EventHandler
  public void onServerLoad(ServerLoadEvent event) {
    // Load variable from file when the server is being loaded
//         myVariable = dataConfig.getInt("myVariable", 0);
  }

  private void saveCubeData() {
    dataConfig.set("cube", cube);
    try {
      dataConfig.save(dataFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // 0 face:
  // - pos x goes to 1, rotate 90
  // - neg x  goes to
  //
  // Pos x edge


    /*


    NOTES:
    - instead of copying to a different location, make the block cone cube inverted pyramid face shape where ur standing when u run a command.
    - run the same command again standing  at a different location to set the next face. the top faces should never be copied, they should stay where they are
    - this means i cant just do the full cube rotation to set the other sides.
    - i also need to make the copying happen over time so it doesn't crash


    Todo:
    - first, create new command to set the place under you a face.
    - find out how to past over time

    - make sure the chunks stay loaded

    -algorithm:

     */

//    private Location[] centerLocations

  int blockX;
  int blockY;
  int blockZ;

  VoidDirection currentVoidSectionClearing = VoidDirection.LEFT;
  boolean leftVoidDoneClearing = false;
  boolean rightVoidDoneClearing = false;

  boolean topCubeDoneClearing = false;
  boolean bottomCubeDoneClearing = false;

  private void clearRegion(Location minCorner, Location maxCorner, int blocksPerTick) {
    Bukkit.getScheduler().runTaskTimer(this, () -> {

    }, 0L, 1L);
  }

  public static int clampValueToRange(int value, int min, int max) {
    if (value > max) {
      return max;
    } else if (value < min) {
      return min;
    }
    return value;
  }

  //WRITE A CODE TO OPTIMIZE THE blocks per tick taken. how to check if server is falling behind?
  //SEE IF I CAN DO MULTIPLE WORLDS. copy a region to an empty void world.
  //also all the entities and block datas


  //USE WORLD EDIT API, its supposed to be more efficient

  private void clearBlocksAroundCubeWorld(Location center, int blocksPerTick, int cubeRadius,
      int clearUntilRadius) {
    World world = center.getWorld();

    int maxHeight = center.getWorld().getMaxHeight();
    int minHeight = center.getWorld().getMinHeight();

    int centerX = center.getBlockX();
    int centerY = center.getBlockY();
    int centerZ = center.getBlockZ();

    blockX = -clearUntilRadius;
    blockY = minHeight;
    blockZ = -clearUntilRadius;

    ISetBlocksOverTimeOperation leftVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX - clearUntilRadius,
            minHeight,
            centerZ - clearUntilRadius),
        new Location(
            world,
            centerX - cubeRadius,
            maxHeight,
            centerZ + clearUntilRadius),
        blocksPerTick, this, null);

    ISetBlocksOverTimeOperation rightVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX + cubeRadius,
            minHeight,
            centerZ - clearUntilRadius),
        new Location(
            world,
            centerX + clearUntilRadius,
            maxHeight,
            centerZ + clearUntilRadius),
        blocksPerTick,
     this, leftVoid);

    ISetBlocksOverTimeOperation frontVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX - cubeRadius,
            minHeight,
            centerZ + cubeRadius),
        new Location(
            world,
            centerX + cubeRadius,
            maxHeight,
            centerZ + clearUntilRadius),
        blocksPerTick, this, rightVoid);

    ISetBlocksOverTimeOperation backVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX - cubeRadius,
            minHeight,
            centerZ - clearUntilRadius),
        new Location(
            world,
            centerX + cubeRadius,
            maxHeight,
            centerZ - cubeRadius),
        blocksPerTick, this, frontVoid);

    ISetBlocksOverTimeOperation bottomVoid = new SetBlocksOverTimeOperation(
        new Location(
            world,
            centerX - clearUntilRadius,
            minHeight,
            centerZ - clearUntilRadius),
        new Location(
            world,
            centerX + clearUntilRadius,
            clampValueToRange(centerY - 2 * cubeRadius, minHeight, maxHeight),
            centerZ + clearUntilRadius),
        blocksPerTick,
        this, backVoid);

    //test it
    bottomVoid.apply();
  }

  List<Location> faceLocations = new ArrayList<Location>();
  private boolean creatingCubeWorld = false;
  private int cubeWorldRadius;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (cmd.getName().equalsIgnoreCase("createCubeWorld")) {

      if (sender.hasPermission("createCubeWorld.use")) {
        try {
          cubeWorldRadius = Integer.parseInt(args[0]);
        } catch (Exception e) {
          sender.sendMessage("You must specify the radius of the cube world");
          return true;
        }
        creatingCubeWorld = true;
        faceLocations.clear();

        sender.sendMessage("Go to 6 locations you want to use as the cube faces and do /addface");

      }
    } else if (cmd.getName().equalsIgnoreCase("changeEdge")) {
      Player p = (Player) sender;

    } else if (cmd.getName().equalsIgnoreCase("addFace")) {
      Player p = (Player) sender;

      faceLocations.add(p.getLocation());

      if (faceLocations.size() >= 6) {
        p.sendMessage("creating");
        clearBlocksAroundCubeWorld(p.getLocation(), 200, cubeWorldRadius, cubeWorldRadius + 20);
        new CubeWorld(faceLocations,cubeWorldRadius,this);
//        new CopyAndRotateCubeFaceOperation(p.getLocation(), p.getLocation().clone().add(0,50,0),Integer.parseInt(args[0]),AxisTransformation.FRONT,Integer.parseInt(args[1]),this,null).apply();
      }

//      int radius = Integer.parseInt(args[0]);
//      int bpt = Integer.parseInt(args[1]);



//      clearBlocksAroundCubeWorld(p.getLocation(), bpt, radius, radius + 3 * 16);



      //save the locations of each of the faces.
      //and i cant just make 1 cube world and then rotate it. well



      //just set make everything outside of the radius air. and maybe force load the chunks on the edges.
      //radius should be by chunk

    } else if (cmd.getName().equalsIgnoreCase("rot")) {
      Player p = (Player) sender;
      GameMode currentMode = p.getGameMode();
      p.setGameMode(GameMode.SPECTATOR);
      Location loc = p.getLocation();

//            armorStand.setHeadPose(new EulerAngle(Math.toRadians(0), 0, 0));
      rotTimer(p, currentMode);


    }
    return true;
  }

  public void rotTimer(Player p, GameMode playerMode) {
    Location pLoc = p.getLocation();

    //create an invisible armor stand and have player spectate it
    ArmorStand a = (ArmorStand) p.getWorld().spawnEntity(pLoc, EntityType.ARMOR_STAND);
    a.setVisible(false);
    a.setGravity(false);
    p.setSpectatorTarget(a);

//        CubeRotation.getLocalYawAxisFacing(pLoc.getYaw())
    //find closest yaw axis

    new BukkitRunnable() {

      int counter = 0;

      @Override
      public void run() {
        counter++;

        pLoc.setYaw(pLoc.getYaw() + 1f);
        a.teleport(pLoc);
        if (counter == 20) {
          cancel();
          p.setGameMode(playerMode);
          a.remove();
        }

      }
    }.runTaskTimer(this, 0, 1);
  }

  //do a wand thing
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    // Check if the player clicked on an item
    if (event.getAction() == Action.RIGHT_CLICK_AIR
        || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      ItemStack item = event.getItem();
      if (event.getPlayer().isOp()) {
        if (item != null && item.getType() == Material.DIAMOND_SWORD) {

        }
      }
    }
  }
//=
  //check every half second instead of every tick
  // get the face center ur closer to, if it changes, find the rotation necessary from the current face center to the next face,
  // and then tp to your coordinates translated to that permutation and rotate by that angle


  private long timeOfSwitchEdgeStart = -1;
  private Location locOfSwitchStart = null;

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (cube == null) {
      return;
    }

    Player p = event.getPlayer();

    if (cube.playerIsReadyToTeleport.getOrDefault(p.getUniqueId(), true)) {
      if (cube.shouldPlayerBeTeleportedToNewFace(p)) { // code is duplicated jhere...
        if (timeOfSwitchEdgeStart > 0) {
          long timeDifferenceBetweenMoves = System.currentTimeMillis() - timeOfSwitchEdgeStart;
          if (timeDifferenceBetweenMoves == 0) {
            return;
          }

          Location displacement = event.getTo().clone().subtract(locOfSwitchStart);

          double distanceTraveled = displacement.length(); // Calculate the distance traveled using the length() method
          double timeInSeconds =
              timeDifferenceBetweenMoves / 1000.0; // Convert milliseconds to seconds

          double speed =
              distanceTraveled / timeInSeconds; // Calculate the speed in blocks per second

          Vector velocity = new Vector(displacement.getX() / timeInSeconds,
              displacement.getY() / timeInSeconds, displacement.getY() / timeInSeconds);
          //find each axis speed

          if (cube.teleportToClosestFace(p, velocity, this)) {
            timeOfSwitchEdgeStart = -1;
            locOfSwitchStart = null;
          }

        }
      }

      //set these back to null values when done successfully teleport!!!!INGAA
      ///8
      //
      timeOfSwitchEdgeStart = System.currentTimeMillis();
      locOfSwitchStart = event.getTo();


    }
  }

  private static int calculateLevitationLevel(double horizontalSpeed) {
    if (horizontalSpeed == 0) {
      return 1;
    }
    return (int) Math.ceil(horizontalSpeed / 0.9);
  }

  private static int calculateLevitationTimeTicks(double distanceNeedToTravel, int level) {
    double seconds = distanceNeedToTravel / ((double) level * 0.9);
    System.out.println("seconds: " + seconds);
    System.out.println(level * 0.9);
    return (int) Math.ceil(seconds * 20);
  }


}
