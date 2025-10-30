package aigis.model.loader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import aigis.Const;

/**
 * あるファイルからある程度高速に整数値と浮動少数を読み取ります。
 * 
 * @author m5161121
 *
 */
public class FastReader {
	BufferedReader br;
	StringTokenizer st;
	public boolean eof = false;
	public boolean hasComment = false;
	public boolean exComment = false;
	private ArrayList<String> peekList = new ArrayList<>();

	String filename;
	int lineNum = 0;
	int checkRow = 4;// 5行目にて判定する

	public FastReader(String filename) throws IOException {
		this.filename = filename;
		// br = new BufferedReader( new FileReader(filename ) );
		br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
		st = new StringTokenizer("");

	}

	public boolean hasMoreTokens() {
		return st.hasMoreTokens();
	}

	// データ名と単位を読み込む
	public String nextString() throws IOException {
		hasComment = false;
		return nextStringImpl(false);
	}

	public String peekString() throws IOException {
		return nextStringImpl(true);
	}

	private String nextStringImpl(boolean peek) throws IOException {
		if (!peek && peekList.size() > 0) {
			String ret = peekList.get(0);
			peekList.remove(0);
			return ret;
		}
		if (!st.hasMoreTokens()) {
			String line = br.readLine();
			if (line == null) {
				eof = true;
				return null;
			}
			if (!peek)
				lineNum++;
			if (line.trim().length() == 0) {
				return nextStringImpl(peek);
			}
			// コメントだったら飛ばす
			if (line.trim().startsWith("#") || (exComment && line.trim().startsWith("-"))) {
				hasComment = true;
				return nextStringImpl(peek);
			}
			line = line.split("#")[0];
			st = new StringTokenizer(line);
		}
		String token = st.nextToken();
		if (peek)
			peekList.add(token.toString());
		return token.toString();
	}

	// 整数値を読み込む
	public int nextInt() throws IOException {
		String str = nextString();
		if (str == null)
			return Integer.MIN_VALUE;
		try {
			return Integer.valueOf(str);
		} catch (Exception e) {
			String[] segments = filename.split("/");
			String outstr = str;
			if (str.length() > 15) {
				outstr = str.substring(0, 15);
				outstr += "...";
			}
			throw new IOException("Data format error in \"" + segments[segments.length - 1] + "\" line:" + lineNum
					+ " \n[" + outstr + "]", e);
		}
	}

	// 浮動小数値を読み込む
	public float nextFloat() throws IOException {
		String str = nextString();
		if (str == null)
			return Float.NEGATIVE_INFINITY;
		try {
			return Float.valueOf(str.toLowerCase().replace("d", "e"));
		} catch (Exception e) {
			String[] segments = filename.split("/");
			String outstr = str;
			if (str.length() > 15) {
				outstr = str.substring(0, 15);
				outstr += "...";
			}
			throw new IOException("Data format error in \"" + segments[segments.length - 1] + "\" line:" + lineNum
					+ " \n[" + outstr + "]", e);
		}
	}

	public void close() throws IOException {
		br.close();
	}

	public float[] scanFloat() throws IOException {
		ArrayList<Float> scanlist = new ArrayList<>();
		while (this.hasMoreTokens()) {
			scanlist.add(this.nextFloat());
		}
		float data[] = new float[scanlist.size()];
		for (int j = 0; j < scanlist.size(); j++) {
			data[j] = scanlist.get(j);
		}
		return data;
	}

	public long[] scanDate() throws IOException, ParseException {
		ArrayList<String> scanlist = new ArrayList<>();
		while (this.hasMoreTokens()) {
			scanlist.add(this.nextString());
		}
		SimpleDateFormat sdFormat = new SimpleDateFormat(Const.DATE_FORMAT);
		long data[] = new long[scanlist.size()];
		for (int j = 0; j < scanlist.size(); j++) {
			String timeStr = scanlist.get(j);
			Date date;
			date = sdFormat.parse(timeStr);
			data[j] = date.getTime() / 1000;
		}
		return data;
	}

}
