package me.zootsuitproductions.cubicworlds;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerTimePosition {
  private final Player player;
  private final long time;
  private final Location location;

  public PlayerTimePosition(Player player, Location location, long time) {
    this.player = player;
    this.time = time;
    this.location = location;
  }

  public Player getPlayer() {
    return player;
  }

  public long getTime() {
    return time;
  }

  public Location getLocation() {
    return location;
  }
}

