package aigis.model;

import java.util.Objects;

public class ModelSelection implements Comparable<ModelSelection> {

	public enum Resolution {
		Low,
		// Normal,
		High,
		// VeryHigh,
	}

	public Resolution resolution;
	public int index;

	public ModelSelection(Resolution resolution, int index) {
		this.resolution = resolution;
		this.index = index;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ModelSelection)) return false;
		ModelSelection other = (ModelSelection)obj;
		if (index != other.index) return false;
		if (resolution != other.resolution) return false;
		return true;
	}

	@Override
	public int compareTo(ModelSelection arg) {
		int comp = Integer.compare(index, arg.index);
		if (comp == 0) {
			return resolution.compareTo(arg.resolution);
		}
		return comp;
	}
	
	@Override
    public int hashCode() {
        return Objects.hash(index, resolution);
    }

}
