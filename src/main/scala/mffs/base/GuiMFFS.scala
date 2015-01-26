package mffs.base

import mffs.ModularForceFieldSystem
import mffs.render.button.GuiIcon
import net.minecraft.client.gui.GuiButton
import net.minecraft.init.Blocks
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import resonantengine.core.network.discriminator.PacketTile
import resonantengine.lib.render.EnumColor
import resonantengine.lib.utility.science.UnitDisplay
import resonantengine.lib.wrapper.CollectionWrapper._
import resonantengine.prefab.gui.GuiContainerBase

class GuiMFFS(container: Container, tile: TileMFFS) extends GuiContainerBase(container)
{
  ySize = 217

  def this(container: Container) = this(container, null)

  override def initGui()
  {
    super.initGui
    buttonList.clear()

    //Activation button
    buttonList.add(new GuiIcon(0, width / 2 - 110, height / 2 - 104, new ItemStack(Blocks.torch), new ItemStack(Blocks.redstone_torch)))
  }

  override def updateScreen()
  {
    super.updateScreen()

    if (tile.isInstanceOf[TileMFFS])
    {
      if (buttonList.size > 0 && this.buttonList.get(0) != null)
      {
        buttonList.get(0).asInstanceOf[GuiIcon].setIndex(if (tile.isRedstoneActive) 1 else 0)
      }
    }
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (tile != null && guiButton.id == 0)
    {
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.toggleActivation.id: Integer))
    }
  }

  protected def drawFortronText(x: Int, y: Int)
  {
    if (tile.isInstanceOf[TileFortron])
    {
      val fortronTile = tile.asInstanceOf[TileFortron]
      drawTextWithTooltip("fortron", EnumColor.WHITE + "" + new UnitDisplay(UnitDisplay.Unit.LITER, fortronTile.getFortronEnergy).symbol() + "/" + new UnitDisplay(UnitDisplay.Unit.LITER, fortronTile.getFortronCapacity).symbol(), 35, 119, x, y)
    }
  }

  protected def drawFrequencyGui()
  {
    //Frequency Card
    drawSlot(7, 113)

    if (tile.isInstanceOf[TileFortron])
    {
      val fortronTile = tile.asInstanceOf[TileFortron]

      //Fortron Bar
      drawLongBlueBar(30, 115, Math.min(fortronTile.getFortronEnergy.asInstanceOf[Float] / fortronTile.getFortronCapacity.asInstanceOf[Float], 1))
    }
  }

}