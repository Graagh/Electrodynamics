package mffs.field.mode

import java.util.{HashSet, Set}

import mffs.render.model.ModelPlane

class ItemModeTube extends ItemModeCube
{
  private val step = 1

	override def getExteriorPoints(projector: IFieldMatrix): Set[Vector3d] =
  {
	  val fieldBlocks = new HashSet[Vector3d]
    val direction: ForgeDirection = projector.getDirection
	  val posScale: Vector3d = projector.getPositiveScale
	  val negScale: Vector3d = projector.getNegativeScale

    for (x <- -negScale.xi to posScale.xi by step; y <- -negScale.yi to posScale.yi by step; z <- -negScale.zi to posScale.zi by step)
    {
      if (!(direction == ForgeDirection.UP || direction == ForgeDirection.DOWN) && (y == -negScale.yi || y == posScale.yi))
      {
		  fieldBlocks.add(new Vector3d(x, y, z))
      }
      else if (!(direction == ForgeDirection.NORTH || direction == ForgeDirection.SOUTH) && (z == -negScale.zi || z == posScale.zi))
      {
		  fieldBlocks.add(new Vector3d(x, y, z))
      }
      else if (!(direction == ForgeDirection.WEST || direction == ForgeDirection.EAST) && (x == -negScale.xi || x == posScale.xi))
      {
		  fieldBlocks.add(new Vector3d(x, y, z))
      }
    }

    return fieldBlocks
  }

  @SideOnly(Side.CLIENT) override def render(projector: IProjector, x: Double, y: Double, z: Double, f: Float, ticks: Long)
  {
    GL11.glScalef(0.5f, 0.5f, 0.5f)
    GL11.glTranslatef(-0.5f, 0, 0)
    ModelPlane.INSTNACE.render
    GL11.glTranslatef(1f, 0, 0)
    ModelPlane.INSTNACE.render
    GL11.glTranslatef(-0.5f, 0f, 0)
    GL11.glRotatef(90, 0, 1, 0)
    GL11.glTranslatef(0.5f, 0f, 0f)
    ModelPlane.INSTNACE.render
    GL11.glTranslatef(-1f, 0f, 0f)
    ModelPlane.INSTNACE.render
  }
}