package aigis.model;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;

import aigis.Logger;

public class Rotate implements Cloneable {

	private Matrix4f matrix = new Matrix4f();

	public Rotate() {
		matrix.setIdentity();
	}

	@Override
	public Rotate clone() {
		Rotate r = null;
		try {
			r = (Rotate) super.clone();
			r.matrix = new Matrix4f(this.matrix);
		} catch (CloneNotSupportedException e) {
			Logger.Error(e);
		}
		return r;
	}

	public float[] getMatrix() {
		float mtx[] = new float[16];
		for (int i = 0; i < 16; i++) {
			mtx[i] = matrix.getElement(i / 4, i % 4);
		}
		return mtx;
	}

	public float[] getInvertMatrix() {
		Matrix4f m = new Matrix4f(matrix);
		m.invert();
		float mtx[] = new float[16];
		for (int i = 0; i < 16; i++) {
			mtx[i] = m.getElement(i / 4, i % 4);
		}
		return mtx;
	}
	
	public void rotate(float x, float y, float z, boolean first) {
		Matrix4f m = new Matrix4f();
		float ang = (float) Math.sqrt(x * x + y * y + z * z);
		m.set(new AxisAngle4f(x, y, z, ang / 100));
		if (first) {
			m.mul(this.matrix);
			this.matrix = m;
		} else {
			this.matrix.mul(m);
		}
	}

	public void rotate(float dx, float dy, Rotate rotate) {
		Matrix4f m = new Matrix4f(rotate.matrix);
		Point3f p = new Point3f();
		m.transform(new Point3f(-dx, -dy, 0), p);
		float ang = (float) Math.sqrt(dx * dx + dy * dy);
		m.set(new AxisAngle4f(p.x, p.y, p.z, ang / 100));
		matrix.mul(m);
	}

	public void moveXZ(float lat, float lon, Rotate rotate) {
		matrix.rotX((float) Math.toRadians(lat - 90));
		Matrix4f m = new Matrix4f();
		m.rotZ((float) Math.toRadians(-lon - 90));
		matrix.mul(m);
		if (rotate != null) {
			matrix.mul(rotate.matrix);
		}
	}

	public void moveZX(float lat, float lon) {
		matrix.rotZ((float) Math.toRadians(lon + 90));
		Matrix4f m = new Matrix4f();
		m.rotX((float) Math.toRadians(-lat + 90));
		matrix.mul(m);
	}

	public LatLon getLatLon(boolean invert, Rotate rotate) {
		Point4f p = new Point4f();
		Matrix4f m1 = new Matrix4f(matrix);
		if (rotate != null) {
			Matrix4f m2 = new Matrix4f(rotate.matrix);
			m2.invert();
			m1.mul(m2);
		}
		if (invert)
			m1.invert();
		m1.transform(new Point4f(0, 0, 1, 1), p);

		float lat = (float) Math.atan2(p.z, Math.sqrt(p.x * p.x + p.y * p.y)) * 180 / (float) Math.PI;
		float lng = (float) Math.atan2(p.y, p.x) * 180 / (float) Math.PI;
		if (lng < 0) {
			lng = 360 + lng;
		}
		if (lat > -0.00001 && lat < 0.00001) {
			lat = 0;
		}
		if (lng > -0.00001 && lng < 0.00001) {
			lng = 0;
		}
		return new LatLon(lat, lng);
	}
}
