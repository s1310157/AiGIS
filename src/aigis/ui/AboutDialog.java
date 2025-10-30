package aigis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import aigis.Const;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AboutDialog dialog = new AboutDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public AboutDialog(Frame owner) {
		super(owner);
		setModal(true);
		setResizable(false);
		setSize(350, 200);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JLabel lblIcon = new JLabel("");
			lblIcon.setIcon(new ImageIcon(AboutDialog.class.getResource("/aigis/res/icon.png")));
			lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPanel.add(lblIcon);
		}
		{
			Component verticalStrut = Box.createVerticalStrut(20);
			contentPanel.add(verticalStrut);
		}
		{
			JLabel lblTitle = new JLabel("AiGIS");
			lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 18));
			lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPanel.add(lblTitle);
		}
		{
			Component verticalGlue = Box.createVerticalGlue();
			contentPanel.add(verticalGlue);
		}
		{
			JLabel lblVersion = new JLabel("Version " + Const.APP_VER);
			lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPanel.add(lblVersion);
		}
		{
			Component verticalStrut = Box.createVerticalStrut(20);
			contentPanel.add(verticalStrut);
		}
		{
			JLabel lblInfo = new JLabel(Const.APP_INFO);
			lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPanel.add(lblInfo);
		}
	}

}
