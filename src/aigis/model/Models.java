package aigis.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// import javax.media.opengl.GL2;
import com.jogamp.opengl.GL2;

/**
 * 複数のモデルを管理するラッパクラスです。
 * 
 * @author m5161121
 *
 */
public class Models {
	private TreeMap<ModelSelection, Model> models;
	private boolean compiled = false;

	public Models() {
		models = new TreeMap<ModelSelection, Model>();
	}

	public void addModel(ModelSelection select, Model model) {
		models.put(select, model);
		compiled = false;
	}

	public void compile(GL2 gl) {
		for (Map.Entry<ModelSelection, Model> entry : models.entrySet()) {
			entry.getValue().compile(gl);
			compiled = true;
		}
	}

	public Model getModel(ModelSelection select) {
		return models.get(select);
	}

	public Collection<Model> getAllModels() {
		return models.values();
	}

	public Set<ModelSelection> getKeys() {
		return models.keySet();
	}

	public boolean isCompiled() {
		return compiled;
	}

	/***
	 * Release the VBOs held by all models.
	 *
	 * @param gl
	 */
	public void dispose(GL2 gl) {
		for (Model model : models.values()) {
			model.dispose(gl);
		}
		compiled = false;
	}

}

// EOF
