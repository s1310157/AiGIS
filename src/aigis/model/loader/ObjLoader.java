package aigis.model.loader;

import java.io.IOException;
import java.util.ArrayList;

import aigis.model.Model;

public class ObjLoader extends ModelLoader {

	public static void load(String filename, Model model) throws IOException {
		FastReader scan = new FastReader(filename);

		ArrayList<float[]> v_list = new ArrayList<>();
		ArrayList<int[]> f_list = new ArrayList<>();
		double maxDist = 0;
		while (true) {
			String type = scan.nextString();
			if (type == null) {
				break;
			}
			if (type.endsWith("v")) {
				float v[] = { scan.nextFloat(), scan.nextFloat(), scan.nextFloat() };
				v_list.add(v);
				float x = v[0];
				float y = v[1];
				double dist = x * x + y * y;
				if (maxDist < dist) {
					maxDist = dist;
				}
			} else if (type.endsWith("f")) {
				int f[] = { scan.nextInt(), scan.nextInt(), scan.nextInt() };
				f_list.add(f);
			}
		}
		int nVertex = v_list.size();
		model.nVertex = nVertex;
		model.vertices = new float[nVertex][3];
		for (int i = 0; i < v_list.size(); i++) {
			model.vertices[i] = v_list.get(i);
		}

		int nFace = f_list.size();
		model.nFace = nFace;
		model.faces = new int[nFace][3];
		for (int i = 0; i < f_list.size(); i++) {
			model.faces[i] = f_list.get(i);
		}

		model.setSize(Math.sqrt(maxDist) * 2);

		makeNormalUV(model, nFace);
	}

}
