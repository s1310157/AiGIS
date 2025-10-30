package aigis.model;

public class ChartData {
	public String mapName;
	public int nFace;
	public float[][] data;
	public String xLabel;
	public String yLabel;
	public float[][] range;

	// 時系列データ用
	public long[][] timeRange;

	// チャート表示判定用
	public boolean isTimeFormat;

}