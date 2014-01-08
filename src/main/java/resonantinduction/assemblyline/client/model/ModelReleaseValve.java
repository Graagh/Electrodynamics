// Date: 1/7/2013 12:20:13 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package resonantinduction.assemblyline.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelReleaseValve extends ModelBase
{

    // fields
    ModelRenderer ValveStem;
    ModelRenderer ValveWheelCenter;
    ModelRenderer ValveRest;
    ModelRenderer WheelBar3;
    ModelRenderer WheelBar4;
    ModelRenderer Wheel;
    ModelRenderer Wheel2;
    ModelRenderer Wheel3;
    ModelRenderer Wheel4;
    ModelRenderer WheelB;
    ModelRenderer WheelB2;
    ModelRenderer WheelB3;
    ModelRenderer WheelB4;
    ModelRenderer[] renders;

    public ModelReleaseValve()
    {
        textureWidth = 128;
        textureHeight = 32;

        ValveStem = new ModelRenderer(this, 50, 21);
        ValveStem.addBox(-1F, -6F, -1F, 2, 3, 2);
        ValveStem.setRotationPoint(0F, 16F, 0F);
        ValveStem.setTextureSize(128, 32);
        ValveStem.mirror = true;
        setRotation(ValveStem, 0F, 0F, 0F);
        ValveWheelCenter = new ModelRenderer(this, 50, 17);
        ValveWheelCenter.addBox(-0.5F, -7.5F, -0.5F, 1, 2, 1);
        ValveWheelCenter.setRotationPoint(0F, 16F, 0F);
        ValveWheelCenter.setTextureSize(128, 32);
        ValveWheelCenter.mirror = true;
        setRotation(ValveWheelCenter, 0F, 0F, 0F);
        ValveRest = new ModelRenderer(this, 50, 27);
        ValveRest.addBox(-1.5F, -4F, -1.5F, 3, 1, 3);
        ValveRest.setRotationPoint(0F, 16F, 0F);
        ValveRest.setTextureSize(128, 32);
        ValveRest.mirror = true;
        setRotation(ValveRest, 0F, 0F, 0F);
        WheelBar3 = new ModelRenderer(this, 85, 15);
        WheelBar3.addBox(-3F, -7F, -0.5F, 6, 1, 1);
        WheelBar3.setRotationPoint(0F, 16F, 0F);
        WheelBar3.setTextureSize(128, 32);
        WheelBar3.mirror = true;
        setRotation(WheelBar3, 0F, 0.7853982F, 0F);
        WheelBar4 = new ModelRenderer(this, 85, 18);
        WheelBar4.addBox(-3F, -7F, -0.5F, 6, 1, 1);
        WheelBar4.setRotationPoint(0F, 16F, 0F);
        WheelBar4.setTextureSize(128, 32);
        WheelBar4.mirror = true;
        setRotation(WheelBar4, 0F, -0.7853982F, 0F);
        Wheel = new ModelRenderer(this, 50, 13);
        Wheel.addBox(-1.5F, -7.5F, -3.5F, 3, 1, 1);
        Wheel.setRotationPoint(0F, 16F, 0F);
        Wheel.setTextureSize(128, 32);
        Wheel.mirror = true;
        setRotation(Wheel, 0F, -0.7853982F, 0F);
        Wheel2 = new ModelRenderer(this, 50, 13);
        Wheel2.addBox(-1.5F, -7.5F, -3.5F, 3, 1, 1);
        Wheel2.setRotationPoint(0F, 16F, 0F);
        Wheel2.setTextureSize(128, 32);
        Wheel2.mirror = true;
        setRotation(Wheel2, 0F, 2.356194F, 0F);
        Wheel3 = new ModelRenderer(this, 50, 13);
        Wheel3.addBox(-1.5F, -7.5F, -3.5F, 3, 1, 1);
        Wheel3.setRotationPoint(0F, 16F, 0F);
        Wheel3.setTextureSize(128, 32);
        Wheel3.mirror = true;
        setRotation(Wheel3, 0F, -2.356194F, 0F);
        Wheel4 = new ModelRenderer(this, 50, 13);
        Wheel4.addBox(-1.5F, -7.5F, -3.5F, 3, 1, 1);
        Wheel4.setRotationPoint(0F, 16F, 0F);
        Wheel4.setTextureSize(128, 32);
        Wheel4.mirror = true;
        setRotation(Wheel4, 0F, 0.7853982F, 0F);
        WheelB = new ModelRenderer(this, 50, 13);
        WheelB.addBox(-1.5F, -7.5F, 2.5F, 3, 1, 1);
        WheelB.setRotationPoint(0F, 16F, 0F);
        WheelB.setTextureSize(128, 32);
        WheelB.mirror = true;
        setRotation(WheelB, 0F, -3.141593F, 0F);
        WheelB2 = new ModelRenderer(this, 50, 13);
        WheelB2.addBox(-1.5F, -7.5F, 2.5F, 3, 1, 1);
        WheelB2.setRotationPoint(0F, 16F, 0F);
        WheelB2.setTextureSize(128, 32);
        WheelB2.mirror = true;
        setRotation(WheelB2, 0F, 0F, 0F);
        WheelB3 = new ModelRenderer(this, 50, 13);
        WheelB3.addBox(-1.5F, -7.5F, 2.5F, 3, 1, 1);
        WheelB3.setRotationPoint(0F, 16F, 0F);
        WheelB3.setTextureSize(128, 32);
        WheelB3.mirror = true;
        setRotation(WheelB3, 0F, 1.570796F, 0F);
        WheelB4 = new ModelRenderer(this, 50, 13);
        WheelB4.addBox(-1.5F, -7.5F, 2.5F, 3, 1, 1);
        WheelB4.setRotationPoint(0F, 16F, 0F);
        WheelB4.setTextureSize(128, 32);
        WheelB4.mirror = true;
        setRotation(WheelB4, 0F, -1.570796F, 0F);
        renders = new ModelRenderer[] { ValveStem, ValveWheelCenter, ValveRest, WheelBar3, WheelBar4, Wheel, Wheel2, Wheel3, Wheel4, WheelB, WheelB2, WheelB3, WheelB4 };

    }

    public void render()
    {
        ModelRenderer[] renderSet = renders;
        for (int i = 0; i < renders.length; i++)
        {
            renderSet[i].render(0.0625F);
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}