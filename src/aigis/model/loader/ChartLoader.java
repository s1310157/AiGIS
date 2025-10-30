package aigis.model.loader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import aigis.Const;
import aigis.model.ChartData;
import aigis.ui.LoadingDialog;

public class ChartLoader {

	public static ChartData load(String filename, LoadingDialog dialog) throws IOException, ParseException {
		// 奇数行フラグ
		boolean isOdd = true;

		FastReader scan = new FastReader(filename);
		ChartData data = new ChartData();
		// １行目：グラフに対応するMap名
		data.mapName = scan.nextString();
		// ２行目：縦軸ラベル
		data.yLabel = scan.nextString();
		// ３行目：横軸ラベル
		data.xLabel = scan.nextString();
		// ４行目：ポリゴン数
		int vtx = scan.nextInt();
		data.nFace = vtx;

		// 5行目を取得して保持しておく
		String checkStr = scan.peekString();

		// 5行目の最初がアンダーバーの場合はX軸固定フォーマット
		if (checkStr.equals("_")) {
			scan.nextString();
			checkStr = scan.peekString();

			data.data = new float[vtx][];
			boolean timeformat = isTimeFormat(checkStr);
			float range[] = null;
			long timeRange[] = null;
			if (timeformat) {
				data.isTimeFormat = true;
				data.timeRange = new long[vtx][];
				timeRange = scan.scanDate();
			} else {
				data.isTimeFormat = false;
				data.range = new float[vtx][];
				range = scan.scanFloat();
			}

			// ６行目以降: グラフの縦軸に用いる数値
			while (!scan.eof) {
				int idx = scan.nextInt() - 1;
				if (scan.eof)
					break;
				data.data[idx] = scan.scanFloat();
				if (timeformat) {
					data.timeRange[idx] = timeRange;
				} else {
					data.range[idx] = range;
				}

				updateDialog(filename, idx, vtx, dialog);
			}
		} else if (checkStr.equals("1")) {
			// 5行目が数値の場合は2次元フォーマット

			checkStr = scan.peekString();
			data.data = new float[vtx][];
			boolean timeformat = isTimeFormat(checkStr);
			data.isTimeFormat = timeformat;
			data.data = new float[vtx][];
			if (timeformat) {
				data.timeRange = new long[vtx][];
			} else {
				data.range = new float[vtx][];
			}
			while (!scan.eof) {
				// 奇数行の時にx軸とy軸読み込み
				if (isOdd) {
					int idx = scan.nextInt() - 1;
					if (scan.eof)
						break;
					if (timeformat) {
						data.timeRange[idx] = scan.scanDate();
					} else {
						data.range[idx] = scan.scanFloat();
					}

					// y軸の値格納
					idx = scan.nextInt() - 1;// polygonIDが格納される
					if (scan.eof)
						break;
					data.data[idx] = scan.scanFloat();

					updateDialog(filename, idx, vtx, dialog);

					isOdd = false;
				} else {
					// 偶数行は読み込まない(奇数行読み込み時に読み込む)
					isOdd = true;
				}
			}
		}

		scan.close();
		return data;
	}

	private static boolean isTimeFormat(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(Const.DATE_FORMAT);
		// 取得した値が時系列データか判定
		try {
			sdf.parse(dateStr);
		} catch (java.text.ParseException e) {
			return false;
		}
		return true;
	}

	private static void updateDialog(String filename, int idx, int vtx, LoadingDialog dialog) {
		if (idx % 10000 == 0) {
			String[] segments = filename.split("/");
			int pc = (int) (idx / (double) vtx * 100);
			dialog.updateInfo(segments[segments.length - 1] + "(" + pc + "%)");
		}
	}
}
