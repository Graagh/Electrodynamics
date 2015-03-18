package edx.electrical.circuit.transformer

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.vec.Vector3
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.prefab.part.PartFace
import edx.electrical.ElectricalContent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{ChatComponentText, MovingObjectPosition}
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.api.graph.INodeProvider
import resonantengine.api.graph.node.INode
import resonantengine.lib.utility.WrenchUtility

/**
 * TODO: We can't use face parts, need to use thicker ones. Also, transformer is currently NO-OP
 *
 * @author Calclavia
 *
 */
class PartElectricTransformer extends PartFace with INodeProvider
{
  /** Step the voltage up */
  var stepUp = true
  /** Amount to mulitply the step by (up x2. down /2) */
  var multiplier: Byte = 2

  var inputNode: ElectricTransformerNode = null
  var outputNode: ElectricTransformerNode = null

  override def preparePlacement(side: Int, facing: Int)
  {
    super.preparePlacement(side, facing)
    outputNode = new ElectricTransformerNode(this, getAbsoluteFacing.getOpposite, false)
    inputNode = new ElectricTransformerNode(this, getAbsoluteFacing, true)
    outputNode.otherNode = inputNode
    inputNode.otherNode = outputNode
  }

  override def read(packet: MCDataInput, id: Int)
  {
    super.read(packet, id)
    multiplier = packet.readByte
  }

  override def write(packet: MCDataOutput, id: Int)
  {
    super.write(packet, id)
    packet.writeByte(multiplier)
  }

  override def doesTick: Boolean = false

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    if (pass == 0)
      RenderTransformer.render(this, pos.x, pos.y, pos.z)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    stepUp = nbt.getBoolean("stepUp")
    multiplier = nbt.getByte("multiplier")
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setBoolean("stepUp", stepUp)
    nbt.setByte("multiplier", multiplier)
  }

  override def activate(player: EntityPlayer, hit: MovingObjectPosition, item: ItemStack): Boolean =
  {
    if (WrenchUtility.isUsableWrench(player, player.inventory.getCurrentItem, x, y, z))
    {
      if (!this.world.isRemote)
      {
        if (player.isSneaking)
        {
          multiplier = ((multiplier + 1) % 3).asInstanceOf[Byte]
        }
        else
        {
          facing = ((facing + 1) % 4).asInstanceOf[Byte]
        }
        WrenchUtility.damageWrench(player, player.inventory.getCurrentItem, x, y, z)
        sendDescUpdate
        tile.notifyPartChange(this)
        if (stepUp)
          outputNode.step = multiplier
        else
          outputNode.step = 1 / multiplier
      }
      return true
    }
    stepUp = !stepUp
    if (!world.isRemote) player.addChatMessage(new ChatComponentText("Transformer set to step " + (if (stepUp) "up" else "down") + "."))
    return true
  }

  override def getNode[N <: INode](nodeType: Class[_ <: N], from: ForgeDirection): N =
  {
    if (from == getAbsoluteFacing)
    {

    }
    else if (from == getAbsoluteFacing.getOpposite)
    {

    }

    return null.asInstanceOf[N]
  }

  protected def getItem: ItemStack =
  {
    return new ItemStack(ElectricalContent.itemTransformer)
  }
}