package aigis.model;

public class LatLon {
	public double latitude; // 緯度
	public double longitude; // 経度
	public float distance; // 中心からの距離
	public float xPosition; // x座標
	public float yPosition; // y座標
	public float zPosition; // z座標

	public LatLon(double d, double e) {
		latitude = d;
		longitude = e;
	}

	public LatLon(float x, float y, float z) {
		latitude = (float) Math.atan2(z, Math.sqrt(x * x + y * y)) * 180 / (float) Math.PI;
		longitude = (float) Math.atan2(y, x) * 180 / (float) Math.PI;
		distance = (float) Math.sqrt(x * x + y * y + z * z);
		xPosition = x;
		yPosition = y;
		zPosition = z;
		if (longitude < 0)
			longitude = 360 + longitude;
	}
}
