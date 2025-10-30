package aigis.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class RotateImageDialog extends JDialog {
	
	public int rotateAngle = 0;
	public int flipType = 0;

	private final JPanel contentPanel = new JPanel();
	private final ButtonGroup buttonGroupRotate = new ButtonGroup();
	private final ButtonGroup buttonGroupFlip = new ButtonGroup();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			RotateImageDialog dialog = new RotateImageDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public RotateImageDialog() {
		setModal(true);
		setResizable(false);
		setTitle("Rotate");
		setBounds(100, 100, 300, 250);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {50, 110, 110, 30, 0};
		gbl_contentPanel.rowHeights = new int[] {45, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblRotate = new JLabel("Rotate right");
			GridBagConstraints gbc_lblRotate = new GridBagConstraints();
			gbc_lblRotate.anchor = GridBagConstraints.WEST;
			gbc_lblRotate.insets = new Insets(0, 0, 5, 5);
			gbc_lblRotate.gridx = 1;
			gbc_lblRotate.gridy = 0;
			contentPanel.add(lblRotate, gbc_lblRotate);
		}
		{
			JLabel lblFlip = new JLabel("Flip");
			GridBagConstraints gbc_lblFlip = new GridBagConstraints();
			gbc_lblFlip.anchor = GridBagConstraints.WEST;
			gbc_lblFlip.insets = new Insets(0, 0, 5, 5);
			gbc_lblFlip.gridx = 2;
			gbc_lblFlip.gridy = 0;
			contentPanel.add(lblFlip, gbc_lblFlip);
		}
		{
			JRadioButton rdbtn0deg = new JRadioButton("0°");
			rdbtn0deg.setSelected(true);
			buttonGroupRotate.add(rdbtn0deg);
			GridBagConstraints gbc_rdbtn0deg = new GridBagConstraints();
			gbc_rdbtn0deg.anchor = GridBagConstraints.WEST;
			gbc_rdbtn0deg.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtn0deg.gridx = 1;
			gbc_rdbtn0deg.gridy = 1;
			contentPanel.add(rdbtn0deg, gbc_rdbtn0deg);
		}
		{
			JRadioButton rdbtnNone = new JRadioButton("None");
			rdbtnNone.setSelected(true);
			buttonGroupFlip.add(rdbtnNone);
			GridBagConstraints gbc_rdbtnNone = new GridBagConstraints();
			gbc_rdbtnNone.anchor = GridBagConstraints.WEST;
			gbc_rdbtnNone.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnNone.gridx = 2;
			gbc_rdbtnNone.gridy = 1;
			contentPanel.add(rdbtnNone, gbc_rdbtnNone);
		}
		{
			JRadioButton rdbtn90deg = new JRadioButton("90°");
			buttonGroupRotate.add(rdbtn90deg);
			GridBagConstraints gbc_rdbtn90deg = new GridBagConstraints();
			gbc_rdbtn90deg.anchor = GridBagConstraints.WEST;
			gbc_rdbtn90deg.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtn90deg.gridx = 1;
			gbc_rdbtn90deg.gridy = 2;
			contentPanel.add(rdbtn90deg, gbc_rdbtn90deg);
		}
		{
			JRadioButton rdbtnVertical = new JRadioButton("Vertical");
			buttonGroupFlip.add(rdbtnVertical);
			GridBagConstraints gbc_rdbtnVertical = new GridBagConstraints();
			gbc_rdbtnVertical.anchor = GridBagConstraints.WEST;
			gbc_rdbtnVertical.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnVertical.gridx = 2;
			gbc_rdbtnVertical.gridy = 2;
			contentPanel.add(rdbtnVertical, gbc_rdbtnVertical);
		}
		{
			JRadioButton rdbtn180deg = new JRadioButton("180°");
			buttonGroupRotate.add(rdbtn180deg);
			GridBagConstraints gbc_rdbtn180deg = new GridBagConstraints();
			gbc_rdbtn180deg.anchor = GridBagConstraints.WEST;
			gbc_rdbtn180deg.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtn180deg.gridx = 1;
			gbc_rdbtn180deg.gridy = 3;
			contentPanel.add(rdbtn180deg, gbc_rdbtn180deg);
		}
		{
			JRadioButton rdbtnHorizontal = new JRadioButton("Horizontal");
			buttonGroupFlip.add(rdbtnHorizontal);
			GridBagConstraints gbc_rdbtnHorizontal = new GridBagConstraints();
			gbc_rdbtnHorizontal.anchor = GridBagConstraints.WEST;
			gbc_rdbtnHorizontal.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnHorizontal.gridx = 2;
			gbc_rdbtnHorizontal.gridy = 3;
			contentPanel.add(rdbtnHorizontal, gbc_rdbtnHorizontal);
		}
		{
			JRadioButton rdbtn270deg = new JRadioButton("270°");
			buttonGroupRotate.add(rdbtn270deg);
			GridBagConstraints gbc_rdbtn270deg = new GridBagConstraints();
			gbc_rdbtn270deg.anchor = GridBagConstraints.WEST;
			gbc_rdbtn270deg.insets = new Insets(0, 0, 0, 5);
			gbc_rdbtn270deg.gridx = 1;
			gbc_rdbtn270deg.gridy = 4;
			contentPanel.add(rdbtn270deg, gbc_rdbtn270deg);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for (Enumeration<AbstractButton> buttons = buttonGroupRotate.getElements(); buttons.hasMoreElements();) {
				            AbstractButton button = buttons.nextElement();
				            if (button.isSelected()) {
				            	rotateAngle = Integer.parseInt(button.getText().replace("°", ""));
				            	break;
				            }
				        }
						for (Enumeration<AbstractButton> buttons = buttonGroupFlip.getElements(); buttons.hasMoreElements();) {
				            AbstractButton button = buttons.nextElement();
				            if (button.isSelected()) {
				            	if (button.getText().endsWith("Vertical")) {
				            		flipType = 1;
				            	}
				            	if (button.getText().endsWith("Horizontal")) {
				            		flipType = 2;
				            	}
				            	break;
				            }
				        }
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

}
