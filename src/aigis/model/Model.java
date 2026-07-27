package aigis.model;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_WRITE_ONLY;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLBufferStorage;
import com.jogamp.opengl.glu.GLU;

import aigis.Const;
import aigis.Logger;
import aigis.model.loader.GridsLoader;
import aigis.model.loader.ModelLoader;
import aigis.model.loader.ObjLoader;

/**
 * モデルに関する情報を持つクラスです。
 * 
 * @author m5161121
 */
public class Model {

	public int nVertex;
	public int nFace;
	public int nNormal;

	public float[][] vertices; // 頂点配列
	public int[][] faces; // 面配列
	public float[][] normals; // 法線配列
	public int[] normalIndex; // 法線のインデックス
	public float[][] point; // 追加
	public float[][] uv; // UV配列

	public float[][] rgba; // カラーマップ

	private int nMap; // マッピングデータの総数
	private Grids grids;

	private int vboIdVertex;
	private int vboIdNormal;
	private int vboIdColor;
	private int vboIdUVs[];

	private SpectrumMaps spms;
	public LatLon[] info; // 追加

	private float[] specular = { 0.5f, 0.5f, 0.5f, 1.0f }; // 鏡面反射光色
	private float[] diffuse = { 0.5f, 0.5f, 0.5f, 1.0f }; // 拡散光色
	private float[] ambient = { 0.4f, 0.4f, 0.4f, 1.0f }; // 環境光色
	private float shiness = 5.0f; // 反射輝度

	private double size = 0;

	/**
	 * 新たなモデルを生成します。
	 * 
	 * @param filename        モデルデータのファイル名。
	 * @param filenameLatLonW 緯度経度データのファイル名。
	 * @param filenameLatLonR 緯度経度データのファイル名。
	 * @throws IOException
	 */
	public Model(String dir, String filename, String filenameLatLonW, String filenameLatLonR) throws IOException {
		String path = dir + File.separator;

		if (filename.endsWith(".obj")) {
			// モデルのWavefrontObjデータ読み込み
			ObjLoader.load(path + filename, this);
		} else {
			// モデルの生データ読み込み
			ModelLoader.load(path + filename, this);
		}

		// グリッドデータ読み込み
		grids = GridsLoader.load(path, filenameLatLonW, filenameLatLonR);

		// initialize color
		nMap = nFace;
		rgba = new float[nMap][4];

		spms = new SpectrumMaps();
	}

	/**
	 * マウスでクリックされた位置のポリゴンIDを求めます。 ポリゴンIDは2次元平面上に対する各面の投影後、三角形の内外判定によって行われます。
	 * 
	 * @param mousePos マウスの位置。
	 * @return ポリゴンID。該当するポリゴンがないとき-1。
	 */
	public Integer getID(Point2D.Double mousePos, double[] modelview, double[] projection, int[] viewport) {
		GLU gll = new GLU();
		int[] internalIndecies = new int[64];
		double[] zDepth = new double[64];
		int count = 0;
		double[] result = new double[9];

		for (int i = 0; i < nFace; i++) {
			for (int j = 0; j < 3; ++j) {
				// オブジェクト座標系からウィンドウ座標系への変換
				gll.gluProject((double) vertices[(faces[i][j] - 1)][0], (double) vertices[(faces[i][j] - 1)][1],
						(double) vertices[(faces[i][j] - 1)][2], modelview, 0, projection, 0, viewport, 0, result,
						3 * j);
			}

			if (Geometry.triangleInside2(result, mousePos)) {
				internalIndecies[count] = i;
				zDepth[count] = (result[2] + result[5] + result[8]) / 3.0d;
				count++;
			}
		}

		// System.out.println("Number of Matched Polygon: " + count);

		if (count > 0) {
			double min = 1e80d;
			int minIndex = 0;
			for (int d = 0; d < count; d++) {
				if (min > zDepth[d]) {
					min = zDepth[d];
					minIndex = d;
				}
			}
			return Integer.valueOf(internalIndecies[minIndex]);
		} else
			return Integer.valueOf(-1);

	}

	/***
	 * UVMapを作成します
	 * 
	 * @param ptr 出力用Buffer
	 * @param vec カメラの位置
	 * @param winZ 深度情報
	 * @param modelview モデル座標変換行列
	 * @param projection 射影変換行列
	 * @param viewport ViewPort
	 */
	public void makeUV(ByteBuffer ptr, Vector3d vec, FloatBuffer winZ, double[] modelview, double[] projection,
			int[] viewport) {
		GLU gll = new GLU();
		FloatBuffer texCoords = ptr.asFloatBuffer();
		double[] result = new double[9];
		for (int i = 0; i < nFace; i++) {

			// 向きを求めるためにポリゴンの法線を計算
			Vector3f p0 = new Vector3f(vertices[(faces[i][0] - 1)]);
			Vector3f p1 = new Vector3f(vertices[(faces[i][1] - 1)]);
			Vector3f p2 = new Vector3f(vertices[(faces[i][2] - 1)]);
			Vector3f v0 = new Vector3f();
			v0.sub(p0, p1);
			Vector3f v1 = new Vector3f();
			v1.sub(p1, p2);
			Vector3f n = new Vector3f();
			n.cross(v0, v1);
			float d = n.dot(new Vector3f(vec));

			if (d < 0) {
				// 裏面は無視
				for (int j = 0; j < 3; ++j) {
					int idx = i * 6 + j * 2;
					texCoords.put(idx, 0);
					texCoords.put(idx + 1, 0);
				}
			} else {
				// 
				boolean flg = false;
				float[] coords = new float[9];
				for (int j = 0; j < 3; ++j) {
					// オブジェクト座標をウィンドウ座標に変換
					float[] vt = vertices[(faces[i][j] - 1)];
					gll.gluProject((double) vt[0], (double) vt[1], (double) vt[2], modelview, 0, projection, 0,
							viewport, 0, result, 0);
					coords[j * 2] = (float) result[0];
					coords[j * 2 + 1] = (float) result[1];

					// 深度情報の大きさに合わせる
					int xx = (int) Math.round(result[0]);
					int yy = (int) Math.round(result[1]);
					int a = xx + yy * 512;
					// 範囲内のもののみ採用＆
					// 深度情報と比較して近いポリゴンを採用する
					//if (xx < 0 || yy < 0 || xx >= 512 || yy >= 512 || Math.abs(winZ.get(a) - result[2]) > 0.0001) {
					// 深度情報と比較して近いポリゴンを採用する
					if (xx < 0 || yy < 0 || xx >= 512 || yy >= 512 || Math.abs(winZ.get(a)- result[2]) > 0.01) {
					} else {
						flg = true;
					}
				}
				// UVを作成
				for (int j = 0; j < 3; ++j) {
					int idx = i * 6 + j * 2;
					if (flg) {
						texCoords.put(idx, coords[j * 2] / 512f);
						texCoords.put(idx + 1, coords[j * 2 + 1] / 512f);
					} else {
						texCoords.put(idx, 0);
						texCoords.put(idx + 1, 0);
					}
				}

			}
		}
	}

	/**
	 * VBOバッファで必要になる要素数を返します。
	 * 
	 * @param type 計算するバッファタイプ(頂点,法線,色)。
	 * @return 計算した要素数。
	 */
	private int calcRequiredBufferSize(int type) {
		int ret = 0;
		if (type == Const.BUFFERTYPE_NORMAL || type == Const.BUFFERTYPE_VERTEX) {
			ret = 9 * nFace + 3 * (grids.w.size + grids.r.size);
		} else if (type == Const.BUFFERTYPE_COLOR) {
			ret = 12 * nFace + 4 * (grids.w.size + grids.r.size);
		} else if (type == Const.BUFFERTYPE_UV) {
			ret = 6 * nFace;
		}
		return ret;
	}

	/**
	 * 表示用のVBOバッファを生成します。
	 * 
	 * @param type 生成するバッファタイプ(頂点,法線,色)。
	 * @return 生成したバッファ。
	 */
	private void buildBuffer(int type, GLBufferStorage storage) {
		ByteBuffer buff = storage.getMappedBuffer();

		if (type == Const.BUFFERTYPE_VERTEX) {

			for (int i = 0; i < nFace; ++i) {
				for (int j = 0; j < 3; j++) {
					buff.putFloat(vertices[faces[i][j] - 1][0]);
					buff.putFloat(vertices[faces[i][j] - 1][1]);
					buff.putFloat(vertices[faces[i][j] - 1][2]);
				}
			}

		} else if (type == Const.BUFFERTYPE_NORMAL) {

			for (int i = 0; i < 3 * nNormal; ++i) {
				for (int j = 0; j < 3; j++) {
					buff.putFloat(normals[normalIndex[i]][j]);
				}
			}

		} else if (type == Const.BUFFERTYPE_COLOR) {

			// i番目のfaceの色
			for (int i = 0; i < nMap; i++) {
				for (int j = 0; j < 3; j++) {
					for (int k = 0; k < 4; k++) {
						buff.putFloat(rgba[i][k]);
					}
				}
			}

		} else if (type >= Const.BUFFERTYPE_UV) {

			for (int i = 0; i < nFace; ++i) {
				for (int j = 0; j < 6; j++) {
					if (type == Const.BUFFERTYPE_UV) {
						buff.putFloat(uv[i][j]);
					} else {
						buff.putFloat(0f);
					}
				}
			}

		}

		/*
		 * 本来はbufferは面集合だけであるが、このアプリケーションの場合それに加えて頂点による面表現の後に 緯経線の表現が入る。
		 */

		if (type == Const.BUFFERTYPE_VERTEX || type == Const.BUFFERTYPE_NORMAL) {

			for (int i = 0; i < grids.r.size; i++) {
				buff.putFloat(grids.r.data[i][0]);
				buff.putFloat(grids.r.data[i][1]);
				buff.putFloat(grids.r.data[i][2]);
			}
			for (int i = 0; i < grids.w.size; i++) {
				buff.putFloat(grids.w.data[i][0]);
				buff.putFloat(grids.w.data[i][1]);
				buff.putFloat(grids.w.data[i][2]);
			}

		} else if (type == Const.BUFFERTYPE_COLOR) {

			for (int i = 0; i < grids.r.size; i++) {
				buff.putFloat(1.0f);
				buff.putFloat(0f);
				buff.putFloat(0f);
				buff.putFloat(1.0f);
			}
			for (int i = 0; i < grids.w.size; i++) {
				for (int j = 0; j < 4; j++) {
					buff.putFloat(1.0f);
				}
			}
		}
	}

	/**
	 * このモデルが持つ色情報でバッファを書き換えます。
	 * 
	 * @param ptr VBOバッファ。
	 * @return 書き換えたとき true, そうでないとき false
	 */
	public boolean rewriteColor(ByteBuffer ptr) {
		if (ptr != null) {
			// ポリゴン表面の色。
			FloatBuffer floatBuf = ptr.asFloatBuffer();
			for (int i = 0; i < nMap; i++) {
				floatBuf.put(rgba[i]);
				floatBuf.put(rgba[i]);
				floatBuf.put(rgba[i]);
			}
			ptr.limit(ptr.position());
			ptr.position(0);

			// // 緯経線はスキップ。
			// ptr.position(ptr.position() + 4 * 4 * (nLatlonW + nLatlonR));
			//
			// // 読み込んだデータの色を格納
			// for (int i = 0; i < nLatlon; i++) {
			// ptr.putFloat((float) (r / 255.0));
			// ptr.putFloat((float) (g / 255.0));
			// ptr.putFloat((float) (b / 255.0));
			// ptr.putFloat((float) (a));
			// }
			return true;
		} else {
			return false;
		}
	}

	/**
	 * このモデルのVBOバッファを作成します。
	 * 
	 * @param gl GLインスタンス。
	 */
	public void compile(GL2 gl) {
		/** VBO番号をリセットします */
		vboIdVertex = 0;
		vboIdNormal = 0;
		vboIdColor = 0;

		//
		int maxTexs[] = new int[1];
		gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, maxTexs, 0);
		int maxTex = maxTexs[0];
		Logger.Debug("MaxTexture: " + maxTex);
		vboIdUVs = new int[maxTex];

		// バッファオブジェクトの名前を4つ作成します
		int max = Const.BUFFERTYPE_UV + maxTex + 1;
		int[] vboIdBuff = new int[max];
		gl.glGenBuffers(max, vboIdBuff, 0);

		// 各バッファオブジェクトに頂点データ配列を転送します
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboIdBuff[0]);
		int totalSize = calcRequiredBufferSize(Const.BUFFERTYPE_VERTEX);
		gl.glBufferData(GL_ARRAY_BUFFER, totalSize * Buffers.SIZEOF_FLOAT, null, GL_STATIC_DRAW);
		GLBufferStorage storage = gl.mapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
		buildBuffer(Const.BUFFERTYPE_VERTEX, storage);
		gl.glUnmapBuffer(GL_ARRAY_BUFFER);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboIdBuff[1]);
		totalSize = calcRequiredBufferSize(Const.BUFFERTYPE_NORMAL);
		gl.glBufferData(GL_ARRAY_BUFFER, totalSize * Buffers.SIZEOF_FLOAT, null, GL_STATIC_DRAW);
		storage = gl.mapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
		buildBuffer(Const.BUFFERTYPE_NORMAL, storage);
		gl.glUnmapBuffer(GL_ARRAY_BUFFER);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vboIdBuff[2]);
		totalSize = calcRequiredBufferSize(Const.BUFFERTYPE_COLOR);
		gl.glBufferData(GL_ARRAY_BUFFER, totalSize * Buffers.SIZEOF_FLOAT, null, GL_DYNAMIC_DRAW);
		storage = gl.mapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
		buildBuffer(Const.BUFFERTYPE_COLOR, storage);
		gl.glUnmapBuffer(GL_ARRAY_BUFFER);

		for (int i = 0; i < maxTex; i++) {
			gl.glBindBuffer(GL_ARRAY_BUFFER, vboIdBuff[Const.BUFFERTYPE_UV + i]);
			totalSize = calcRequiredBufferSize(Const.BUFFERTYPE_UV);
			gl.glBufferData(GL_ARRAY_BUFFER, totalSize * Buffers.SIZEOF_FLOAT, null, GL_DYNAMIC_DRAW);
			storage = gl.mapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
			buildBuffer(Const.BUFFERTYPE_UV + i, storage);
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
			vboIdUVs[i] = vboIdBuff[Const.BUFFERTYPE_UV + i];
		}

		vboIdVertex = vboIdBuff[0];
		vboIdNormal = vboIdBuff[1];
		vboIdColor = vboIdBuff[2];
	}

	/**
	 * key で示されるスペクトルマップで色情報を計算します。
	 * 
	 * @param key
	 */
	public void changeSpectrumMap(String key, int colorbarIndex) {
		spms.convertToRGB(key, rgba, colorbarIndex);
	}

	/**
	 * key で新たなスペクトルマップを追加します。
	 * 
	 * @param key 追加するスペクトルマップのキー。
	 * @param spm スペクトルマップ。
	 * @throws IOException
	 */
	public void addSpectrumMap(String key, SpectrumMap spm) throws IOException {
		spms.addSpectrumMap(key, spm);
	}

	public void emphasizePolygon(Integer polygonID) {
		int pid = polygonID;
		rgba[pid][0] = 1.0f;
		rgba[pid][1] = 0.0f;
		rgba[pid][2] = 0.0f;
		rgba[pid][3] = 1.0f;
	}

	/*
	 * getters
	 */

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		System.out.println("size:" + size);
		this.size = size;
	}

	public SpectrumMap getSpectrum(String key) {
		return spms.getSpectrum(key);
	}

	public float getSpectrumData(String key, int face) {
		return spms.getSpectrumData(key, face);
	}

	public SpectrumMaps getAvailableSpectrumMaps() {
		return spms;
	}

	public int getVboId(int type) {
		if (type == Const.BUFFERTYPE_VERTEX) {
			return vboIdVertex;
		} else if (type == Const.BUFFERTYPE_NORMAL) {
			return vboIdNormal;
		} else if (type == Const.BUFFERTYPE_COLOR) {
			return vboIdColor;
		} else if (type >= Const.BUFFERTYPE_UV) {
			return vboIdUVs[type - Const.BUFFERTYPE_UV];
		} else
			return -1;
	}

	public int getNVertex() {
		return nVertex;
	}

	public int getNFace() {
		return nFace;
	}

	public int getNNormal() {
		return nNormal;
	}

	public float getVertex(int i, int j) {
		return vertices[i][j];
	}

	public int getFace(int i, int j) {
		return faces[i][j];
	}

	public float getNormal(int i, int j) {
		return normals[i][j];
	}

	public int getNormalIndex(int i) {
		return normalIndex[i];
	}

	public int getTotalMapping() {
		return nMap;
	}

	public Grids getGrids() {
		return grids;
	}

	public float[][] getPoint() {
		return point;
	}

	public LatLon[] getLatLon() {
		return info;
	}

	public float[][] getNormal() {
		return normals;
	};

	public float[] getSpecular() {
		return specular;
	}

	public float[] getDiffuse() {
		return diffuse;
	}

	public float[] getAmbient() {
		return ambient;
	}

	public float getShiness() {
		return shiness;
	}

	/***
	 * Release the VBOs held by this model.
	 *
	 * @param gl
	 */
	public void dispose(GL2 gl) {
		int[] ids = { vboIdVertex, vboIdNormal, vboIdColor };
		gl.glDeleteBuffers(ids.length, ids, 0);
		if (vboIdUVs != null) {
			gl.glDeleteBuffers(vboIdUVs.length, vboIdUVs, 0);
		}
	}
}

// EOF
