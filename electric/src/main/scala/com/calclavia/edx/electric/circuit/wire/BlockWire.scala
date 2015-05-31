package com.calclavia.edx.electric.circuit.wire

import java.lang.{Iterable => JIterable}
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.CategoryEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.graph.NodeElectricJunction
import com.calclavia.edx.electric.graph.api.Electric
import com.calclavia.microblock.micro.{Microblock, MicroblockContainer}
import nova.core.block.Block
import nova.core.block.Block.{BlockPlaceEvent, RightClickEvent}
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.misc.Collider
import nova.core.component.renderer.ItemRenderer
import nova.core.game.Game
import nova.core.network.{Packet, PacketHandler, Sync}
import nova.core.render.model.{BlockModelUtil, Model, StaticCubeTextureCoordinates}
import nova.core.retention.{Storable, Stored}
import nova.core.util.{RotationUtil, Direction}
import nova.core.util.transform.matrix.Quaternion
import nova.core.util.transform.shape.Cuboid
import nova.core.util.transform.vector.Vector3d

import scala.collection.convert.wrapAll._
import com.resonant.lib.WrapFunctions._
/**
 * This is the class for all flat wire
 *
 * A flat wire can have 4 adjacent connections.
 * Each side the wire connected two can have 4 different states.
 *
 * A wire's adjacent connections are dependent on the orientation of the wire.
 *
 * @author Calclavia
 */
object BlockWire {
	var occlusionBounds = Array.ofDim[Cuboid](3, 6)
	init()

	def init() {
		for (thickness <- 0 until 3) {
			val occlusion = new Cuboid(2.5 / 8D, 0, 2.5 / 8D, 5.5 / 8D, (thickness + 2) / 16D, 5.5 / 8D) - 0.5

			for (s <- 0 until 6) {
				val rot = s match {
					case 0 => Quaternion.identity
					case 1 => Quaternion.fromAxis(Vector3d.xAxis, Math.PI)
					case 2 => Quaternion.fromAxis(Vector3d.xAxis, Math.PI / 2)
					case 3 => Quaternion.fromAxis(Vector3d.xAxis, -Math.PI / 2)
					case 4 => Quaternion.fromAxis(Vector3d.zAxis, -Math.PI / 2)
					case 5 => Quaternion.fromAxis(Vector3d.zAxis, Math.PI / 2)
				}
				occlusionBounds(thickness)(s) = occlusion.transform(rot)
			}
		}
	}
}

// with TWire with TFacePart with TNormalOcclusion
class BlockWire extends Block with Storable with PacketHandler {

	private val electricNode = new NodeElectricJunction(this)

	/**
	 * The side the wire is placed on.
	 */
	@Sync
	@Stored
	private var side: Byte = 0

	/**
	 * A map of the connections relative to the {@link side}. Split into four 2-bits.
	 *
	 * Each 2 bit represents a state:
	 * 00 - Not connected
	 * 01 - Internal connection (connect upward to a wire within this block space)
	 * 10 - Straight connection (connect directly forward)
	 * 11 - Corner connection (wrapper around a block)
	 *
	 * Format of bitmask:
	 * 00-00-00-00
	 */
	@Sync(ids = Array(0, 1))
	private var connectionMask = 0x00000000

	/**
	 * Add components
	 */
	add(electricNode)
		.setConnections(() => computeConnection)

	add(new Microblock(this))
		.setOnPlace(
	    (evt: BlockPlaceEvent) => {
		    this.side = evt.side.opposite.ordinal.toByte
		    //TODO: Fix wire material
		    get(classOf[MaterialWire]).material = WireMaterial.COPPER
		    Optional.of(MicroblockContainer.sidePosition(Direction.fromOrdinal(this.side)))
	    }
		)

	add(new Collider())
		.setBoundingBox(() => {
		//println("getBounds: " + side)
		BlockWire.occlusionBounds(0)(side) + 0.5
	})
		.isCube(false)
		.isOpaqueCube(false)

	add(new MaterialWire)

	add(new StaticBlockRenderer(this))
		.setOnRender(
	    (model: Model) => {
		    get(classOf[Collider]).occlusionBoxes.apply(Optional.empty()).foreach(cuboid => {
			    BlockModelUtil.drawCube(model, cuboid - 0.5, StaticCubeTextureCoordinates.instance)
		    })

		    model.faces.foreach(_.vertices.map(_.setColor(get(classOf[MaterialWire]).material.color)))
		    model.bindAll(ElectricContent.wireTexture)
	    }
		)

	add(new ItemRenderer(this))
	add(new CategoryEDX)

	rightClickEvent.add((evt: RightClickEvent) => System.out.println(this + " right clicked with side: " + side))

	override def read(packet: Packet) {
		super[PacketHandler].read(packet)
		world.markStaticRender(position)
	}

	/**
	 * Return the connections the block currently is connected to
	 */
	def computeConnection: Set[Electric] = {
		//The new 8-bit connection mask
		var newConnectionMask = 0x00000000
		var connections = Set.empty[Electric]

		for (relativeSide <- 0 until 4) {
			val absSide = RotationUtil.rotateSide(relativeSide, relativeSide)

			if (maskOpen(absSide)) {
				if (!computeInnerConnection(relativeSide, absSide)) {
					if (!computeStraightConnection(relativeSide, absSide)) {
						computeStraightConnection(relativeSide, absSide)
					}
				}
			}
		}

		//Apply connection masks
		if (newConnectionMask != connectionMask) {
			connectionMask = newConnectionMask
			//Update client render
			Game.instance.networkManager.sync(1, this)
		}

		/**
		 * Check inner connection (01)
		 * @return True if a connection is found
		 */
		def computeInnerConnection(relativeSide: Int, absSide: Int): Boolean = {
			val opMicroblock = get(classOf[Microblock]).containers.head.get(Direction.fromOrdinal(absSide))
			if (opMicroblock.isPresent) {
				val otherMicroblock = opMicroblock.get()
				val opElectric = otherMicroblock.block.getOp(classOf[Electric])

				if (opElectric.isPresent) {
					connections += opElectric.get
					newConnectionMask |= 0x01 << (relativeSide * 2)
					return true
				}
			}
			return false
		}

		/**
		 * Check straight connection (11)
			@return True if a connection is found
		 */
		def computeStraightConnection(relativeSide: Int, absSide: Int): Boolean = {
			//The position to check for another wire or a device.
			val checkPos = position + Direction.fromOrdinal(absSide).toVector
			val checkBlock = world.getBlock(checkPos)

			if (checkBlock.isPresent) {

				//First check for microblocks for another wire
				val opMicroblockHolder = checkBlock.get.getOp(classOf[MicroblockContainer])
				if (opMicroblockHolder.isPresent) {
					//Try to find the microblock that is has the component NodeElectric
					val opMicroblock = opMicroblockHolder.get().get(this.side)
					if (opMicroblock.isPresent) {
						val opElectric = opMicroblock.get.block.getOp(classOf[Electric])

						if (opElectric.isPresent) {
							connections += opElectric.get
							newConnectionMask |= 0x10 << (relativeSide * 2)
							return true
						}
					}
				}

				//A microblock is not present. Try checking if the block is electric
				val opElectric = checkBlock.get.getOp(classOf[Electric])

				if (opElectric.isPresent) {
					connections += opElectric.get
					newConnectionMask |= 0x10 << (relativeSide * 2)
					return true
				}
			}
			return false
		}

		/**
		 * Check corner connection (11)
		 * @return True if a connection is found
		 */
		def computeCornerConnection(relativeSide: Int, absSide: Int): Boolean = {
			/**
			 * The position to check for another wire or a device.
			 * Our position check has to move one block towards the side, then one block towards the side the wire is attached to.
			 */
			val checkPos = position + Direction.fromOrdinal(absSide).toVector + Direction.fromOrdinal(side).toVector
			val checkBlock = world.getBlock(checkPos)

			if (checkBlock.isPresent) {
				val opMicroblockHolder = checkBlock.get.getOp(classOf[MicroblockContainer])
				if (opMicroblockHolder.isPresent) {
					//Try to find the microblock that is has the component NodeElectric
					//We look for opposite of the side we are checking, as the block has to be flat placed onto the same block this wire is flat-placed on.
					val opMicroblock = opMicroblockHolder.get().get(absSide ^ -1)
					if (opMicroblock.isPresent) {
						val opElectric = opMicroblock.get.block.getOp(classOf[Electric])

						if (opElectric.isPresent) {
							connections += opElectric.get
							newConnectionMask |= 0x11 << (relativeSide * 2)
							return true
						}
					}
				}
			}
			return false
		}

		/**
		 * Check if there's a cover on a specific side
		 */
		def maskOpen(absSide: Int): Boolean = {
			//Check bounding space (cuboid)
			//TODO:Multiple containers?
			return get(classOf[Microblock]).containers.head.get(absSide).isPresent
		}

		return connections
	}

	override def getID: String = "electricWire"
}