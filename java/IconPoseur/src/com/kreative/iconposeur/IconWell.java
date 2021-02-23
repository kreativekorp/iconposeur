package com.kreative.iconposeur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
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
	
	public void setImage(Image image) {
		if (image == null) {
			for (int i = 0; i < numTypes; i++) icns.remove(type[i]);
			this.image = null;
		} else {
			for (int i = 0; i < numTypes; i++) {
				BufferedImage bi = loadImage(image, width[i], height[i]);
				if (bi != null) icns.putImage(type[i], bi);
			}
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
							setImage(image);
							e.dropComplete(true);
							return;
						}
					}
					if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						List<?> list = (List<?>)t.getTransferData(DataFlavor.javaFileListFlavor);
						if (list != null && list.size() == 1) {
							BufferedImage image = ImageIO.read((File)list.get(0));
							if (image != null) {
								setImage(image);
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
	
	private BufferedImage loadImage(Image image, int width, int height) {
		if (image instanceof BufferedImage) {
			BufferedImage bi = (BufferedImage)image;
			int w = bi.getWidth(), h = bi.getHeight();
			if (w == width && h == height) return bi;
		} else {
			MediaTracker mt = new MediaTracker(this);
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
		MediaTracker mt = new MediaTracker(this);
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
