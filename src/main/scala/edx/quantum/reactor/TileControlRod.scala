package edx.quantum.reactor

import net.minecraft.block.material.Material
import resonant.lib.prefab.tile.spatial.SpatialBlock
import resonant.lib.transform.region.Cuboid

/**
 * Control rod block
 */
class TileControlRod extends SpatialBlock(Material.iron)
{
  bounds = new Cuboid(0.3f, 0f, 0.3f, 0.7f, 1f, 0.7f)
  isOpaqueCube = true
}