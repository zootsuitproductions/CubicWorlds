package me.zootsuitproductions.cubicworlds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Vector3d;


public class CubicWorlds extends JavaPlugin implements Listener {

  //TODO: calculate the average height of each edge, and put the center of the faces not actually in the center
  //(the + x radius can be more than the - x, etc. faces dont have to be squares.

  //TODO: also tinker with the timing of the WE pasting commands.
  // find a quadratic scaling time so i dont have to adjust it for each size.
  //also why is it doing every operation 5 times?

  //!!!!todo:!!!!! only change face if there are no blocks above your head (walking over the edge) and blocks at feet at least 1 block drop

  public static String creatingWorldStateFileName = "creatingNewWorldState.txt";

  private static Vector[] faceCenters = new Vector[] {
      new Vector(500,60,500),
      new Vector(1500,60,1500),
      new Vector(2500,60,2500),
      new Vector(3500,60,3500),
      new Vector(4500,60,4500),
      new Vector(5500,60,5500),
  };


  //config stuff:
  //-------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------
  private File configFile;
  private FileConfiguration config;

  private void setupConfig() {
    configFile = new File(getDataFolder(), "config.yml");

    if (!configFile.exists()) {
      saveResource("config.yml", false);
    }

    config = YamlConfiguration.loadConfiguration(configFile);
  }

  private void saveCubeWorldRadius() {
    config.set("cubeWorldRadius", cubeWorldRadius);

    try {
      config.save(configFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void loadCubeWorldRadius() {
    cubeWorldRadius = config.getInt("cubeWorldRadius");
    System.out.println("RADIUS: +" + cubeWorldRadius);
  }
  //-------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------

  private WECubeWorldCreator cubeWorld;
  private CubeWorld cube;

  Location cubeCenter;

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    // Log the player's join event
    cube.setCurrentPermutationOfPlayer(event.getPlayer());
  }

  public static void deleteFolder(File folder) {
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteFolder(file);
          System.out.println("Deleted the folder");
        } else {
          file.delete();
        }
      }
    }
    folder.delete();
  }

  public static String readFileToString(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    byte[] bytes = Files.readAllBytes(path);
    return new String(bytes);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (cmd.getName().equalsIgnoreCase("createCubeWorld")) {

      if (sender.hasPermission("createCubeWorld.use")) {
        try {
          cubeWorldRadius = Integer.parseInt(args[0]);
          cubeWorld = new WECubeWorldCreator(cubeWorldRadius,cubeWorldRadius,cubeWorldRadius);
          saveCubeWorldRadius();
        } catch (Exception e) {
          sender.sendMessage("You must specify the radius of the cube world: /createcubeworld [radius]");
          return true;
        }

        //after testing, change the world
        String worldName = "cube_world";
        if (args.length >= 2) {
          worldName = args[1];
        }

        deleteFolder(new File(worldName));

        createVoidWorld(worldName);
        createAndWriteFile("world_to_change_to.txt", worldName);

        readyToAddFaces = true;

        sender.sendMessage("Go to 6 locations you want to use as the cube faces and do /addface");

      }
    } else if (cmd.getName().equalsIgnoreCase("pasteWorld")) {
      cubeWorld.pasteWorldAtLocation(((Player) sender).getLocation(), this);
      cubeCenter = ((Player) sender).getLocation();

      cube = new CubeWorld(cubeCenter,cubeWorldRadius, CubeWorld.spacing);

    } else if (cmd.getName().equalsIgnoreCase("goBackToNormalWorld")) {
      Player p = (Player) sender;

      createAndWriteFile("world_to_change_to.txt","world");
      Bukkit.shutdown();

    } else if (cmd.getName().equalsIgnoreCase("addFace")) {
      Player p = (Player) sender;
      Location ploc = p.getLocation();

      if (!readyToAddFaces) {
        p.sendMessage("Use /createCubeWorld [r] first");
        return true;
      }

      cubeWorld.addFace(((Player) sender).getLocation());
      sender.sendMessage("Added the current location");

    } else if (cmd.getName().equalsIgnoreCase("rot")) {
      Player p = (Player) sender;

      if (cube.playerIsReadyToTeleport.getOrDefault(p.getUniqueId(), true)) {
        if (cube.shouldPlayerBeTeleportedToNewFace(p)) { // code is duplicated jhere...
          if (timeOfSwitchEdgeStart > 0) {
//            long timeDifferenceBetweenMoves = System.currentTimeMillis() - timeOfSwitchEdgeStart;
//            if (timeDifferenceBetweenMoves == 0) {
//              return true;
//            }

//            Location displacement = event.getTo().clone().subtract(locOfSwitchStart);

            double distanceTraveled = 1; // Calculate the distance traveled using the length() method
            double timeInSeconds =
                1000 / 1000.0; // Convert milliseconds to seconds

            double speed =
                distanceTraveled / timeInSeconds; // Calculate the speed in blocks per second

            Vector velocity = new Vector(1 / timeInSeconds,
                1 / timeInSeconds, 1 / timeInSeconds);
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
//        locOfSwitchStart = event.getTo();


      }

//      Player p = (Player) sender;
//      GameMode currentMode = p.getGameMode();
//      p.setGameMode(GameMode.SPECTATOR);
//      Location loc = p.getLocation();
//
//      rotTimer(p, currentMode);
    }
    return true;
  }

  private static boolean shouldCreateNewCubeWorld() {
    try {
      String content = new String(Files.readAllBytes(Paths.get(creatingWorldStateFileName)));
      return true;
    } catch (IOException e) {

      System.out.println("i cant read ium a dumm");
      return false;
    }
  }

  public void deleteFile(String filePath) {
    File file = new File(filePath);

    if (file.exists()) {
      boolean deleted = file.delete();

      if (deleted) {
        System.out.println("File deleted successfully.");
      } else {
        System.out.println("Failed to delete the file.");
      }
    } else {
      System.out.println("File does not exist.");
    }
  }

  private void setupCubeWorld() {

    cubeWorld = new WECubeWorldCreator(cubeWorldRadius,cubeWorldRadius,cubeWorldRadius);

    cubeCenter = new Location(getServer().getWorlds().get(0),500,60,500);

    cube = new CubeWorld(cubeCenter,cubeWorldRadius, CubeWorld.spacing);

    if (shouldCreateNewCubeWorld()) {
      cubeWorld.pasteWorldAtLocation(cubeCenter, this);
      System.out.println("Creating new cube world");
      deleteFile(creatingWorldStateFileName);
    }
  }

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    setupConfig();
    loadCubeWorldRadius();

    //check if we are not in the normal world, outside of the cube world creation phase. if so, then  run the rest of this setup code
    if (!Bukkit.getWorlds().get(0).getName().equalsIgnoreCase("world")) {
      setupCubeWorld();
    }
  }
  private boolean readyToAddFaces = false;
  private int cubeWorldRadius;

  private static void createVoidWorld(String name) {
    //check if this will delete, for general usability. not essential though.
    WorldCreator creator = new WorldCreator(name);
    creator.type(WorldType.FLAT);

    creator.generatorSettings("{\"layers\": [{\"block\": \"air\", \"height\": 1}, {\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}");
    creator.generateStructures(false);

    creator.createWorld();
  }


  public static void createAndWriteFile(String filename, String content) {
    try {
      // Create the file if it doesn't exist
      Path filePath = Paths.get(filename);
      if (!Files.exists(filePath)) {
        Files.createFile(filePath);
        System.out.println("File created: " + filePath.toAbsolutePath());
      }

      // Write content to the file
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
        writer.write(content);
        System.out.println("Content written to the file successfully.");
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @EventHandler
  public void onBlockFromTo(BlockFromToEvent event) {
    if(event.getBlock().getBiome() == Biome.THE_VOID) {
      event.setCancelled(true);
    }
  }

  public void rotTimer(Player p, GameMode playerMode) {
    Location pLoc = p.getLocation();

    //create an invisible armor stand and have player spectate it
    ArmorStand a = (ArmorStand) p.getWorld().spawnEntity(pLoc, EntityType.ARMOR_STAND);
    a.setVisible(false);
    a.setGravity(false);
    p.setSpectatorTarget(a);

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


  private long timeOfSwitchEdgeStart = -1;
  private Location locOfSwitchStart = null;

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    if (cube == null) {
      return;
    }
    Location bLoc = event.getBlock().getLocation();

    WorldPermutation perm = cube.getClosestPermutation(bLoc); //make this return a pair, the perm,


    Vector3d cubeWorldCoord = perm.getWorldCoordinate(bLoc);

    cube.setBlockOnAllPerms(Material.AIR.createBlockData(), cubeWorldCoord);

  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    if (cube == null) {
      return;
    }

    Location bLoc = event.getBlock().getLocation();

    WorldPermutation perm = cube.getClosestPermutation(bLoc);
    Vector3d cubeWorldCoord = perm.getWorldCoordinate(bLoc);

    cube.setBlockOnAllPerms(event.getBlockPlaced().getBlockData(), cubeWorldCoord);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
//    cube = null;
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
