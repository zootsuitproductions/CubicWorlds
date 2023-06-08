package me.zootsuitproductions.cubicworlds;

import org.bukkit.Bukkit;

public class WorldEditCommandExecutor {

  private String worldName;

  WorldEditCommandExecutor(String worldName) {
    this.worldName = worldName;
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/world " + worldName);
  }

  public void setPos1(int x, int y, int z) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos1 " + x + "," + y + "," + z);
  }

  public void setPos2(int x, int y, int z) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos2 " + x + "," + y + "," + z);
  }

  //check the last pos to see if the set has been done

  public void setAir() {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/set air");
  }

}
