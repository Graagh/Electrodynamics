package edx.core.prefab.part.connector

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * Trait applied to objects that can be insulated/enhanced by a certain item.
 * @author Calclavia
 */
trait TInsulatable extends PartAbstract
{
  /**
   * The item used to remove the insulation. Set to null to not require anything.
   */
  protected lazy val insulationRemovalItem: Item = Items.shears
  /**
   * The item that is used to insulate this object.
   */
  protected val insulationItem: Item
  /**
   * Is this object currently insulated?
   */
  private var _insulated = false

  /**
   * Changes the wire's color.
   */
  override def activate(player: EntityPlayer, part: MovingObjectPosition, itemStack: ItemStack): Boolean =
  {
    if (itemStack != null)
    {
      if (insulated)
      {
        if (itemStack.getItem == insulationRemovalItem || insulationRemovalItem == null)
        {
          if (!world.isRemote && player.capabilities.isCreativeMode)
            tile.dropItems(Seq(new ItemStack(insulationItem)))

          insulated = false
          return true
        }
      }
      else
      {
        if (itemStack.getItem == insulationItem)
        {
          if (!player.capabilities.isCreativeMode)
            player.inventory.decrStackSize(player.inventory.currentItem, 1)

          insulated = true
          return true
        }
      }
    }

    return false
  }

  def insulated: Boolean = _insulated

  /**
   * Insulation Methods
   */
  def insulated_=(insulated: Boolean)
  {
    _insulated = insulated

    if (!world.isRemote)
    {
      tile.notifyPartChange(this)
      sendInsulationUpdate()
    }
    else
      tile.markRender()
  }

  def sendInsulationUpdate()
  {
    tile.getWriteStream(this).writeByte(1).writeBoolean(this._insulated)
  }

  override def write(packet: MCDataOutput, id: Int)
  {
    if (id <= 1)
      packet.writeBoolean(insulated)
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    if (packetID <= 1)
      _insulated = packet.readBoolean
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setBoolean("isInsulated", insulated)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    _insulated = nbt.getBoolean("isInsulated")
  }

  protected override def getDrops(drops: mutable.Set[ItemStack])
  {
    super.getDrops(drops)

    if (insulated)
      drops += new ItemStack(insulationItem)
  }
}
