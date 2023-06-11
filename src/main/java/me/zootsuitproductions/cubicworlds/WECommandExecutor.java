package me.zootsuitproductions.cubicworlds;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WECommandExecutor {

  private String worldName;

  WECommandExecutor(String worldName) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/world " + worldName);
  }

  public void setSelectionConvex() {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/sel convex");
  }

  public void setPos1(int x, int y, int z) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos1 " + x + "," + y + "," + z);
  }

  public void setPos2(int x, int y, int z) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos2 " + x + "," + y + "," + z);
  }

  //check the last pos to see if the set has been done

  public void copy() {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/copy");
  }

  public void pasteAt(Location location) {
    setPos1(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/paste");
  }

  public void saveSchematic(String name) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"/schem save " + name + " -f");
  }

  public void loadSchematic(String name) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"/schem load " + name);
  }

  public void rotate(int degX, int degY, int degZ) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"/rotate " + degY + " " + degX + " " + degZ);
  }



}
