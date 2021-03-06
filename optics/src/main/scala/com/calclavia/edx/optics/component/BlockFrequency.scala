package com.calclavia.edx.optics.component

import java.util.{Set => JSet}

import com.calclavia.edx.optics.GraphFrequency
import com.calclavia.edx.optics.api.Frequency
import com.calclavia.edx.optics.item.card.ItemCardFrequency
import nova.core.block.Stateful.{LoadEvent, UnloadEvent}
import nova.core.component.inventory.InventorySimple
import nova.core.item.Item
import nova.scala.wrapper.FunctionalWrapper._

/**
 * All blocks that have a frequency value should extend this
 * @author Calclavia
 */
abstract class BlockFrequency extends BlockMachine with Frequency {
	val inventory: InventorySimple
	val frequencySlot = 0

	events.on(classOf[LoadEvent]).bind((evt: LoadEvent) => GraphFrequency.instance.add(this))
	events.on(classOf[UnloadEvent]).bind((evt: UnloadEvent) => GraphFrequency.instance.remove(this))

	override def getFrequency: Int = {
		val frequencyCard = getFrequencyCard
		return if (frequencyCard != null) frequencyCard.getFrequency else 0
	}

	override def setFrequency(frequency: Int) {
		val frequencyCard = getFrequencyCard
		if (frequencyCard != null) {
			frequencyCard.setFrequency(frequency)
		}
	}

	def getFrequencyCard: ItemCardFrequency = {
		val item = inventory.get(frequencySlot)

		if (item.isPresent && item.get().isInstanceOf[ItemCardFrequency]) {
			return item.get().asInstanceOf[ItemCardFrequency]
		}

		return null
	}

	/**
	 * Gets a set of cards that define frequency.
	 */
	def getConnectionCards: Set[Item] = Set(inventory.get(0).orElse(null))
}