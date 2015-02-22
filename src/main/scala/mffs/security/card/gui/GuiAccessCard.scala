package mffs.security.card.gui

import mffs.ModularForceFieldSystem
import mffs.item.gui.GuiItem
import mffs.security.card.ItemCardAccess

/**
 * A gui that contains the permissions
 * @author Calclavia
 */
abstract class GuiAccessCard(player: EntityPlayer, Item: Item, container: Container) extends GuiItem(Item, container)
{
	val itemAccess = Item.getItem.asInstanceOf[ItemCardAccess]
  val permissions = Permissions.root.getAllChildren.toList
  val scroll = new GuiScroll(Math.max(permissions.size - 4, 0))

  override def initGui()
  {
    super.initGui()
    (0 until permissions.size) foreach (i => buttonList.add(new GuiButton(i, 0, 0, 160, 20, permissions(i).toString)))
  }

  override def handleMouseInput()
  {
    super.handleMouseInput()
    scroll.handleMouseInput()
  }

  /**
   * Updates the scroll list
   */
  protected override def updateScreen()
  {
    val index = (scroll.currentScroll * buttonList.size).toInt
    val maxIndex = index + 3
    val access = itemAccess.getAccess(player.getCurrentEquippedItem)

    buttonList map (_.asInstanceOf[GuiButton]) foreach (button =>
    {
      if (button.id >= index && button.id <= maxIndex)
      {
        val perm = permissions(button.id)
        button.displayString = (if (access != null && access.permissions.exists(_.toString.equals(perm.toString))) EnumColor.BRIGHT_GREEN else EnumColor.RED) + perm.toString.getLocal
        button.xPosition = width / 2 - 80
        button.yPosition = height / 2 - 60 + (button.id - index) * 20
        button.visible = true
      }
      else
      {
        button.visible = false
      }
    })
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    //Toggle this specific permission
    ModularForceFieldSystem.packetHandler.sendToServer(new PacketPlayerItem(player) <<< 0 <<< permissions(guiButton.id).toString)
  }

}
