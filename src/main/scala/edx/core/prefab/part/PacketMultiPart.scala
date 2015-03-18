package edx.core.prefab.part

import codechicken.multipart.{TMultiPart, TileMultipart}
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import net.minecraft.entity.player.EntityPlayer
import resonantengine.core.network.discriminator.PacketType
import resonantengine.lib.transform.vector.Vector3
import resonantengine.prefab.network.TPacketReceiver

/**
 * Packet handler for blocks and tile entities.
 *
 * @author Calclavia
 */
class PacketMultiPart extends PacketType
{
  var x: Int = 0
  var y: Int = 0
  var z: Int = 0
  var partID: Int = 0

  def this(part: TMultiPart, partID: Int)
  {
    this()
    this.x = part.x
    this.y = part.y
    this.z = part.z
    this.partID = partID

    this <<< x
    this <<< y
    this <<< z
    this <<< partID
  }

  def encodeInto(ctx: ChannelHandlerContext, buffer: ByteBuf)
  {
    buffer.writeInt(x)
    buffer.writeInt(y)
    buffer.writeInt(z)
    buffer.writeInt(partID)
    buffer.writeBytes(data)
  }

  def decodeInto(ctx: ChannelHandlerContext, buffer: ByteBuf)
  {
    x = buffer.readInt
    y = buffer.readInt
    z = buffer.readInt
    partID = buffer.readInt
    data_$eq(buffer.slice)
  }

  override def handleClientSide(player: EntityPlayer)
  {
    handle(player)
  }

  override def handleServerSide(player: EntityPlayer)
  {
    handle(player)
  }

  def handle(player: EntityPlayer)
  {
    val tile = player.getEntityWorld.getTileEntity(this.x, this.y, this.z)

    if (tile.isInstanceOf[TileMultipart])
    {
      val part = tile.asInstanceOf[TileMultipart].partMap(data.readInt)

      if (part.isInstanceOf[TPacketReceiver])
      {
        part.asInstanceOf[TPacketReceiver].read(data.slice, player, this)
      }
    }
    else
    {
      throw new UnsupportedOperationException("Packet was sent to a multipart not implementing IPacketReceiver, this is a coding error [" + tile + "] in " + new Vector3(x, y, z))
    }
  }
}