package com.calclavia.edx.optics.util

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.GraphFrequency
import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.content.OpticsContent
import com.calclavia.edx.optics.field.BlockProjector
import com.calclavia.edx.optics.field.shape.ItemShapeCustom
import com.resonant.core.access.Permission
import nova.core.block.{Block, BlockFactory}
import nova.core.component.ComponentProvider
import nova.core.component.inventory.Inventory
import nova.core.entity.component.Player
import nova.core.item.{Item, ItemBlock}
import nova.core.network.NetworkTarget.Side
import nova.core.util.Direction
import nova.core.util.math.RotationUtil
import nova.core.world.World
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

import scala.collection.convert.wrapAll._

/**
 * A class containing some general helpful functions.
 *
 * @author Calclavia
 */
object OpticUtility {

	/**
	 * Gets the first Item that is an ItemBlock in this TileEntity or in nearby chests.
	 */
	def getFirstItemBlock(block: Block, Item: Item, recur: Boolean = true): Item = {
		block match {
			case projector: BlockProjector =>
				val firstItemBlock = projector
					.getModuleSlots()
					.view
					.map(projector.components.get(classOf[Inventory]).get)
					.collect { case op if op.isPresent => op.get() }
					.collect { case item: ItemBlock => item }
					.headOption
					.orNull

				if (firstItemBlock != null) {
					return firstItemBlock
				}
			//TODO: Check withPriority Sided
			case invProvider: ComponentProvider if invProvider.components.has(classOf[Inventory]) =>
				Direction.VALID_DIRECTIONS
					.view
					//TODO: Check all inventories
					.collect { case dir if invProvider.components.get(classOf[Inventory]).size() > 0 => invProvider.components.get(classOf[Inventory]) }
					.flatten
					.headOption match {
					case Some(entry) => return entry
					case _ =>
				}
		}

		if (recur) {
			Direction.VALID_DIRECTIONS.foreach(
				direction => {
					val checkPos = block.position() + direction.toVector
					val checkBlock = block.world.getBlock(checkPos)

					if (checkBlock.isPresent) {
						val checkStack = getFirstItemBlock(checkBlock.get(), Item, false)

						if (checkStack != null) {
							return checkStack
						}
					}
				})
		}
		return null
	}

	def getFirstItemBlock(i: Int, inventory: Inventory, Item: Item): Item = {
		return inventory.toSet.headOption.orNull
	}

	def getCamoBlock(proj: Projector, position: Vector3D): BlockFactory = {
		val projector = proj.asInstanceOf[BlockProjector]

		if (projector != null) {
			if (Side.get().isServer) {
				if (projector.getModuleCount(OpticsContent.moduleCamouflage) > 0) {
					if (projector.getShapeItem.isInstanceOf[ItemShapeCustom]) {
						val fieldMap = projector.getShapeItem.asInstanceOf[ItemShapeCustom].getStructure

						if (fieldMap != null) {
							val fieldCenter = projector.position() + projector.getTranslation()
							var relativePosition = position - fieldCenter
							relativePosition = new Rotation(RotationUtil.DEFAULT_ORDER, -projector.getRotationYaw, -projector.getRotationPitch, 0).applyTo(relativePosition)

							val theBlock = fieldMap.getBlockFactory(relativePosition)

							if (theBlock.isPresent) {
								return theBlock.get()
							}
						}
					}

					projector.getFilterItems
						.filter(getFilterBlock(_) != null)
						.collect { case item: ItemBlock => item.blockFactory }
						.headOption match {
						case Some(block) => return block
						case _ => return null
					}
				}
			}
		}

		return null
	}

	def getFilterBlock(item: Item): BlockFactory = {
		val opItem = EDX.items.getBlockFromItem(item)
		if (opItem.isPresent) {
			return opItem.get()
		}

		return null
	}

	def hasPermission(world: World, position: Vector3D, permission: Permission, player: Player): Boolean =
		hasPermission(world, position, permission, player.getID())

	/*
		def hasPermission(world: World, position: Vector3d, action: PlayerInteractEvent.Action, player: EntityPlayer): Boolean =
			getRelevantProjectors(world, position) forall (_.isAccessGranted(world, position, player, action))
	*/

	def hasPermission(world: World, position: Vector3D, permission: Permission, id: String): Boolean =
		getRelevantProjectors(world, position)
			.forall(_.hasPermission(id, permission))

	/**
	 * Gets the set of projectors that have an intersect in this position.
	 */
	def getRelevantProjectors(world: World, position: Vector3D): Set[BlockProjector] =
		GraphFrequency
			.instance
			.getNodes
			.collect { case proj: BlockProjector if proj.world.equals(world) => proj }
			.filter(_.isInField(position))
			.toSet

}