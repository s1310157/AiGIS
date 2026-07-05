package aigis.gl;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRONT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_LINE_SMOOTH;
import static com.jogamp.opengl.GL.GL_LINE_STRIP;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import static com.jogamp.opengl.GL2ES3.GL_READ_WRITE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_COLOR_MATERIAL;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW_MATRIX;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION_MATRIX;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_COLOR_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_NORMAL_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;

import aigis.Const;
import aigis.Logger;
import aigis.Scene;
import aigis.model.CameraInfo;
import aigis.model.General3D;
import aigis.model.Grids;
import aigis.model.LatLon;
import aigis.model.Light;
import aigis.model.Model;
import aigis.model.ModelSelection;
import aigis.model.ModelSelection.Resolution;
import aigis.model.Models;
import aigis.model.SpectrumMap;

/***
 * Renderer
 */
public class Renderer {

	public class RenderSetting {
		public boolean displayLightDirection = false;
		public boolean displayColorBar = true;
		public boolean displayLatLonGrid = false;
		public boolean displayAdditional3D = true;
		public boolean displayAxis = true;
		public boolean isShading = true;
		public boolean isTexRender = true;
		public boolean fixedLightPos = false;
		public boolean isPerspective = true;
		public int colorbarIndex = 0;
	}

	public RenderSetting setting = new RenderSetting();

	private CameraInfo camera = new CameraInfo();
	private double[] modelview = new double[16];
	private double[] projection = new double[16];
	private int[] viewport = new int[4];
	private Light light = new Light();

	private HashMap<ModelSelection, String> spectrums = new HashMap<ModelSelection, String>();
	
	private ModelSelection currentSelection = new ModelSelection(Resolution.Low, 0);
	private ModelSelection prevSelection = null;
	private Model currentModel;
	private String currentSpectrum = Const.SPECTRUMKEY_FLAT;
	private ArrayList<Integer> polygonIDs = new ArrayList<Integer>();
	private HashMap<Integer, float[]> prevRGBAs = new HashMap<Integer, float[]>();

	private Scene scene;

	private Font font = new Font("Monospaced", java.awt.Font.BOLD, 32);
	private TextRenderer tr = new TextRenderer(font, true, true);
	private GLU glu = new GLU();

	public Renderer(Scene scene) {
		this.scene = scene;
	}

	/***
	 * Change the scene to display.
	 *
	 * @param scene
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	/***
	 * Get the scene displayed by this renderer.
	 *
	 * @return
	 */
	public Scene getScene() {
		return scene;
	}

	/**
	 * Reset settings
	 */
	public void resetSetting(boolean resetCamera) {
		if (resetCamera) {
			camera.reset();
			polygonIDs.clear();
		}
		setting = new RenderSetting();
		currentSpectrum = Const.SPECTRUMKEY_FLAT;
		spectrums.clear();
		changeModel(new ModelSelection(Resolution.Low, 0));
		changeColorbar(setting.colorbarIndex);
		changeShading(setting.isShading);
	}

	/***
	 * Get current selected polygon ID.
	 * 
	 * @return
	 */
	public int getPolygonID() {
		return polygonIDs.isEmpty() ? -1 : polygonIDs.get(polygonIDs.size() - 1).intValue();
	}

	/***
	 * Select a polygon with ID.
	 * 
	 * @param polygonID
	 * @return
	 */
	public LatLon selectPolygon(int polygonID, boolean multi) {
		if (polygonID >= 0) {
			if (multi) {
				// print log
				String str = "";
				for (int id : polygonIDs) {
					str += id + ",";
				}
				str += polygonID;
				Logger.Info("Selected:" + str);
			} else {
				polygonIDs.clear();
				prevRGBAs.clear();
			}
			polygonIDs.add(polygonID);
			float prevRGBA[] = new float[4];
			for (int k = 0; k < 3; k++) {
				prevRGBA[k] = currentModel.rgba[polygonID][k];
			}
			prevRGBAs.put(polygonID, prevRGBA);
			//
			return currentModel.getLatLon()[polygonID];
		} else {
			polygonIDs.clear();
			prevRGBAs.clear();
		}
		return null;
	}

	/***
	 * Change current model.
	 * 
	 * @param model
	 */
	private void changeModel(ModelSelection select, String spectrum) {
		polygonIDs.clear();
		currentSelection = select;
		currentModel = scene.getModel(select);
		currentSpectrum = spectrum;
		if (currentModel != null) {
			currentModel.changeSpectrumMap(currentSpectrum, setting.colorbarIndex);
		}
	}
	
	public void changeModel(ModelSelection select) {
		String spectrum = spectrums.get(select);
		if (spectrum == null) {
			spectrum = Const.SPECTRUMKEY_FLAT;
		}
		changeModel(select, spectrum);
	}
	
	/***
	 * 
	 */
	public void changeResolution(Resolution res) {
		if (res == currentSelection.resolution) return;
		if (res == null) {
			if (prevSelection == null) return;
			changeModel(prevSelection);
			prevSelection = null;
		} else {
			prevSelection = currentSelection;
			changeModel(new ModelSelection(res, currentSelection.index));
		}
	}

	/***
	 * Get current model.
	 * 
	 * @return
	 */
	public Model getCurrentModel() {
		return currentModel;
	}

	public boolean isHighResolution() {
		return currentSelection.resolution == Resolution.High;
	}

	public ModelSelection getCuttentSelection() {
		return prevSelection == null ? currentSelection : prevSelection;
	}

	/***
	 * Change the current spectrum with row index.
	 * 
	 * @param index
	 */
	public void changeSpectrum(String key) {
		if (currentModel != null) {
			spectrums.put(currentSelection, key);
			currentSpectrum = key;
			currentModel.changeSpectrumMap(key, setting.colorbarIndex);
		}
	}

	public SpectrumMap getCurrentSpectrum() {
		return currentModel.getSpectrum(currentSpectrum);
	}


	/***
	 * Get the index of the current spectrum.
	 * 
	 * @return
	 */
	public String getCurrentSpectrumKey() {
		return currentSpectrum;
	}

	/***
	 * Switch spectrum color.
	 * 
	 * @param spectrunIndex
	 */
	public void changeColorbar(int colorbarIndex) {
		setting.colorbarIndex = colorbarIndex;
		changeSpectrum(currentSpectrum);
	}

	/***
	 * 
	 * @param shading
	 */
	public void changeShading(boolean shading) {
		setting.isShading = shading;
		light.changeDiffuse(!shading);
	}

	/***
	 * Get the ID of the polygon at a specific position.
	 * 
	 * @param mousePos
	 * @param scales
	 * @return
	 */
	public Integer getPointPolygonID(Point2D.Double mousePos, float[] scales) {
		// for retina
		if (scales[0] > 1) {
			viewport[0] /= scales[0];
			viewport[2] /= scales[0];
		}
		if (scales[1] > 1) {
			viewport[1] /= scales[1];
			viewport[3] /= scales[1];
		}
		if (currentModel == null) {
			return -1;
		}
		return currentModel.getID(mousePos, modelview, projection, viewport);
	}

	/***
	 * Synchronize the camera with another renderer.
	 * 
	 * @param renderer
	 */
	public void syncCamera(Renderer renderer) {
		this.camera = (CameraInfo) renderer.camera.clone();
	}

	/***
	 * Change scale.
	 * 
	 * @param scale
	 */
	public void scale(float scale) {
		double s = camera.getDistance() - (scene.getModelSize() / 2 * 0.7);
		double t = 3.2 - camera.fov * 2;
		if (t < 0)
			t = 0;
		if (s < 0)
			s = 0;
		if (s < t) {
			s = (t - s + 1);
			s = s * s;
		} else {
			s = t / s;
		}

		Vector3d vdir = new Vector3d(camera.direction);
		vdir.scale(-scale / 10f / s);
		camera.position.add(vdir);
	}

	/**
	 * Move the camera on z axis.
	 * 
	 * @param distance
	 */
	public void setCameraDistance(float distance) {
		Vector3d p = new Vector3d(camera.position);
		camera.direction.normalize();
		double dp = camera.direction.dot(p) * 2;
		double t = (-dp - Math.sqrt(Math.pow(dp, 2) - 4 * (p.lengthSquared() - Math.pow(distance, 2)))) / 2;
		if (Double.isNaN(t))
			return;
		Vector3d d = new Vector3d(camera.direction);
		d.scale(t);
		camera.position.add(d);
	}

	/**
	 * Get camera information.
	 * 
	 * @return info
	 */
	public CameraInfo getCameraInfo() {
		return camera;
	}

	/**
	 * Set camera information.
	 * 
	 * @param info
	 */
	public void setCameraInfo(CameraInfo info) {
		this.camera = info;
	}

	/***
	 * Move object position.
	 * 
	 * @param x
	 * @param y
	 */
	public void move(float x, float y) {
		camera.move(x / 100f, y / 100f);
	}

	/**
	 * Rotate object.
	 * 
	 * @param dx
	 * @param dy
	 */
	public void rotateObject(float x, float y, float z, boolean fixedAxis) {
		double s = camera.getDistance() - (scene.getModelSize() / 2 * 0.7);
		double t = 3.2 - camera.fov * 2;
		if (t < 0)
			t = 0;
		if (s < 0)
			s = 0;
		if (s < t) {
			s = (t - s + 1);
			s = s * s * s;
			x /= s;
			y /= s;
			z /= s;
		}
		camera.rotate(x, y, z, fixedAxis, setting.fixedLightPos);
	}

	/**
	 * Move camera position.
	 * 
	 * @param lat
	 * @param lng
	 */
	public void moveCamera(float lat, float lng, float roll) {
		camera.moveLatLng(lat, lng, roll);
	}

	/***
	 * Move light position.
	 * 
	 * @param lat
	 * @param lng
	 */
	public void moveLight(float lat, float lng) {
		camera.moveLightLatLng(lat, lng);
	}

	/***
	 * Rotate light.
	 * 
	 * @param dx
	 * @param dy
	 */
	public void rotateLight(float dx, float dy) {
		camera.rotateLight(dx, dy, 0);
	}

	/***
	 * Return the position of the light in latitude and longitude.
	 * 
	 * @return latitude and longitude
	 */
	public LatLon getLightLatLon() {
		return camera.getLightLatLng();
	}

	public void setFov(float fov) {
		camera.fov = Math.toRadians(fov);
	}

	/***
	 * Render.
	 * 
	 * @param gl
	 * @throws Exception
	 */
	public void render(GL2 gl, Textures textures, int view) throws Exception {
		Models models = scene.getRegisteredModels();
		if (!models.isCompiled()) {
			return;
		}
		if (currentModel == null) {
			return;
		}
		textures.bind(gl, currentModel, view);
		// error check
		int err = gl.glGetError();
		if (err != GL.GL_NO_ERROR) {
			throw new Exception("gl error: " + err);
		}
		renderMain(gl, textures, view);
		textures.unbind(gl);
		// error check
		err = gl.glGetError();
		if (err != GL.GL_NO_ERROR) {
			throw new Exception("gl error: " + err);
		}
		renderOthers(gl);
	}

	/***
	 * Render main.
	 * 
	 * @param gl
	 * @throws Exception
	 */
	private void renderMain(GL2 gl, Textures textures, int view) throws Exception {

		Models models = scene.getRegisteredModels();
		if (!models.isCompiled()) {
			return;
		}
		if (currentModel == null)
			return;

		// change spectrum
		changeSpectrum(currentSpectrum);

		// selected polygon
		for (int polygonID : polygonIDs) {
			float prevRGBA[] = prevRGBAs.get(polygonID);
			for (int k = 0; k < 3; k++) {
				currentModel.rgba[polygonID][k] = 1.0f - prevRGBA[k];
			}
		}

		// setup
		if (setting.isShading) {
			gl.glEnable(GL_LIGHTING);
			gl.glEnable(GL_LIGHT0);
		} else {
			gl.glDisable(GL_LIGHTING);
			gl.glDisable(GL_LIGHT0);
		}
		gl.glEnable(GL_COLOR_MATERIAL);
		gl.glColor3f(1, 1, 1);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);

		// get viewport
		gl.glGetIntegerv(GL_VIEWPORT, viewport, 0);

		// projection
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		float aspect = (float) ((double) viewport[2] / viewport[3]);
		float dist = camera.getDistance();
		if (setting.isPerspective) {
			glu.gluPerspective(Math.toDegrees(camera.fov), aspect, dist / 100, dist * 1.5);
		} else {
			double hh = dist * Math.tan(camera.fov / 2f);
			double ww = hh * aspect;
			gl.glOrtho(-ww, ww, -hh, hh, 0.3f, dist * 1.5);
		}
		

		// modelview
		gl.glMatrixMode(GL_MODELVIEW);
		double[] v = camera.lookatArray();
		gl.glLoadMatrixd(v, 0);
		
		// light
		float lightPos[] = new float[3];
		camera.light.get(lightPos);
		gl.glLightfv(GL_LIGHT0, GL_POSITION, lightPos, 0);
		gl.glLightfv(GL_LIGHT0, GL_SPECULAR, light.getSpecular(), 0);
		gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, light.getDiffuse(), 0);
		gl.glLightfv(GL_LIGHT0, GL_AMBIENT, light.getAmbient(), 0);

		// light direction
		if (setting.displayLightDirection) {
			gl.glLineWidth(3);
			gl.glColor3f(255, 100, 0);
			{
				gl.glBegin(GL_LINES);
				gl.glVertex3f(0f, 0f, 0f);
				gl.glVertex3f(lightPos[0], lightPos[1], lightPos[2]);
				gl.glEnd();
			}
		}

		// draw frustum
		textures.drawLine(gl, view);

		// get modelview and projection matrix
		gl.glGetDoublev(GL_MODELVIEW_MATRIX, modelview, 0);
		gl.glGetDoublev(GL_PROJECTION_MATRIX, projection, 0);

		gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
		if (setting.isPerspective) {
			gl.glPolygonOffset(1f, 1f);
		} else {
			gl.glPolygonOffset(0.8f, 0.8f);
		}
		

		// draw model
		renderModel(gl, models);
		
		gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);

		// error check
		int err = gl.glGetError();
		if (err != GL.GL_NO_ERROR) {
			throw new Exception("gl error: " + err);
		}
	}

	/***
	 * Render others.
	 * 
	 * @param gl
	 * @throws Exception
	 */
	private void renderOthers(GL2 gl) throws Exception {
		Models models = scene.getRegisteredModels();
		if (!models.isCompiled()) {
			return;
		}
		if (currentModel == null)
			return;

		// draw grid
		if (setting.displayLatLonGrid) {
			renderGrid(gl, currentModel);
		}

		// draw additional 3D
		if (setting.displayAdditional3D) {
			renderAdditional3D(gl);
		}

		// draw axis
		if (setting.displayAxis) {
			renderAxis(gl);
		}

		// draw color-bar
		if (setting.displayColorBar) {
			gl.glMatrixMode(GL_PROJECTION);
			gl.glLoadIdentity();
			float aspect = (float) ((double) viewport[2] / viewport[3]);
			glu.gluPerspective(7.6f, aspect, 0.3f, 300f);
			gl.glMatrixMode(GL_MODELVIEW);
			gl.glLoadIdentity();
			scene.colorbar.draw(gl, viewport[2], viewport[3], scene, this);
		}

		// error check
		int err = gl.glGetError();
		if (err != GL.GL_NO_ERROR) {
			throw new Exception("gl error: " + err);
		}

		// clear selected polygon
		for (int polygonID : polygonIDs) {
			float prevRGBA[] = prevRGBAs.get(polygonID);
			for (int k = 0; k < 3; k++) {
				currentModel.rgba[polygonID][k] = prevRGBA[k];
			}
		}
	}

	/***
	 * Render model.
	 * 
	 * @param gl
	 * @throws Exception
	 */
	private void renderModel(GL2 gl, Models models) {
		if (!models.isCompiled())
			return;

		// changing color
		gl.glBindBuffer(GL_ARRAY_BUFFER, currentModel.getVboId(Const.BUFFERTYPE_COLOR));
		ByteBuffer ptr = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);
		if (currentModel.rewriteColor(ptr)) {
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		}

		// material parameters
		gl.glMaterialfv(GL_FRONT, GL_SPECULAR, currentModel.getSpecular(), 0);
		gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, currentModel.getDiffuse(), 0);
		gl.glMaterialfv(GL_FRONT, GL_AMBIENT, currentModel.getAmbient(), 0);
		gl.glMaterialf(GL_FRONT, GL_SHININESS, currentModel.getShiness());

		gl.glEnableClientState(GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL_COLOR_ARRAY);

		// bind buffers
		gl.glBindBuffer(GL_ARRAY_BUFFER, currentModel.getVboId(Const.BUFFERTYPE_VERTEX));
		gl.glVertexPointer(3, GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, currentModel.getVboId(Const.BUFFERTYPE_NORMAL));
		gl.glNormalPointer(GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, currentModel.getVboId(Const.BUFFERTYPE_COLOR));
		gl.glColorPointer(4, GL_FLOAT, 0, 0);

		// draw
		int num = 3 * currentModel.getNFace();
		gl.glDrawArrays(GL_TRIANGLES, 0, num);

		gl.glDisableClientState(GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL_COLOR_ARRAY);

	}

	/**
	 * Render grid.
	 * 
	 * @param gl
	 * @param model
	 */
	private void renderGrid(GL2 gl, Model model) {
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL_COLOR_ARRAY);

		int num = 3 * model.getNFace();
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_COLOR_MATERIAL);
		gl.glDisable(GL_LIGHT0);
		gl.glEnable(GL_BLEND);

		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glLineWidth(0.5f);
		Grids grids = model.getGrids();
		int last = 0;
		Grids.Grid[] list = { grids.r, grids.w };
		for (Grids.Grid grid : list) {
			for (int i = 0; i < grid.zeroIndex.size(); i++) {
				int index = grid.zeroIndex.get(i);
				int count = index - last - 1;
				if (count > 0) {
					gl.glDrawArrays(GL_LINE_STRIP, num + last + 1, count);
				}
				last = index;
			}
			last = 0;
			num += grid.size;
		}

		gl.glDisable(GL_LINE_SMOOTH);
		gl.glDisable(GL_BLEND);

		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_COLOR_MATERIAL);
		gl.glEnable(GL_LIGHT0);

		gl.glDisableClientState(GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL_COLOR_ARRAY);
	}

	/***
	 * Render additional 3D.
	 * 
	 * @param gl
	 */
	private void renderAdditional3D(GL2 gl) {

		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_COLOR_MATERIAL);
		gl.glDisable(GL_LIGHT0);
		gl.glEnable(GL_BLEND);

		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		for (General3D general : scene.generals) {
			if (!general.active)
				continue;

			gl.glLineWidth(general.lineSize);

			for (float[][] line : general.lines) {
				gl.glBegin(GL_LINE_STRIP);
				// color
				float[] lineColor = general.lineColor;
				gl.glColor4d(lineColor[0], lineColor[1], lineColor[2], lineColor[3]);
				// line
				for (int i = 0; i < line.length; i++) {
					// not draw origin
					float[] l = line[i];
					if (!(l[0] == 0.0 && l[1] == 0.0 && l[2] == 0.0)) {
						gl.glVertex3f(l[0], l[1], l[2]);
					}
				}
				gl.glEnd();
			}
		}

		gl.glDisable(GL_LINE_SMOOTH);
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_COLOR_MATERIAL);
		gl.glEnable(GL_LIGHT0);
	}

	/**
	 * Render axis.
	 * 
	 * @param gl
	 */
	public void renderAxis(GL2 gl) {

		gl.glDisable(GL_DEPTH_TEST);
		gl.glLineWidth(3);

		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glTranslatef(0.85f, 0.85f, 0f);

		float aspect = (float) ((double) viewport[3] / viewport[2]);
		gl.glScaled(aspect, 1f, 1f);

		Matrix4d lookat = camera.lookat();
		lookat.setTranslation(new Vector3d());
		gl.glMultMatrixd(camera.matrixToArray(lookat), 0);

		// draw axes
		gl.glBegin(GL2.GL_LINES);
		// x axis
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.1f, 0.0f, 0.0f);
		// y axis
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.1f, 0.0f);
		// z axis
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.1f);
		gl.glEnd();
		// title
		lookat.invert();
		double[] invertMtx = camera.matrixToArray(lookat);
		String[] axTitles = { "X", "Z", "Y" };
		float[] pos = { 0.08f, 0.01f, 0.01f, 0.08f, 0.01f };
		for (int i = 0; i < 3; i++) {
			gl.glPushMatrix();
			tr.begin3DRendering();
			tr.setColor(1.0f, 1.0f, 0.0f, 1.0f);
			gl.glTranslatef(pos[i], pos[i + 1], pos[i + 2]);
			gl.glMultMatrixd(invertMtx, 0);
			tr.draw3D(axTitles[i], 0f, 0f, 0f, 0.0015f);
			tr.end3DRendering();
			gl.glPopMatrix();
		}

	}

}
