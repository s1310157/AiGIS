package aigis.gl;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import static com.jogamp.opengl.GL2ES3.GL_READ_WRITE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW_MATRIX;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_TEXTURE_COORD_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import aigis.Const;
import aigis.Logger;
import aigis.model.CameraInfo;
import aigis.model.Model;
import aigis.model.Models;
import aigis.model.loader.FastReader;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.ImageHDU;

/**
 * Texture
 */
public class Textures {

	private static GLU glu = new GLU();

	private static final int TEXSIZE = 1024;
	private static final int DEPTH_SIZE = 512;

	/**
	 * Texture settings
	 */
	public class Setting {
		public CameraInfo info;
		public Texture tex;
		public File imageFile;
		public File infoFile;
		public boolean needReloadTex = true;
		public boolean[] active = { true, true, true, true };
		public boolean showFrustum = true;
		public boolean removeFlg = false;
		public Integer uvIndex = null;
		public Image img;
		public int rotate;
		public int flip;
	}

	/** List of textures */
	private ArrayList<Setting> texs = new ArrayList<>();
	/** List of UV VBO indices */
	private LinkedList<Integer> uvs = new LinkedList<>();
	/** Max texture units */
	private int maxTex = 0;
	/** Whether init(gl) has been called */
	private boolean initialized = false;

	/**
	 * Create textures without GL. Call init(gl) before use.
	 * A Scene owns its Textures, but the GL context is not
	 * available when a Scene is created.
	 */
	public Textures() {
	}

	public Textures(GL2 gl) {
		init(gl);
	}

	/**
	 * Initialize with the GL context. Does nothing after the first call.
	 *
	 * @param gl
	 */
	public void init(GL2 gl) {
		if (initialized) {
			return;
		}
		initialized = true;
		int max[] = new int[1];
		gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, max, 0);
		maxTex = max[0];
		Logger.Debug("MaxTextureUnits: " + maxTex);
		gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_SIZE, max, 0);
		Logger.Debug("MaxTextureSize: " + max[0]);
		Logger.Debug("isNPOTTextureAvailable: " + (gl.isNPOTTextureAvailable() ? "true" : "false"));

		// for ATI
		TextureIO.setTexRectEnabled(false);
		Logger.Debug("isTexRectEnabled: " + (TextureIO.isTexRectEnabled() ? "true" : "false"));

		boolean npot = gl.isExtensionAvailable("GL_ARB_texture_non_power_of_two");
		Logger.Debug("GL_ARB_texture_non_power_of_two: " + (npot ? "true" : "false"));
		if (!npot) {
			JOptionPane.showMessageDialog(null, "'GL_ARB_texture_non_power_of_two' is not available. \n", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}

		for (int i = 1; i < maxTex; i++) {
			uvs.offer(i);
		}
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<Setting> getTexsInfo() {
		return texs;
	}

	/***
	 * Release all texture objects.
	 *
	 * @param gl
	 */
	public void dispose(GL2 gl) {
		for (Setting st : texs) {
			if (st.tex != null) {
				st.tex.destroy(gl);
				st.tex = null;
			}
		}
		texs.clear();
	}

	/**
	 * 
	 * @return
	 */
	public boolean canAddTexture() {
		return texs.size() < (maxTex - 1);
	}

	/**
	 * Add texture.
	 * 
	 * @param imageFile
	 * @param infoFile
	 * @throws Exception
	 */
	public void addTexture(File imageFile, File infoFile, int rotate, int flip) throws Exception {
		Setting st = new Setting();
		st.imageFile = imageFile;
		st.infoFile = infoFile;
		st.rotate = rotate;
		st.flip = flip;
		if (infoFile != null) {
			String path = infoFile.getPath();
			if (path.toLowerCase().endsWith(".sum")) {
				CameraInfo info = new CameraInfo();
				FastReader scan = new FastReader(path);
				int imageSize[] = null;
				float focalLength = 0;
				while (true) {
					String str = scan.nextString();
					if (str == null)
						break;
					str = str.toUpperCase();
					// Logger.Debug(str);
					if (str.contains(":") && str.contains(".")) {
						int w = scan.nextInt();
						int h = scan.nextInt();
						imageSize = new int[] { w, h };
						// Logger.Debug("w: " + w + " h: " + h);
					}
					if (str.equals("THRSH")) {
						focalLength = scan.nextFloat();
						// Logger.Debug("focalLength: " + focalLength);
					}
					if (str.equals("CTR")) {
						float x = scan.nextFloat() * -1;
						float y = scan.nextFloat() * -1;
						float z = scan.nextFloat() * -1;
						// Logger.Debug("spacecraft_position: " + x + ", " + y + ", " + z);
						info.position.set(x, y, z);
					}
					if (str.equals("SCOBJ")) {
						float x = scan.nextFloat();
						float y = scan.nextFloat();
						float z = scan.nextFloat();
						// Logger.Debug("up_direction: " + x + ", " + y + ", " + z);
						info.up.set(x, y, z);
					}
					if (str.equals("CY")) {
						float x = scan.nextFloat();
						float y = scan.nextFloat();
						float z = scan.nextFloat();
						// Logger.Debug("boresight_direction: " + x + ", " + y + ", " + z);
						info.direction.set(x, y, z);
					}
					if (str.equals("SZ")) {
						if (imageSize == null || focalLength == 0) {
							scan.close();
							throw new RuntimeException("data format is incorrect");
						}
						scan.nextFloat();
						scan.nextFloat();
						scan.nextFloat();
						scan.nextFloat();
						float y = scan.nextFloat();
						// Logger.Debug("k-mtx: " + x + ", " + y);

						info.fov = 2 * Math.atan((1 / y * imageSize[1]) / (2 * focalLength));
						st.info = info;
						break;
					}
				}
				scan.close();
			} else {
				st.info = CameraInfo.loadFromInfo(path);
			}
		}
		texs.add(st);
	}

	/**
	 * Remove texture.
	 * 
	 * @param index
	 */
	public void removeTexture(int index) {
		Setting st = texs.get(index);
		st.removeFlg = true;
		if (st.uvIndex != null && !uvs.contains(st.uvIndex))
			uvs.offerFirst(st.uvIndex);
	}

	/**
	 * Clear all textures.
	 */
	public void clearTextures() {
		for (int i = 0; i < texs.size(); i++) {
			removeTexture(i);
		}
	}

	/**
	 * Activate texture.
	 * 
	 * @param index
	 * @param view
	 * @param active
	 */
	public void activate(int index, int view, boolean active) {
		Setting st = texs.get(index);
		st.active[view] = active;
		if (active) {
			if (st.tex == null) {
				st.needReloadTex = true;
			}
		}
	}

	/**
	 * Change order.
	 * 
	 * @param index
	 */
	public void upOrder(int index) {
		Collections.swap(texs, index, index - 1);
	}

	/**
	 * Bind all textures.
	 * 
	 * @param gl
	 * @param model
	 * @param view
	 */
	public void bind(GL2 gl, Model model, int view) {

		for (int i = 0; i < texs.size(); i++) {
			Setting st = texs.get(i);
			if (st.tex != null) {
				int offset = i + 1;
				gl.glActiveTexture(GL.GL_TEXTURE0 + offset);
				if (!st.active[view]) {
					st.tex.disable(gl);
					gl.glActiveTexture(GL.GL_TEXTURE0);
					continue;
				}

				st.tex.bind(gl);
				st.tex.enable(gl);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);
				gl.glClientActiveTexture(GL.GL_TEXTURE0 + offset);
				gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
				if (st.infoFile == null) {
					gl.glBindBuffer(GL_ARRAY_BUFFER, model.getVboId(Const.BUFFERTYPE_UV));
				} else {
					gl.glBindBuffer(GL_ARRAY_BUFFER, model.getVboId(Const.BUFFERTYPE_UV + st.uvIndex));
				}
				gl.glTexCoordPointer(2, GL_FLOAT, 0, 0);
			}
		}
	}

	/**
	 * Unbind all textures.
	 * 
	 * @param gl
	 */
	public void unbind(GL2 gl) {
		for (int i = 0; i < texs.size(); i++) {
			Setting st = texs.get(i);
			if (st.tex != null) {
				gl.glActiveTexture(GL.GL_TEXTURE0 + i + 1);
				st.tex.disable(gl);
			}
		}
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glClientActiveTexture(GL.GL_TEXTURE0);
	}

	//
	public void drawLine(GL2 gl, int view) {
		for (int i = 0; i < texs.size(); i++) {
			Setting st = texs.get(i);
			if (st.infoFile != null) {
				if (!st.active[view]) {
					continue;
				}
				if (!st.showFrustum) {
					continue;
				}
				CameraInfo info = st.info.clone();
				Vector3d frustum[] = info.getFrustum();
				double dist = info.position.distance(new Point3d());
				
				gl.glLineWidth(1);
				gl.glColor3f(0, 255, 0);
				{
					gl.glBegin(GL_LINES);
					for (int j = 0; j < 4; j++) {
						Vector3d vec = new Vector3d(info.position);
						frustum[j].scale(dist);
						vec.add(frustum[j]);
						gl.glVertex3f((float)info.position.x, (float)info.position.y, (float)info.position.z);
						gl.glVertex3f((float)vec.x, (float)vec.y, (float)vec.z);
					}
					gl.glEnd();
				}
			}
		}
	}

	/**
	 * Initialization.
	 * 
	 * @param gl
	 * @param models
	 * @throws Exception
	 */
	public void initTexture(GL2 gl, Models models) throws Exception {
		Iterator<Setting> it = texs.iterator();
		while (it.hasNext()) {
			Setting st = it.next();

			if (st.removeFlg) {
				if (st.tex != null) {
					st.tex.destroy(gl);
					st.tex = null;
				}
				it.remove();
				continue;
			}

			if (st.active[0] || st.active[1] || st.active[2] || st.active[3]) {
				initTexture(gl, models, st);
			}
		}
	}

	/**
	 * Initialization.
	 * 
	 * @param gl
	 * @param models
	 * @param st
	 * @throws Exception
	 */
	private void initTexture(GL2 gl, Models models, Setting st) throws Exception {

		if (!st.needReloadTex) {
			return;
		}
		int err = gl.glGetError();
		if (err != GL.GL_NO_ERROR) {
			throw new Exception("gl error: " + err);
		}

		try {
			String name = st.imageFile.getName().toLowerCase();
			boolean isFits = name.endsWith(".fit") || name.endsWith(".fits");

			BufferedImage img;
			if (isFits) {
				img = loadFits(st.imageFile);
			} else {
				img = ImageIO.read(st.imageFile);
			}
			if (st.rotate > 0) {
				img = rotateImage(st.rotate, img);
			}
			if (st.flip > 0) {
				img = flipImage(st.flip, img);
			}
			st.img = img;

			gl.glActiveTexture(GL.GL_TEXTURE0 + texs.size());
			if (st.infoFile == null) {
				// copy preview image
				ColorModel cm = img.getColorModel();
				boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
				WritableRaster raster = img.copyData(null);
				BufferedImage dimg = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
				st.img = dimg;
				// flip texture
				ImageUtil.flipImageVertically(img);
				st.tex = AWTTextureIO.newTexture(gl.getGLProfile(), img, true);
				st.tex.setTexParameteri(gl, GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				st.tex.setTexParameteri(gl, GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			} else {
				// 画像の大きさをTEXSIZEに合わせる
				int h = img.getHeight();
				int w = img.getWidth();
				int rh = TEXSIZE - 2;
				int rw = (int) (w / (double) h * rh);
				Image tmp = img.getScaledInstance(rw, rh, Image.SCALE_SMOOTH);
				BufferedImage dimg = new BufferedImage(TEXSIZE, TEXSIZE, BufferedImage.TYPE_INT_ARGB);
				// Put 1px space on the outer & Aspect ratio adjustment
				Graphics2D g2d = dimg.createGraphics();
				int d = rh / 2 - rw / 2;
				g2d.drawImage(tmp, 1 + d, 1, null);
				g2d.dispose();
				dimg.flush();

				// 画像と座標軸を合わせるために反転
				ImageUtil.flipImageVertically(dimg);
				
				// テクスチャを作成
				st.tex = AWTTextureIO.newTexture(gl.getGLProfile(), dimg, true);
				// UVマップを作成
				st.uvIndex = uvs.poll();
				for (Model model : models.getAllModels()) {
					makeUV(gl, model, st);
				}
			}
			gl.glEnable(GL.GL_TEXTURE_2D);
			st.tex.setTexParameteri(gl, GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			st.tex.setTexParameteri(gl, GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		} catch (Exception e) {
			st.tex = null;
			st.needReloadTex = false;
			if (st.uvIndex != null && !uvs.contains(st.uvIndex))
				uvs.offerFirst(st.uvIndex);
			texs.remove(st);
			throw e;
		}
		gl.glActiveTexture(GL.GL_TEXTURE0);
		st.needReloadTex = false;

		err = gl.glGetError();
		if (err != GL.GL_NO_ERROR) {
			throw new Exception("gl error: " + err);
		}
	}

	/**
	 * Rotate image
	 * 
	 * @param rotate
	 * @param img
	 * @return
	 */
	private BufferedImage rotateImage(int angle, BufferedImage img) {
		double rads = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
		int w = img.getWidth();
		int h = img.getHeight();
		int newWidth = (int) Math.floor(w * cos + h * sin);
		int newHeight = (int) Math.floor(h * cos + w * sin);

		BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = rotated.createGraphics();
		AffineTransform at = new AffineTransform();
		at.translate((newWidth - w) / 2, (newHeight - h) / 2);

		int x = w / 2;
		int y = h / 2;

		at.rotate(rads, x, y);
		g2d.setTransform(at);
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();

		return rotated;
	}

	/**
	 * Flip image
	 * 
	 * @param flip 1:vertically 2:horizontally
	 * @param img
	 * @return
	 */
	private BufferedImage flipImage(int flip, BufferedImage img) {
		int s = 1;
		double x, y;
		if (flip == 1) {
			x = 0;
			y = -img.getHeight(null);
		} else {
			s = -1;
			x = -img.getWidth(null);
			y = 0;
		}
		AffineTransform transform = AffineTransform.getScaleInstance(s, -s);
		transform.translate(x, y);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(img, null);
	}

	/**
	 * Load image from Fits file.
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private BufferedImage loadFits(File file) throws Exception {
		Fits fits = new Fits(file);
		fits.read();
		// Logger.Debug("fits hdu:" + fits.getNumberOfHDUs());

		for (int i = 0; i < fits.getNumberOfHDUs(); i++) {
			BasicHDU<?> hdu = fits.getHDU(i);
			if (!(hdu instanceof ImageHDU))
				continue;
			int axes[] = hdu.getAxes();
			if (axes == null || axes.length != 2) {
				continue;
			}
			// Logger.Debug("ax1:" + axes[0] + " ax2:" + axes[1]);
			int fdata[] = null;
			Object raw = hdu.getData().getData();
			// Logger.Debug("fits:" + raw.toString());
			if (raw instanceof byte[][]) {
				fdata = new int[axes[0] * axes[1]];
				byte data[][] = (byte[][]) raw;
				int max = Byte.MIN_VALUE;
				int min = Byte.MAX_VALUE;
				for (int j = 0; j < data.length; j++) {
					byte d[] = data[j];
					for (int k = 0; k < d.length; k++) {
						int v = data[j][k] & 0xFF;
						if (v > max) {
							max = v;
						}
						if (v < min) {
							min = v;
						}
					}
				}
				// Logger.Debug("max:" + max + " min:" + min);
				for (int j = 0; j < data.length; j++) {
					byte d[] = data[j];
					for (int k = 0; k < d.length; k++) {
						int idx = j * d.length + k;
						fdata[idx] = (int) (((data[j][k] & 0xFF) - min) / (double) (max - min) * 255);
					}
				}
			} else if (raw instanceof short[][]) {
				fdata = new int[axes[0] * axes[1]];
				short data[][] = (short[][]) raw;
				short max = Short.MIN_VALUE;
				short min = Short.MAX_VALUE;
				for (int j = 0; j < data.length; j++) {
					short d[] = data[j];
					for (int k = 0; k < d.length; k++) {
						short v = data[j][k];
						if (v > max) {
							max = v;
						}
						if (v < min) {
							min = v;
						}
					}
				}
				// Logger.Debug("max:" + max + " min:" + min);
				for (int j = 0; j < data.length; j++) {
					short d[] = data[j];
					for (int k = 0; k < d.length; k++) {
						int idx = j * d.length + k;
						fdata[idx] = (int) (((double) data[j][k] - min) / ((double) max - min) * 255);
					}
				}
			} else if (raw instanceof float[][]) {
				fdata = new int[axes[0] * axes[1]];
				float data[][] = (float[][]) raw;
				float max = Float.MIN_VALUE;
				float min = Float.MAX_VALUE;
				for (int j = 0; j < data.length; j++) {
					float d[] = data[j];
					for (int k = 0; k < d.length; k++) {
						float v = data[j][k];
						if (v > max) {
							max = v;
						}
						if (v < min) {
							min = v;
						}
					}
				}
				// Logger.Debug("max:" + max + " min:" + min);
				for (int j = 0; j < data.length; j++) {
					float d[] = data[j];
					for (int k = 0; k < d.length; k++) {
						int idx = j * d.length + k;
						fdata[idx] = (int) ((data[j][k] - min) / (max - min) * 255);
					}
				}
			} else if (raw instanceof double[][]) {
				fdata = new int[axes[0] * axes[1]];
				double data[][] = (double[][]) raw;
				double max = Double.MIN_VALUE;
				double min = Double.MAX_VALUE;
				for (int j = 0; j < data.length; j++) {
					double d[] = data[j];
					for (int k = 0; k < d.length; k++) {
						double v = data[j][k];
						if (v > max) {
							max = v;
						}
						if (v < min) {
							min = v;
						}
					}
				}
				// Logger.Debug("max: " + max + " min: " + min);
				for (int j = 0; j < data.length; j++) {
					double d[] = data[j];
					for (int k = 0; k < d.length; k++) {
						int idx = j * d.length + k;
						fdata[idx] = (int) ((data[j][k] - min) / (max + min) * 255);
					}
				}
			} else {
				fits.close();
				throw new RuntimeException("not supported data type");
			}
			BufferedImage image = new BufferedImage(axes[1], axes[0], BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster raster = image.getRaster();
			raster.setPixels(0, 0, axes[1], axes[0], fdata);

			// ImageIO.write(image, "png", new File(file.getParentFile().getAbsolutePath() +
			// "/test.png"));
			fits.close();
			return image;
		}
		fits.close();
		throw new RuntimeException("not supported image format");
	}

	/**
	 * Create UV Map.
	 * 
	 * @param gl
	 * @param models
	 * @param st
	 * @param currentModel
	 */
	private void makeUV(GL2 gl, Model model, Setting st) {

		if (model == null)
			return;

		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		// 現在のViewPortを退避しておく
		int[] viewport = new int[4];
		gl.glGetIntegerv(GL_VIEWPORT, viewport, 0);
		// DEPTH_SIZEでViewPortを作成
		gl.glViewport(0, 0, DEPTH_SIZE, DEPTH_SIZE);

		// 変換行列を初期化
		gl.glMatrixMode(GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		// 座標軸を合わせるために回転
		gl.glRotated(-90, 0, 0, 1);

		// 視錐台を作成
		//CameraInfo info = st.info;
		//glu.gluPerspective(Math.toDegrees(info.fov), 1.0, 0.001, 500);
		// 視錐台を作成（余裕を持たせてモデルの直径*2の大きさにしてあります）
		CameraInfo info = st.info;
		double size = model.getSize();
		double dist = info.getDistance();
		double near = dist - size;
		double far = dist + size;
		if (near < 0) {
			near = 0.001;
			}
		glu.gluPerspective(Math.toDegrees(info.fov), 1.0, near, far);

		// カメラの位置と向き
		Vector3d vpos = new Vector3d(info.position);
		Vector3d vat = new Vector3d();
		vat.add(vpos, info.direction);
		glu.gluLookAt(vpos.x, vpos.y, vpos.z, vat.x, vat.y, vat.z, info.up.x, info.up.y, info.up.z);

		// 深度テストの有効化と不要なオプションの解除
		gl.glColorMask(false, false, false, false);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL_LIGHTING);
		gl.glCullFace(GL_BACK);
		gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);

		// モデル座標変換行列を取得
		double depthMv[] = new double[16];
		gl.glGetDoublev(GL_MODELVIEW_MATRIX, depthMv, 0);

		// 描画
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		gl.glBindBuffer(GL_ARRAY_BUFFER, model.getVboId(Const.BUFFERTYPE_VERTEX));
		gl.glVertexPointer(3, GL_FLOAT, 0, 0);
		int num = 3 * model.getNFace();
		gl.glDrawArrays(GL_TRIANGLES, 0, num);
		gl.glFinish();
		
		// UV用のVBOのメモリ空間を書き込み用として取得
		gl.glBindBuffer(GL_ARRAY_BUFFER, model.getVboId(Const.BUFFERTYPE_UV + st.uvIndex));
		ByteBuffer ptr = gl.glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE);

		// 深度情報を取得
		FloatBuffer winZ = FloatBuffer.allocate(DEPTH_SIZE * DEPTH_SIZE);
		gl.glReadPixels(0, 0, DEPTH_SIZE, DEPTH_SIZE, GL2.GL_DEPTH_COMPONENT, GL.GL_FLOAT, winZ);

		// ViewPortと射影変換行列（ここでは単位行列）を準備
		int vp[] = { 0, 0, DEPTH_SIZE, DEPTH_SIZE };
		double pj[] = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
		// UVを作成（書き込み用VBOのポインタ、カメラ位置、深度情報、モデル座標変換行列、射影変換行列、ViewPort）
		model.makeUV(ptr, vpos, winZ, depthMv, pj, vp);
		gl.glUnmapBuffer(GL_ARRAY_BUFFER);

		// 後始末
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
		gl.glMatrixMode(GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glColorMask(true, true, true, true);
		gl.glEnable(GL_LIGHTING);
		gl.glCullFace(GL_BACK);
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

	}

}
