package aigis.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import aigis.model.loader.PropLoader;

/**
 * Information on camera shooting
 */
public class CameraInfo implements Cloneable {
	public final Point3d position = new Point3d();
	public final Vector3d direction = new Vector3d();
	public final Vector3d up = new Vector3d();
	public final Point3f light = new Point3f();

	public double fov;
	public double roll;

	// Create objects for performance
	private final Vector3d vec3 = new Vector3d();
	private final Matrix4d mtx4 = new Matrix4d();
	private final Vector3d side = new Vector3d();

	public CameraInfo() {
		reset();
	}

	@Override
	public CameraInfo clone() {
		CameraInfo c = null;
		c = new CameraInfo();
		c.position.set(this.position);
		c.direction.set(this.direction);
		c.up.set(this.up);
		c.light.set(this.light);
		c.fov = this.fov;
		c.roll = this.roll;
		return c;
	}

	public Matrix4d lookat() {
		return lookat(new Matrix4d());
	}
	
	public Matrix4d lookatWithoutYaw() {
		CameraInfo info = this.clone();
		info.direction.set(position);
		info.direction.negate();
		info.direction.normalize();
		side.cross(info.direction, up);
		info.up.cross(side, info.direction);
		return info.lookat();
	}

	public Matrix4d lookat(Matrix4d mx) {
		side.cross(direction, up);
		side.normalize();

		// rotate
		mx.setIdentity();
		mx.m00 = side.x;
		mx.m01 = side.y;
		mx.m02 = side.z;

		mx.m10 = up.x;
		mx.m11 = up.y;
		mx.m12 = up.z;

		mx.m20 = -direction.x;
		mx.m21 = -direction.y;
		mx.m22 = -direction.z;

		// move
		mtx4.setIdentity();
		mtx4.m03 = -position.x;
		mtx4.m13 = -position.y;
		mtx4.m23 = -position.z;

		mx.mul(mtx4);

		return mx;
	}

	public float getDistance() {
		return (float) position.distance(new Point3d());
	}

	public double[] matrixToArray(Matrix4d m) {
		// @formatter:off
		return new double[] { 
				m.m00, m.m10, m.m20, m.m30, 
				m.m01, m.m11, m.m21, m.m31, 
				m.m02, m.m12, m.m22, m.m32, 
				m.m03, m.m13, m.m23, m.m33 };
		// @formatter:on
	}

	public double[] lookatArray() {
		return matrixToArray(lookat());
	}

	public void move(float x, float y) {

		if (x == 0 && y == 0) {
			return;
		}

		vec3.set(y, -x, 0);
		Matrix4d m = lookat();
		m.invert();
		m.transform(vec3);
		vec3.normalize();

		float ang = (float) Math.sqrt(x * x + y * y) / 10f;
		m.setIdentity();
		m.setRotation(new AxisAngle4d(vec3.x, vec3.y, vec3.z, ang));
		m.transform(direction);
		m.transform(up);
	}

	public void rotate(float x, float y, float z, boolean fixedAxis, boolean fixedLightPos) {

		if (x == 0 && y == 0 && z == 0) {
			return;
		}

		vec3.set(x, y, z);
		Matrix4d m;
		if (fixedAxis) {
			m = mtx4;
		} else {
			m = lookat();
			m.invert();
			m.transform(vec3);
		}

		vec3.normalize();
		float ang = (float) Math.sqrt(x * x + y * y + z * z) / 100;
		m.setIdentity();
		m.setRotation(new AxisAngle4d(vec3.x, vec3.y, vec3.z, ang));
		m.transform(position);
		m.transform(direction);
		m.transform(up);
		if (fixedLightPos) {
			m.transform(light);
		}

		calcRoll();
	}

	public void rotateLight(float x, float y, float z) {
		if (x == 0 && y == 0 && z == 0) {
			return;
		}

		vec3.set(x, y, z);
		Matrix4d m = lookat();
		m.invert();
		m.transform(vec3);
		vec3.normalize();
		float ang = (float) Math.sqrt(x * x + y * y + z * z) / 100;
		m.setIdentity();
		m.setRotation(new AxisAngle4d(vec3.x, vec3.y, vec3.z, ang));
		m.transform(light);
	}

	private LatLon getLatLng(double x, double y, double z) {
		double lat = Math.atan2(z, Math.sqrt(x * x + y * y));
		double lng = Math.atan2(y, x);
		if (lng < 0) {
			lng = lng + 2 * Math.PI;
		}
		return new LatLon(Math.toDegrees(lat), Math.toDegrees(lng));
	}

	public LatLon getLatLng() {
		return getLatLng(position.x, position.y, position.z);
	}

	public LatLon getLightLatLng() {
		return getLatLng(light.x, light.y, light.z);
	}

	public void moveLatLng(float lat, float lng, float roll) {
		
		this.roll = roll;

		Matrix4d m1 = lookatWithoutYaw();
		Matrix4d m2 = lookat();
		m2.invert();
		m1.mul(m2);

		double dist = position.distance(new Point3d());
		position.set(0, 0, dist);
		direction.set(0, 0, -1);
		up.set(0, 1, 0);

		Matrix4d m3 = mtx4;
		m3.setIdentity();
		m3.rotZ(Math.toRadians(lng + 90));
		Matrix4d m4 = new Matrix4d();
		m4.setIdentity();
		m4.rotX(Math.toRadians(-lat + 90));
		m3.mul(m4);

		m4.setIdentity();
		m4.setRotation(new AxisAngle4d(direction.x, direction.y, direction.z, -Math.toRadians(roll)));
		m3.mul(m4);

		m3.transform(position);
		m3.mul(m1);
		m3.transform(direction);
		m3.transform(up);

	}

	public void moveLightLatLng(float lat, float lng) {
		float dist = light.distance(new Point3f());
		light.set(0, 0, dist);

		Matrix4d m1 = mtx4;
		m1.rotZ(Math.toRadians(lng + 90));
		Matrix4d m2 = new Matrix4d();
		m2.rotX(Math.toRadians(-lat + 90));
		m1.mul(m2);
		m1.transform(light);
	}

	public Vector3d[] getFrustum() {
		Matrix4d mtx = lookat();
		mtx.invert();
		Vector3d ret[] = new Vector3d[4];
		double angle = (fov * Math.sqrt(2)) / 2;
		for (int i = 0; i < 2; i++) {
			double y = i == 0 ? -0.5 : 0.5;
			for (int j = 0; j < 2; j++) {
				double x = j == 0 ? -0.5 : 0.5;
				vec3.set(x, y, 0);
				mtx.transform(vec3);
				vec3.normalize();
				mtx4.setIdentity();
				mtx4.setRotation(new AxisAngle4d(vec3.x, vec3.y, vec3.z, angle));
				vec3.set(direction);
				mtx4.transform(vec3);
				vec3.normalize();
				ret[j + i * 2] = new Vector3d(vec3);
			}
		}
		return ret;
	}

	public void setDefaultSize(double size) {
		double ang = Math.toRadians(7.5) / 2;
		double z = (size / 2 * 1.4) / Math.tan(ang);
		position.set(0, 0, z);
	}

	public void reset() {
		position.set(0, 0, 10);
		direction.set(0, 0, -1);
		up.set(0, 1, 0);
		light.set(0, 0, 5000f);
		fov = Math.toRadians(7.5f);
		roll = 270;
	}
	
	private void calcRoll() {
		vec3.set(0, 0, 1);
		Matrix4d m = lookatWithoutYaw();
		m.transform(vec3);
		if (vec3.x == 0 && vec3.y == 0) {
			vec3.set(0, 1, 0);
			m.transform(vec3);
			roll = Math.PI * 2;
		} else {
			roll = Math.PI / 2;
		}
		roll -= Math.atan2(vec3.y, vec3.x);
		roll = Math.toDegrees(roll);
		if (roll < 0) {
			roll += 360;
		}
	}

	public static CameraInfo loadFromInfo(String path)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		PropLoader conf = new PropLoader();
		conf.load(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		CameraInfo info = new CameraInfo();
		info.position.set(convertStringToFloat(conf.getProperty("SPACECRAFT_POSITION")));
		info.direction.normalize();
		info.direction.set(convertStringToFloat(conf.getProperty("BORESIGHT_DIRECTION")));
		info.up.set(convertStringToFloat(conf.getProperty("UP_DIRECTION")));
		info.up.normalize();
		double frustum1[] = convertStringToFloat(conf.getProperty("FRUSTUM1"));
		double frustum2[] = convertStringToFloat(conf.getProperty("FRUSTUM2"));

		Vector3d vfs1 = new Vector3d(frustum1);
		Vector3d vfs2 = new Vector3d(frustum2);
		info.fov = vfs1.angle(vfs2);
		// Logger.Debug("fov:" + Math.toDegrees(info.fov));
		info.calcRoll();
		return info;
	}

	private static double[] convertStringToFloat(String str) {
		String replacedStr = str.replace("(", "");
		replacedStr = replacedStr.replace(")", "");
		String[] splitedStr = replacedStr.split(",", 0);
		double convertedDouble[] = { Double.parseDouble(splitedStr[0]), Double.parseDouble(splitedStr[1]),
				Double.parseDouble(splitedStr[2]) };
		return convertedDouble;
	}

	public double getFov() {
		return Math.toDegrees(fov);
	}

	public void setFov(float fov) {
		this.fov = Math.toRadians(fov);
	}
}
