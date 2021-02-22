package com.kreative.iconposeur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class IconWell extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private final List<IconWellListener> listeners = new ArrayList<IconWellListener>();
	private final MacIconSuite icns;
	private final int numTypes;
	private final int[] type;
	private final int[] width;
	private final int[] height;
	private BufferedImage image;
	
	public IconWell(MacIconSuite icns, int numTypes, int... typesWidthsHeights) {
		this.icns = icns;
		this.numTypes = numTypes;
		this.type = new int[numTypes];
		this.width = new int[numTypes];
		this.height = new int[numTypes];
		for (int p = 0, i = 0; i < numTypes; i++) {
			type[i] = typesWidthsHeights[p++];
			width[i] = typesWidthsHeights[p++];
			height[i] = typesWidthsHeights[p++];
		}
		this.image = icns.containsKey(type[0]) ? icns.getImage(type[0]) : null;
		createListeners();
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setImage(BufferedImage image) {
		if (image == null) {
			for (int i = 0; i < numTypes; i++) icns.remove(type[i]);
			this.image = null;
		} else {
			for (int i = 0; i < numTypes; i++) icns.putImage(type[i], image);
			this.image = icns.getImage(type[0]);
		}
		for (IconWellListener l : listeners) l.iconChanged(this);
		repaint();
	}
	
	public void addIconWellListener(IconWellListener listener) {
		listeners.add(listener);
	}
	
	public void removeIconWellListener(IconWellListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public Dimension getMinimumSize() {
		Insets i = getInsets();
		int w = width[0] + 8 + i.left + i.right;
		int h = height[0] + 8 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Insets i = getInsets();
		int w = width[0] + 8 + i.left + i.right;
		int h = height[0] + 8 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getMaximumSize() {
		Insets i = getInsets();
		int w = width[0] + 8 + i.left + i.right;
		int h = height[0] + 8 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Insets i = getInsets();
		int w = getWidth() - i.left - i.right;
		int h = getHeight() - i.top - i.bottom;
		g.setColor(Color.darkGray);
		g.fillRect(i.left, i.top, w, 1);
		g.fillRect(i.left, i.top, 1, h);
		g.fillRect(i.left, i.top + h - 1, w, 1);
		g.fillRect(i.left + w - 1, i.top, 1, h);
		if (image != null) {
			int ix = i.left + (w - width[0]) / 2;
			int iy = i.top + (h - height[0]) / 2;
			g.drawImage(image, ix, iy, null);
		}
	}
	
	private void createListeners() {
		setFocusable(true);
		setRequestFocusEnabled(true);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				requestFocusInWindow();
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_DELETE) {
					setImage(null);
				}
			}
		});
		new DropTarget(this, new DropTargetListener() {
			public void dragEnter(DropTargetDragEvent e) {}
			public void dragExit(DropTargetEvent e) {}
			public void dragOver(DropTargetDragEvent e) {}
			public void dropActionChanged(DropTargetDragEvent e) {}
			public void drop(DropTargetDropEvent e) {
				e.acceptDrop(e.getDropAction());
				Transferable t = e.getTransferable();
				try {
					if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
						Image image = (Image)t.getTransferData(DataFlavor.imageFlavor);
						if (image != null) {
							BufferedImage bi = toBufferedImage(image);
							if (bi != null) {
								setImage(resizeImage(bi, width[0], height[0]));
								e.dropComplete(true);
								return;
							}
						}
					}
					if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						List<?> list = (List<?>)t.getTransferData(DataFlavor.javaFileListFlavor);
						if (list != null && list.size() == 1) {
							BufferedImage bi = ImageIO.read((File)list.get(0));
							if (bi != null) {
								setImage(resizeImage(bi, width[0], height[0]));
								e.dropComplete(true);
								return;
							}
						}
					}
				} catch (Exception e2) {}
				e.dropComplete(false);
			}
		});
	}
	
	private static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) return (BufferedImage)image;
		System.out.println("converting");
		long start = System.currentTimeMillis();
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		while (w < 0 || h < 0) {
			if (System.currentTimeMillis() - start > 1000) return null;
			w = image.getWidth(null);
			h = image.getHeight(null);
		}
		for (;;) {
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = bi.createGraphics();
			boolean ok = g.drawImage(image, 0, 0, null);
			g.dispose();
			if (ok) return bi;
			if (System.currentTimeMillis() - start > 1000) return null;
		}
	}
	
	private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
		if (image.getWidth() == width && image.getHeight() == height) return image;
		System.out.println("resizing");
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		double sx = (double)width / (double)image.getWidth();
		double sy = (double)height / (double)image.getHeight();
		AffineTransform tx = AffineTransform.getScaleInstance(sx, sy);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
		g.drawImage(image, op, 0, 0);
		g.dispose();
		return resized;
	}
}
