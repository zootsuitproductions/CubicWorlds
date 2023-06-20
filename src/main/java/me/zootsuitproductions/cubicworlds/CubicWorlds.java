package me.zootsuitproductions.cubicworlds;

import static me.zootsuitproductions.cubicworlds.CubeWorld.mainCubeXPos;
import static me.zootsuitproductions.cubicworlds.CubeWorld.mainCubeZPos;
import static me.zootsuitproductions.cubicworlds.CubeWorld.spacing;
import static me.zootsuitproductions.cubicworlds.FileUtils.createAndWriteFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.joml.Vector3d;


public class CubicWorlds extends JavaPlugin implements Listener {
  public static String creatingWorldStateFileName = "creatingNewWorldState.txt";
  public int cubeWorldRadius;
  private PersistentStorage persistentStorage;
  private CubeWorld cube;
  Location cubeCenter;
  private World overworld;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    persistentStorage = new PersistentStorage(this);
    persistentStorage.loadCubeWorldRadius();

    this.overworld = Bukkit.getWorlds().get(0);

    //check if we are not in the normal world, outside of the cube world creation phase. if so, then  run the rest of this setup code
    if (!overworld.getName().equalsIgnoreCase("world")) {
      System.out.println("setting up");
      cubeCenter = new Location(getServer().getWorlds().get(0), mainCubeXPos,60, mainCubeZPos);
      cube = new CubeWorld(cubeCenter,cubeWorldRadius, spacing, this);
    }
  }

  //EVENTS : make seperate files
  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    if (cube == null) return;

    Location bLoc = event.getBlock().getLocation();
    WorldPermutation perm = cube.getClosestPermutation(bLoc);

    Vector3d cubeWorldCoord = perm.getWorldCoordinate(bLoc);

    cube.setBlockOnAllPermsExcept(Material.AIR.createBlockData(), cubeWorldCoord, perm);
  }


  //todo: also do waterbucket placements and gravel n shit
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    if (cube == null) return;

    Location bLoc = event.getBlock().getLocation();

    WorldPermutation perm = cube.getClosestPermutation(bLoc);
    Vector3d cubeWorldCoord = perm.getWorldCoordinate(bLoc);

    cube.setBlockOnAllPermsExcept(event.getBlockPlaced().getBlockData(), cubeWorldCoord, perm);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (cube == null) return;
    cube.setCurrentPermutationOfPlayer(event.getPlayer());
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    if (cube == null) return;
    Bukkit.getScheduler().runTaskLater(this, new Runnable() {
      @Override
      public void run() {
        cube.setCurrentPermutationOfPlayer(event.getPlayer());
      }
    }, 2L);
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (cube == null) return;
    cube.currentPermutationOfPlayer.put(event.getEntity().getUniqueId(), null);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("createCubeWorld")) {

      if (sender.hasPermission("createCubeWorld.use")) {
        try {
          cubeWorldRadius = Integer.parseInt(args[0]);
          persistentStorage.saveCubeWorldRadius();

          new WECubeWorldSaver(cubeWorldRadius).saveFacesAroundLocation(((Player) sender).getLocation(),this);
        } catch (Exception e) {
          sender.sendMessage("You must specify the radius of the cube world: /createcubeworld [radius]");
          return true;
        }

        String worldName = "cube_world";
        if (args.length >= 2) {
          worldName = args[1];
        }

        sender.sendMessage("Creating cube world, this will take ~20 seconds. When it's done the server will close. switch your world do cube_world and start it back up");

        FileUtils.deleteFolder(new File(worldName));

        createVoidWorld(worldName);
        createAndWriteFile("world_to_change_to.txt", worldName);
      }
    } else if (cmd.getName().equalsIgnoreCase("goBackToNormalWorld")) {

      createAndWriteFile("world_to_change_to.txt","world");
      Bukkit.shutdown();

    } else if (cmd.getName().equalsIgnoreCase("rot")) {
      cube.teleportPlayersIfNecessary();
    }
    return true;
  }

  private static void createVoidWorld(String name) {
    WorldCreator creator = new WorldCreator(name);
    creator.type(WorldType.FLAT);

    creator.generatorSettings("{\"layers\": [{\"block\": \"air\", \"height\": 1}, {\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}");
    creator.generateStructures(false);

    creator.createWorld();
  }

  public static boolean shouldCreateNewCubeWorld() {
    try {
      String content = new String(Files.readAllBytes(Paths.get(creatingWorldStateFileName)));
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
