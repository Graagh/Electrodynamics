package resonantinduction.core.resource;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.OreDetectionBlackList;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class ResourceGenerator
{
	public static final ResourceGenerator INSTANCE = new ResourceGenerator();
	public static final Set<String> materialNames = new HashSet<String>();
	public static final HashMap<String, Integer> materialColors = new HashMap<String, Integer>();
	public static final HashMap<Item, Integer> itemColorMap = new HashMap<Item, Integer>();

	@ForgeSubscribe
	public void oreRegisterEvent(OreRegisterEvent evt)
	{
		if (evt.Name.startsWith("ingot"))
		{
			String ingotName = evt.Name.replace("ingot", "");

			if (OreDetectionBlackList.isIngotBlackListed("ingot" + ingotName) || OreDetectionBlackList.isOreBlackListed("ore" + ingotName))
				return;

			ResourceGenerator.materialNames.add(ingotName.toLowerCase());
		}
	}

	public static void generateOreResources()
	{
		OreDictionary.registerOre("ingotGold", Item.ingotGold);
		OreDictionary.registerOre("ingotIron", Item.ingotIron);

		OreDictionary.registerOre("oreGold", Block.oreGold);
		OreDictionary.registerOre("oreIron", Block.oreIron);
		OreDictionary.registerOre("oreLapis", Block.oreLapis);

		for (String materialName : materialNames)
		{
			String name = materialName.substring(0, 1).toUpperCase() + materialName.substring(1);

			if (OreDictionary.getOres("ore" + name).size() > 0)
			{
				// if (OreDictionary.getOres("dust" + name).size() == 0)
				{
					OreDictionary.registerOre("rubble" + name, ResonantInduction.itemRubble.getStackFromDust(materialName));
					OreDictionary.registerOre("dust" + name, ResonantInduction.itemDust.getStackFromDust(materialName));
					OreDictionary.registerOre("dustRefined" + name, ResonantInduction.itemRefinedDust.getStackFromDust(materialName));
				}

				// Add rubble to crushing recipes
				// TODO: Change this to CRUSHING when the crusher is finished.
				MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, "ore" + name, "rubble" + name, "rubble" + name);

				// Add dust to mixer recipes, dummy item because mixer doesn't produce any items.
				MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER, "rubble" + name, "dust" + name);
			}
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void reloadTextures(TextureStitchEvent.Post e)
	{
		computeColors();
	}

	@SideOnly(Side.CLIENT)
	public static void computeColors()
	{
		for (String ingotName : materialNames)
		{
			// Compute color
			int totalR = 0;
			int totalG = 0;
			int totalB = 0;

			int colorCount = 0;

			for (ItemStack ingotStack : OreDictionary.getOres("ingot" + ingotName.substring(0, 1).toUpperCase() + ingotName.substring(1)))
			{
				Item theIngot = ingotStack.getItem();
				materialColors.put(ingotName, getAverageColor(ingotStack));
			}

			if (!materialColors.containsKey(ingotName))
			{
				materialColors.put(ingotName, 0xFFFFFF);
			}
		}
	}

	/**
	 * Gets the average color of this item.
	 * 
	 * @param itemStack
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public static int getAverageColor(ItemStack itemStack)
	{
		int totalR = 0;
		int totalG = 0;
		int totalB = 0;

		int colorCount = 0;
		Item item = itemStack.getItem();

		if (itemColorMap.containsKey(item))
		{
			return itemColorMap.get(item);
		}

		try
		{
			Icon icon = item.getIconIndex(itemStack);
			String iconString = icon.getIconName();

			if (iconString != null && !iconString.contains("MISSING_ICON_ITEM"))
			{
				iconString = (iconString.contains(":") ? iconString.replace(":", ":" + Reference.ITEM_TEXTURE_DIRECTORY) : Reference.ITEM_TEXTURE_DIRECTORY + iconString) + ".png";
				ResourceLocation textureLocation = new ResourceLocation(iconString);

				InputStream inputstream = Minecraft.getMinecraft().getResourceManager().getResource(textureLocation).getInputStream();
				BufferedImage bufferedimage = ImageIO.read(inputstream);

				int width = bufferedimage.getWidth();
				int height = bufferedimage.getWidth();

				for (int x = 0; x < width; x++)
				{
					for (int y = 0; y < height; y++)
					{
						Color rgb = new Color(bufferedimage.getRGB(x, y));
						totalR += rgb.getRed();
						totalG += rgb.getGreen();
						totalB += rgb.getBlue();
						colorCount++;
					}
				}
			}
		}
		catch (Exception e)
		{
			ResonantInduction.LOGGER.fine("Failed to compute colors for: " + item);
			// e.printStackTrace();
		}

		if (colorCount > 0)
		{
			totalR /= colorCount;
			totalG /= colorCount;
			totalB /= colorCount;
			int averageColor = new Color(totalR, totalG, totalB).brighter().getRGB();
			itemColorMap.put(item, averageColor);
			return averageColor;
		}

		return 0xFFFFFF;
	}
}
