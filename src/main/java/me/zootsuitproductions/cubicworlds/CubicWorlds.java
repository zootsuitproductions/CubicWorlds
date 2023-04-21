package me.zootsuitproductions.cubicworlds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Vine;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.Location;


public class CubicWorlds extends JavaPlugin implements Listener {

    Map<UUID, Long> playerLastMoveTime = new HashMap<UUID, Long>();
    Map<UUID, Integer> playerCurrentFace = new HashMap<UUID, Integer>();

    // 0 face:
    // - pos x goes to 1, rotate 90
    // - neg x  goes to
    //
    // Pos x edge

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
            }
        } else if (cmd.getName().equalsIgnoreCase("changeEdge")) {
            Player p = (Player) sender;
            cube.teleportToClosestFace(p);
        }
        return true;
    }

    //check every half second instead of every tick
    // get the face center ur closer to, if it changes, find the rotation necessary from the current face center to the next face,
    // and then tp to your coordinates translated to that permutation and rotate by that angle
/*
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (cube == null) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        int currentFace = playerCurrentFace.getOrDefault(playerId, 0);

        Location CurrentFacePosXEdge = cube.getFacePosXEdgeCoord(currentFace);
        Location NextFaceNegXEdge = cube.getFacePosXEdgeCoord(currentFace + 1); //need to change later


        if (player.getLocation().getBlockX() > CurrentFacePosXEdge.getBlockX()) {
            // Get the player's last move time
            long lastMoveTime = playerLastMoveTime.getOrDefault(playerId, (long) -1);
            playerLastMoveTime.put(playerId, System.currentTimeMillis());

            if (lastMoveTime == -1) {
                return;
            }

            long timeElapsed = System.currentTimeMillis() - lastMoveTime;

            playerLastMoveTime.remove(playerId);

            double xVelocity = (event.getTo().getX() - event.getFrom().getX()) / (timeElapsed / 1000.0);

            Location loc = player.getLocation();
            Vector direction = loc.getDirection();

            float xAngle = (float) Math.toDegrees(Math.atan2(-direction.getY(), Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ())));
            float rotated = xAngle - 90;

            int level = calculateLevitationLevel(xVelocity);
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, calculateLevitationTimeTicks(2, level), level));


            loc.setPitch(rotated);
            double x = NextFaceNegXEdge.getBlockX() - (loc.getY() - (CurrentFacePosXEdge.getBlockY())) + (2-1.62);

            loc.setY(CurrentFacePosXEdge.getBlockY() - (CurrentFacePosXEdge.getBlockX() - loc.getX()) - 0.62);
            loc.setX(x);

            double zValRelativeToCurrentFace = loc.getZ() - CurrentFacePosXEdge.getBlockZ();
            loc.setZ(NextFaceNegXEdge.getBlockZ() + zValRelativeToCurrentFace);


            player.teleport(loc);
//            playerCurrentFace.put(playerId, currentFace + 1);

        }
    }*/

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

    }



    @Override
    public void onDisable() {
    }

}
