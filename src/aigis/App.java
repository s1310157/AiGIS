package aigis;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import aigis.ui.MainFrame;

public class App {

	public static boolean isMacExe = false;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (args.length > 0 && args[0].equalsIgnoreCase("mac")) {
						App.isMacExe = true;
					}
					MainFrame frame = new MainFrame();
					frame.setSize(1180, 850);
					frame.setVisible(true);
					frame.openFirst();
				} catch (Exception e) {
					Logger.Error(e);
					String msg = e.getMessage();
					JOptionPane.showMessageDialog(null, (msg == null ? "An unknown error occurred.." : msg), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	public static File getWorkingDir() {
		CodeSource codeSource = App.class.getProtectionDomain().getCodeSource();
		try {
			return new File(codeSource.getLocation().toURI().getPath()).getParentFile();
		} catch (URISyntaxException e) {
			Logger.Error(e);
		}
		return null;
	}

	private static ResourceBundle getBundle() throws Exception {
		File path = new File(".");
		if (isMacExe) {
			path = App.getWorkingDir();
		}
		URLClassLoader urlLoader = new URLClassLoader(new URL[] { path.toURI().toURL() });
		ResourceBundle bundle = ResourceBundle.getBundle(Const.APP_NAME, Locale.getDefault(), urlLoader);
		return bundle;
	}

	public static String getProp(String key) {
		try {
			ResourceBundle bundle = getBundle();
			return bundle.getString(key);
		} catch (Exception e) {
		}
		return null;
	}

	public static void setProp(String key, String value) {
		try {
			Properties conf = new Properties();
			try {
				ResourceBundle bundle = getBundle();
				for (String item : Collections.list(bundle.getKeys())) {
					conf.setProperty(item, bundle.getString(item));
				}
			} catch (Exception e) {
			}
			conf.setProperty(key, value);
			File path = new File("AiGIS.properties");
			if (isMacExe) {
				path = new File(App.getWorkingDir(), "AiGIS.properties");
			}
			conf.store(new FileOutputStream(path), "AiGIS");
		} catch (Exception e) {
			Logger.Error(e);
		}
	}

	public static JFileChooser showOpenDialog(Component parent, String path, int mode, FileNameExtensionFilter filter,
			boolean open) {
		if (path == null) {
			path = System.getProperty("user.home");
		}
		File workingDirectory = new File(path);
		JFileChooser chooser = new JFileChooser(workingDirectory);
		chooser.setFileSelectionMode(mode);
		if (filter != null) {
			chooser.setFileFilter(filter);
		}
		int selected;
		if (open) {
			selected = chooser.showOpenDialog(parent);
		} else {
			selected = chooser.showSaveDialog(parent);
		}
		if (selected == JFileChooser.APPROVE_OPTION) {
			return chooser;
		}
		return null;
	}

}
