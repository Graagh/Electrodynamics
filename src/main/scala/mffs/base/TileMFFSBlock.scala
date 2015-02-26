package mffs.base

class TileMFFSBlock extends BlockMachine
{
  protected var blockIconTop: IIcon = null
  protected var blockIconOn: IIcon = null
  protected var blockIconTopOn: IIcon = null

  isOpaqueCube = true
  normalRender = true

  override def getIcon(access: IBlockAccess, side: Int): IIcon =
  {
    if (isActive)
    {
      if (side == 0 || side == 1)
      {
        return this.blockIconTopOn
      }
      return this.blockIconOn
    }

    if (side == 0 || side == 1)
    {
      return this.blockIconTop
    }

    return super.getIcon(access, side)
  }

  override def registerIcons(iconRegister: IIconRegister)
  {
    super.registerIcons(iconRegister)
    this.blockIconTop = iconRegister.registerIcon(this.getTextureName + "_top")
    this.blockIconOn = iconRegister.registerIcon(this.getTextureName + "_on")
    this.blockIconTopOn = iconRegister.registerIcon(this.getTextureName + "_top_on")
  }
}