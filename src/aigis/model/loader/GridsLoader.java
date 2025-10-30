package aigis.model.loader;

import java.io.IOException;

import aigis.model.Grids;

public class GridsLoader {

	public static Grids load(String path, String wFilename, String rFilename) throws IOException {
		Grids grids = new Grids();
		if (wFilename != null) {
			GridsLoader._load(grids.w, path + wFilename);
		}
		if (rFilename != null) {
			GridsLoader._load(grids.r, path + rFilename);
		}
		return grids;
	}

	private static void _load(Grids.Grid grid, String filename) throws IOException {
		FastReader scan = new FastReader(filename);
		int nLatlon = scan.nextInt();
		grid.data = new float[nLatlon][3];
		for (int i = 0; i < nLatlon; i++) {
			scan.nextInt();
			float f0 = scan.nextFloat();
			float f1 = scan.nextFloat();
			float f2 = scan.nextFloat();
			grid.data[i][0] = f0;
			grid.data[i][1] = f1;
			grid.data[i][2] = f2;
			if (f0 == 0 && f1 == 0 && f2 == 0) {
				grid.zeroIndex.add(i);
			}
		}
		grid.size = grid.data.length;
		scan.close();
	}
}
