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
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import aigis.App;

@SuppressWarnings("serial")
public class OpenImageDialog extends JDialog {

	public File imageFile;
	public File infoFile;
	public boolean canceled = true;
	public int rotateAngle = 0;
	public int flipType = 0;

	private final JPanel contentPanel = new JPanel();
	private JTextField textImage;
	private JTextField textInfo;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JButton btnOpenInfo;
	private JRadioButton rdbtnImage;
	private JRadioButton rdbtnMap;
	private JButton okButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			OpenImageDialog dialog = new OpenImageDialog(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public OpenImageDialog(Frame owner, String imageMapPath) {
		super(owner);
		setResizable(false);
		setModal(true);
		setTitle("Open Image");
		setBounds(100, 100, 370, 220);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 30, 50, 0, 20, 30 };
		gbl_contentPanel.rowHeights = new int[] { 35, 26, 26, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblImage = new JLabel("Image");
			GridBagConstraints gbc_lblImage = new GridBagConstraints();
			gbc_lblImage.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblImage.insets = new Insets(0, 0, 5, 5);
			gbc_lblImage.gridx = 1;
			gbc_lblImage.gridy = 1;
			contentPanel.add(lblImage, gbc_lblImage);
		}
		{
			textImage = new JTextField();
			textImage.setEditable(false);
			GridBagConstraints gbc_textImage = new GridBagConstraints();
			gbc_textImage.insets = new Insets(0, 0, 5, 0);
			gbc_textImage.fill = GridBagConstraints.HORIZONTAL;
			gbc_textImage.gridx = 2;
			gbc_textImage.gridy = 1;
			contentPanel.add(textImage, gbc_textImage);
			textImage.setColumns(10);
		}
		{
			JButton btnOpenImage = new JButton("...");
			btnOpenImage.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "fit", "fits");
					JFileChooser chooser = App.showOpenDialog(OpenImageDialog.this, imageMapPath,
							JFileChooser.FILES_ONLY, filter, true);
					if (chooser != null) {
						imageFile = chooser.getSelectedFile();
						textImage.setText(chooser.getName(imageFile));
						checkValues();
					}
				}
			});
			btnOpenImage.setPreferredSize(new Dimension(26, 20));
			GridBagConstraints gbc_btnOpenImage = new GridBagConstraints();
			gbc_btnOpenImage.insets = new Insets(0, 0, 5, 5);
			gbc_btnOpenImage.gridx = 3;
			gbc_btnOpenImage.gridy = 1;
			contentPanel.add(btnOpenImage, gbc_btnOpenImage);
		}
		{
			JLabel lblInfo = new JLabel("Info/SUM");
			GridBagConstraints gbc_lblInfo = new GridBagConstraints();
			gbc_lblInfo.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblInfo.anchor = GridBagConstraints.EAST;
			gbc_lblInfo.insets = new Insets(0, 0, 5, 5);
			gbc_lblInfo.gridx = 1;
			gbc_lblInfo.gridy = 2;
			contentPanel.add(lblInfo, gbc_lblInfo);
		}
		{
			textInfo = new JTextField();
			textInfo.setEditable(false);
			GridBagConstraints gbc_textInfo = new GridBagConstraints();
			gbc_textInfo.insets = new Insets(0, 0, 5, 0);
			gbc_textInfo.fill = GridBagConstraints.HORIZONTAL;
			gbc_textInfo.gridx = 2;
			gbc_textInfo.gridy = 2;
			contentPanel.add(textInfo, gbc_textInfo);
			textInfo.setColumns(10);
		}
		{
			btnOpenInfo = new JButton("...");
			btnOpenInfo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Info/SUM file", "info", "txt", "sum");
					JFileChooser chooser = App.showOpenDialog(OpenImageDialog.this, imageMapPath,
							JFileChooser.FILES_ONLY, filter, true);
					if (chooser != null) {
						infoFile = chooser.getSelectedFile();
						textInfo.setText(chooser.getName(infoFile));
						checkValues();
					}
				}
			});
			btnOpenInfo.setPreferredSize(new Dimension(26, 20));
			GridBagConstraints gbc_btnOpenInfo = new GridBagConstraints();
			gbc_btnOpenInfo.insets = new Insets(0, 0, 5, 5);
			gbc_btnOpenInfo.gridx = 3;
			gbc_btnOpenInfo.gridy = 2;
			contentPanel.add(btnOpenInfo, gbc_btnOpenInfo);
		}
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.gridwidth = 3;
			gbc_panel.insets = new Insets(0, 0, 0, 5);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 1;
			gbc_panel.gridy = 3;
			contentPanel.add(panel, gbc_panel);
			{
				rdbtnImage = new JRadioButton("Image");
				rdbtnImage.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						btnOpenInfo.setEnabled(true);
						checkValues();
					}
				});
				rdbtnImage.setSelected(true);
				buttonGroup.add(rdbtnImage);
				panel.add(rdbtnImage);
			}
			{
				rdbtnMap = new JRadioButton("Map");
				rdbtnMap.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						btnOpenInfo.setEnabled(false);
						textInfo.setText("");
						infoFile = null;
						checkValues();
					}
				});
				buttonGroup.add(rdbtnMap);
				panel.add(rdbtnMap);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						canceled = false;
						setVisible(false);
						RotateImageDialog dialog = new RotateImageDialog();
						dialog.setLocation(OpenImageDialog.this.getLocation());
						dialog.setVisible(true);
						rotateAngle = dialog.rotateAngle;
						flipType = dialog.flipType;
					}
				});
				okButton.setEnabled(false);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public void setFiles(File imageFile, File infoFile) {
		this.imageFile = imageFile;
		this.infoFile = infoFile;
		if (imageFile != null) {
			textImage.setText(imageFile.getName());
			rdbtnMap.setSelected(true);
			btnOpenInfo.setEnabled(false);
		}
		if (infoFile != null) {
			textInfo.setText(infoFile.getName());
			rdbtnImage.setSelected(true);
			btnOpenInfo.setEnabled(true);
		}
		checkValues();
	}

	private void checkValues() {
		if (rdbtnMap.isSelected()) {
			String val = textImage.getText();
			okButton.setEnabled(val != null && !val.isEmpty());
		} else {
			String val = textImage.getText();
			if (val != null && !val.isEmpty()) {
				String val2 = textInfo.getText();
				okButton.setEnabled(val2 != null && !val2.isEmpty());
			} else {
				okButton.setEnabled(false);
			}
		}
	}

	protected JButton getBtnOpenInfo() {
		return btnOpenInfo;
	}

	protected JRadioButton getRdbtnImage() {
		return rdbtnImage;
	}

	protected JRadioButton getRdbtnMap() {
		return rdbtnMap;
	}

	protected JButton getOkButton() {
		return okButton;
	}
}
