package aigis.model.loader;

import java.io.IOException;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import aigis.model.LatLon;
import aigis.model.Model;

public class ModelLoader {

	public static void load(String filename, Model model) throws IOException {
		FastReader scan = new FastReader(filename);
		int nVertex = scan.nextInt();
		model.nVertex = nVertex;
		model.vertices = new float[nVertex][3];

		// モデルの最大半径を算出
		double maxDist = 0;
		for (int i = 0; i < nVertex; i++) {
			scan.nextInt();
			for (int j = 0; j < 3; j++) {
				model.vertices[i][j] = scan.nextFloat();
			}
			float x = model.vertices[i][0];
			float y = model.vertices[i][1];
			double dist = x * x + y * y;
			if (maxDist < dist) {
				maxDist = dist;
			}
		}
		model.setSize(Math.sqrt(maxDist) * 2);

		int nFace = scan.nextInt();
		model.nFace = nFace;

		model.faces = new int[nFace][3];
		for (int i = 0; i < nFace; i++) {
			scan.nextInt();
			model.faces[i][0] = scan.nextInt();
			model.faces[i][1] = scan.nextInt();
			model.faces[i][2] = scan.nextInt();
		}
		scan.close();

		makeNormalUV(model, nFace);
	}

	public static void makeNormalUV(Model model, int nFace) {
		float uv[][] = new float[nFace][6];
		model.nNormal = nFace;
		model.normals = new float[nFace][3];
		model.normalIndex = new int[3 * nFace];
		model.point = new float[nFace][3];
		model.info = new LatLon[nFace];

		for (int i = 0; i < (nFace); i++) {
			// @formatter:off
			Point3f p0 = new Point3f(
					model.vertices[(model.faces[i][0] - 1)][0],
					model.vertices[(model.faces[i][0] - 1)][1],
					model.vertices[(model.faces[i][0] - 1)][2]);
			Point3f p1 = new Point3f(
					model.vertices[(model.faces[i][1] - 1)][0],
					model.vertices[(model.faces[i][1] - 1)][1],
					model.vertices[(model.faces[i][1] - 1)][2]);
			Point3f p2 = new Point3f(
					model.vertices[(model.faces[i][2] - 1)][0],
					model.vertices[(model.faces[i][2] - 1)][1],
					model.vertices[(model.faces[i][2] - 1)][2]);
			// @formatter:on

			model.point[i][0] = (p0.x + p1.x + p2.x) / 3;
			model.point[i][1] = (p0.y + p1.y + p2.y) / 3;
			model.point[i][2] = (p0.z + p1.z + p2.z) / 3;
			model.info[i] = new LatLon(model.point[i][0], model.point[i][1], model.point[i][2]);

			Vector3f v1 = new Vector3f(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
			Vector3f v2 = new Vector3f(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z);

			Vector3f normal = new Vector3f();

			normal.cross(v1, v2);
			normal.normalize();

			model.normals[i][0] = normal.x;
			model.normals[i][1] = normal.y;
			model.normals[i][2] = normal.z;
			model.normalIndex[(i * 3) + 0] = i;
			model.normalIndex[(i * 3) + 1] = i;
			model.normalIndex[(i * 3) + 2] = i;

			// UV
			LatLon ll = new LatLon(p0.x, p0.y, p0.z);
			uv[i][0] = (float) ll.longitude / 360;
			uv[i][1] = (float) (ll.latitude + 90) / 180;
			ll = new LatLon(p1.x, p1.y, p1.z);
			uv[i][2] = (float) ll.longitude / 360;
			uv[i][3] = (float) (ll.latitude + 90) / 180;
			ll = new LatLon(p2.x, p2.y, p2.z);
			uv[i][4] = (float) ll.longitude / 360;
			uv[i][5] = (float) (ll.latitude + 90) / 180;
			// 端の処理
			int idxs[] = { 0, 2, 4, 0, 2 };
			for (int j = 0; j < 3; j++) {
				if (uv[i][idxs[j]] > 0.9) {
					for (int k = 0; k < 2; k++) {
						if (uv[i][idxs[j + k + 1]] < 0.1) {
							uv[i][idxs[j + k + 1]] = 1 + uv[i][idxs[j + k + 1]];
						}
					}
				}
			}
		}
		model.uv = uv;
	}

}
