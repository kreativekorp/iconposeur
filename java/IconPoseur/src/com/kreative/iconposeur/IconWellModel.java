package com.kreative.iconposeur;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;

public abstract class IconWellModel {
	public abstract Dimension getImageSize();
	public abstract Image getImage();
	public abstract void setImage(Component parent, Image image);
	public abstract void removeImage();
	
	protected final BufferedImage loadImage(Component parent, Image image, int width, int height) {
		if (image instanceof BufferedImage) {
			BufferedImage bi = (BufferedImage)image;
			int w = bi.getWidth(), h = bi.getHeight();
			if (w == width && h == height) return bi;
		} else {
			MediaTracker mt = new MediaTracker(parent);
			mt.addImage(image, 0);
			try { mt.waitForID(0); }
			catch (InterruptedException e) { return null; }
			int w = image.getWidth(null), h = image.getHeight(null);
			if (w == width && h == height) {
				BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = bi.createGraphics();
				g.drawImage(image, 0, 0, null);
				g.dispose();
				return bi;
			}
		}
		image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		MediaTracker mt = new MediaTracker(parent);
		mt.addImage(image, 0);
		try { mt.waitForID(0); }
		catch (InterruptedException e) { return null; }
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bi;
	}
}
