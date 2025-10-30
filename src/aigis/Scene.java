package aigis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.jogamp.opengl.GL2;

import aigis.gl.ColorBar;
import aigis.model.ChartData;
import aigis.model.General3D;
import aigis.model.Model;
import aigis.model.ModelSelection;
import aigis.model.ModelSelection.Resolution;
import aigis.model.Models;
import aigis.model.SettingModel;
import aigis.model.SettingModel.ShapePath;
import aigis.model.SettingModel.ShapePathData;
import aigis.model.SpectrumMap;
import aigis.model.loader.ChartLoader;
import aigis.model.loader.GeneralLoader;
import aigis.ui.LoadingDialog;

/***
 * Scene
 */
public class Scene {

	public ColorBar colorbar = new ColorBar();

	private SettingModel settings = new SettingModel();
	private Models registeredModels = new Models();
	private HashMap<String, ChartData> chartData = new HashMap<>();
	private double modelSize = 0;

	private boolean isDirty = false;

	public ArrayList<General3D> generals = new ArrayList<General3D>();

	/***
	 * Read the data in the directory.
	 * 
	 * @param dir
	 * @param dialog
	 * @throws Exception
	 */
	public void load(File dir, LoadingDialog dialog, boolean sort) throws Exception {
		try {
			dialog.updateInfo("configfile");
			settings.loadData(dir);
			settings.dump();

			// loading models
			registeredModels = new Models();
			int index = 0;
			for (ShapePath shapePath : settings.getPathList()) {
				for (Resolution res : shapePath.keySet()) {
					ShapePathData data = shapePath.get(res);
					dialog.updateInfo(data.path);
					Model model = new Model(dir.getAbsolutePath(), data.path, settings.getGridwPath(),
							settings.getGridrPath());
					registeredModels.addModel(new ModelSelection(res, index), model);
					modelSize = model.getSize();
				}
				index++;
			}

			// loading general 3d file
			loadGeneral3D();
			// loading map data
			loadMapData(dialog, sort);
		} catch (Exception e) {
			registeredModels = new Models();
			generals.clear();
			settings.clear();
			chartData.clear();
			throw e;
		}

		Model model = registeredModels.getModel(new ModelSelection(Resolution.Low, 0));
		model.addSpectrumMap(Const.SPECTRUMKEY_FLAT, new SpectrumMap(model.getNFace(), "#None", colorbar));
		model.changeSpectrumMap(Const.SPECTRUMKEY_FLAT, 0);
		isDirty = true;
	}

	/**
	 * loading general 3d file.
	 */
	public void loadGeneral3D() throws Exception {
		String filenameLatLon = settings.getGridPath();
		if (filenameLatLon != null) {
			File file = new File(settings.getBaseDir().getAbsolutePath() + File.separator + filenameLatLon);
			String[] filenames = file.list();

			ArrayList<General3D> gens = new ArrayList<General3D>();

			for (int i = 0; i < filenames.length; i++) {
				String name = filenames[i];
				// Exclude hidden files
				if (name.trim().startsWith("."))
					continue;
				//
				General3D gen = findGeneral(name);
				if (gen == null) {
					gen = new General3D();
				}
				GeneralLoader.load(file.getAbsolutePath(), name, gen);
				gens.add(gen);
			}
			this.generals = gens;
		} else {
			this.generals.clear();
		}
	}

	/**
	 * loading general 3d file by name.
	 * 
	 * @param name
	 */
	public void loadGeneral3D(String name) throws Exception {
		String filenameLatLon = settings.getGridPath();
		if (filenameLatLon == null)
			return;
		File file = new File(settings.getBaseDir().getAbsolutePath() + File.separator + filenameLatLon);
		General3D gen = findGeneral(name);
		if (gen == null) {
			gen = new General3D();
			generals.add(gen);
		}
		GeneralLoader.load(file.getAbsolutePath(), name, gen);
	}

	/**
	 * Find general3d object by name.
	 * 
	 * @param name
	 * @return
	 */
	private General3D findGeneral(String name) {
		for (General3D general : generals) {
			if (general.name.equals(name)) {
				return general;
			}
		}
		return null;
	}

	/**
	 * Load lookup-table files.
	 */
	public void loadLookUpTable() throws Exception {
		String lookupPath = App.getProp(Const.LOOKUP_PATH_KEY);
		if (lookupPath == null)
			return;
		File targetDir = new File(lookupPath);
		File[] files = targetDir.listFiles();
		colorbar.init();
		if (files == null)
			return;
		for (int j = 0; j < files.length; j++) {
			if (files[j].getName().startsWith(".")) {
				continue;
			}
			Logger.Debug("lookup:" + files[j].getName());
			colorbar.load(files[j].getName(), files[j].getAbsolutePath());
		}
	}

	/**
	 * Load the map data.
	 * 
	 * @param dialog
	 * @throws Exception
	 */
	public void loadMapData(LoadingDialog dialog, boolean sort) throws Exception {
		String path = settings.getBaseDir().getAbsolutePath() + File.separator;
		List<String> specNames = new ArrayList<>();
		chartData.clear();
		List<String> chartSpecNames = new ArrayList<>();
		int index = 0;
		for (ShapePath shapePath : settings.getPathList()) {
			for (Resolution res : shapePath.keySet()) {
				ShapePathData data = shapePath.get(res);
				dialog.updateInfo(data.mapDir);
				Model model = registeredModels.getModel(new ModelSelection(res, index));
				model.getAvailableSpectrumMaps().clear();
				model.addSpectrumMap(Const.SPECTRUMKEY_FLAT, new SpectrumMap(model.getNFace(), "#None", colorbar));
				File targetDir = new File(path + data.mapDir);
				File[] files = targetDir.listFiles();
				if (files == null) {
					files = new File[0];
				}
				// additional data
				String mapdata = data.mapData;
				if (mapdata != null) {
					String[] maplist = mapdata.split(",", 0);
					int oldlen = files.length;
					files = new File[files.length + maplist.length];
					if (oldlen > 0) {
						System.arraycopy(targetDir.listFiles(), 0, files, 0, oldlen);
					}
					for (int j = 0; j < maplist.length; j++) {
						files[oldlen + j] = new File(path + maplist[j]);
					}
				}
				// sort by file name
				java.util.Arrays.sort(files, new java.util.Comparator<File>() {
					public int compare(File file1, File file2) {
						return file1.getName().compareToIgnoreCase(file2.getName());
					}
				});
				// load
				List<SpectrumMap> specs = new ArrayList<>();
				for (int j = 0; j < files.length; j++) {
					if (files[j].getName().startsWith(".")) {
						continue;
					}
					SpectrumMap spec = new SpectrumMap(files[j].getPath(), colorbar);
					String specKey = index + "-" + spec.getName() + "-" + res;
					if (specNames.contains(specKey)) {
						throw new Exception("The map name \"" + spec.getName() + "\" is duplicated in \""
								+ files[j].getName() + "\".");
					}
					Logger.Debug("spec:" + specKey);
					specNames.add(specKey);
					specs.add(spec);
				}
				// sort by name
				if (sort) {
					Collections.sort(specs, new Comparator<SpectrumMap>() {
						@Override
						public int compare(SpectrumMap map1, SpectrumMap map2) {
							return map1.getName().compareToIgnoreCase(map2.getName());
						}
					});
				}
				for (SpectrumMap spec : specs) {
					model.addSpectrumMap(spec.getName(), spec);
				}
			}
			index++;
		}
		index = 0;
		for (ShapePath shapePath : settings.getPathList()) {
			ShapePathData data = shapePath.get(Resolution.Low);
			Model model = registeredModels.getModel(new ModelSelection(Resolution.Low, index));
			// loading chart data
			String chartdata = data.chartData;
			if (chartdata != null) {

				String[] list = chartdata.split(",", 0);
				for (int i = 0; i < list.length; i++) {
					ChartData chart;
					dialog.updateInfo(list[i]);

					chart = ChartLoader.load(path + list[i], dialog);
					Resolution res = Resolution.Low;
					if (chart.nFace != model.nFace) {
						res = Resolution.High;
					}
					String mapKey = index + "-" + chart.mapName + "-" + res;
					if (!specNames.contains(mapKey)) {
						throw new Exception("The map name \"" + chart.mapName + "\" is not defined in '" + res
								+ "' type of map data. \n[" + list[i] + "].");
					}
					if (chartSpecNames.contains(mapKey)) {
						throw new Exception(
								"The map name \"" + chart.mapName + "\" is duplicated in \"" + list[i] + "\".");
					}
					chartSpecNames.add(mapKey);
					chartData.put(mapKey, chart);
				}
			}
			index++;
		}
	}

	/***
	 * Get the chart data.
	 * 
	 * @param model
	 * @param map
	 * @return
	 */
	public ChartData getChartData(String map, ModelSelection selection) {
		return chartData.get(selection.index + "-" + map + "-" + selection.resolution);
	}

	/***
	 * Compile models.
	 * 
	 * @param gl
	 * @throws Exception
	 */
	public void compile(GL2 gl) throws Exception {
		if (!isDirty)
			return;
		try {
			registeredModels.compile(gl);
			isDirty = false;
		} catch (OutOfMemoryError e) {
			Logger.Error(e);
			throw new Exception("OutOfMemoryError");
		}

	}

	/***
	 * 
	 * @return
	 */
	public Models getRegisteredModels() {
		return registeredModels;
	}

	public Model getModel(ModelSelection select) {
		return getRegisteredModels().getModel(select);
	}

	/***
	 * 
	 * @return
	 */
	public boolean isChangeModel() {
		return settings.isChangeModel();
	}

	public String getTitle() {
		return settings.getBodyName();
	}

	public ShapePathData getShapePath(ModelSelection sel) {
		return settings.getPathList().get(sel.index).get(sel.resolution);
	}

	public String getImageMapPath() {
		return settings.getImageMapPath();
	}

	public double getModelSize() {
		return modelSize;
	}
}
