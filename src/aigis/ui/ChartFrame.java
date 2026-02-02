package aigis.ui;

import java.awt.Dimension;

import javax.swing.JFrame;

import aigis.i18n.I18n;
import static aigis.i18n.I18n.t;

import aigis.Const;

public class ChartFrame {

	private JFrame frame = new JFrame("AiGIS " + t("j.chart"));
	private MainChartPanel chartPanel = new MainChartPanel();

	public ChartFrame() {
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				chartPanel.clearData();
			}
		});
		 frame.setContentPane(chartPanel);
		 frame.pack();
		 frame.setSize(600, 400);
		 frame.setMaximumSize(new Dimension(Const.CHART_MAX_SIZE, Const.CHART_MAX_SIZE));
	}

	public void setVisible(boolean visible) {
		frame.setVisible(visible);
	}

	public MainChartPanel getPanel() {
		return chartPanel;
	}

}
