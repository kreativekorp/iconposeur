package com.kreative.iconposeur;

import java.util.ArrayList;
import java.util.List;

public class IconWellGroup {
	private final List<IconWell> wells = new ArrayList<IconWell>();
	
	public void add(IconWell well) {
		wells.add(well);
		well.addIconWellListener(listener);
	}
	
	public void remove(IconWell well) {
		wells.remove(well);
		well.removeIconWellListener(listener);
	}
	
	private final IconWellListener listener = new IconWellListener() {
		@Override
		public void iconChanged(IconWell srcWell) {
			IconWellModel srcModel = srcWell.getModel();
			for (IconWell dstWell : wells) {
				if (dstWell != srcWell) {
					IconWellModel dstModel = dstWell.getModel();
					if (dstModel.intersects(srcModel)) {
						dstWell.updateImage();
					}
				}
			}
		}
	};
}
