package mffs.content

import com.resonant.core.prefab.modcontent.ContentLoader
import mffs.Reference
import nova.core.render.model.TechneModel

/**
 * Textures
 * @author Calclavia
 */
object Models extends ContentLoader {
	val fortronCapacitor = new TechneModel(Reference.domain, "fortronCapacitor.tcn")
}
