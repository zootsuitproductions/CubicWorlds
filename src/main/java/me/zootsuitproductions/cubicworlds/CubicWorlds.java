package me.zootsuitproductions.cubicworlds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Vector3d;


public class CubicWorlds extends JavaPlugin implements Listener {
  public static String creatingWorldStateFileName = "creatingNewWorldState.txt";
  private boolean readyToAddFaces = false;
  public int cubeWorldRadius;

  private Config config;

  private WECubeWorldCreator cubeWorld;
  private CubeWorld cube;

  Location cubeCenter;

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    cube.setCurrentPermutationOfPlayer(event.getPlayer());
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    System.out.println();
    cube.setCurrentPermutationOfPlayer(event.getPlayer());
    cube.playerIsReadyToTeleport.put(event.getPlayer().getUniqueId(), false);
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
          config.saveCubeWorldRadius();
          cubeWorld.saveFacesAroundLocation(((Player) sender).getLocation(),this);
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
      switchPlayerPermutationsIfNecessaryRepeatingTask();


//
//      if (cube.playerIsReadyToTeleport.getOrDefault(p.getUniqueId(), true)) {
//        if (cube.shouldPlayerBeTeleportedToNewFace(p)) { // code is duplicated jhere...
//          if (timeOfSwitchEdgeStart > 0) {
////            long timeDifferenceBetweenMoves = System.currentTimeMillis() - timeOfSwitchEdgeStart;
////            if (timeDifferenceBetweenMoves == 0) {
////              return true;
////            }
//
////            Location displacement = event.getTo().clone().subtract(locOfSwitchStart);
//
//            double distanceTraveled = 1; // Calculate the distance traveled using the length() method
//            double timeInSeconds =
//                1000 / 1000.0; // Convert milliseconds to seconds
//
//            double speed =
//                distanceTraveled / timeInSeconds; // Calculate the speed in blocks per second
//
//            Vector velocity = new Vector(1 / timeInSeconds,
//                1 / timeInSeconds, 1 / timeInSeconds);
//            //find each axis speed
//
//            if (cube.teleportToClosestFace(p, velocity, this)) {
//              timeOfSwitchEdgeStart = -1;
//              locOfSwitchStart = null;
//            }
//
//          }
//        }
//
//        //set these back to null values when done successfully teleport!!!!INGAA
//        ///8
//        //
//        timeOfSwitchEdgeStart = System.currentTimeMillis();
////        locOfSwitchStart = event.getTo();
//
//
//      }

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

  public static int mainCubeZPos = 500;
  public static int mainCubeXPos = 500;

  private void setupCubeWorld() {

    cubeWorld = new WECubeWorldCreator(cubeWorldRadius,cubeWorldRadius,cubeWorldRadius);

    cubeCenter = new Location(getServer().getWorlds().get(0),mainCubeXPos,60,mainCubeZPos);

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

    config = new Config(this);

    config.setupConfig();
    config.loadCubeWorldRadius();

    this.overworld = Bukkit.getWorlds().get(0);

    //check if we are not in the normal world, outside of the cube world creation phase. if so, then  run the rest of this setup code
    if (!overworld.getName().equalsIgnoreCase("world")) {
      System.out.println("setting up");
      setupCubeWorld();
    }

    Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        switchPlayerPermutationsIfNecessaryRepeatingTask();
      }
    }, 20L, 5L);
  }

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

    cube.setBlockOnAllPermsExcept(Material.AIR.createBlockData(), cubeWorldCoord, perm);

  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    if (cube == null) {
      return;
    }


    Location bLoc = event.getBlock().getLocation();

    WorldPermutation perm = cube.getClosestPermutation(bLoc);
    Vector3d cubeWorldCoord = perm.getWorldCoordinate(bLoc);

    cube.setBlockOnAllPermsExcept(event.getBlockPlaced().getBlockData(), cubeWorldCoord, perm);
  }

  private World overworld;

  private List<PlayerTimePosition> playerTimePositions = new ArrayList<>();


  private final Plugin pl = this;


  private void switchPlayerPermutationsIfNecessaryRepeatingTask() {
    if (cube == null) {
      return;
    }

    overworld.getPlayers().forEach(p ->
    {
      if (cube.shouldPlayerBeTeleportedToNewFace(p)) {
        playerTimePositions.add(new PlayerTimePosition(p, p.getLocation(), System.currentTimeMillis()));
      }
    });



    Bukkit.getScheduler().runTaskLater(this, new Runnable() {
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

          if (cube.teleportToClosestFace(p, velocity, pl)) {
          }
        }

        playerTimePositions.clear();
      }
    }, 1);
  }


//
//  @EventHandler
//  public void onPlayerMove(PlayerMoveEvent event) {
//    if (true) {
//      return;
//    }
//    if (cube == null) {
//      return;
//    }
//
//    Player p = event.getPlayer();
//    UUID id = p.getUniqueId();
//
//    if (cube.playerIsReadyToTeleport.getOrDefault(id, true)) {
//      if (cube.shouldPlayerBeTeleportedToNewFace(p)) {
//        if (cube.playerLastMoveTime.getOrDefault(id, -1L) > 0) {
//
//          long timeDifferenceBetweenMoves = System.currentTimeMillis() - timeOfSwitchEdgeStart;
//
//          if (timeDifferenceBetweenMoves == 0) {
//            p.sendMessage("false " + timeDifferenceBetweenMoves);
//            return;
//          }
//
//
//          p.sendMessage("time diff: " + timeDifferenceBetweenMoves);
//
//          Location displacement = event.getTo().clone().subtract(locOfSwitchStart);
//
//          double distanceTraveled = displacement.length(); // Calculate the distance traveled using the length() method
//          double timeInSeconds =
//              timeDifferenceBetweenMoves / 1000.0; // Convert milliseconds to seconds
//
//          double speed =
//              distanceTraveled / timeInSeconds; // Calculate the speed in blocks per second
//
//          Vector velocity = new Vector(displacement.getX() / timeInSeconds,
//              displacement.getY() / timeInSeconds, displacement.getY() / timeInSeconds);
//
//            if (cube.teleportToClosestFace(p, velocity, this)) {
//              int duration = 20; // Duration of the effect in ticks (20 ticks = 1 second)
//              int amplifier = 1; // The level of the effect
//
////              p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, amplifier));
//
//              //TODO: make the"line of scrimmage" in the middle of the cube face centers. the world faces
//              //shouldnt be square. imagine a rectanguar box for the world. on the right edge the distance is way closer to the right face
//              //but it shouldnt flip yet
//
//              //make an algorithm thats goes x and z layer by layer, find the y height on the top face of the cube that its supposed to match with,
//              //and the y height on the bottom face, and transforms all the blocks on that layer up or down to match up.
//              //will also need to multiply.
//
//              //todo: do linear interpolation of the cut out pieces on edges (when theres a hill), and instead of adding 6 faces
//              //manually use the adjacent ones
//
//
//              cube.playerLastMoveTime.put(id, -1L);
//              locOfSwitchStart = null;
//            }
////          }
//          //find each axis speed
//
//
//
//        }
//      }
//
//      //
//      cube.playerLastMoveTime.put(id, System.currentTimeMillis());
//
//      locOfSwitchStart = event.getTo();
//
//    }
//  }

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
