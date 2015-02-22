package mffs.render.button

import mffs.Reference
import mffs.base.GuiMFFS

class GuiIndexedButton(id: Int, x: Int, y: Int, val offset: Vector2 = new Vector2, mainGui: GuiMFFS = null, name: String = "") extends GuiButton(id, x, y, 18, 18, name)
{
  /**
   * Stuck determines if the button is should render as pressed or disabled.
   */
  var stuck = false

  override def drawButton(minecraft: Minecraft, x: Int, y: Int)
  {
    if (this.visible)
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(Reference.guiButtonTexture)
      if (this.stuck)
      {
        GL11.glColor4f(0.6f, 0.6f, 0.6f, 1)
      }
      else if (this.isPointInRegion(this.xPosition, this.yPosition, this.width, this.height, x, y))
      {
        GL11.glColor4f(0.85f, 0.85f, 0.85f, 1)
      }
      else
      {
        GL11.glColor4f(1, 1, 1, 1)
      }
      this.drawTexturedModalRect(this.xPosition, this.yPosition, this.offset.xi, this.offset.yi, this.width, this.height)
      this.mouseDragged(minecraft, x, y)
    }
  }

  protected override def mouseDragged(minecraft: Minecraft, x: Int, y: Int)
  {
    if (this.mainGui != null && this.displayString != null && this.displayString.length > 0)
    {
      if (this.isPointInRegion(this.xPosition, this.yPosition, this.width, this.height, x, y))
      {
		  val title: String = Game.instance.get.languageManager.getLocal("gui." + this.displayString + ".name")
		  this.mainGui.tooltip = Game.instance.get.languageManager.getLocal("gui." + this.displayString + ".tooltip")
        if (title != null && title.length > 0)
        {
          this.mainGui.tooltip = title + ": " + this.mainGui.tooltip
        }
      }
    }
  }

  protected def isPointInRegion(x: Int, y: Int, width: Int, height: Int, checkX: Int, checkY: Int): Boolean =
  {
    return checkX >= x - 1 && checkX < x + width + 1 && checkY >= y - 1 && checkY < y + height + 1
  }

}