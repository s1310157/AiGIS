package aigis.model;

import java.io.IOException;

import aigis.gl.ColorBar;
import aigis.model.loader.FastReader;

/**
 * スペクトルマップクラスです。 モデルの各面に対応付けられるデータや色変換のためのデータを保持します。
 * 
 * @author m5161121
 */
public class SpectrumMap {
	public double minColor;
	public double maxColor;
	public double orgMinColor;
	public double orgMaxColor;

	private int nData;
	private float[] faceData;
	private String name;
	private String unitRepresentation;
	private double customRangeMin = Double.MAX_VALUE;
	private double customRangeMax = Double.MAX_VALUE;
	private ColorBar colorbar;

	/**
	 * すべてのデータが0のスペクトルマップを作成します。
	 * 
	 * @param nFace スペクトルデータの数。
	 * @param name  ユニークな名前。これはプログラム独自の名前です。
	 */
	public SpectrumMap(int nFace, String name, ColorBar colorbar) {
		nData = nFace;
		faceData = new float[nData];
		for (int i = 0; i < nData; ++i) {
			faceData[i] = Float.NEGATIVE_INFINITY;
		}
		this.name = name;
		this.colorbar = colorbar;
	}

	/**
	 * 新たなスペクトルマップを作るには、パスを含むファイル名、プログラム上でのユニークな名前、そのスペクトルデータの物理量単位を指定します。
	 * 
	 * @param filename           ファイル名。
	 * @param name               ユニークな名前 (キー)。これはプログラム独自の名前です。
	 *                           {@link SpectrumDefinitions}に一覧があります。
	 * @param unitRepresentation 指定したスペクトルデータの物理量の単位を示す文字列。
	 * @throws IOException ファイル入出力例外をスローします。
	 */
	public SpectrumMap(String filename, ColorBar colorbar) throws IOException {
		FastReader scan = new FastReader(filename);
		this.name = scan.nextString();
		this.unitRepresentation = scan.nextString();

		nData = scan.nextInt();
		faceData = new float[nData];
		for (int i = 0; i < nData; i++) {
			scan.nextInt();
			String v = scan.nextString();
			if (v.equals("-")) {
				faceData[i] = Float.NEGATIVE_INFINITY;
			} else {
				faceData[i] = Float.valueOf(v);
			}
		}
		scan.close();
		this.colorbar = colorbar;
	}

	/*
	 * データ名と単位を直接表示 SpectrumMap(String filename, String name, String
	 * unitRepresentation) throws IOException {
	 * 
	 * this.name = name; this.unitRepresentation = unitRepresentation;
	 * 
	 * FastReader scan = new FastReader(filename);
	 * 
	 * nData = scan.nextInt(); faceData = new float[nData];
	 * 
	 * for (int i = 0; i < nData; i++) { scan.nextInt(); faceData[i] =
	 * scan.nextFloat(); } scan.close(); }
	 */

	public String getName() {
		return name;
	}

	public String getUnitRepresentation() {
		return unitRepresentation;
	}

	public void setCustomRange(double max, double min) {
		customRangeMax = max;
		customRangeMin = min;
	}

	public void clearCustomRange() {
		customRangeMin = Double.MAX_VALUE;
		customRangeMax = Double.MAX_VALUE;
	}

	/**
	 * スペクトルデータをRBGに変換します。 値はHSV表色系のHの適当な範囲にマップされた後、S=1,V=1としてRGBA表色系へ変換されます。
	 * 
	 * @param rgba 変換された色データを保存する配列。これは面の数と同じだけの要素を持ちます。
	 */
	public void convertToRGB(float[][] rgba, int colorbarIndex) {

		float maxi = Float.NEGATIVE_INFINITY;
		float mini = Float.POSITIVE_INFINITY;

		for (int i = 0; i < nData; i++) {
			if (maxi < faceData[i] && (faceData[i] != Float.NEGATIVE_INFINITY)) {
				maxi = faceData[i];
			}
			if ((mini > faceData[i]) && (faceData[i] != Float.NEGATIVE_INFINITY)) {
				mini = faceData[i];
			}
		}

		double MR = maxi;
		double mR = mini;

		orgMinColor = mR;
		orgMaxColor = MR;
		if (customRangeMin != Double.MAX_VALUE) {
			mR = customRangeMin;
		}
		if (customRangeMax != Double.MAX_VALUE) {
			MR = customRangeMax;
		}
		minColor = mR;
		maxColor = MR;

		for (int i = 0; i < nData; i++) {
			if (rgba.length <= i) {
				return;
			}
			rgba[i] = colorbar.convertSpectrum(colorbarIndex, this, faceData[i]);
		}
	}

	public float getSpectrumData(int face) {
		return faceData[face];
	}
}

// EOF
