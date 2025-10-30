package aigis.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class LoadingDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JLabel infoLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			LoadingDialog dialog = new LoadingDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public LoadingDialog(Frame owner) {
		super(owner);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);
		setResizable(false);
		setSize(300, 200);
		infoLabel = new JLabel("loading");
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(infoLabel, BorderLayout.SOUTH);
		{
			JLabel loadingLabel = new JLabel(" Loading...");
			loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
			getContentPane().add(loadingLabel, BorderLayout.CENTER);
			loadingLabel.setIcon(new ImageIcon(LoadingDialog.class.getResource("/aigis/res/ajax-loader.gif")));
		}
	}

	public void updateInfo(String info) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				infoLabel.setText(info);
			}
		});
	}
}
