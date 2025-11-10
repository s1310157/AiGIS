package aigis.ui;

import java.awt.Dimension;

import javax.swing.JFrame;

import aigis.Const;

public class ChartFrame {

	private JFrame frame = new JFrame("AiGIS -図表-");
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
