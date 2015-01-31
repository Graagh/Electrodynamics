package mffs.field.module

import java.util.Set

import mffs.base.ItemModule
import mffs.field.TileElectromagneticProjector
import net.minecraft.tileentity.TileEntity
import resonantengine.api.mffs.fortron.FrequencyGridRegistry
import resonantengine.api.mffs.machine.IProjector
import resonantengine.lib.transform.vector.Vector3

import scala.collection.convert.wrapAll._

class ItemModuleFusion extends ItemModule
{
  setMaxStackSize(1)
  setCost(1f)

  override def onProject(projector: IProjector, fieldBlocks: Set[Vector3]): Boolean =
  {
    val tile = projector.asInstanceOf[TileEntity]
    val projectors = FrequencyGridRegistry.instance.getNodes(classOf[TileElectromagneticProjector], projector.getFrequency)

    //TOOD: Check threading efficiency
    val checkProjectors = projectors.par filter (proj => proj.getWorldObj == tile.getWorldObj && proj.isActive && proj.getMode != null)
    val removeFields = (fieldBlocks.par filter (pos => checkProjectors exists (proj => proj.getInteriorPoints.contains(pos) || proj.getMode.isInField(proj, pos)))).seq
    fieldBlocks --= removeFields

    return false
  }
}