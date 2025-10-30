package aigis.model;

public class Light {
	private float[] lightSpecular = { 0, 0, 0, 1.0f }; // 反射光強度
	private float[] lightDiffuseDefault = { 0.65f, 0.65f, 0.65f, 1.0f };
	private float[] lightAmbientDefault = { 0.3f, 0.3f, 0.3f, 1.0f };
	private float[] lightDiffuseMin = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float[] lightAmbientMax = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float[] lightDiffuse = lightDiffuseDefault; // 拡散光強度
	private float[] lightAmbient = lightAmbientDefault; // 環境光強度

	public float[] getSpecular() {
		return lightSpecular;
	}

	public float[] getAmbient() {
		return lightAmbient;
	}

	public float[] getDiffuse() {
		return lightDiffuse;
	}

	public void changeDiffuse(boolean off) {
		if (off) {
			lightDiffuse = lightDiffuseMin;
			lightAmbient = lightAmbientMax;
		} else {
			lightDiffuse = lightDiffuseDefault;
			lightAmbient = lightAmbientDefault;
		}
	}

}
