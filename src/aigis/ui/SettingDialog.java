package aigis.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import aigis.App;
import aigis.Const;

import aigis.i18n.I18n;
import static aigis.i18n.I18n.t;

@SuppressWarnings("serial")
public class SettingDialog extends JDialog {
	private JTextField textDataPath;
	private JTextField textPngPath;
	private JTextField textGraphPath;
	private JTextField textLookUpPath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SettingDialog dialog = new SettingDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SettingDialog(Frame owner) {
		super(owner);
		setModal(true);
		setResizable(false);
		setTitle(t("j.setting"));
		setSize(450, 277);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						App.setProp(Const.DATA_PATH_KEY, textDataPath.getText());
						App.setProp(Const.SS_PATH_KEY, textPngPath.getText());
						App.setProp(Const.SAVE_GRAPH_PATH_KEY, textGraphPath.getText());
						App.setProp(Const.LOOKUP_PATH_KEY, textLookUpPath.getText());
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(t("j.cancel"));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand(t("j.cancel"));
				buttonPane.add(cancelButton);
			}
		}
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 20, 0, 10, 0, 30, 20 };
			gbl_panel.rowHeights = new int[] { 20, 40, 40, 40, 40, 0 };
			gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0 };
			gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gbl_panel);
			{
				JLabel lblDataPath = new JLabel(t("j.datapath"));
				GridBagConstraints gbc_lblDataPath = new GridBagConstraints();
				gbc_lblDataPath.insets = new Insets(0, 0, 5, 5);
				gbc_lblDataPath.gridx = 1;
				gbc_lblDataPath.gridy = 1;
				panel.add(lblDataPath, gbc_lblDataPath);
			}
			{
				textDataPath = new JTextField();
				GridBagConstraints gbc_textDataPath = new GridBagConstraints();
				gbc_textDataPath.insets = new Insets(0, 0, 5, 5);
				gbc_textDataPath.fill = GridBagConstraints.HORIZONTAL;
				gbc_textDataPath.gridx = 3;
				gbc_textDataPath.gridy = 1;
				panel.add(textDataPath, gbc_textDataPath);
				textDataPath.setColumns(10);
				textDataPath.setText(App.getProp(Const.DATA_PATH_KEY));
			}
			{
				JButton btnDataPath = new JButton("...");
				btnDataPath.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = App.showOpenDialog(owner, null, JFileChooser.DIRECTORIES_ONLY, null,
								true);
						if (chooser != null) {
							textDataPath.setText(chooser.getSelectedFile().getAbsolutePath());
						}
					}
				});
				btnDataPath.setPreferredSize(new Dimension(30, 21));
				GridBagConstraints gbc_btnDataPath = new GridBagConstraints();
				gbc_btnDataPath.insets = new Insets(0, 0, 5, 5);
				gbc_btnDataPath.gridx = 4;
				gbc_btnDataPath.gridy = 1;
				panel.add(btnDataPath, gbc_btnDataPath);
			}
			{
				JLabel lblPngPath = new JLabel(t("j.destinationpath"));
				GridBagConstraints gbc_lblPngPath = new GridBagConstraints();
				gbc_lblPngPath.insets = new Insets(0, 0, 5, 5);
				gbc_lblPngPath.gridx = 1;
				gbc_lblPngPath.gridy = 2;
				panel.add(lblPngPath, gbc_lblPngPath);
			}
			{
				textPngPath = new JTextField();
				GridBagConstraints gbc_textPngPath = new GridBagConstraints();
				gbc_textPngPath.insets = new Insets(0, 0, 5, 5);
				gbc_textPngPath.fill = GridBagConstraints.HORIZONTAL;
				gbc_textPngPath.gridx = 3;
				gbc_textPngPath.gridy = 2;
				panel.add(textPngPath, gbc_textPngPath);
				textPngPath.setColumns(10);
				textPngPath.setText(App.getProp(Const.SS_PATH_KEY));
			}
			{
				JButton btnPngPath = new JButton("...");
				btnPngPath.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = App.showOpenDialog(owner, null, JFileChooser.DIRECTORIES_ONLY, null,
								true);
						if (chooser != null) {
							textPngPath.setText(chooser.getSelectedFile().getAbsolutePath());
						}
					}
				});
				btnPngPath.setPreferredSize(new Dimension(30, 21));
				GridBagConstraints gbc_btnPngPath = new GridBagConstraints();
				gbc_btnPngPath.insets = new Insets(0, 0, 5, 5);
				gbc_btnPngPath.gridx = 4;
				gbc_btnPngPath.gridy = 2;
				panel.add(btnPngPath, gbc_btnPngPath);
			}
			{
				JLabel lblGraphPath = new JLabel(t("j.graphdatapath"));
				GridBagConstraints gbc_lblGraphPath = new GridBagConstraints();
				gbc_lblGraphPath.insets = new Insets(0, 0, 5, 5);
				gbc_lblGraphPath.gridx = 1;
				gbc_lblGraphPath.gridy = 3;
				panel.add(lblGraphPath, gbc_lblGraphPath);
			}
			{
				textGraphPath = new JTextField();
				GridBagConstraints gbc_textGraphPath = new GridBagConstraints();
				gbc_textGraphPath.insets = new Insets(0, 0, 5, 5);
				gbc_textGraphPath.fill = GridBagConstraints.HORIZONTAL;
				gbc_textGraphPath.gridx = 3;
				gbc_textGraphPath.gridy = 3;
				panel.add(textGraphPath, gbc_textGraphPath);
				textGraphPath.setColumns(10);
				textGraphPath.setText(App.getProp(Const.SAVE_GRAPH_PATH_KEY));
			}
			{
				JButton btnGraphPath = new JButton("...");
				btnGraphPath.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = App.showOpenDialog(owner, null, JFileChooser.DIRECTORIES_ONLY, null,
								true);
						if (chooser != null) {
							textGraphPath.setText(chooser.getSelectedFile().getAbsolutePath());
						}
					}
				});
				btnGraphPath.setPreferredSize(new Dimension(30, 21));
				GridBagConstraints gbc_btnGraphPath = new GridBagConstraints();
				gbc_btnGraphPath.insets = new Insets(0, 0, 5, 5);
				gbc_btnGraphPath.gridx = 4;
				gbc_btnGraphPath.gridy = 3;
				panel.add(btnGraphPath, gbc_btnGraphPath);
			}
			{
				JLabel lblLookUpPath = new JLabel(t("j.lookupdatapath"));
				GridBagConstraints gbc_lblLookUpPath = new GridBagConstraints();
				gbc_lblLookUpPath.insets = new Insets(0, 0, 0, 5);
				gbc_lblLookUpPath.gridx = 1;
				gbc_lblLookUpPath.gridy = 4;
				panel.add(lblLookUpPath, gbc_lblLookUpPath);
			}
			{
				textLookUpPath = new JTextField();
				GridBagConstraints gbc_textLookUpPath = new GridBagConstraints();
				gbc_textLookUpPath.insets = new Insets(0, 0, 0, 5);
				gbc_textLookUpPath.fill = GridBagConstraints.HORIZONTAL;
				gbc_textLookUpPath.gridx = 3;
				gbc_textLookUpPath.gridy = 4;
				panel.add(textLookUpPath, gbc_textLookUpPath);
				textLookUpPath.setColumns(10);
				textLookUpPath.setText(App.getProp(Const.LOOKUP_PATH_KEY));
			}
			{
				JButton btnNewButton = new JButton("...");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						JFileChooser chooser = App.showOpenDialog(owner, null, JFileChooser.DIRECTORIES_ONLY, null,
								true);
						if (chooser != null) {
							textLookUpPath.setText(chooser.getSelectedFile().getAbsolutePath());
						}
					}
				});
				btnNewButton.setPreferredSize(new Dimension(30, 21));
				GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
				gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
				gbc_btnNewButton.gridx = 4;
				gbc_btnNewButton.gridy = 4;
				panel.add(btnNewButton, gbc_btnNewButton);
			}
		}
	}

}
