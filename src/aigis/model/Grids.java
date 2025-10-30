package aigis.model;

import java.util.ArrayList;

public class Grids {
	public Grid w = new Grid();
	public Grid r = new Grid();

	public class Grid {
		public int size = 0;
		public float data[][];
		public ArrayList<Integer> zeroIndex = new ArrayList<Integer>();
	}
}
