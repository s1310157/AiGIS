package aigis.model.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import aigis.model.General3D;

public class GeneralLoader {

	public static void load(String dir, String name, General3D general) throws IOException {
		FastReader scan = new FastReader(dir + File.separator + name);
		scan.exComment = true;
		// color
		for (int i = 0; i < 4; i++) {
			general.lineColor[i] = scan.nextFloat();
		}
		// line size
		general.lineSize = scan.nextFloat();
		general.lines.clear();
		// total
		scan.nextInt();

		// lines
		ArrayList<float[]> lines = new ArrayList<float[]>();
		while (true) {
			int cnt = scan.nextInt();
			if (cnt == Integer.MIN_VALUE) {
				break;
			}
			// next data if there is a comment
			if (scan.hasComment) {
				general.lines.add(lines.toArray(new float[lines.size()][3]));
				lines.clear();
			}
			float[] line = new float[3];
			for (int i = 0; i < 3; i++) {
				line[i] = scan.nextFloat();
			}
			lines.add(line);
		}
		general.lines.add(lines.toArray(new float[lines.size()][3]));
		general.name = name;
		scan.close();
	}
}
