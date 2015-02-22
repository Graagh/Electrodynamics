package mffs.field.module

import mffs.ModularForceFieldSystem
import mffs.base.ItemModule
import mffs.field.TileForceField
import mffs.security.MFFSPermissions

class ItemModuleShock extends ItemModule
{
	override def onCollideWithForceField(world: World, x: Int, y: Int, z: Int, entity: Entity, moduleStack: Item): Boolean =
  {
    if (entity.isInstanceOf[EntityPlayer])
    {
      val entityPlayer = entity.asInstanceOf[EntityPlayer]
      val tile = world.getTileEntity(x, y, z)

      if (tile.isInstanceOf[TileForceField])
      {
        if (tile.asInstanceOf[TileForceField].getProjector.hasPermission(entityPlayer.getGameProfile, MFFSPermissions.forceFieldWarp))
          return true
      }

      entity.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, moduleStack.stackSize)
    }

    return true
  }
}