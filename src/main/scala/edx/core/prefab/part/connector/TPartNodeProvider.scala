package edx.core.prefab.part.connector

import java.util
import java.util.{List => JList}

import codechicken.multipart.TMultiPart
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.ISave
import resonant.api.tile.INodeProvider
import resonant.api.tile.node.INode
import resonant.lib.debug.IDebugInfo
import resonant.lib.grid.core.Node

import scala.collection.convert.wrapAll._

/**
 * A node trait that can be mixed into any multipart nodes. Mixing this trait will cause nodes to reconstruct/deconstruct when needed.
 * @author Calclavia
 */
trait TPartNodeProvider extends PartAbstract with INodeProvider with IDebugInfo
{
  protected val nodes = new util.HashSet[Node]

  override def start()
  {
    super.start()

    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def onWorldJoin()
  {
    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def onNeighborChanged()
  {
    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def onPartChanged(part: TMultiPart)
  {
    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def onWorldSeparate()
  {
    if (!world.isRemote)
      nodes.foreach(_.deconstruct())
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nodes.filter(_.isInstanceOf[ISave]).foreach(_.asInstanceOf[ISave].save(nbt))
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    nodes.filter(_.isInstanceOf[ISave]).foreach(_.asInstanceOf[ISave].load(nbt))
  }

  override def getNode[N <: INode](nodeType: Class[_ <: N], from: ForgeDirection): N =
  {
    return nodes.filter(node => nodeType.isAssignableFrom(node.getClass)).headOption.getOrElse(null).asInstanceOf[N]
  }

  override def getDebugInfo: JList[String] =
  {
    val debugs = nodes.toList.filter(_.isInstanceOf[IDebugInfo])

    if (debugs.size > 0)
    {
      return debugs.map(_.asInstanceOf[IDebugInfo].getDebugInfo.toList).reduceLeft(_ ::: _)
    }
    return List[String]()
  }
}
