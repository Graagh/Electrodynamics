package mffs.field

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import mffs.security.MFFSPermissions
import mffs.util.MFFSUtility
import mffs.{Content, ModularForceFieldSystem}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{Entity, EntityLiving}
import net.minecraft.init.Blocks
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.potion.{Potion, PotionEffect}
import net.minecraft.util.{IIcon, MovingObjectPosition}
import net.minecraft.world.IBlockAccess
import resonantengine.api.mffs.machine.{IForceField, IProjector}
import resonantengine.api.mffs.modules.IModule
import resonantengine.core.network.discriminator.{PacketTile, PacketType}
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.transform.region.Cuboid
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.wrapper.ByteBufWrapper._
import resonantengine.prefab.network.TPacketReceiver

import scala.collection.convert.wrapAll._

class TileForceField extends ResonantTile(Material.glass) with TPacketReceiver with IForceField
{
  private var camoStack: ItemStack = null
  private var projector: Vector3 = null

  /**
   * Constructor
   */
  blockHardness = -1
  blockResistance = Float.MaxValue
  creativeTab = null
  isOpaqueCube = false
  normalRender = false
  renderStaticBlock = true

  override def canSilkHarvest(player: EntityPlayer, metadata: Int): Boolean = false

  override def quantityDropped(meta: Int, fortune: Int): Int = 0

  /**
   * Rendering
   */
  override def getRenderBlockPass: Int = 1

  @SideOnly(Side.CLIENT)
  override def renderStatic(renderer: RenderBlocks, pos: Vector3, pass: Int): Boolean =
  {
    var renderType = 0
    var camoBlock: Block = null
    val tileEntity = access.getTileEntity(x, y, z)

    if (camoStack != null && camoStack.getItem().isInstanceOf[ItemBlock])
    {
      camoBlock = camoStack.getItem().asInstanceOf[ItemBlock].field_150939_a

      if (camoBlock != null)
      {
        renderType = camoBlock.getRenderType()
      }
    }

    if (renderType >= 0)
    {
      try
      {
        if (camoBlock != null)
        {
          renderer.setRenderBoundsFromBlock(camoBlock)
        }

        renderType match
        {
          case 4 =>
            renderer.renderBlockLiquid(block, x, y, z)
          case 31 =>
            renderer.renderBlockLog(block, x, y, z)
          case 1 =>
            renderer.renderCrossedSquares(block, x, y, z)
          case 20 =>
            renderer.renderBlockVine(block, x, y, z)
          case 39 =>
            renderer.renderBlockQuartz(block, x, y, z)
          case 5 =>
            renderer.renderBlockRedstoneWire(block, x, y, z)
          case 13 =>
            renderer.renderBlockCactus(block, x, y, z)
          case 23 =>
            renderer.renderBlockLilyPad(block, x, y, z)
          case 6 =>
            renderer.renderBlockCrops(block, x, y, z)
          case 7 =>
            renderer.renderBlockDoor(block, x, y, z)
          case 12 =>
            renderer.renderBlockLever(block, x, y, z)
          case 29 =>
            renderer.renderBlockTripWireSource(block, x, y, z)
          case 30 =>
            renderer.renderBlockTripWire(block, x, y, z)
          case 14 =>
            renderer.renderBlockBed(block, x, y, z)
          case 16 =>
            renderer.renderPistonBase(block, x, y, z, false)
          case 17 =>
            renderer.renderPistonExtension(block, x, y, z, true)
          case _ =>
            super.renderStatic(renderer, pos, pass)
        }
      }
      catch
        {
          case e: Exception =>
          {
            if (camoStack != null && camoBlock != null)
            {
              renderer.renderBlockAsItem(camoBlock, camoStack.getItemDamage, 1)
            }
          }
        }
      return true
    }

    return false
  }

  /**
   * Block Logic
   */
  @SideOnly(Side.CLIENT)
  override def shouldSideBeRendered(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean =
  {
    if (camoStack != null)
    {
      try
      {
        val block = camoStack.getItem.asInstanceOf[ItemBlock].field_150939_a
        return block.shouldSideBeRendered(access, x, y, z, side)
      }
      catch
        {
          case e: Exception =>
          {
            e.printStackTrace
          }
        }
      return true
    }

    return if (access.getBlock(x, y, z) == block) false else super.shouldSideBeRendered(access, x, y, z, side)
  }

  override def click(player: EntityPlayer)
  {
    val projector = getProjector

    if (projector != null)
      projector.getModuleStacks(projector.getModuleSlots(): _*) forall (stack => stack.getItem.asInstanceOf[IModule].onCollideWithForceField(world, x, y, z, player, stack))
  }

  override def getCollisionBoxes(intersect: Cuboid, entity: Entity): Iterable[Cuboid] =
  {
    //TODO: Check if the entity filter actually works...
    val projector = getProjector()

    if (projector != null && entity.isInstanceOf[EntityPlayer])
    {
      val biometricIdentifier = projector.getBiometricIdentifier
      val entityPlayer = entity.asInstanceOf[EntityPlayer]

      if (entityPlayer.isSneaking)
      {
        if (entityPlayer.capabilities.isCreativeMode)
        {
          return null
        }
        else if (biometricIdentifier != null)
        {
          if (biometricIdentifier.hasPermission(entityPlayer.getGameProfile, MFFSPermissions.forceFieldWarp))
          {
            return null
          }
        }
      }
    }

    return super.getCollisionBoxes(intersect, entity)
  }

  /**
   * @return Gets the projector block controlling this force field. Removes the force field if no
   *         projector can be found.
   */
  def getProjector: TileElectromagneticProjector =
  {
    if (this.getProjectorSafe != null)
    {
      return getProjectorSafe
    }

    if (!this.worldObj.isRemote)
    {
      world.setBlock(xCoord, yCoord, zCoord, Blocks.air)
    }

    return null
  }

  def getProjectorSafe: TileElectromagneticProjector =
  {
    if (projector != null)
    {
      val projTile = projector.getTileEntity(world)

      if (projTile.isInstanceOf[TileElectromagneticProjector])
      {
        val projector = projTile.asInstanceOf[IProjector]
        if (world.isRemote || (projector.getCalculatedField != null && projector.getCalculatedField.contains(position)))
        {
          return projTile.asInstanceOf[TileElectromagneticProjector]
        }
      }
    }
    return null
  }

  override def collide(entity: Entity)
  {
    val projector = getProjector()

    if (projector != null)
    {
      if (!projector.getModuleStacks(projector.getModuleSlots(): _*).forall(stack => stack.getItem().asInstanceOf[IModule].onCollideWithForceField(world, x, y, z, entity, stack)))
        return

      val biometricIdentifier = projector.getBiometricIdentifier

      if (center.distance(new Vector3(entity)) < 0.5)
      {
        if (!world.isRemote && entity.isInstanceOf[EntityLiving])
        {
          val entityLiving = entity.asInstanceOf[EntityLiving]

          entityLiving.addPotionEffect(new PotionEffect(Potion.confusion.id, 4 * 20, 3))
          entityLiving.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20, 1))

          if (entity.isInstanceOf[EntityPlayer])
          {
            val player = entity.asInstanceOf[EntityPlayer]

            if (player.isSneaking)
            {
              if (player.capabilities.isCreativeMode)
              {
                return
              }
              else if (biometricIdentifier != null)
              {
                if (biometricIdentifier.hasPermission(player.getGameProfile, MFFSPermissions.forceFieldWarp))
                {
                  return
                }
              }
            }
          }

          entity.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, 100)
        }
      }
    }

  }

  @SideOnly(Side.CLIENT)
  override def getIcon(access: IBlockAccess, side: Int): IIcon =
  {
    if (camoStack != null)
    {
      try
      {
        val block = camoStack.getItem().asInstanceOf[ItemBlock].field_150939_a
        val icon = block.getIcon(side, camoStack.getItemDamage)

        if (icon != null)
        {
          return icon
        }
      }
      catch
        {
          case e: Exception =>
          {
            e.printStackTrace
          }
        }
    }

    return super.getIcon(access, side)
  }

  /**
   * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color.
   * Note only called when first determining what to render.
   */
  override def colorMultiplier: Int =
  {
    if (camoStack != null)
    {
      try
      {
        return camoStack.getItem().asInstanceOf[ItemBlock].field_150939_a.colorMultiplier(access, x, y, z)
      }
      catch
        {
          case e: Exception =>
          {
            e.printStackTrace
          }
        }
    }
    return super.colorMultiplier
  }

  override def getLightValue(access: IBlockAccess): Int =
  {
    try
    {
      val projector = getProjectorSafe
      if (projector != null)
      {
        return ((Math.min(projector.getModuleCount(Content.moduleGlow), 64).asInstanceOf[Float] / 64) * 15f).toInt
      }
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }

    return 0
  }

  override def getExplosionResistance(entity: Entity): Float = Float.MaxValue

  override def weakenForceField(energy: Int)
  {
    val projector = getProjector

    if (projector != null)
    {
      projector.provideFortron(energy, true)
    }

    if (!world.isRemote)
    {
      world.setBlockToAir(x, y, z)
    }
  }

  override def getPickBlock(target: MovingObjectPosition): ItemStack = null

  /**
   * Tile Logic
   */
  override def canUpdate: Boolean = false

  override def getDescriptionPacket: Packet =
  {
    if (getProjector() != null)
    {
      if (camoStack != null)
      {
        val nbt = new NBTTagCompound
        camoStack.writeToNBT(nbt)
        return ModularForceFieldSystem.packetHandler.toMCPacket(new PacketTile(this) <<< projector.xi <<< projector.yi <<< projector.zi <<< true <<< nbt)
      }

      return ModularForceFieldSystem.packetHandler.toMCPacket(new PacketTile(this) <<< projector.xi <<< projector.yi <<< projector.zi <<< false)
    }

    return null
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    setProjector(new Vector3(buf.readInt, buf.readInt, buf.readInt))
    markRender()
    camoStack = null

    if (buf.readBoolean)
    {
      camoStack = ItemStack.loadItemStackFromNBT(buf.readTag())
    }
  }

  def setProjector(position: Vector3)
  {
    projector = position

    if (!world.isRemote)
    {
      refreshCamoBlock()
    }
  }

  /**
   * Server Side Only
   */
  def refreshCamoBlock()
  {
    if (getProjectorSafe != null)
    {
      camoStack = MFFSUtility.getCamoBlock(getProjector, position)
    }
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    projector = new Vector3(nbt.getCompoundTag("projector"))
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)

    if (getProjector != null)
    {
      nbt.setTag("projector", projector.toNBT)
    }
  }
}