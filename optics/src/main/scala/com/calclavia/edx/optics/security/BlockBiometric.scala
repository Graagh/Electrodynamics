package com.calclavia.edx.optics.security

import java.util.{Set => JSet}

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.api.card.AccessCard
import com.calclavia.edx.optics.component.BlockFrequency
import com.calclavia.edx.optics.content.{OpticsModels, OpticsTextures}
import com.resonant.core.access.Permission
import nova.core.block.Block.{PlaceEvent, RightClickEvent}
import nova.core.component.inventory.InventorySimple
import nova.core.component.renderer.{DynamicRenderer, StaticRenderer}
import nova.core.component.transform.Orientation
import nova.core.item.Item
import nova.core.render.model.Model
import nova.scala.util.ExtendedUpdater
import nova.scala.wrapper.FunctionalWrapper._

import scala.collection.convert.wrapAll._

object BlockBiometric {
	val SLOT_COPY = 12
}

class BlockBiometric extends BlockFrequency with ExtendedUpdater with PermissionHandler {

	/**
	 * 2 slots: Card copying
	 * 9 x 4 slots: Access Cards
	 * Under access cards we have a permission selector
	 */
	override val inventory = new InventorySimple(1 + 45)

	/**
	 * Rendering
	 */
	var lastFlicker = 0L

	components.add(new Orientation(this)).hookBasedOnEntity().hookRightClickRotate()

	components.get(classOf[StaticRenderer])
		.onRender(
			(model: Model) => {
				model.matrix.rotate(components.get(classOf[Orientation]).orientation.rotation)
				val modelBiometric = OpticsModels.biometric.getModel
				modelBiometric.children.removeAll(modelBiometric.children.filter(_.name.equals("holoScreen")))
				model.children.add(modelBiometric)
				modelBiometric.bindAll(if (isActive) OpticsTextures.biometricOn else OpticsTextures.biometricOff)
			}
		)

	components.add(new DynamicRenderer())
		.onRender(
			(model: Model) => {
				model.matrix.rotate(components.get(classOf[Orientation]).orientation.rotation)
				/**
				 * Simulate flicker and, hovering
				 */
				val t = System.currentTimeMillis()
				val dist = position.distance(EDX.clientManager.getPlayer.position)

				if (dist < 3) {
					if (Math.random() > 0.05 || (lastFlicker - t) > 200) {
						model.matrix.translate(0, Math.sin(Math.toRadians(animation)) * 0.05, 0)
						//RenderUtility.enableBlending()
						val screenModel = OpticsModels.biometric.getModel
						screenModel.children.removeAll(screenModel.filterNot(_.name.equals("holoScreen")))
						model.children.add(screenModel)
						//RenderUtility.disableBlending()
						lastFlicker = t
						screenModel.bindAll(if (isActive) OpticsTextures.biometricOn else OpticsTextures.biometricOff)
					}
				}
			}
		)

	events.on(classOf[PlaceEvent]).bind((evt: PlaceEvent) => world.markStaticRender(position))
	events.on(classOf[RightClickEvent]).bind((evt: RightClickEvent) => world.markStaticRender(position))

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		animation += deltaTime
	}

	override def hasPermission(playerID: String, permission: Permission): Boolean = {
		super.hasPermission(playerID, permission)

		if (!isActive /*|| ModularForceFieldSystem.proxy.isOp(profile) && Settings.allowOpOverride*/ ) {
			return true
		}

		return getConnectionCards
			.map(stack => stack.asInstanceOf[AccessCard].getAccess)
			.filter(_ != null)
			.exists(_.hasPermission(playerID, permission))
	}

	override def getConnectionCards: Set[Item] = inventory.filter(_ != null).filter(_.isInstanceOf[AccessCard]).toSet

	/*
	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean = {
		if (slotID == 0) {
			return Item.getItem.isInstanceOf[ItemCardFrequency]
		}

		return Item.getItem.isInstanceOf[IAccessCard]
	}

	override def getInventoryStackLimit: Int = 1*/

	override def getBiometricIdentifiers: Set[BlockBiometric] = Set(this)
}