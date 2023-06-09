package me.zootsuitproductions.cubicworlds;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PersistentStorage {

  private File configFile;
  private FileConfiguration config;
  private final CubicWorlds plugin;

  PersistentStorage(CubicWorlds plugin) {
    this.plugin = plugin;
    configFile = new File(plugin.getDataFolder(), "config.yml");

    if (!configFile.exists()) {
      plugin.saveResource("config.yml", false);
    }

    config = YamlConfiguration.loadConfiguration(configFile);
  }

  public void saveCubeWorldRadius() {
    config.set("cubeWorldRadius", plugin.cubeWorldRadius);

    try {
      config.save(configFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadCubeWorldRadius() {
    plugin.cubeWorldRadius = config.getInt("cubeWorldRadius");
  }
  //-------------------------------------------------------------------------------------------------
  //-------------------------------------------------------------------------------------------------

}
