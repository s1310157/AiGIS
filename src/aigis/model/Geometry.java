package aigis.model;

import java.awt.geom.Point2D;

public class Geometry {
	public static double cross(Point2D.Double a, Point2D.Double b) {
		return a.getX() * b.getY() - a.getY() * b.getX();
	}

	public static double dot(Point2D.Double a, Point2D.Double b) {
		return a.getX() * b.getX() + a.getY() * b.getY();
	}

	public static double norm(Point2D.Double a) {
		return a.distance(0, 0);
	}

	/**
	 * ccw 演算を行います。 ccw 演算は与えられた点a,b,cによって以下の値を返します。
	 * 
	 * @param a
	 *            点a
	 * @param b
	 *            点b
	 * @param c
	 *            点c
	 * @return ccw 演算の結果。 点cが直線abの 左側にあるとき 1, 右側にあるとき -1で, 半直線ba上であって線分ab上にないとき 2,
	 *         半直線ab上であって線分ab上にないとき -2, それ以外の(線分ab上に存在する)とき 0 を返します。
	 */
	public static int ccw(Point2D.Double a, Point2D.Double b, Point2D.Double c) {
		Point2D.Double B = new Point2D.Double(b.getX() - a.getX(), b.getY() - a.getY());
		Point2D.Double C = new Point2D.Double(c.getX() - a.getX(), c.getY() - a.getY());
		if (cross(B, C) > 1e-11)
			return 1;
		if (cross(B, C) < -1e-11)
			return -1;
		if (dot(B, C) < -1e-11)
			return 2;
		if (norm(B) < norm(C) - 1e-11)
			return -2;
		return 0;
	}

	public static boolean intersectedSS(Point2D.Double a, Point2D.Double b, Point2D.Double c, Point2D.Double d) {
		return ccw(a, b, c) * ccw(a, b, d) <= 0 && ccw(c, d, a) * ccw(c, d, b) <= 0;
	}

	/**
	 * ある点Pが三角形の内側にあるかどうか判定します。 判定は ccw(triangle[i],triangle[i+1],P) が
	 * すべてのiについて等しいかあるいは点Pが三角形の返上に存在するかどうかによって行います。
	 * 
	 * @param tri
	 *            三角形
	 * @param mp
	 *            点P
	 * @return 点Pが三角形の内側にあるとき true, それ以外のとき false
	 */
	public static boolean triangleInside2(double[] tri, Point2D.Double mp) {
		Point2D.Double[] triangle = new Point2D.Double[3];
		triangle[0] = new Point2D.Double(tri[0], tri[1]);
		triangle[1] = new Point2D.Double(tri[3], tri[4]);
		triangle[2] = new Point2D.Double(tri[6], tri[7]);
		int cw = ccw(triangle[0], triangle[1], mp);
		for (int i = 1; i < 3; ++i) {
			int tmp = ccw(triangle[i], triangle[(i + 1) % 3], mp);
			if (!(cw == tmp || tmp == 0))
				return false;
		}
		return true;
	}

	/**
	 * ある点Pが三角形の内側にあるかどうか判定します。 判定は点Pと三角形の重心を結ぶ線分が三角形の辺と交わるかどうかによって行います。
	 * このメソッドは非推奨です。
	 * 
	 * @param _3DTriangle
	 *            三角形
	 * @param mp
	 *            点P
	 * @return 点Pが三角形の内側にあるとき true, それ以外のとき false
	 */
	@Deprecated
	public static boolean triangleInside(double[] _3DTriangle, Point2D.Double mp) {
		Point2D.Double gravityCenter = new Point2D.Double((_3DTriangle[0] + _3DTriangle[3] + _3DTriangle[6]) / 3.d,
				(_3DTriangle[1] + _3DTriangle[4] + _3DTriangle[7]) / 3.d);

		Point2D.Double[] triangle = new Point2D.Double[3];
		triangle[0] = new Point2D.Double(_3DTriangle[0], _3DTriangle[1]);
		triangle[1] = new Point2D.Double(_3DTriangle[3], _3DTriangle[4]);
		triangle[2] = new Point2D.Double(_3DTriangle[6], _3DTriangle[7]);

		if (intersectedSS(triangle[0], triangle[1], gravityCenter, mp))
			return false;
		if (intersectedSS(triangle[1], triangle[2], gravityCenter, mp))
			return false;
		if (intersectedSS(triangle[2], triangle[0], gravityCenter, mp))
			return false;
		return true;
	}
}

// EOF
