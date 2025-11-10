package aigis.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.OverlayChangeListener;
import org.jfree.chart.panel.AbstractOverlay;
import org.jfree.chart.panel.Overlay;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;

import aigis.App;
import aigis.Const;
import aigis.Logger;
import aigis.model.ChartData;

public class MainChartPanel extends JPanel implements ChartMouseListener, ActionListener {

	private static final long serialVersionUID = 1420405664687350894L;

	private ChartData data;
	private ChartPanel chartPanel;
	private InfoOverlay infoOverlay;
	// private int _polygonID;
	private boolean isTime = false;
	private String[] xColumns;
	private String[] yRows;
	private List<Integer> polygonList = new ArrayList<>();
	private List<String[]> xColumnsList = new ArrayList<>();
	private List<String[]> yRowsList = new ArrayList<>();

	private JButton jbuttonChart = new JButton("図表を保存");

	/** 情報Window表示用のデータ */
	class InfoValue {
		public Color color;
		public String title;
		public String value;
	}

	/**
	 * 情報Window表示用Overlay
	 */
	class InfoOverlay extends AbstractOverlay implements Overlay {

		private double basePosX;
		private double basePosY;
		private String title;
		private InfoValue[] values;

		public void setLocation(double x, double y) {
			basePosX = x;
			basePosY = y;
		}

		public void setValues(String title, InfoValue[] values) {
			this.title = title;
			this.values = values;
		}

		public String gettitle() {
			return this.title;
		}

		@Override
		public void addChangeListener(OverlayChangeListener arg0) {
		}

		@Override
		public void paintOverlay(Graphics2D g2, ChartPanel chartPanel) {

			if (title == null)
				return;

			Shape savedClip = g2.getClip();
			Rectangle2D dataArea = chartPanel.getScreenDataArea();
			
			g2.clip(dataArea);
			g2.setStroke(new BasicStroke(1.0f));

			int width = 180;
			int height = 20;

			// 位置調整
			int offsetPosX = (int) basePosX + 10;
			int offsetPosY = (int) basePosY + 30;
			if (offsetPosX + width + 10 > dataArea.getMaxX()) {
				offsetPosX = (int) (dataArea.getMaxX()) - width - 10;
			}
			if (offsetPosY + height * (values.length + 1) + 10 > dataArea.getMaxY()) {
				offsetPosY = (int) (dataArea.getMaxY()) - height * (values.length + 1) - 10;
			}

			// 全体のタイトル
			Shape titleArea = new Rectangle((int) offsetPosX, (int) offsetPosY, width, height);
			g2.setClip(offsetPosX, offsetPosY, width + 1, height + 1);
			g2.setPaint(Color.LIGHT_GRAY);
			g2.fill(titleArea);
			g2.draw(titleArea);
			g2.setPaint(Color.WHITE);
			TextUtilities.drawAlignedString(title, g2, offsetPosX + 6, offsetPosY + 3, TextAnchor.TOP_LEFT);

			for (int i = 0; i < values.length; i++) {
				// タイトル
				InfoValue value = values[i];
				int xx = (int) offsetPosX;
				int yy = (int) offsetPosY + height * (i + 1);
				Shape area = new Rectangle(xx, yy, width / 2, height);
				g2.setClip(xx, yy, width / 2 + 1, height + 1);
				g2.setPaint(Color.WHITE);
				g2.fill(area);
				g2.setPaint(Color.LIGHT_GRAY);
				g2.draw(area);
				g2.setPaint(value.color);
				Shape colorArea = new Rectangle(xx + 6, yy + 6, height / 2, height / 2);
				g2.fill(colorArea);
				g2.setPaint(Color.GRAY);
				TextUtilities.drawAlignedString(value.title, g2, xx + 20, yy + 3, TextAnchor.TOP_LEFT);
				// データ
				xx += width / 2;
				area = new Rectangle(xx, yy, width / 2, height);
				g2.setClip(xx, yy, width / 2 + 1, height + 1);
				g2.setPaint(Color.WHITE);
				g2.fill(area);
				g2.setPaint(Color.LIGHT_GRAY);
				g2.draw(area);
				g2.setPaint(Color.GRAY);
				TextUtilities.drawAlignedString(value.value, g2, xx + 6, yy + 3, TextAnchor.TOP_LEFT);
			}
			g2.setClip(savedClip);
		}

		@Override
		public void removeChangeListener(OverlayChangeListener arg0) {
		}

	}

	/**
	 * グラフ表示用パネル
	 */
	public MainChartPanel() {
		// Chart準備
		// initChart(true);
	}

	// タイトル、ラベルを設定
	public void setChartInfo(String title, String xLabel, String yLabel) {
		chartPanel.getChart().setTitle(title);
		XYPlot xyplot = (XYPlot) chartPanel.getChart().getPlot();
		xyplot.getDomainAxis().setLabel(xLabel);
		xyplot.getRangeAxis().setLabel(yLabel);
	}

	// 表示データをクリア
	public void clearData() {
		XYPlot plot = chartPanel.getChart().getXYPlot();
		plot.setDataset(null);
		plot.setDomainCrosshairValue(0);
		infoOverlay.setValues(null, null);
		polygonList.clear();
		xColumnsList.clear();
		yRowsList.clear();
	}

	// 表示データを設定
	public void setData(ChartData data) {
		if (this.data != null && this.data.mapName.equals(data.mapName)) {
			return;
		}
		this.initChart(data.isTimeFormat);
		this.data = data;
	}

	// 表示対象PolygonIDを追加
	public void addPolygonID(Integer polygonID) {
		// チャート初期化
		// initChart(data.isTimeFormat);

		// 出力用に保持
		// _polygonID = polygonID+1;
		polygonList.add(polygonID + 1);
		if (data == null)
			return;
		if (data.data.length < polygonID)
			return;
		XYPlot plot = chartPanel.getChart().getXYPlot();
		if (data.isTimeFormat) {
			TimeSeriesCollection dataset = (TimeSeriesCollection) plot.getDataset();
			if (dataset == null) {
				dataset = new TimeSeriesCollection();
			}
			@SuppressWarnings("非推奨")
			TimeSeries series2 = new TimeSeries("", Second.class);
			xColumns = new String[data.timeRange[polygonID].length];
			yRows = new String[data.timeRange[polygonID].length];

			for (int x = 0; x < data.timeRange[polygonID].length; x++) {
				Date date = new Date(data.timeRange[polygonID][x] * 1000);// unixtimeは1000かける必要がある
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				int month = cal.get(Calendar.MONTH) + 1;// monthは-1された値が取得されるため+1で調整する必要がある
				int year = cal.get(Calendar.YEAR);
				int day = cal.get(Calendar.DATE);
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int minute = cal.get(Calendar.MINUTE);
				int second = cal.get(Calendar.SECOND);

				series2.addOrUpdate(new Second(second, minute, hour, day, month, year), data.data[polygonID][x]);

				// 出力用に保持
				xColumns[x] = String.valueOf(data.data[polygonID][x]);
				// 時間を整形してから格納
				SimpleDateFormat sdf = new SimpleDateFormat(Const.DATE_FORMAT);
				yRows[x] = String.valueOf(sdf.format(cal.getTime()) + ":00");// :00がなくなるため追加する

			}

			series2.setDescription("" + (polygonID + 1));
			dataset.addSeries(series2);
			chartPanel.getChart().getXYPlot().setDataset(dataset);

		} else {

			XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
			if (dataset == null) {
				dataset = new XYSeriesCollection();
			}
			if (dataset.getSeriesIndex(polygonID.toString()) >= 0) {
				return;
			}
			XYSeries series = new XYSeries(polygonID.toString());
			xColumns = new String[data.range[polygonID].length];
			yRows = new String[data.range[polygonID].length];

			for (int x = 0; x < data.range[polygonID].length; x++) {
				series.add(data.range[polygonID][x], data.data[polygonID][x]);

				// 出力用に保持
				xColumns[x] = String.valueOf(data.data[polygonID][x]);
				yRows[x] = String.valueOf(data.range[polygonID][x]);
			}
			series.setDescription("" + (polygonID + 1));
			dataset.addSeries(series);
			chartPanel.getChart().getXYPlot().setDataset(dataset);
		}

		// yRowsとXcolumnsをlistにもつ
		xColumnsList.add(xColumns);
		yRowsList.add(yRows);
		infoOverlay.setValues(null, null);
	}

	public void saveChart() {
		// テキストファイルに書き込み
		try {
			String path = "";
			// 設定ファイル読み込み
			String saveGraphDataPath = App.getProp(Const.SAVE_GRAPH_PATH_KEY);
			if (saveGraphDataPath == null) {
				path = System.getProperty("user.home");
			} else {
				File checkedfile = new File(saveGraphDataPath);
				// propertiesのパスにディレクトリが存在しない場合またはカラの場合はホームディレクトリから開く
				if (checkedfile.exists() == false || saveGraphDataPath.equals("")) {
					path = System.getProperty("user.home");
				} else {
					path = saveGraphDataPath;
				}
			}

			// ファイル名をつけて保存
			path = path + File.separator + "graphdata.txt";

			File file = new File(path);
			FileWriter filewriter = new FileWriter(file, true);
			// filewriter.write("polygonID: " + _polygonID+ "\n");
			// filewriter.write("x: " + infoOverlay.gettitle() + "\n");
			// filewriter.write("y: " + infoOverlay.values[0].value + "\n");

			for (int h = 0; h < polygonList.size(); h++) {

				filewriter.write(String.valueOf(polygonList.get(h)));
				for (int i = 0; i < yRowsList.get(h).length; i++) {
					filewriter.write(" " + yRowsList.get(h)[i]);
				}
				filewriter.write("\n");
				filewriter.write(String.valueOf(polygonList.get(h)));
				for (int i = 0; i < xColumnsList.get(h).length; i++) {
					filewriter.write(" " + xColumnsList.get(h)[i]);
				}
				filewriter.write("\n");
			}

			filewriter.close();

			JOptionPane.showMessageDialog(this, "図表データが保存されました\n'" + path + "'", "完了",
					JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e) {
			Logger.Error(e);
			JOptionPane.showMessageDialog(this, "図表データを保存できません\n[" + e.getMessage() + "]", "エラー",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {

	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		Rectangle2D dataArea = chartPanel.getScreenDataArea();
		JFreeChart chart = event.getChart();
		XYPlot plot = chart.getXYPlot();
		ValueAxis xAxis = plot.getDomainAxis();
		// マウスのいる位置の一番近くにCrosshairを表示
		EntityCollection entities = chartPanel.getChartRenderingInfo().getEntityCollection();
		@SuppressWarnings("未確認")
		Iterator<ChartEntity> iterator = entities.iterator();
		int minDist = Integer.MAX_VALUE;
		int mouseX = event.getTrigger().getX();
		XYItemEntity target = null;
		while (iterator.hasNext()) {
			ChartEntity entity = iterator.next();
			if (entity instanceof XYItemEntity) {
				int dist = Math.abs(mouseX - (int) entity.getArea().getBounds().getMaxX());
				if (dist < minDist) {
					minDist = dist;
					target = (XYItemEntity) entity;
				}
			}
		}
		if (target == null)
			return;
		double x = xAxis.java2DToValue(target.getArea().getBounds().getCenterX(), dataArea,
				plot.getDomainAxisEdge());
		plot.setDomainCrosshairValue(x);
		// 情報を表示
		int itemID = target.getItem();
			
		String title = null;
		InfoValue values[] = null;
		if (isTime) {
			TimeSeriesCollection dataset = (TimeSeriesCollection) plot.getDataset();
			values = new InfoValue[dataset.getSeriesCount()];
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				Paint paint = plot.getRenderer().getSeriesPaint(i);
				TimeSeries series = dataset.getSeries(i);
				TimeSeriesDataItem item = series.getDataItem(itemID);
				InfoValue info = new InfoValue();
				info.color = (Color) paint;
				info.title = series.getDescription();
				info.value = item.getValue().toString();
				values[i] = info;
				if (i == target.getSeriesIndex()) {
					SimpleDateFormat sdf = new SimpleDateFormat(Const.DATE_FORMAT);
					title = String.valueOf(sdf.format(item.getPeriod().getMiddleMillisecond()) + ":00");// :00がなくなるため追加する
				}
			}
		} else {
			XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
			values = new InfoValue[dataset.getSeriesCount()];
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				Paint paint = plot.getRenderer().getSeriesPaint(i);
				XYSeries series = dataset.getSeries(i);
				XYDataItem item = series.getDataItem(itemID);
				InfoValue info = new InfoValue();
				info.color = (Color) paint;
				info.title = series.getDescription();
				info.value = item.getY().toString();
				values[i] = info;
				if (i == target.getSeriesIndex()) {
					title = item.getX().toString();
				}
			}
		}
		infoOverlay.setLocation(target.getArea().getBounds().getCenterX(), event.getTrigger().getY());
		infoOverlay.setValues(title, values);
	}

	public void initChart(boolean isTimeFormat) {
		// パラメータを保持
		isTime = isTimeFormat;
		// Chart準備
		JFreeChart chart;
		if (isTimeFormat) {
			chart = ChartFactory.createTimeSeriesChart(null, null, null, null);
		} else {
			chart = ChartFactory.createXYLineChart(null, null, null, null);
		}

		chart.removeLegend();
		if (chartPanel != null) {
			this.remove(chartPanel);
		}
		chartPanel = new ChartPanel(chart);
		chartPanel.addChartMouseListener(this);
		chartPanel.setMaximumDrawWidth(Const.CHART_MAX_SIZE);
		chartPanel.setMaximumDrawHeight(Const.CHART_MAX_SIZE);
		// 背景色設定
		XYPlot xyplot = (XYPlot) chart.getPlot();
		xyplot.setBackgroundPaint(Color.WHITE);
		xyplot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		// データ位置のドット表示
		XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer();
		xylineandshaperenderer.setBaseShapesVisible(true);
		xyplot.setRenderer(0, xylineandshaperenderer);
		// Crosshair表示
		xyplot.setDomainCrosshairVisible(true);
		xyplot.setRangeCrosshairLockedOnData(true);
		// パネルに追加
		this.setLayout(new BorderLayout());
		this.add(chartPanel, BorderLayout.CENTER);
		// 情報表示パネル用Overlay
		infoOverlay = new InfoOverlay();
		chartPanel.addOverlay(infoOverlay);

		chartPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		chartPanel.add(jbuttonChart);
		jbuttonChart.addActionListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object source = e.getSource();

		if (source == jbuttonChart) {
			saveChart();
		}
	}

}
