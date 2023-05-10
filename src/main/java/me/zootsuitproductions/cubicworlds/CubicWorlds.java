package me.zootsuitproductions.cubicworlds;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Vine;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;


public class CubicWorlds extends JavaPlugin implements Listener {

    Map<UUID, Long> playerLastMoveTime = new HashMap<UUID, Long>();
    Map<UUID, Integer> playerCurrentFace = new HashMap<UUID, Integer>();

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
    private CubeWorld cube;
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


        if (cmd.getName().equalsIgnoreCase("copypaste")) {

            if (sender.hasPermission("copypaste.use")) {
                Player p = (Player) sender;
                int radius = Integer.parseInt(args[0]);
                Location dest = new Location(p.getWorld(), p.getLocation().getBlockX(), 150, p.getLocation().getBlockZ());

                cube = new CubeWorld(p.getLocation(),dest,radius);
//                new CubePermutation(p.getLocation(),dest,radius,);
                getServer().getPluginManager().registerEvents(this, this);
            }
        } else if (cmd.getName().equalsIgnoreCase("changeEdge")) {
            Player p = (Player) sender;
            cube.teleportToClosestFace(p, this);
        } else if (cmd.getName().equalsIgnoreCase("back")) {
            Player p = (Player) sender;
            cube.undoFaceSwitch(p);
        }
        else if (cmd.getName().equalsIgnoreCase("rot")) {
            Player p = (Player) sender;
            System.out.println("suck");
        }
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the player clicked on an item
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (event.getPlayer().isOp()) {
                if (item != null && item.getType() == Material.DIAMOND_SWORD) {





                }
            }
        }
    }

    //check every half second instead of every tick
    // get the face center ur closer to, if it changes, find the rotation necessary from the current face center to the next face,
    // and then tp to your coordinates translated to that permutation and rotate by that angle
//
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (cube == null) return;

        cube.teleportToClosestFace(event.getPlayer(), this);


    }

    private static int calculateLevitationLevel(double horizontalSpeed) {
        if (horizontalSpeed == 0)
            return 1;
        return (int) Math.ceil(horizontalSpeed / 0.9);
    }

    private static int calculateLevitationTimeTicks(double distanceNeedToTravel, int level) {
        double seconds = distanceNeedToTravel /((double) level * 0.9);
        System.out.println("seconds: " + seconds);
        System.out.println(level * 0.9);
        return (int) Math.ceil(seconds * 20);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
//        getServer().createWorld(new WorldCreator("myWorld").generator(new ChunkGen()));

        WorldCreator worldCreator = new WorldCreator("air_world");
        worldCreator.generator(new ChunkGen());
        World world = worldCreator.createWorld();
    }



    @Override
    public void onDisable() {
    }

}
