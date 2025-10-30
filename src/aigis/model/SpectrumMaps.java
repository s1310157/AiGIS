package aigis.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * あるモデルが持つスペクトルマップを管理するラッパクラスです。
 * 
 * @author m5161121
 *
 */
public class SpectrumMaps {
	private LinkedHashMap<String, SpectrumMap> spectrumMaps;

	SpectrumMaps() {
		spectrumMaps = new LinkedHashMap<String, SpectrumMap>();
	}

	public int getSize() {
		return spectrumMaps.size();
	}

	public void clear() {
		spectrumMaps.clear();
	}

	public void addSpectrumMap(String key, SpectrumMap spectrumMap) {
		spectrumMaps.put(key, spectrumMap);
	}

	public void convertToRGB(String key, float[][] rgba, int colorbarIndex) {
		if (spectrumMaps.containsKey(key)) {
			spectrumMaps.get(key).convertToRGB(rgba, colorbarIndex);
		} else {
			return;
		}
	}

	public SpectrumMap getSpectrum(String key) {
		return spectrumMaps.get(key);
	}

	public float getSpectrumData(String key, int face) {
		if (spectrumMaps.containsKey(key)) {
			return spectrumMaps.get(key).getSpectrumData(face);
		} else {
			return -1;
		}
	}

	public ArrayList<String> getSpectrumList() {
		ArrayList<String> ret = new ArrayList<String>();
		for (Map.Entry<String, SpectrumMap> entry : spectrumMaps.entrySet()) {
			ret.add(entry.getValue().getName());
		}
		return ret;
	}

	public Set<Map.Entry<String, SpectrumMap>> getEntrySet() {
		return spectrumMaps.entrySet();
	}

}
