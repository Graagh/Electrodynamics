package mffs.field.module

import java.util.Set

import mffs.base.ItemModule
import net.minecraft.tileentity.TileEntity
import resonantengine.api.mffs.machine.IFieldMatrix
import resonantengine.lib.transform.vector.Vector3

import scala.collection.convert.wrapAll._

class ItemModuleDome extends ItemModule
{
  setMaxStackSize(1)

  override def onPostCalculate(projector: IFieldMatrix, fieldBlocks: Set[Vector3])
  {
    val absoluteTranslation = new Vector3(projector.asInstanceOf[TileEntity]) + projector.getTranslation
    val newField = fieldBlocks.par.filter(_.y > absoluteTranslation.y).seq
    fieldBlocks.clear()
    fieldBlocks.addAll(newField)
  }
}