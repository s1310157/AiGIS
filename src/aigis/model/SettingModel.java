package aigis.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import aigis.Const;
import aigis.Logger;
import aigis.model.ModelSelection.Resolution;
import aigis.model.loader.PropLoader;

public class SettingModel {

	public class ShapePath {
		private HashMap<Resolution, ShapePathData> paths = new HashMap<Resolution, ShapePathData>();

		public void put(Resolution res, ShapePathData data) {
			paths.put(res, data);
		}

		public ShapePathData get(Resolution res) {
			return paths.get(res);
		}

		public Set<Resolution> keySet() {
			return paths.keySet();
		}
	}

	public class ShapePathData {
		public String path;
		public String mapDir;
		public String mapData;
		public String chartData;
	}

	private File baseDir = null;
	private String bodyName = null;
	private ArrayList<ShapePath> paths;
	private String gridrPath = null;
	private String gridwPath = null;
	private String gridPath = null;
	private String imageMapPath = null;
	private boolean isChangeModel = false;

	public void loadData(File dir) throws Exception {
		PropLoader conf = new PropLoader();
		conf.load(new InputStreamReader(new FileInputStream(new File(dir, Const.SETTING_TXT)), "UTF-8"));

		paths = new ArrayList<ShapePath>();
		int index = 0;
		do {
			ShapePath path = new ShapePath();
			if (!addPath(conf, path, Resolution.Low, index)) {
				break;
			}
			addPath(conf, path, Resolution.High, index);
			paths.add(path);
			index++;
		} while (true);

		if (paths.isEmpty()) {
			throw new Exception("Property key 'SHAPE' is required");
		}

		bodyName = conf.getProperty("BODYNAME");
		gridrPath = conf.getProperty("GRIDR");
		gridwPath = conf.getProperty("GRIDW");
		gridPath = conf.getProperty("GRID");
		String changeModel = conf.getProperty("CHANGEMODEL");
		isChangeModel = changeModel == null ? false : changeModel.toLowerCase().equals("true");
		String imageMapDir = conf.getProperty("IMAGEMAPDIR");
		if (imageMapDir != null) {
			imageMapPath = new File(dir, imageMapDir).getAbsolutePath();
		} else {
			imageMapPath = dir.getAbsolutePath();
		}
		baseDir = dir;
	}

	private boolean addPath(PropLoader conf, ShapePath path, Resolution res, int index) {
		ShapePathData data = new ShapePathData();
		String suffix = "";
		if (res == Resolution.High) {
			suffix = "HIGH";
		}
		if (index > 0) {
			suffix += index;
		}
		data.path = conf.getProperty("SHAPE" + suffix);
		data.mapDir = conf.getProperty("MAPDATADIR" + suffix);
		data.mapData = conf.getProperty("MAPDATA" + suffix);
		if (index > 0) {
			suffix = String.valueOf(index);
		} else {
			suffix = "";
		}
		data.chartData = conf.getProperty("CHARTDATA" + suffix);
		if (data.mapDir == null) {
			if (res == Resolution.High) {
				data.mapDir = Const.MAPDATA_HIGH_DIR + suffix;
			} else {
				data.mapDir = Const.MAPDATA_DIR + suffix;
			}
		}
		if (data.path != null) {
			path.put(res, data);
			return true;
		}
		return false;
	}

	public void clear() {
		bodyName = null;
		paths = null;
		gridrPath = null;
		gridwPath = null;
		gridPath = null;
		isChangeModel = false;
		imageMapPath = null;
		baseDir = null;
	}

	public void dump() {
		Logger.Info("--" + Const.SETTING_TXT + "--\n" + "bodyName = " + bodyName + "\n" + "gridrPath = " + gridrPath
				+ "\n" + "gridwPath = " + gridwPath + "\n" + "gridPath = " + gridPath + "\n" + "isChangeModel = "
				+ isChangeModel);
		int index = 0;
		String suffix = "";
		for (ShapePath path : paths) {
			for (Resolution res : path.keySet()) {
				if (index > 0) {
					suffix = "" + index;
				}
				ShapePathData data = path.get(res);
				Logger.Info("shapePath" + suffix + " = " + data.path + "\n" + "mapDir" + suffix + " = " + data.mapDir
						+ "\n" + "mapData" + suffix + " = " + data.mapData + "\n" + "chartData" + suffix + " = "
						+ data.chartData);
			}
			index++;
		}
		Logger.Info("");
	}

	public ArrayList<ShapePath> getPathList() {
		return paths;
	}

	public String getGridrPath() {
		return gridrPath;
	}

	public String getGridwPath() {
		return gridwPath;
	}

	public String getGridPath() {
		return gridPath;
	}

	public boolean isChangeModel() {
		return isChangeModel;
	}

	public String getBodyName() {
		return bodyName;
	}

	public String getImageMapPath() {
		return imageMapPath;
	}

	public File getBaseDir() {
		return baseDir;
	}
}
