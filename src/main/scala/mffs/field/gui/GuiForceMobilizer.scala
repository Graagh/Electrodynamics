package mffs.field.gui

import mffs.ModularForceFieldSystem
import mffs.base.TilePacketType
import mffs.field.mobilize.BlockMobilizer
import mffs.render.button.GuiIcon

class GuiForceMobilizer(player: EntityPlayer, tile: BlockMobilizer) extends GuiMatrix(new ContainerMatrix(player, tile), tile)
{
  override def initGui
  {
    super.initGui
	  buttonList.add(new GuiIcon(1, width / 2 - 110, height / 2 - 16, new Item(Items.clock)))
	  buttonList.add(new GuiIcon(2, width / 2 - 110, height / 2 - 82, null, new Item(Items.redstone), new Item(Blocks.redstone_block)))
	  buttonList.add(new GuiIcon(3, width / 2 - 110, height / 2 - 60, null, new Item(Blocks.anvil)))
	  buttonList.add(new GuiIcon(4, width / 2 - 110, height / 2 - 38, null, new Item(Items.compass)))

    setupTooltips()
  }

  override def updateScreen
  {
    super.updateScreen
    buttonList.get(2).asInstanceOf[GuiIcon].setIndex(tile.previewMode)
    buttonList.get(3).asInstanceOf[GuiIcon].setIndex(if (tile.doAnchor) 1 else 0)

    if (buttonList.get(4).asInstanceOf[GuiIcon].setIndex(if (tile.absoluteDirection) 1 else 0))
    {
      setupTooltips()
    }
  }

  override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    drawMatrix()
    drawFrequencyGui()
  }

  override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)
    if (guiButton.id == 1)
    {

      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.toggleMoe.id: Integer))
    }
    else if (guiButton.id == 2)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.toggleMode2.id: Integer))
    }
    else if (guiButton.id == 3)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.toggleMode3.id: Integer))
    }
    else if (guiButton.id == 4)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.toggleMode4.id: Integer))
    }
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(tile.getInventoryName)

	  drawString(EnumColor.DARK_AQUA + Game.instance.get.languageManager.getLocal("gui.mobilizer.anchor") + ":", 8, 20)
    drawString(tile.anchor.xi + ", " + tile.anchor.yi + ", " + tile.anchor.zi, 8, 32)

	  drawString(EnumColor.DARK_AQUA + Game.instance.get.languageManager.getLocal("gui.direction") + ":", 8, 48)
    drawString(tile.getDirection.name, 8, 60)

	  drawString(EnumColor.DARK_AQUA + Game.instance.get.languageManager.getLocal("gui.mobilizer.time") + ":", 8, 75)
    drawString((tile.clientMoveTime / 20) + "s", 8, 87)

    drawTextWithTooltip("fortron", EnumColor.DARK_RED + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost * 20).symbol().toString + "/s", 8, 100, x, y)
    drawFortronText(x, y)
    super.drawGuiContainerForegroundLayer(x, y)
  }
}