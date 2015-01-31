package mffs.field.mode

import java.util.{HashSet, Set}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.Tessellator
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11
import resonantengine.api.mffs.machine.{IFieldMatrix, IProjector}
import resonantengine.lib.transform.region.Cuboid
import resonantengine.lib.transform.rotation.EulerAngle
import resonantengine.lib.transform.vector.Vector3

class ItemModePyramid extends ItemMode
{
  private val step = 1

  def getExteriorPoints(projector: IFieldMatrix): Set[Vector3] =
  {
    val fieldBlocks = new HashSet[Vector3]

    val posScale = projector.getPositiveScale
    val negScale = projector.getNegativeScale

    var xSize = Math.max(posScale.x, Math.abs(negScale.x))
    var zSize = Math.max(posScale.z, Math.abs(negScale.z))
    val ySize = Math.max(posScale.y, Math.abs(negScale.y)).asInstanceOf[Int]

    val initX = xSize.asInstanceOf[Int]
    val initZ = xSize.asInstanceOf[Int]

    val xDecr = xSize / (ySize * 2)
    val zDecr = zSize / (ySize * 2)

    //Create pyramid
    for (y <- -ySize to ySize)
    {
      for (x <- -initX to initX; z <- -initZ to initZ)
      {
        if (Math.abs(x) == Math.round(xSize) && Math.abs(z) <= Math.round(zSize))
          fieldBlocks.add(new Vector3(x, y, z))
        else if (Math.abs(z) == Math.round(zSize) && Math.abs(x) <= Math.round(xSize))
          fieldBlocks.add(new Vector3(x, y, z))
        else if (y == -ySize)
          fieldBlocks.add(new Vector3(x, y, z))
      }

      xSize -= xDecr
      zSize -= zDecr
    }

    return fieldBlocks
  }

  def getInteriorPoints(projector: IFieldMatrix): Set[Vector3] =
  {
    val fieldBlocks: Set[Vector3] = new HashSet[Vector3]
    val posScale: Vector3 = projector.getPositiveScale
    val negScale: Vector3 = projector.getNegativeScale
    val xStretch: Int = posScale.xi + negScale.xi
    val yStretch: Int = posScale.yi + negScale.yi
    val zStretch: Int = posScale.zi + negScale.zi
    val translation = new Vector3(0, -0.4, 0)

    for (x <- -xStretch to xStretch by step; y <- 0 to yStretch by step; z <- -zStretch to zStretch by step)
    {
      val position = new Vector3(x, y, z) + translation

      if (isInField(projector, position + new Vector3(projector.asInstanceOf[TileEntity])))
      {
        fieldBlocks.add(position)
      }
    }
    return fieldBlocks
  }

  override def isInField(projector: IFieldMatrix, position: Vector3): Boolean =
  {
    val posScale: Vector3 = projector.getPositiveScale.clone
    val negScale: Vector3 = projector.getNegativeScale.clone
    val xStretch: Int = posScale.xi + negScale.xi
    val yStretch: Int = posScale.yi + negScale.yi
    val zStretch: Int = posScale.zi + negScale.zi
    val projectorPos: Vector3 = new Vector3(projector.asInstanceOf[TileEntity])
    projectorPos.add(projector.getTranslation)
    projectorPos.add(new Vector3(0, -negScale.yi + 1, 0))
    val relativePosition: Vector3 = position.clone.subtract(projectorPos)
    relativePosition.transform(new EulerAngle(-projector.getRotationYaw, -projector.getRotationPitch, 0))
    val region: Cuboid = new Cuboid(-negScale, posScale)

    if (region.intersects(relativePosition) && relativePosition.y > 0)
    {
      if ((1 - (Math.abs(relativePosition.x) / xStretch) - (Math.abs(relativePosition.z) / zStretch) > relativePosition.y / yStretch))
      {
        return true
      }
    }

    return false
  }

  @SideOnly(Side.CLIENT) override def render(projector: IProjector, x: Double, y: Double, z: Double, f: Float, ticks: Long)
  {
    val tessellator = Tessellator.instance
    GL11.glPushMatrix
    GL11.glRotatef(180, 0, 0, 1)
    val height: Float = 0.5f
    val width: Float = 0.3f
    val uvMaxX: Int = 2
    val uvMaxY: Int = 2
    val translation: Vector3 = new Vector3(0, -0.4, 0)
    tessellator.startDrawing(6)
    tessellator.setColorRGBA(72, 198, 255, 255)
    tessellator.addVertexWithUV(0 + translation.x, 0 + translation.y, 0 + translation.z, 0, 0)
    tessellator.addVertexWithUV(-width + translation.x, height + translation.y, -width + translation.z, -uvMaxX, -uvMaxY)
    tessellator.addVertexWithUV(-width + translation.x, height + translation.y, width + translation.z, -uvMaxX, uvMaxY)
    tessellator.addVertexWithUV(width + translation.x, height + translation.y, width + translation.z, uvMaxX, uvMaxY)
    tessellator.addVertexWithUV(width + translation.x, height + translation.y, -width + translation.z, uvMaxX, -uvMaxY)
    tessellator.addVertexWithUV(-width + translation.x, height + translation.y, -width + translation.z, -uvMaxX, -uvMaxY)
    tessellator.draw
    GL11.glPopMatrix
  }
}