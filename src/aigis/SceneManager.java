package aigis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;

import aigis.ui.LoadingDialog;

/***
 * SceneManager
 *
 * Manages multiple scenes so that models loaded from different
 * folders can be displayed at the same time.
 */
public class SceneManager {

	private ArrayList<Scene> scenes = new ArrayList<Scene>();
	private int activeIndex = 0;

	public SceneManager() {
		// default scene
		scenes.add(new Scene());
	}

	/***
	 * Get the active scene.
	 *
	 * @return
	 */
	public Scene getActiveScene() {
		return scenes.get(activeIndex);
	}

	/***
	 * Set the active scene.
	 *
	 * @param scene
	 */
	public void setActiveScene(Scene scene) {
		int index = scenes.indexOf(scene);
		if (index >= 0) {
			activeIndex = index;
		}
	}

	/***
	 * Get the index of the scene.
	 *
	 * @param scene
	 * @return
	 */
	public int indexOf(Scene scene) {
		return scenes.indexOf(scene);
	}

	/***
	 * Get all scenes.
	 *
	 * @return
	 */
	public List<Scene> getScenes() {
		return scenes;
	}

	/***
	 * Create a new scene, read the data in the directory,
	 * and register it as the active scene.
	 *
	 * @param dir
	 * @param dialog
	 * @param sort
	 * @return the loaded scene
	 * @throws Exception
	 */
	public Scene loadNewScene(File dir, LoadingDialog dialog, boolean sort) throws Exception {
		Scene scene = new Scene();
		scene.loadLookUpTable();
		scene.load(dir, dialog, sort);
		scenes.add(scene);
		activeIndex = scenes.indexOf(scene);
		return scene;
	}

	/***
	 * Compile all scenes.
	 *
	 * @param gl
	 * @throws Exception
	 */
	public void compile(GL2 gl) throws Exception {
		for (Scene scene : scenes) {
			scene.compile(gl);
		}
	}
}
