package com.kreative.iconposeur;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class WinIconWellModel extends IconWellModel {
	public static final class Size {
		public final Integer width;
		public final Integer height;
		public final Integer bpp;
		public final Boolean png;
		public final int[] colorTable;
		public final boolean view;
		public final boolean set;
		public final boolean removeOnSet;
		public final boolean remove;
		public Size(
			Integer width, Integer height, Integer bpp, Boolean png, int[] colorTable,
			boolean view, boolean set, boolean removeOnSet, boolean remove
		) {
			this.width = width;
			this.height = height;
			this.bpp = bpp;
			this.png = png;
			this.colorTable = colorTable;
			this.view = view;
			this.set = set;
			this.removeOnSet = removeOnSet;
			this.remove = remove;
		}
	}
	
	private final WinIconDir ico;
	private final Size[] sizes;
	
	public WinIconWellModel(WinIconDir ico, Size... sizes) {
		this.ico = ico;
		this.sizes = sizes;
	}
	
	@Override
	public Dimension getImageSize() {
		return new Dimension(sizes[0].width, sizes[0].height);
	}
	
	@Override
	public Image getImage() {
		for (Size size : sizes) {
			if (size.view) {
				WinIconDirEntry e = ico.get(size.width, size.height, size.bpp, size.png);
				if (e != null) {
					return e.getImage();
				}
			}
		}
		return null;
	}
	
	@Override
	public void setImage(Component parent, Image image) {
		for (Size size : sizes) {
			if (size.removeOnSet) {
				ico.removeAll(ico.getAll(size.width, size.height, size.bpp, size.png));
			}
			if (size.set) {
				BufferedImage bi = loadImage(parent, image, size.width, size.height);
				if (bi != null) {
					try {
						WinIconDirEntry e = new WinIconDirEntry(ico.isCursor());
						if (size.png) {
							e.setPNGImage(bi);
						} else {
							e.setBMPImage(bi, size.bpp, size.colorTable);
						}
						ico.add(e);
					} catch (Exception e) {
						// Ignored.
					}
				}
			}
		}
	}
	
	@Override
	public void removeImage() {
		for (Size size : sizes) {
			if (size.remove) {
				ico.removeAll(ico.getAll(size.width, size.height, size.bpp, size.png));
			}
		}
	}
	
	@Override
	public boolean intersects(IconWellModel model) {
		if (model instanceof WinIconWellModel) {
			if (((WinIconWellModel)model).ico == this.ico) {
				for (Size a : ((WinIconWellModel)model).sizes) {
					for (Size b : this.sizes) {
						if (
							(a.width == null || b.width == null || a.width.equals(b.width)) &&
							(a.height == null || b.height == null || a.height.equals(b.height)) &&
							(a.bpp == null || b.bpp == null || a.bpp.equals(b.bpp)) &&
							(a.png == null || b.png == null || a.png.equals(b.png))
						) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
