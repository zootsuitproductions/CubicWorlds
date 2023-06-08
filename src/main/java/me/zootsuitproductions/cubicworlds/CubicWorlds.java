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
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class CubicWorlds extends JavaPlugin implements Listener {

  Map<UUID, Long> playerLastMoveTime = new HashMap<UUID, Long>();
  Map<UUID, Integer> playerCurrentFace = new HashMap<UUID, Integer>();

  private CubeWorld cube;
  private static String creatingWorldStateFileName = "creatingNewWorldState.txt";

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

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (cmd.getName().equalsIgnoreCase("createCubeWorld")) {

      if (sender.hasPermission("createCubeWorld.use")) {
        try {
          cubeWorldRadius = Integer.parseInt(args[0]);
          saveCubeWorldRadius();
        } catch (Exception e) {
          sender.sendMessage("You must specify the radius of the cube world: /createcubeworld [radius]");
          return true;
        }

        String worldName = "cube_world";
        if (args.length >= 2) {
          worldName = args[1];
        }
        createVoidWorld(worldName);
        createAndWriteFile("world_to_change_to.txt", worldName);

        readyToAddFaces = true;
        faceLocations.clear();

        sender.sendMessage("Go to 6 locations you want to use as the cube faces and do /addface");

      }
    } else if (cmd.getName().equalsIgnoreCase("goBackToNormalWorld")) {

      createAndWriteFile("world_to_change_to.txt","world");
      Bukkit.shutdown();

    } else if (cmd.getName().equalsIgnoreCase("addFace")) {
      Player p = (Player) sender;
      Location ploc = p.getLocation();

      if (!readyToAddFaces) {
        p.sendMessage("Use /createCubeWorld [r] first");
        return true;
      }

      faceLocations.add(ploc);

      int minHeight = ploc.getWorld().getMinHeight();
      int maxHeight = ploc.getWorld().getMaxHeight();

      int x1 = ploc.getBlockX() - cubeWorldRadius;
      int y1 = clampValueToRange(ploc.getBlockY() - cubeWorldRadius, minHeight,maxHeight);
      int z1 = ploc.getBlockZ() - cubeWorldRadius;

      int x2 = ploc.getBlockX() + cubeWorldRadius;
      int y2 = clampValueToRange(ploc.getBlockY() + cubeWorldRadius, minHeight,maxHeight);
      int z2 = ploc.getBlockZ() + cubeWorldRadius;

      p.performCommand("/pos1 " + x1 + "," + y1 + "," + z1);
      p.performCommand("/pos2 " + x2 + "," + y2 + "," + z2);
      p.performCommand("/copy -b -e");
      p.performCommand("/schem save face" + currentFace + " -f");


      if (currentFace >= 5) {
        createAndWriteFile(creatingWorldStateFileName, "true");
        Bukkit.shutdown();
      }

      currentFace ++;
    } else if (cmd.getName().equalsIgnoreCase("rot")) {
      Player p = (Player) sender;
      GameMode currentMode = p.getGameMode();
      p.setGameMode(GameMode.SPECTATOR);
      Location loc = p.getLocation();

      rotTimer(p, currentMode);
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

  private void copyFaceCentersFromSchematics() {
    String worldName = Bukkit.getWorlds().get(0).getName();
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/world " + worldName);

    pasteFaceCentersOverTime(0, faceCenters.length);
  }

  private void pasteFaceCentersOverTime(int currentFaceIndex, int lastFaceIndex) {
    if (currentFaceIndex >= lastFaceIndex) {
      World world = Bukkit.getWorlds().get(0);

      ArrayList<Location> locs = new ArrayList<Location>();
      locs.add(new Location(world, 500, 60, 500));
      locs.add(new Location(world, 1500, 60, 1500));
      locs.add(new Location(world, 2500, 60, 2500));
      locs.add(new Location(world, 3500, 60, 3500));
      locs.add(new Location(world, 4500, 60, 4500));
      locs.add(new Location(world, 5500, 60, 5500));

      cube = new CubeWorld(locs,cubeWorldRadius,this);
      return;
    }

    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/schem load face" + currentFaceIndex);
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos1 " +
        faceCenters[currentFaceIndex].getBlockX() + "," +
        faceCenters[currentFaceIndex].getBlockY() + "," +
        faceCenters[currentFaceIndex].getBlockZ());
    Bukkit.getScheduler().runTaskLater(this, new Runnable() {
      public void run() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/paste");
        pasteFaceCentersOverTime(currentFaceIndex + 1, lastFaceIndex);
      }
    }, 100L); //5 seconds
  }

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    setupConfig();
    loadCubeWorldRadius();


//
//    World world = Bukkit.getWorlds().get(0);

//    ArrayList<Location> locs = new ArrayList<Location>();
//    locs.add(new Location(world, 500, 60, 500));
//    locs.add(new Location(world, 1500, 60, 1500));
//    locs.add(new Location(world, 2500, 60, 2500));
//    locs.add(new Location(world, 3500, 60, 3500));
//    locs.add(new Location(world, 4500, 60, 4500));
//    locs.add(new Location(world, 5500, 60, 5500));
//
//    cubeWorldRadius = 40;
    //need to save the face locations after quitting

    //AND SAVE THE RADIUS


    //some falling gravel from the top face might fuck things. apply the reverse pyamid of air
    //first (copy the top face upright from another location (not 500,500), then delete it

    //do this after copying shit
//    cube = new CubeWorld(locs,cubeWorldRadius,this);


    //remove this later, just for tesitng
//    copyFaceCentersFromSchematics();

    if (shouldCreateNewCubeWorld()) {
      System.out.println("Creating new cube world");
      deleteFile(creatingWorldStateFileName);

      copyFaceCentersFromSchematics();


      //this will get called before the delay:
//      /*cube = */new CubeWorld(faceLocations,cubeWorldRadius,this);

    }
  }
/*

    Todo:
    - first, create new command to set the place under you a face.
    - find out how to past over time

    - make sure the chunks stay loaded

    -algorithm:

     */

  public static int clampValueToRange(int value, int min, int max) {
    if (value > max) {
      return max;
    } else if (value < min) {
      return min;
    }
    return value;
  }

  List<Location> faceLocations = new ArrayList<Location>();
  private boolean readyToAddFaces = false;
  private int cubeWorldRadius;
  private int currentFace = 0;

  private static void createVoidWorld(String name) {
    //check if this will delete, for general usability. not essential though.
    WorldCreator creator = new WorldCreator(name);
    creator.type(WorldType.FLAT);

    creator.generatorSettings("{\"layers\": [{\"block\": \"air\", \"height\": 1}, {\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}");
    creator.generateStructures(false);

    World world = creator.createWorld();
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
