package me.zootsuitproductions.cubicworlds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CreateCubeWorldCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
      String[] args) {
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

    return false;
  }
}
