package mffs.render.button

import mffs.base.GuiMFFS
import mffs.production.BlockFortronCapacitor

class GuiTransferModeButton(id: Int, x: Int, y: Int, mainGui: GuiMFFS, tile: BlockFortronCapacitor) extends GuiIndexedButton(id, x, y, new Vector2, mainGui)
{
  override def drawButton(minecraft: Minecraft, x: Int, y: Int)
  {
    displayString = "transferMode." + tile.getTransferMode.toString
    offset.y = 18 * tile.getTransferMode.id
    super.drawButton(minecraft, x, y)
  }
}