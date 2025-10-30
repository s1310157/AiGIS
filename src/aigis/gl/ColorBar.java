package aigis.gl;

import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL2.GL_QUAD_STRIP;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_Q;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_R;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_S;
import static com.jogamp.opengl.GL2.GL_TEXTURE_GEN_T;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.awt.Font;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;

import aigis.Scene;
import aigis.model.SpectrumMap;
import aigis.model.loader.FastReader;

public class ColorBar {

	private class Model {
		public String title;
		public float table[][];
	}

	private ArrayList<Model> models = new ArrayList<>();
	private Font font = new Font("Monospaced", java.awt.Font.PLAIN, 20);
	private TextRenderer tr = new TextRenderer(font, true, true);

	public ColorBar() {
		init();
	}

	public void init() {
		// @formatter:off
		models.clear();
		// Rainbow
		Model m = new Model();
		m.title = "Rainbow";
		m.table = new float[][] {
			{ 0.0f, 0.0f, 1.0f }, 
			{ 0.0f, 1.0f, 1.0f }, 
			{ 0.0f, 1.0f, 0.0f },
			{ 1.0f, 1.0f, 0.0f }, 
			{ 1.0f, 0.0f, 0.0f } 
		};
		models.add(m);
		// Grayscale
		m = new Model();
		m.title = "Grayscale";
		m.table = new float[][] {
			{ 0.0f, 0.0f, 0.0f }, 
			{ 1.0f, 1.0f, 1.0f }
		};
		models.add(m);
		// @formatter:on
	}

	public void load(String filename, String path) throws IOException {
		FastReader scan = new FastReader(path);
		ArrayList<float[]> list = new ArrayList<>();
		while (true) {
			float val = scan.nextFloat();
			if (val == Float.NEGATIVE_INFINITY)
				break;
			float v[] = { val, scan.nextFloat(), scan.nextFloat() };
			list.add(v);
		}
		Model m = new Model();
		m.title = filename;
		m.table = list.toArray(new float[list.size()][3]);
		models.add(m);
	}

	public String[] getTitles() {
		ArrayList<String> list = new ArrayList<>();
		for (Model m : models) {
			list.add(m.title);
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * カラーバーの文字を描画します.
	 */
	private void drawString(String str, int index, int width, int height, boolean title) {
		float aspect = width / (float) height;
		int widthDiff = width - 720;
		int heightDiff = height - 640 + 50;
		float scale = height / 850f + 0.25f;
		if (title) {
			tr.draw3D(str, (360 + widthDiff * 0.5f) - (str.length() * 6f * scale), 90.0f + heightDiff * 0.15f, 0f,
					scale);
			return;
		}
		if (aspect > 0.9) {
			tr.draw3D(str, (100 + 130 * index + widthDiff * 0.5f + heightDiff * (0.2f * (float) (index - 2)))
					- (str.length() * 6f * scale), 30.0f + heightDiff * 0.05f, 0f, scale);
		} else {
			tr.draw3D(str, (200 + 88 * index + widthDiff * 0.5f + heightDiff * (0.15f * (float) (index - 2)))
					- (str.length() * 6f * scale), 30.0f + heightDiff * 0.05f, 0f, scale * 0.7f);
		}
	}

	/**
	 * カラーバーを描画します.
	 */
	public void draw(GL2 gl, int width, int height, Scene scene, Renderer renderer) {
		SpectrumMap spec = renderer.getCurrentSpectrum();
		if (spec == null)
			return;
		String dataName = spec.getName();
		String unitRepresentation = spec.getUnitRepresentation();
		if (dataName == null || unitRepresentation == null)
			return;

		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_LIGHT0);

		gl.glDisable(GL_TEXTURE_GEN_S);
		gl.glDisable(GL_TEXTURE_GEN_T);
		gl.glDisable(GL_TEXTURE_GEN_R);
		gl.glDisable(GL_TEXTURE_GEN_Q);

		String red, blue, cyan, green, yellow;

		double max = spec.maxColor;
		double min = spec.minColor;
		double mid = (max - min) / 4;

		// round down to two decimal places
		BigDecimal maxbd = new BigDecimal(max);
		BigDecimal minbd = new BigDecimal(min);
		BigDecimal midbd = new BigDecimal(min + mid);
		BigDecimal midbd2 = new BigDecimal(min + mid * 2);
		BigDecimal midbd3 = new BigDecimal(min + mid * 3);
		if (max < 0.001) {
			DecimalFormat df = new DecimalFormat("0.00E0");
			red = df.format(maxbd);
			blue = df.format(minbd);
			cyan = df.format(midbd);
			green = df.format(midbd2);
			yellow = df.format(midbd3);
		} else {
			red = maxbd.setScale(3, RoundingMode.DOWN).toString();
			blue = minbd.setScale(3, RoundingMode.DOWN).toString();
			cyan = midbd.setScale(3, RoundingMode.DOWN).toString();
			green = midbd2.setScale(3, RoundingMode.DOWN).toString();
			yellow = midbd3.setScale(3, RoundingMode.DOWN).toString();
		}

		float w = width;
		float h = height;
		float aspect = w / h;
		float scale = h / 850f + 0.25f;

		// bar value
		tr.beginRendering(width, height);
		tr.setColor(1.0f, 1.0f, 0.0f, 1.0f);
		this.drawString(blue, 0, width, height, false);
		this.drawString(cyan, 1, width, height, false);
		this.drawString(green, 2, width, height, false);
		this.drawString(yellow, 3, width, height, false);
		this.drawString(red, 4, width, height, false);
		// title
		tr.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawString(dataName, 2, width, height, true);
		// unit
		tr.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		if (!unitRepresentation.equals("-")) {
			tr.draw3D("(" + unitRepresentation + ")", width - (unitRepresentation.length() * 8f * scale) - 42f * scale,
					10f * scale, 0f, scale * 0.8f);
		}
		tr.endRendering();

		// show color bar
		gl.glLoadIdentity();
		gl.glDisable(GL_DEPTH_TEST);
		gl.glTranslated(0, -0.55, -10);
		gl.glBegin(GL_QUAD_STRIP);

		Model m = models.get(renderer.setting.colorbarIndex);
		float table[][] = m.table; // lookupTable[renderer.setting.colorbarIndex];
		float base = -0.6f;
		float step = 1.2f / (table.length - 1);
		if (aspect < 0.9) {
			base = -0.4f;
			step = 0.8f / (table.length - 1);
		}
		for (int i = 0; i < table.length; i++) {
			float val[] = table[i];
			gl.glColor3f(val[0], val[1], val[2]);
			gl.glVertex2d(base + step * i, 0.06);
			gl.glVertex2d(base + step * i, 0);
		}
		gl.glEnd();
		gl.glEnable(GL_DEPTH_TEST);
	}

	/**
	 * スペクトルデータの色変換をします。
	 */
	public float[] convertSpectrum(int colorbarIndex, SpectrumMap spec, float faceData) {
		Model m = models.get(colorbarIndex);
		float table[][] = m.table;// lookupTable[colorbarIndex];
		float col[] = { 0.8f, 0.8f, 0.8f };
		if (faceData == Float.NEGATIVE_INFINITY) {
		} else if ((float)spec.maxColor <= faceData) {
			col = table[table.length - 1];
		} else if ((float)spec.minColor >= faceData) {
			col = table[0];
		} else {
			float maxvol = (float)(spec.maxColor - spec.minColor);
			float val = ((faceData - (float)spec.minColor) / maxvol);
			val = val / (1.0f / (table.length - 1));
			int idx = (int) Math.floor(val);
			if (idx >= table.length - 1) {
				idx = table.length - 2;
			}
			float col1[] = table[idx];
			float col2[] = table[idx + 1];
			float per = (float) (val % 1.0f);
			col[0] = col1[0] + (col2[0] - col1[0]) * per;
			col[1] = col1[1] + (col2[1] - col1[1]) * per;
			col[2] = col1[2] + (col2[2] - col1[2]) * per;
		}
		return new float[] { col[0], col[1], col[2], 1f };
	}

}
