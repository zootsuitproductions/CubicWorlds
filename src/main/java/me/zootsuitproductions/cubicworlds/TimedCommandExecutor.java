package me.zootsuitproductions.cubicworlds;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TimedCommandExecutor {
  List<String> commands;
  List<Integer> delays; //in ticks
  private final Plugin plugin;

  int current = 0;

  TimedCommandExecutor(Plugin plugin, List<String> commands, List<Integer> delays) {
    if (commands.size() != delays.size()) {
      throw new IllegalArgumentException("Commands and delays lists must be the same length");
    }

    this.plugin = plugin;
    this.commands = commands;
    this.delays = delays;
  }

  public void execute() {
    runNextCommandAfterDelay();
  }

  private void runNextCommandAfterDelay() {
    if (current >= commands.size()) {
      return;
    }

    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
      @Override
      public void run() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),commands.get(current));
        current++;
        runNextCommandAfterDelay();
      }
    }, delays.get(current));
  }

}
