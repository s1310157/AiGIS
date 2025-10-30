package aigis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import aigis.Logger;
import aigis.gl.Textures.Setting;

@SuppressWarnings("serial")
public class TexPrevDialog extends JDialog {
	private JLabel lblImageName;
	private JLabel lblImagePrev;
	private JTextArea textArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			TexPrevDialog dialog = new TexPrevDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public TexPrevDialog(Frame owner) {
		super(owner);
		setTitle("Preview");
		setSize(800, 500);
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
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(300, 10));
			getContentPane().add(panel, BorderLayout.WEST);
			panel.setLayout(new BorderLayout(0, 0));
			{
				lblImageName = new JLabel("");
				lblImageName.setHorizontalAlignment(SwingConstants.CENTER);
				panel.add(lblImageName, BorderLayout.NORTH);
			}
			{
				lblImagePrev = new JLabel("");
				lblImagePrev.setHorizontalTextPosition(SwingConstants.CENTER);
				lblImagePrev.setHorizontalAlignment(SwingConstants.CENTER);
				lblImagePrev.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(lblImagePrev, BorderLayout.CENTER);
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			getContentPane().add(scrollPane, BorderLayout.CENTER);
			{
				textArea = new JTextArea();
				textArea.setEditable(false);
				scrollPane.setViewportView(textArea);
			}
		}
	}

	public void setTextureSetting(Setting tex) {
		lblImageName.setText(tex.imageFile.getName());
		ImageIcon icon = null;
		if (tex.img == null) {
			icon = new ImageIcon(tex.imageFile.getAbsolutePath());
		} else {
			icon = new ImageIcon(tex.img);
		}
		float aspect = (float) icon.getIconHeight() / icon.getIconWidth();
		if (aspect > 1) {
			int newWidth = (int) (1 / aspect * 400);
			if (newWidth == 0) newWidth = 1;
			icon = new ImageIcon(icon.getImage().getScaledInstance(newWidth, 400, Image.SCALE_DEFAULT));
		} else {
			int newHeight = (int) (aspect * 300);
			if (newHeight == 0) newHeight = 1;
			icon = new ImageIcon(icon.getImage().getScaledInstance(300, newHeight, Image.SCALE_DEFAULT));

		}
		lblImagePrev.setIcon(icon);

		if (tex.infoFile == null) {
			textArea.setText("None");
			return;
		}

		try {
			StringBuffer info = new StringBuffer();
			List<String> lines = Files.readAllLines(tex.infoFile.toPath(), StandardCharsets.UTF_8);
			for (String str : lines) {
				info.append(str);
				info.append("\n");
			}
			textArea.setText(info.toString());
		} catch (IOException e) {
			Logger.Error(e);
			textArea.setText(e.getLocalizedMessage());
		}
	}

	protected JLabel getLblImageName() {
		return lblImageName;
	}

	protected JLabel getLblImagePrev() {
		return lblImagePrev;
	}

	protected JTextArea getTextArea() {
		return textArea;
	}
}
