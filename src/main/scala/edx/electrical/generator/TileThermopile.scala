package edx.electrical.generator

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.init.Blocks
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.prefab.tile.TileElectric
import resonant.lib.prefab.tile.spatial.SpatialBlock
import resonant.lib.transform.vector.Vector3

class TileThermopile extends TileElectric(Material.rock)
{

  this.ioMap = 728.asInstanceOf[Short]

  private final val MAX_USE_TICKS: Int = 120 * 20
  /**
   * The amount of ticks the thermopile will use the temperature differences before turning all
   * adjacent sides to thermal equilibrium.
   */
  private var usingTicks: Int = 0

  override def update
  {
    super.update
    if (!this.worldObj.isRemote)
    {
      var heatSources: Int = 0
      var coolingSources: Int = 0
      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        val checkPos: Vector3 = toVector3.add(dir)
        val block: Block = checkPos.getBlock(worldObj)
        if (block eq Blocks.water)
        {
          coolingSources += 1
        }
        else if (block eq Blocks.snow)
        {
          coolingSources += 2
        }
        else if (block eq Blocks.ice)
        {
          coolingSources += 2
        }
        else if (block eq Blocks.fire)
        {
          heatSources += 1
        }
        else if (block eq Blocks.lava)
        {
          heatSources += 2
        }
      }
      val multiplier: Int = (3 - Math.abs(heatSources - coolingSources))
      if (multiplier > 0 && coolingSources > 0 && heatSources > 0)
      {
        //        electricNode.addEnergy(ForgeDirection.UNKNOWN, 15 * multiplier, true)
        if (({
          usingTicks += 1;
          usingTicks
        }) >= MAX_USE_TICKS)
        {
          for (dir <- ForgeDirection.VALID_DIRECTIONS)
          {
            val checkPos: Vector3 = toVector3.add(dir)
            val blockID: Block = checkPos.getBlock(worldObj)
            if (blockID eq Blocks.water)
            {
              checkPos.setBlockToAir(worldObj)
            }
            else if (blockID eq Blocks.ice)
            {
              checkPos.setBlock(worldObj, Blocks.water)
            }
            else if (blockID eq Blocks.fire)
            {
              checkPos.setBlockToAir(worldObj)
            }
            else if (blockID eq Blocks.lava)
            {
              checkPos.setBlock(worldObj, Blocks.stone)
            }
          }
          usingTicks = 0
        }
      }
    }
  }

  @SideOnly(Side.CLIENT) override def registerIcons(iconReg: IIconRegister)
  {
    SpatialBlock.icon.put("thermopile_top", iconReg.registerIcon(Reference.prefix + "thermopile_top"))
    super.registerIcons(iconReg)
  }

  @SideOnly(Side.CLIENT) override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == 1)
    {
      return SpatialBlock.icon.get("thermopile_top")
    }
    return super.getIcon(side, meta)
  }
}