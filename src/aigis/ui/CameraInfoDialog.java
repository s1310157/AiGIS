package aigis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import aigis.App;
import aigis.model.CameraInfo;

@SuppressWarnings("serial")
public class CameraInfoDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textPositionX;
	private JTextField textPositionY;
	private JTextField textPositionZ;
	private JTextField textDirectionX;
	private JTextField textDirectionY;
	private JTextField textDirectionZ;
	private JTextField textUpX;
	private JTextField textUpY;
	private JTextField textUpZ;
	private JTextField textFrustum1X;
	private JTextField textFrustum1Y;
	private JTextField textFrustum1Z;
	private JTextField textFrustum2X;
	private JTextField textFrustum2Y;
	private JTextField textFrustum2Z;
	private JTextField textFrustum3X;
	private JTextField textFrustum3Y;
	private JTextField textFrustum3Z;
	private JTextField textFrustum4X;
	private JTextField textFrustum4Y;
	private JTextField textFrustum4Z;
	private JButton btnOpen;
	private Component horizontalStrut;
	private JButton btnSave;

	private CameraInfo cameraInfo;
	public boolean canceled = true;

	public CameraInfo getCameraInfo() {
		cameraInfo.position.x = getDouble(textPositionX);
		cameraInfo.position.y = getDouble(textPositionY);
		cameraInfo.position.z = getDouble(textPositionZ);
		cameraInfo.direction.x = getDouble(textDirectionX);
		cameraInfo.direction.y = getDouble(textDirectionY);
		cameraInfo.direction.z = getDouble(textDirectionZ);
		cameraInfo.direction.normalize();
		cameraInfo.up.x = getDouble(textUpX);
		cameraInfo.up.y = getDouble(textUpY);
		cameraInfo.up.z = getDouble(textUpZ);
		cameraInfo.up.normalize();
		return cameraInfo;
	}

	private double getDouble(JTextField text) {
		return Double.parseDouble(text.getText());
	}

	private void setText(JTextField text, double val) {
		text.setText(String.format("%.16e", val));
	}

	private String toupleStr(Tuple3d val) {
		return String.format("( %.16e, %.16e, %.16e )", val.x, val.y, val.z);
	}

	private void updateUI() {
		setText(textPositionX, cameraInfo.position.x);
		setText(textPositionY, cameraInfo.position.y);
		setText(textPositionZ, cameraInfo.position.z);
		setText(textDirectionX, cameraInfo.direction.x);
		setText(textDirectionY, cameraInfo.direction.y);
		setText(textDirectionZ, cameraInfo.direction.z);
		setText(textUpX, cameraInfo.up.x);
		setText(textUpY, cameraInfo.up.y);
		setText(textUpZ, cameraInfo.up.z);
		Vector3d[] frustum = cameraInfo.getFrustum();
		setText(textFrustum1X, frustum[0].x);
		setText(textFrustum1Y, frustum[0].y);
		setText(textFrustum1Z, frustum[0].z);
		setText(textFrustum2X, frustum[1].x);
		setText(textFrustum2Y, frustum[1].y);
		setText(textFrustum2Z, frustum[1].z);
		setText(textFrustum3X, frustum[2].x);
		setText(textFrustum3Y, frustum[2].y);
		setText(textFrustum3Z, frustum[2].z);
		setText(textFrustum4X, frustum[3].x);
		setText(textFrustum4Y, frustum[3].y);
		setText(textFrustum4Z, frustum[3].z);

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			CameraInfoDialog dialog = new CameraInfoDialog(null, new CameraInfo());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public CameraInfoDialog(Frame owner, CameraInfo cameraInfo) {
		super(owner);
		this.cameraInfo = cameraInfo.clone();
		setModal(true);
		setTitle("カメラの情報");
		setBounds(100, 100, 660, 335);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 24, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblPosition = new JLabel("位置");
			GridBagConstraints gbc_lblPosition = new GridBagConstraints();
			gbc_lblPosition.anchor = GridBagConstraints.EAST;
			gbc_lblPosition.insets = new Insets(0, 0, 5, 5);
			gbc_lblPosition.gridx = 1;
			gbc_lblPosition.gridy = 1;
			contentPanel.add(lblPosition, gbc_lblPosition);
		}
		{
			textPositionX = new JTextField();
			GridBagConstraints gbc_textPositionX = new GridBagConstraints();
			gbc_textPositionX.insets = new Insets(0, 0, 5, 5);
			gbc_textPositionX.fill = GridBagConstraints.HORIZONTAL;
			gbc_textPositionX.gridx = 2;
			gbc_textPositionX.gridy = 1;
			contentPanel.add(textPositionX, gbc_textPositionX);
			textPositionX.setColumns(10);
		}
		{
			textPositionY = new JTextField();
			GridBagConstraints gbc_textPositionY = new GridBagConstraints();
			gbc_textPositionY.insets = new Insets(0, 0, 5, 5);
			gbc_textPositionY.fill = GridBagConstraints.HORIZONTAL;
			gbc_textPositionY.gridx = 3;
			gbc_textPositionY.gridy = 1;
			contentPanel.add(textPositionY, gbc_textPositionY);
			textPositionY.setColumns(10);
		}
		{
			textPositionZ = new JTextField();
			GridBagConstraints gbc_textPositionZ = new GridBagConstraints();
			gbc_textPositionZ.insets = new Insets(0, 0, 5, 0);
			gbc_textPositionZ.fill = GridBagConstraints.HORIZONTAL;
			gbc_textPositionZ.gridx = 4;
			gbc_textPositionZ.gridy = 1;
			contentPanel.add(textPositionZ, gbc_textPositionZ);
			textPositionZ.setColumns(10);
		}
		{
			JLabel lblDirection = new JLabel("方向");
			GridBagConstraints gbc_lblDirection = new GridBagConstraints();
			gbc_lblDirection.anchor = GridBagConstraints.EAST;
			gbc_lblDirection.insets = new Insets(0, 0, 5, 5);
			gbc_lblDirection.gridx = 1;
			gbc_lblDirection.gridy = 2;
			contentPanel.add(lblDirection, gbc_lblDirection);
		}
		{
			textDirectionX = new JTextField();
			GridBagConstraints gbc_textDirectionX = new GridBagConstraints();
			gbc_textDirectionX.insets = new Insets(0, 0, 5, 5);
			gbc_textDirectionX.fill = GridBagConstraints.HORIZONTAL;
			gbc_textDirectionX.gridx = 2;
			gbc_textDirectionX.gridy = 2;
			contentPanel.add(textDirectionX, gbc_textDirectionX);
			textDirectionX.setColumns(10);
		}
		{
			textDirectionY = new JTextField();
			GridBagConstraints gbc_textDirectionY = new GridBagConstraints();
			gbc_textDirectionY.insets = new Insets(0, 0, 5, 5);
			gbc_textDirectionY.fill = GridBagConstraints.HORIZONTAL;
			gbc_textDirectionY.gridx = 3;
			gbc_textDirectionY.gridy = 2;
			contentPanel.add(textDirectionY, gbc_textDirectionY);
			textDirectionY.setColumns(10);
		}
		{
			textDirectionZ = new JTextField();
			GridBagConstraints gbc_textDirectionZ = new GridBagConstraints();
			gbc_textDirectionZ.insets = new Insets(0, 0, 5, 0);
			gbc_textDirectionZ.fill = GridBagConstraints.HORIZONTAL;
			gbc_textDirectionZ.gridx = 4;
			gbc_textDirectionZ.gridy = 2;
			contentPanel.add(textDirectionZ, gbc_textDirectionZ);
			textDirectionZ.setColumns(10);
		}
		{
			JLabel lblUp = new JLabel("上昇");
			GridBagConstraints gbc_lblUp = new GridBagConstraints();
			gbc_lblUp.anchor = GridBagConstraints.EAST;
			gbc_lblUp.insets = new Insets(0, 0, 5, 5);
			gbc_lblUp.gridx = 1;
			gbc_lblUp.gridy = 3;
			contentPanel.add(lblUp, gbc_lblUp);
		}
		{
			textUpX = new JTextField();
			GridBagConstraints gbc_textUpX = new GridBagConstraints();
			gbc_textUpX.insets = new Insets(0, 0, 5, 5);
			gbc_textUpX.fill = GridBagConstraints.HORIZONTAL;
			gbc_textUpX.gridx = 2;
			gbc_textUpX.gridy = 3;
			contentPanel.add(textUpX, gbc_textUpX);
			textUpX.setColumns(10);
		}
		{
			textUpY = new JTextField();
			GridBagConstraints gbc_textUpY = new GridBagConstraints();
			gbc_textUpY.insets = new Insets(0, 0, 5, 5);
			gbc_textUpY.fill = GridBagConstraints.HORIZONTAL;
			gbc_textUpY.gridx = 3;
			gbc_textUpY.gridy = 3;
			contentPanel.add(textUpY, gbc_textUpY);
			textUpY.setColumns(10);
		}
		{
			textUpZ = new JTextField();
			GridBagConstraints gbc_textUpZ = new GridBagConstraints();
			gbc_textUpZ.insets = new Insets(0, 0, 5, 0);
			gbc_textUpZ.fill = GridBagConstraints.HORIZONTAL;
			gbc_textUpZ.gridx = 4;
			gbc_textUpZ.gridy = 3;
			contentPanel.add(textUpZ, gbc_textUpZ);
			textUpZ.setColumns(10);
		}
		{
			JLabel lblFrustum1 = new JLabel("視錐台1");
			GridBagConstraints gbc_lblFrustum1 = new GridBagConstraints();
			gbc_lblFrustum1.anchor = GridBagConstraints.EAST;
			gbc_lblFrustum1.insets = new Insets(0, 0, 5, 5);
			gbc_lblFrustum1.gridx = 1;
			gbc_lblFrustum1.gridy = 4;
			contentPanel.add(lblFrustum1, gbc_lblFrustum1);
		}
		{
			textFrustum1X = new JTextField();
			GridBagConstraints gbc_textFrustum1X = new GridBagConstraints();
			gbc_textFrustum1X.insets = new Insets(0, 0, 5, 5);
			gbc_textFrustum1X.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum1X.gridx = 2;
			gbc_textFrustum1X.gridy = 4;
			contentPanel.add(textFrustum1X, gbc_textFrustum1X);
			textFrustum1X.setColumns(10);
		}
		{
			textFrustum1Y = new JTextField();
			GridBagConstraints gbc_textFrustum1Y = new GridBagConstraints();
			gbc_textFrustum1Y.insets = new Insets(0, 0, 5, 5);
			gbc_textFrustum1Y.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum1Y.gridx = 3;
			gbc_textFrustum1Y.gridy = 4;
			contentPanel.add(textFrustum1Y, gbc_textFrustum1Y);
			textFrustum1Y.setColumns(10);
		}
		{
			textFrustum1Z = new JTextField();
			GridBagConstraints gbc_textFrustum1Z = new GridBagConstraints();
			gbc_textFrustum1Z.insets = new Insets(0, 0, 5, 0);
			gbc_textFrustum1Z.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum1Z.gridx = 4;
			gbc_textFrustum1Z.gridy = 4;
			contentPanel.add(textFrustum1Z, gbc_textFrustum1Z);
			textFrustum1Z.setColumns(10);
		}
		{
			JLabel lblFrustum2 = new JLabel("視錐台2");
			GridBagConstraints gbc_lblFrustum2 = new GridBagConstraints();
			gbc_lblFrustum2.anchor = GridBagConstraints.EAST;
			gbc_lblFrustum2.insets = new Insets(0, 0, 5, 5);
			gbc_lblFrustum2.gridx = 1;
			gbc_lblFrustum2.gridy = 5;
			contentPanel.add(lblFrustum2, gbc_lblFrustum2);
		}
		{
			textFrustum2X = new JTextField();
			GridBagConstraints gbc_textFrustum2X = new GridBagConstraints();
			gbc_textFrustum2X.insets = new Insets(0, 0, 5, 5);
			gbc_textFrustum2X.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum2X.gridx = 2;
			gbc_textFrustum2X.gridy = 5;
			contentPanel.add(textFrustum2X, gbc_textFrustum2X);
			textFrustum2X.setColumns(10);
		}
		{
			textFrustum2Y = new JTextField();
			GridBagConstraints gbc_textFrustum2Y = new GridBagConstraints();
			gbc_textFrustum2Y.insets = new Insets(0, 0, 5, 5);
			gbc_textFrustum2Y.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum2Y.gridx = 3;
			gbc_textFrustum2Y.gridy = 5;
			contentPanel.add(textFrustum2Y, gbc_textFrustum2Y);
			textFrustum2Y.setColumns(10);
		}
		{
			textFrustum2Z = new JTextField();
			GridBagConstraints gbc_textFrustum2Z = new GridBagConstraints();
			gbc_textFrustum2Z.insets = new Insets(0, 0, 5, 0);
			gbc_textFrustum2Z.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum2Z.gridx = 4;
			gbc_textFrustum2Z.gridy = 5;
			contentPanel.add(textFrustum2Z, gbc_textFrustum2Z);
			textFrustum2Z.setColumns(10);
		}
		{
			JLabel lblFrustum3 = new JLabel("視錐台3");
			GridBagConstraints gbc_lblFrustum3 = new GridBagConstraints();
			gbc_lblFrustum3.anchor = GridBagConstraints.EAST;
			gbc_lblFrustum3.insets = new Insets(0, 0, 5, 5);
			gbc_lblFrustum3.gridx = 1;
			gbc_lblFrustum3.gridy = 6;
			contentPanel.add(lblFrustum3, gbc_lblFrustum3);
		}
		{
			textFrustum3X = new JTextField();
			GridBagConstraints gbc_textFrustum3X = new GridBagConstraints();
			gbc_textFrustum3X.insets = new Insets(0, 0, 5, 5);
			gbc_textFrustum3X.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum3X.gridx = 2;
			gbc_textFrustum3X.gridy = 6;
			contentPanel.add(textFrustum3X, gbc_textFrustum3X);
			textFrustum3X.setColumns(10);
		}
		{
			textFrustum3Y = new JTextField();
			GridBagConstraints gbc_textFrustum3Y = new GridBagConstraints();
			gbc_textFrustum3Y.insets = new Insets(0, 0, 5, 5);
			gbc_textFrustum3Y.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum3Y.gridx = 3;
			gbc_textFrustum3Y.gridy = 6;
			contentPanel.add(textFrustum3Y, gbc_textFrustum3Y);
			textFrustum3Y.setColumns(10);
		}
		{
			textFrustum3Z = new JTextField();
			GridBagConstraints gbc_textFrustum3Z = new GridBagConstraints();
			gbc_textFrustum3Z.insets = new Insets(0, 0, 5, 0);
			gbc_textFrustum3Z.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum3Z.gridx = 4;
			gbc_textFrustum3Z.gridy = 6;
			contentPanel.add(textFrustum3Z, gbc_textFrustum3Z);
			textFrustum3Z.setColumns(10);
		}
		{
			JLabel lblFrustum = new JLabel("視錐台4");
			GridBagConstraints gbc_lblFrustum = new GridBagConstraints();
			gbc_lblFrustum.anchor = GridBagConstraints.EAST;
			gbc_lblFrustum.insets = new Insets(0, 0, 0, 5);
			gbc_lblFrustum.gridx = 1;
			gbc_lblFrustum.gridy = 7;
			contentPanel.add(lblFrustum, gbc_lblFrustum);
		}
		{
			textFrustum4X = new JTextField();
			GridBagConstraints gbc_textFrustum4X = new GridBagConstraints();
			gbc_textFrustum4X.insets = new Insets(0, 0, 0, 5);
			gbc_textFrustum4X.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum4X.gridx = 2;
			gbc_textFrustum4X.gridy = 7;
			contentPanel.add(textFrustum4X, gbc_textFrustum4X);
			textFrustum4X.setColumns(10);
		}
		{
			textFrustum4Y = new JTextField();
			GridBagConstraints gbc_textFrustum4Y = new GridBagConstraints();
			gbc_textFrustum4Y.insets = new Insets(0, 0, 0, 5);
			gbc_textFrustum4Y.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum4Y.gridx = 3;
			gbc_textFrustum4Y.gridy = 7;
			contentPanel.add(textFrustum4Y, gbc_textFrustum4Y);
			textFrustum4Y.setColumns(10);
		}
		{
			textFrustum4Z = new JTextField();
			GridBagConstraints gbc_textFrustum4Z = new GridBagConstraints();
			gbc_textFrustum4Z.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFrustum4Z.gridx = 4;
			gbc_textFrustum4Z.gridy = 7;
			contentPanel.add(textFrustum4Z, gbc_textFrustum4Z);
			textFrustum4Z.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnOpen = new JButton("開く...");
				btnOpen.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						FileNameExtensionFilter filter = new FileNameExtensionFilter("Info file", "info", "txt");
						CameraInfoDialog self = CameraInfoDialog.this;
						JFileChooser chooser = App.showOpenDialog(self, null, JFileChooser.FILES_ONLY, filter, true);
						if (chooser != null) {
							try {
								File file = chooser.getSelectedFile();
								self.cameraInfo = CameraInfo.loadFromInfo(file.getAbsolutePath());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							updateUI();
						}
					}
				});
				buttonPane.add(btnOpen);
			}
			{
				btnSave = new JButton("保存...");
				btnSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						FileNameExtensionFilter filter = new FileNameExtensionFilter("Info file", "info", "txt");
						CameraInfoDialog self = CameraInfoDialog.this;
						JFileChooser chooser = App.showOpenDialog(self, null, JFileChooser.FILES_ONLY, filter, false);
						if (chooser != null) {
							try {
								File f = chooser.getSelectedFile();
								if (f.exists()) {
									int ret = JOptionPane.showConfirmDialog(self,
											"ファイルが存在します。\n上書きしますか？");
									if (ret != JOptionPane.OK_OPTION) {
										return;
									}
								}
								if (!f.getName().toLowerCase().endsWith(".info")) {
									f = new File(f.getParent() + File.separator + f.getName() + ".info");
								}
								FileWriter file = new FileWriter(f);
								PrintWriter pw = new PrintWriter(new BufferedWriter(file));
								String now = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
								CameraInfo info = CameraInfoDialog.this.cameraInfo;
								pw.println("開始時間          = " + now);
								pw.println("停止時間           = " + now);
								pw.println("宇宙船の位置 = " + toupleStr(info.position));
								pw.println("照準方向 = " + toupleStr(info.direction));
								pw.println("上方向        = " + toupleStr(info.up));
								Vector3d vec[] = info.getFrustum();
								pw.println("視錐台1            = " + toupleStr(vec[0]));
								pw.println("視錐台2            = " + toupleStr(vec[1]));
								pw.println("視錐台3            = " + toupleStr(vec[2]));
								pw.println("視錐台4            = " + toupleStr(vec[3]));
								pw.println("太陽の位置_左図     = " + toupleStr(info.position));
								pw.close();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							updateUI();
						}

					}
				});
				buttonPane.add(btnSave);
			}
			{
				horizontalStrut = Box.createHorizontalStrut(20);
				horizontalStrut.setPreferredSize(new Dimension(60, 0));
				buttonPane.add(horizontalStrut);
			}
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						canceled = false;
						setVisible(false);
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
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("キャンセル");
				buttonPane.add(cancelButton);
			}
		}
		updateUI();
	}

}
