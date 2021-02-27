package com.kreative.iconposeur;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class MacIconWellModel extends IconWellModel {
	public static final class Type {
		public final int type;
		public final int width;
		public final int height;
		public final boolean view;
		public final boolean set;
		public Type(int type, int width, int height, boolean view, boolean set) {
			this.type = type;
			this.width = width;
			this.height = height;
			this.view = view;
			this.set = set;
		}
	}
	
	private final MacIconSuite icns;
	private final Type[] types;
	
	public MacIconWellModel(MacIconSuite icns, Type... types) {
		this.icns = icns;
		this.types = types;
	}
	
	@Override
	public Dimension getImageSize() {
		return new Dimension(types[0].width, types[0].height);
	}
	
	@Override
	public BufferedImage getImage() {
		for (Type type : types) {
			if (type.view && icns.containsKey(type.type)) {
				return icns.getImage(type.type);
			}
		}
		return null;
	}
	
	@Override
	public void setImage(Component parent, Image image) {
		for (Type type : types) {
			if (type.set) {
				BufferedImage bi = loadImage(parent, image, type.width, type.height);
				if (bi != null) {
					icns.putImage(type.type, bi);
					continue;
				}
			}
			icns.remove(type.type);
		}
	}
	
	@Override
	public void removeImage() {
		for (Type type : types) {
			icns.remove(type.type);
		}
	}
}
