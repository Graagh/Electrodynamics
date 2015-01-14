package edx.basic.fluid.gutter

import edx.core.prefab.node.{NodeFluidPressure, TileFluidProvider}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidContainerRegistry

/**
 * A node for fluid that follows pressure and gravity.
 * @author Calclavia
 */
class NodeFluidGravity(parent: TileFluidProvider, volume: Int = FluidContainerRegistry.BUCKET_VOLUME) extends NodeFluidPressure(parent, volume)
{
  override protected def doDistribute(dir: ForgeDirection, nodeA: NodeFluidPressure, nodeB: NodeFluidPressure, flowRate: Int)
  {
    val tankA = nodeA.getPrimaryTank
    val tankB = nodeB.getPrimaryTank
    val pressureA = nodeA.pressure(dir)
    val pressureB = nodeB.pressure(dir.getOpposite)
    val amountA = tankA.getFluidAmount
    val amountB = tankB.getFluidAmount

    var quantity = 0

    if (dir == ForgeDirection.DOWN)
    {
      quantity = Math.max(if (pressureA > pressureB) (pressureA - pressureB) * flowRate else amountA, amountA)
    }
    else if (dir != ForgeDirection.UP)
    {
      if (nodeB.isInstanceOf[NodeFluidGravity])
        quantity = Math.max(if (pressureA > pressureB) (pressureA - pressureB) * flowRate else Math.min((amountA - amountB) / 2, flowRate), Math.min((amountA - amountB) / 2, flowRate))
      else
        quantity = if (pressureA > pressureB) (pressureA - pressureB) * flowRate else 0
    }

    //TODO: There's a slight pressure backflow
    quantity = Math.min(Math.min(quantity, tankB.getCapacity - amountB), amountA)

    if (quantity > 0)
    {
      val drainStack = drain(dir.getOpposite, quantity, false)
      if (drainStack != null && drainStack.amount > 0)
      {
        drain(dir.getOpposite, nodeB.fill(dir, drainStack, true), true)
      }
    }

  }
}