package me.zootsuitproductions.cubicworlds;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;

public class DecayCreator {
  private final Location epicenter;
  private final int radius;
  private final int durationOfDecay;
  private final Map<Material, Material> decayReplacementBlocks = new HashMap<>();


  public DecayCreator(Location epicenter, int radius, int durationOfDecay) {
    this.epicenter = epicenter;
    this.radius = radius;
    this.durationOfDecay = durationOfDecay;

    decayReplacementBlocks.put(Material.GRASS, Material.SCULK_SENSOR);
    decayReplacementBlocks.put(Material.GRASS_BLOCK, Material.SCULK);
    decayReplacementBlocks.put(Material.DIRT, Material.SCULK);
    decayReplacementBlocks.put(Material.OAK_LOG, Material.WARPED_STEM);
    decayReplacementBlocks.put(Material.OAK_LEAVES, Material.WARPED_WART_BLOCK);
    decayReplacementBlocks.put(Material.ACACIA_LOG, Material.WARPED_STEM);
    decayReplacementBlocks.put(Material.ACACIA_LEAVES, Material.WARPED_WART_BLOCK);
    decayReplacementBlocks.put(Material.SPRUCE_LOG, Material.WARPED_STEM);
    decayReplacementBlocks.put(Material.SPRUCE_LEAVES, Material.WARPED_WART_BLOCK);
    decayReplacementBlocks.put(Material.BIRCH_LOG, Material.WARPED_STEM);
    decayReplacementBlocks.put(Material.BIRCH_LEAVES, Material.WARPED_WART_BLOCK);
    decayReplacementBlocks.put(Material.JUNGLE_LOG, Material.WARPED_STEM);
    decayReplacementBlocks.put(Material.JUNGLE_LEAVES, Material.WARPED_WART_BLOCK);
    decayReplacementBlocks.put(Material.DARK_OAK_LOG, Material.WARPED_STEM);
    decayReplacementBlocks.put(Material.DARK_OAK_LEAVES, Material.WARPED_WART_BLOCK);

  }

  public void startDecayAction() {

  }
}
