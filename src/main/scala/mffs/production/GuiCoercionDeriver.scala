package mffs.production

import mffs.ModularForceFieldSystem
import mffs.base.{GuiMFFS, TilePacketType}

class GuiCoercionDeriver(player: EntityPlayer, tile: TileCoercionDeriver) extends GuiMFFS(new ContainerCoercionDeriver(player, tile), tile)
{
  override def initGui
  {
    super.initGui
	  this.buttonList.add(new GuiButton(1, this.width / 2 - 10, this.height / 2 - 35, 58, 20, Game.instance.get.languageManager.getLocal("gui.deriver.derive")))
  }

  override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(tile.getInventoryName)
    GL11.glPushMatrix
    GL11.glRotatef(-90, 0, 0, 1)
    drawTextWithTooltip("upgrade", -95, 140, x, y)
    GL11.glPopMatrix

    if (!tile.isInversed)
    {
		buttonList.get(1).asInstanceOf[GuiButton].displayString = Game.instance.get.languageManager.getLocal("gui.deriver.derive")
    }
    else
    {
		buttonList.get(1).asInstanceOf[GuiButton].displayString = Game.instance.get.languageManager.getLocal("gui.deriver.integrate")
    }

    drawString(EnumColor.AQUA + "Energy Requirement:", 8, 20)
    renderUniversalDisplay(8, 30, tile.getPower, x, y, UnitDisplay.Unit.WATT)

	  drawTextWithTooltip("progress", "%1: " + (if (this.tile.isActive) Game.instance.get.languageManager.getLocal("gui.deriver.running") else Game.instance.get.languageManager.getLocal("gui.deriver.idle")), 8, 60, x, y)
    drawString("Production: " + (if (this.tile.isInversed) EnumColor.DARK_RED else EnumColor.DARK_GREEN) + new UnitDisplay(UnitDisplay.Unit.LITER, tile.productionRate * 20) + "/s", 8, 100)

    drawFortronText(x, y)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)

    //Upgrade slots
    (0 to 2) foreach (y => drawSlot(153, 46 + y * 18))

    drawSlot(8, 75, SlotType.BATTERY)
    drawSlot(8 + 20, 75)
    drawBar(50, 77, 1)

    drawFrequencyGui()
  }

  override def actionPerformed(guibutton: GuiButton)
  {
    super.actionPerformed(guibutton)

    if (guibutton.id == 1)
    {
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.toggleMoe.id: Integer))
    }
  }
}