package aigis.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class RescaleRangeDialog extends JDialog {

	public abstract interface EventListener {
		public abstract void applay(double max, double min);
		public abstract void close();
		public abstract void reset();
	}

	private final JPanel contentPanel = new JPanel();
	private JTextField textMinimum;
	private JTextField textMaximum;

	private EventListener eventListener;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			RescaleRangeDialog dialog = new RescaleRangeDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public RescaleRangeDialog(Frame owner) {
		super(owner);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (eventListener != null) {
					eventListener.close();
				}
			}
		});
		setTitle("再スケーリング範囲");
		setResizable(false);
		setBounds(100, 100, 370, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 40, 62, 0, 40 };
		gbl_contentPanel.rowHeights = new int[] { 35, 26, 26, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblMinimum = new JLabel("最小");
			GridBagConstraints gbc_lblMinimum = new GridBagConstraints();
			gbc_lblMinimum.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblMinimum.insets = new Insets(0, 0, 5, 5);
			gbc_lblMinimum.gridx = 1;
			gbc_lblMinimum.gridy = 1;
			contentPanel.add(lblMinimum, gbc_lblMinimum);
		}
		{
			textMinimum = new JTextField();
			textMinimum.setText("0");
			textMinimum.setHorizontalAlignment(SwingConstants.TRAILING);
			GridBagConstraints gbc_textMinimum = new GridBagConstraints();
			gbc_textMinimum.anchor = GridBagConstraints.NORTH;
			gbc_textMinimum.fill = GridBagConstraints.HORIZONTAL;
			gbc_textMinimum.insets = new Insets(0, 0, 5, 5);
			gbc_textMinimum.gridx = 2;
			gbc_textMinimum.gridy = 1;
			contentPanel.add(textMinimum, gbc_textMinimum);
			textMinimum.setColumns(10);
		}
		{
			JLabel lblMaximum = new JLabel("最大");
			GridBagConstraints gbc_lblMaximum = new GridBagConstraints();
			gbc_lblMaximum.anchor = GridBagConstraints.WEST;
			gbc_lblMaximum.insets = new Insets(0, 0, 0, 5);
			gbc_lblMaximum.gridx = 1;
			gbc_lblMaximum.gridy = 2;
			contentPanel.add(lblMaximum, gbc_lblMaximum);
		}
		{
			textMaximum = new JTextField();
			textMaximum.setText("0");
			textMaximum.setHorizontalAlignment(SwingConstants.TRAILING);
			GridBagConstraints gbc_textMaximum = new GridBagConstraints();
			gbc_textMaximum.anchor = GridBagConstraints.NORTH;
			gbc_textMaximum.fill = GridBagConstraints.HORIZONTAL;
			gbc_textMaximum.insets = new Insets(0, 0, 0, 5);
			gbc_textMaximum.gridx = 2;
			gbc_textMaximum.gridy = 2;
			contentPanel.add(textMaximum, gbc_textMaximum);
			textMaximum.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.RIGHT);
			fl_buttonPane.setHgap(0);
			buttonPane.setLayout(fl_buttonPane);
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnApply = new JButton("適用");
				btnApply.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						applay();
					}
				});
				buttonPane.add(btnApply);
			}
			{
				JButton btnReset = new JButton("リセット");
				btnReset.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (eventListener != null) {
							eventListener.reset();
						}
					}
				});
				buttonPane.add(btnReset);
			}
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						applay();
						close();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("キャンセル");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						close();
					}
				});
				cancelButton.setActionCommand("キャンセル");
				buttonPane.add(cancelButton);
			}
		}
	}

	public void setValues(double max, double min) {
		DecimalFormat df = new DecimalFormat("####.##########");
		textMaximum.setText(df.format(max));
		textMinimum.setText(df.format(min));
	}

	public void setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
	}
	
	private void applay() {
		try {
			double dMin = Double.parseDouble(textMinimum.getText());
			double dMax = Double.parseDouble(textMaximum.getText());
			if (eventListener != null) {
				eventListener.applay(dMax, dMin);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "無効なデータです。数値を入力してください！！", "エラー",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void close() {
		setVisible(false);
		if (eventListener != null) {
			eventListener.close();
		}
	}
}
