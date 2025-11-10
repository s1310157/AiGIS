package aigis.ui;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.FloatBuffer;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JOptionPane;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;

import aigis.Logger;
import aigis.Scene;
import aigis.gl.Renderer;
import aigis.gl.Textures;
import aigis.model.CameraInfo;
import aigis.model.LatLon;
import aigis.model.ModelSelection.Resolution;
import aigis.model.Models;

/***
 * 4 parts window.
 */
public class GLSplitWindow {

	public abstract interface EventListener {
		public abstract void cameraMoved(CameraInfo info);

		public abstract void lightMoved(LatLon latlon);

		public abstract void polygonSelected(int polygonID, LatLon info);

		public abstract void screenChanged(int index);
	}

	public boolean isSync = true;

	private GL2 gl;
	private Renderer renderers[] = new Renderer[4];
	public Textures textures;
	private GLJPanel glPanel = null;
	private int division = 1;
	private Dimension divideSize;
	private int currentScreen = 0;
	private EventListener eventListener;
	private Point prevMousePos = new Point();
	private float viewScales[] = { 1, 1 };
	private boolean withoutFrame = false;
	private Character fixedAxis = null;

	private Scene scene;

	/***
	 * constructor
	 */
	public GLSplitWindow(GLJPanel glPanel, Scene scene) {
		this.glPanel = glPanel;
		this.scene = scene;

		// mouse events
		glPanel.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int index = getScreenIndex(e.getPoint());
				if (isSync) {
					for (int i = 0; i < division; i++) {
						renderers[i].scale(-e.getWheelRotation());
					}
				} else {
					renderers[index].scale(-e.getWheelRotation());
				}
				if (eventListener != null) {
					eventListener.cameraMoved(renderers[index].getCameraInfo());
				}
				setScreenIndex(index);
				glPanel.repaint();
			}
		});

		glPanel.setFocusable(true);
		glPanel.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				// Logger.Debug(e.getKeyCode() + "");
				// shift key
				if (e.getKeyCode() == 16) {
					renderers[currentScreen].setting.displayLightDirection = true;
				}
				if (e.getKeyCode() >= 88 && e.getKeyCode() <= 90) {
					fixedAxis = e.getKeyChar();
				}
				glPanel.repaint();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				for (int i = 0; i < division; i++) {
					renderers[i].setting.displayLightDirection = false;
				}
				fixedAxis = null;
				glPanel.repaint();
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

		});

		glPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				int index = getScreenIndex(e.getPoint());
				if (renderers[index] == null) {
					return;
				}
				setScreenIndex(index);
				if (e.isAltDown()) {
					// scale
					float scale = y - prevMousePos.y;
					if (isSync) {
						for (int i = 0; i < division; i++) {
							renderers[i].scale(scale);
						}
					} else {
						renderers[index].scale(scale);
					}
					if (eventListener != null) {
						eventListener.cameraMoved(renderers[index].getCameraInfo());
					}
				} else if (e.isControlDown()) {
					// move
					float diffX = (float) (x - prevMousePos.x) / 10.0f;
					float diffY = (float) (prevMousePos.y - y) / 10.0f;
					if (isSync) {
						for (int i = 0; i < division; i++) {
							renderers[i].move(-diffX, -diffY);
						}
					} else {
						renderers[index].move(-diffX, -diffY);
					}
				} else if (e.isShiftDown()) {
					// light
					float dy = (x - prevMousePos.x);
					float dx = (y - prevMousePos.y);
					if (isSync) {
						for (int i = 0; i < division; i++) {
							renderers[i].rotateLight(dx, dy);
						}
					} else {
						renderers[index].rotateLight(dx, dy);
					}
					for (int i = 0; i < division; i++) {
						renderers[i].setting.displayLightDirection = currentScreen == i;
					}
					if (eventListener != null) {
						eventListener.lightMoved(renderers[index].getLightLatLon());
					}
				} else {
					// rotate
					float dy = prevMousePos.x - x;
					float dx = prevMousePos.y - y;
					float dz = 0;
					if (fixedAxis != null) {
						switch (fixedAxis) {
						case 'x':
						case 'X':
							dy = 0;
							break;
						case 'y':
						case 'Y':
							dx = 0;
							break;
						case 'z':
						case 'Z':
							dz = dx + dy;
							dx = 0;
							dy = 0;
							break;
						}
					}

					if (isSync) {
						for (int i = 0; i < division; i++) {
							renderers[i].rotateObject(dx, dy, dz, fixedAxis != null);
						}
					} else {
						renderers[index].rotateObject(dx, dy, dz, fixedAxis != null);
					}
					if (eventListener != null) {
						eventListener.cameraMoved(renderers[index].getCameraInfo());
						if (renderers[index].setting.fixedLightPos) {
							eventListener.lightMoved(renderers[index].getLightLatLon());
						}
					}
				}
				prevMousePos.x = x;
				prevMousePos.y = y;
				glPanel.repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
		});

		glPanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int index = getScreenIndex(e.getPoint());
				if (index == currentScreen) {
					Point2D.Double mousePos = new Point2D.Double(e.getX(), glPanel.getHeight() - e.getY());
					int polygonID = renderers[index].getPointPolygonID(mousePos, viewScales);
					LatLon info = renderers[index].selectPolygon(polygonID, e.isControlDown());
					if (eventListener != null) {
						eventListener.polygonSelected(polygonID, info);
					}
				}
				setScreenIndex(index);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				prevMousePos.x = e.getX();
				prevMousePos.y = e.getY();
				int index = getScreenIndex(e.getPoint());
				if (scene.isChangeModel()) {
					if (isSync) {
						for (int i = 0; i < division; i++) {
							renderers[i].changeResolution(Resolution.Low);
						}
					} else {
						renderers[index].changeResolution(Resolution.Low);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				for (int i = 0; i < division; i++) {
					if (scene.isChangeModel()) {
						renderers[i].changeResolution(null);
					}
				}
				glPanel.repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				glPanel.requestFocus();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				for (int i = 0; i < division; i++) {
					renderers[i].setting.displayLightDirection = false;
				}
			}

		});

		// gl events
		glPanel.addGLEventListener(new GLEventListener() {

			@Override
			public void init(GLAutoDrawable drawable) {
				Logger.Debug("*GLの初期化");
				initgl(drawable);
			}

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
				Logger.Debug("*GLの再形成: x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);
				glPanel.getCurrentSurfaceScale(viewScales);
				Logger.Debug("*ビュースケール: x=" + viewScales[0] + ", y=" + viewScales[1]);
				divide(division);
			}

			@Override
			public void display(GLAutoDrawable drawable) {

				gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				gl.glEnable(GL.GL_TEXTURE_2D);
				gl.glActiveTexture(GL.GL_TEXTURE0);
				gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

				try {
					scene.compile(gl);
					Models models = scene.getRegisteredModels();

					try {
						textures.initTexture(gl, models);
					} catch (RuntimeException e) {
						Logger.Error(e);
						String msg = e.getMessage();
						JOptionPane.showMessageDialog(null, msg != null ? msg : e.getClass(), "エラー",
								JOptionPane.WARNING_MESSAGE);
					}

					gl.glMatrixMode(GL_MODELVIEW);

					for (int i = 0; i < division; i++) {
						setViewPort(i);
						renderers[i].render(gl, textures, i);
					}
					drawFrame();
				} catch (Exception e) {
					Logger.Error(e);
					String msg = e.getMessage();
					JOptionPane.showMessageDialog(null, msg != null ? msg : e.getClass(), "エラー",
							JOptionPane.ERROR_MESSAGE);
					System.exit(1);
					return;
				}
			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				Logger.Debug("*GLの破棄");
			}

		});
	}

	/***
	 * Listener to be notified about updates to a GLSplitWindow.
	 * 
	 * @param listener
	 */
	public void setEventListener(EventListener listener) {
		this.eventListener = listener;
	}

	/***
	 * Divide the screen into 1, 2, 4.
	 * 
	 * @param division
	 */
	public void divide(int division) {
		this.division = division;
		int screen = currentScreen;
		if (currentScreen + 1 > division) {
			screen = 0;
		}
		Dimension size = glPanel.getSize();
		if (division == 1) {
			divideSize = new Dimension(size);
		} else if (division == 2) {
			divideSize = new Dimension(size.width, size.height / 2);
		} else {
			divideSize = new Dimension(size.width / 2, size.height / 2);
		}
		for (int i = 0; i < division; i++) {
			if (renderers[i] == null) {
				renderers[i] = new Renderer(scene);
				renderers[i].resetSetting(true);
			}
			if (eventListener != null && screen == i) {
				eventListener.lightMoved(renderers[screen].getLightLatLon());
				eventListener.cameraMoved(renderers[screen].getCameraInfo());
			}
		}
		currentScreen = screen;
		if (eventListener != null) {
			eventListener.screenChanged(screen);
		}
		sync();
		glPanel.repaint();
	}

	/***
	 * Synchronize the camera with current renderer.
	 */
	public void sync() {
		for (int i = 0; i < division; i++) {
			if (currentScreen != i) {
				renderers[i].syncCamera(renderers[currentScreen]);
			}
		}
		glPanel.repaint();
	}

	/**
	 * Move camera position.
	 * 
	 * @param lat
	 * @param lng
	 */
	public void moveCamera(float lat, float lng, float roll) {
		if (isSync) {
			for (int i = 0; i < division; i++) {
				renderers[i].moveCamera(lat, lng, roll);
			}
		} else {
			renderers[currentScreen].moveCamera(lat, lng, roll);
		}
	}

	/***
	 * Move light position.
	 * 
	 * @param lat
	 * @param lng
	 */
	public void moveLight(float lat, float lng) {
		if (isSync) {
			for (int i = 0; i < division; i++) {
				renderers[i].moveLight(lat, lng);
			}
		} else {
			renderers[currentScreen].moveLight(lat, lng);
		}
	}

	/**
	 * Move the camera on z axis.
	 * 
	 * @param distance
	 */
	public void setCameraDistance(float distance) {
		if (isSync) {
			for (int i = 0; i < division; i++) {
				renderers[i].setCameraDistance(distance);
			}
		} else {
			renderers[currentScreen].setCameraDistance(distance);
		}
	}

	/**
	 * 
	 * 
	 * @param fov
	 */
	public void setFov(float fov) {
		if (isSync) {
			for (int i = 0; i < division; i++) {
				renderers[i].setFov(fov);
			}
		} else {
			renderers[currentScreen].setFov(fov);
		}
	}

	/***
	 * Get active renderer.
	 * 
	 * @return
	 */
	public Renderer getActiveRenderer() {
		return renderers[currentScreen];
	}

	/**
	 * Reset all renderers.
	 */
	public void resetRenderer(boolean resetCamera, double size) {
		for (int i = 0; i < division; i++) {
			renderers[i].resetSetting(resetCamera);
			if (resetCamera) {
				renderers[i].getCameraInfo().setDefaultSize(size);
			}
		}
		if (eventListener != null) {
			eventListener.screenChanged(currentScreen);
			eventListener.lightMoved(renderers[0].getLightLatLon());
			eventListener.cameraMoved(renderers[0].getCameraInfo());
		}
		glPanel.repaint();
	}

	/**
	 * Reset all colorbars.
	 */
	public void resetColorbar() {
		for (int i = 0; i < division; i++) {
			if (renderers[i] != null) {
				renderers[i].changeColorbar(0);
			}
		}
		glPanel.repaint();
	}

	/***
	 * Initialize opengl.
	 * 
	 * @param drawable
	 */
	private void initgl(GLAutoDrawable drawable) {
		Logger.Debug("JOGLを初期化中...");
		gl = drawable.getGL().getGL2();
		Logger.Debug("GL:" + gl.glGetString(GL.GL_VERSION));
		boolean enabledVBO = gl.isExtensionAvailable("GL_ARB_vertex_buffer_object");
		Logger.Debug("VBO:" + enabledVBO);
		Package pkg = Package.getPackage("com.jogamp.opengl");
		if (pkg == null) {
			pkg = Package.getPackage("javax.media.opengl");
		}
		if (pkg != null) {
			Logger.Debug("JOGL バージョン: " + pkg.getImplementationVersion());
		}
		FloatBuffer glGetBuf = com.jogamp.common.nio.Buffers.newDirectFloatBuffer(2);
		gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, glGetBuf);
		Logger.Debug("ライン幅: " + glGetBuf.get(0) + "~" + glGetBuf.get(1));
		textures = new Textures(gl);

		// VBOが使えない場合は終了
		if (enabledVBO == false) {
			JOptionPane.showMessageDialog(null, "VBOはサポートされていません\n", "エラー", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}
	}

	/***
	 * Get the screen index of the specified point.
	 * 
	 * @param p point
	 * @return index
	 */
	private int getScreenIndex(Point p) {
		int index = 0;
		if (division == 2) {
			if (p.y > divideSize.height) {
				index = 1;
			} else {
				index = 0;
			}
			return index;
		}
		if (p.x < divideSize.width) {
			if (p.y > divideSize.height) {
				index = 2;
			} else {
				index = 0;
			}
		} else {
			if (p.y > divideSize.height) {
				index = 3;
			} else {
				index = 1;
			}
		}
		return index;
	}

	/***
	 * Set the screen index.
	 * 
	 * @param index
	 */
	private void setScreenIndex(int index) {
		if (currentScreen == index) {
			return;
		}
		currentScreen = index;
		if (eventListener != null) {
			eventListener.lightMoved(renderers[currentScreen].getLightLatLon());
			eventListener.cameraMoved(renderers[currentScreen].getCameraInfo());
			eventListener.screenChanged(index);
		}
	}

	/***
	 * Set the viewport.
	 * 
	 * @param index
	 */
	private void setViewPort(int index) {
		int x = index % 2;
		int y = index / 2;
		if (division == 4) {
			y = 1 - y;
		}
		int w = divideSize.width * (int) viewScales[0];
		int h = divideSize.height * (int) viewScales[1];
		if (division == 2) {
			gl.glViewport(0, h * (1 - index), w, h);
		} else {
			gl.glViewport(w * x, h * y, w, h);
		}
	}

	/**
	 * Draw a frame indicating active.
	 */
	private void drawFrame() {
		if (withoutFrame) {
			return;
		}
		if (division == 1) {
			return;
		}
		setViewPort(currentScreen);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_LIGHTING);

		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glLineWidth(6 * viewScales[0]);
		gl.glColor3f(0, 0, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		{
			float pos = 0.995f;
			gl.glVertex2d(-pos, -pos);
			gl.glVertex2d(pos, -pos);
			gl.glVertex2d(pos, pos);
			gl.glVertex2d(-pos, pos);
		}
		gl.glEnd();
	}

	/***
	 * Capture the screenshot.
	 * 
	 * @return
	 * @throws Exception
	 */
	public void screenshot(File file) throws Exception {
		int sw = (int) viewScales[0];
		int sh = (int) viewScales[1];
		int w = glPanel.getWidth() * sw;
		int h = glPanel.getHeight() * sh;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = bi.createGraphics();
		withoutFrame = true;
		g.scale(sw, sh);
		glPanel.paint(g);
		withoutFrame = false;

		saveImage(file, bi);
	}

	private void saveImage(File file, BufferedImage image) throws Exception {
		final String formatName = "png";

		for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
			ImageWriter writer = iw.next();
			ImageWriteParam writeParam = writer.getDefaultWriteParam();
			ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
					.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
			IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
			if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
				continue;
			}

			setDPI(metadata);

			final ImageOutputStream stream = ImageIO.createImageOutputStream(new FileOutputStream(file));
			try {
				writer.setOutput(stream);
				writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
			} finally {
				stream.close();
			}
			break;
		}

	}

	private void setDPI(IIOMetadata metadata) throws IIOInvalidTreeException {
		int sw = (int) viewScales[0];
		int sh = (int) viewScales[1];
		double dotsPerMilli = 72f / 10f / 2.54f;
		IIOMetadataNode horiz = new IIOMetadataNode("水平ピクセルサイズ");
		horiz.setAttribute("値", Double.toString(dotsPerMilli * sh));
		IIOMetadataNode vert = new IIOMetadataNode("垂直ピクセルサイズ");
		vert.setAttribute("値", Double.toString(dotsPerMilli * sw));
		IIOMetadataNode dim = new IIOMetadataNode("次元");
		dim.appendChild(horiz);
		dim.appendChild(vert);
		IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
		root.appendChild(dim);
		metadata.mergeTree("javax_imageio_1.0", root);
	}
}
