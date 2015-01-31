package mffs.util

import com.mojang.authlib.GameProfile
import mffs.Content
import mffs.field.TileElectromagneticProjector
import mffs.field.mode.ItemModeCustom
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import resonantengine.api.mffs.fortron.FrequencyGridRegistry
import resonantengine.api.mffs.machine.IProjector
import resonantengine.lib.access.Permission
import resonantengine.lib.grid.frequency.GridFrequency
import resonantengine.lib.transform.rotation.EulerAngle
import resonantengine.lib.transform.vector.Vector3

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * A class containing some general helpful functions.
 *
 * @author Calclavia
 */
object MFFSUtility
{
  /**
   * Gets the first itemStack that is an ItemBlock in this TileEntity or in nearby chests.
   */
  def getFirstItemBlock(tileEntity: TileEntity, itemStack: ItemStack): ItemStack =
  {
    return getFirstItemBlock(tileEntity, itemStack, true)
  }

  def getFirstItemBlock(tileEntity: TileEntity, itemStack: ItemStack, recur: Boolean): ItemStack =
  {
    if (tileEntity.isInstanceOf[IProjector])
    {
      val projector = tileEntity.asInstanceOf[IProjector]

      projector.getModuleSlots().find(getFirstItemBlock(_, projector, itemStack) != null) match
      {
        case Some(entry) => return getFirstItemBlock(entry, projector, itemStack)
        case _ =>
      }
    }
    else if (tileEntity.isInstanceOf[IInventory])
    {
      val inventory = tileEntity.asInstanceOf[IInventory]

      (0 until inventory.getSizeInventory()).view map (getFirstItemBlock(_, inventory, itemStack)) headOption match
      {
        case Some(entry) => return entry
        case _ =>
      }
    }

    if (recur)
    {
      ForgeDirection.VALID_DIRECTIONS.foreach(
        direction =>
        {
          val vector = new Vector3(tileEntity) + direction
          val checkTile = vector.getTileEntity(tileEntity.getWorldObj())

          if (checkTile != null)
          {
            val checkStack: ItemStack = getFirstItemBlock(checkTile, itemStack, false)

            if (checkStack != null)
            {
              return checkStack
            }
          }
        })
    }
    return null
  }

  def getFirstItemBlock(i: Int, inventory: IInventory, itemStack: ItemStack): ItemStack =
  {
    val checkStack: ItemStack = inventory.getStackInSlot(i)
    if (checkStack != null && checkStack.getItem.isInstanceOf[ItemBlock])
    {
      if (itemStack == null || checkStack.isItemEqual(itemStack))
      {
        return checkStack
      }
    }
    return null
  }

  def getCamoBlock(proj: IProjector, position: Vector3): ItemStack =
  {
    val projector = proj.asInstanceOf[TileElectromagneticProjector]
    val tile = projector.asInstanceOf[TileEntity]

    if (projector != null)
    {
      if (!tile.getWorldObj().isRemote)
      {
        if (projector.getModuleCount(Content.moduleCamouflage) > 0)
        {
          if (projector.getMode.isInstanceOf[ItemModeCustom])
          {
            val fieldMap = (projector.getMode.asInstanceOf[ItemModeCustom]).getFieldBlockMap(projector, projector.getModeStack)

            if (fieldMap != null)
            {
              val fieldCenter = new Vector3(projector.asInstanceOf[TileEntity]) + projector.getTranslation()
              var relativePosition: Vector3 = position - fieldCenter
              relativePosition = relativePosition.transform(new EulerAngle(-projector.getRotationYaw, -projector.getRotationPitch, 0))

              val blockInfo = fieldMap(relativePosition.round)

              if (blockInfo != null && !blockInfo._1.isAir(tile.getWorldObj(), position.xi, position.yi, position.zi))
              {
                return new ItemStack(blockInfo._1, 1, blockInfo._2)
              }
            }
          }

          projector.getFilterStacks filter (getFilterBlock(_) != null) headOption match
          {
            case Some(entry) => return entry
            case _ => return null
          }
        }
      }
    }

    return null
  }

  def getFilterBlock(itemStack: ItemStack): Block =
  {
    if (itemStack != null)
    {
      return getFilterBlock(itemStack.getItem)

    }
    return null
  }

  def getFilterBlock(item: Item): Block =
  {
    if (item.isInstanceOf[ItemBlock])
    {
      return item.asInstanceOf[ItemBlock].field_150939_a
    }

    return null
  }

  def hasPermission(world: World, position: Vector3, permission: Permission, player: EntityPlayer): Boolean =
  {
    return hasPermission(world, position, permission, player.getGameProfile())
  }

  def hasPermission(world: World, position: Vector3, permission: Permission, profile: GameProfile): Boolean =
  {
    return getRelevantProjectors(world, position).forall(_.hasPermission(profile, permission))
  }

  def hasPermission(world: World, position: Vector3, action: PlayerInteractEvent.Action, player: EntityPlayer): Boolean =
  {
    return getRelevantProjectors(world, position) forall (_.isAccessGranted(world, position, player, action))
  }

  /**
   * Gets the set of projectors that have an effect in this position.
   */
  def getRelevantProjectors(world: World, position: Vector3): mutable.Set[TileElectromagneticProjector] =
  {
    return FrequencyGridRegistry.instance.asInstanceOf[GridFrequency].getNodes(classOf[TileElectromagneticProjector]) filter (_.isInField(position))
  }
}