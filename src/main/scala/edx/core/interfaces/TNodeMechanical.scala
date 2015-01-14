package edx.core.interfaces

import resonant.api.tile.node.INode
import resonant.lib.transform.vector.IVectorWorld

/**
 * Applied to any node that will act as a mechanical object
 *
 * @author Darkguardsman, Calclavia
 */
trait TNodeMechanical extends INode with IVectorWorld
{
  /**
   * Gets the angular velocity of the mechanical device from a specific side
   *
   * @return Angular velocity in meters per second
   */
  def angularVelocity: Double

  /**
   * Gets the torque of the mechanical device from a specific side
   *
   * @return force
   */
  def torque: Double

  /**
   * The mechanical load energy loss per second.
   * @return Power loss in Watts.
   */
  def getLoad = 10D

  /**
   * The radius of rotation
   */
  def radius(other: TNodeMechanical) = 0.5

  /**
   * Does the direction flip on this side for rotation
   *
   * @param prev - The other mechanical node
   * @return boolean, true = flipped, false = not
   */
  def inverseRotation(prev: TNodeMechanical): Boolean = true

  /**
   * Does this node flip the next node's rotation?
   * @param next - The next node
   * @return True to flip the next node
   */
  def inverseNext(next: TNodeMechanical): Boolean = true

  /**
   * Applies rotational force and velocity to this node increasing its current rotation value
   *
   * @param torque          - force at an angle
   */
  def rotate(torque: Double, angularVelocity: Double)
}