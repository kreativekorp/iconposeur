package com.kreative.iconposeur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JColorChooser;
import javax.swing.JComponent;

public class ColorTablePanel extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private int rows;
	private int columns;
	private int[] colorTable;
	private int selectedIndex;
	private final List<ColorTableListener> listeners;
	
	public ColorTablePanel(int rows, int columns, int[] colorTable) {
		this.rows = rows;
		this.columns = columns;
		this.colorTable = colorTable;
		this.selectedIndex = -1;
		this.listeners = new ArrayList<ColorTableListener>();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}
	
	public int getRowCount() {
		return rows;
	}
	
	public void setRowCount(int rows) {
		this.rows = rows;
		this.repaint();
		for (ColorTableListener l : listeners) l.dimensionsChanged(this);
	}
	
	public int getColumnCount() {
		return columns;
	}
	
	public void setColumnCount(int columns) {
		this.columns = columns;
		this.repaint();
		for (ColorTableListener l : listeners) l.dimensionsChanged(this);
	}
	
	public int[] getColorTable() {
		return colorTable;
	}
	
	public void setColorTable(int[] colorTable) {
		this.colorTable = colorTable;
		this.repaint();
		for (ColorTableListener l : listeners) l.colorTableChanged(this);
	}
	
	public int getColor(int index) {
		return colorTable[index];
	}
	
	public void setColor(int index, int color) {
		this.colorTable[index] = color;
		this.repaint();
		for (ColorTableListener l : listeners) l.colorTableChanged(this);
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
		this.repaint();
		for (ColorTableListener l : listeners) l.selectionChanged(this);
	}
	
	public void addColorTableListener(ColorTableListener listener) {
		listeners.add(listener);
	}
	
	public void removeColorTableListener(ColorTableListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public Dimension getMinimumSize() {
		Insets i = getInsets();
		int w = columns * 16 + i.left + i.right;
		int h = rows * 16 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Insets i = getInsets();
		int w = columns * 16 + i.left + i.right;
		int h = rows * 16 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getMaximumSize() {
		Insets i = getInsets();
		int w = columns * 16 + i.left + i.right;
		int h = rows * 16 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (colorTable == null) return;
		Insets i = getInsets();
		int w = getWidth() - i.left - i.right;
		int h = getHeight() - i.top - i.bottom;
		for (int idx = 0, y = 0; y < rows; y++) {
			int gy = (h * y / rows);
			int gh = (h * (y + 1) / rows) - gy;
			gy += i.top;
			for (int x = 0; x < columns; x++, idx++) {
				int gx = (w * x / columns);
				int gw = (w * (x + 1) / columns) - gx;
				gx += i.left;
				if (idx < colorTable.length) {
					if (idx == selectedIndex) {
						g.setColor(Color.black);
						g.fillRect(gx, gy, gw, gh);
					}
					g.setColor(new Color(colorTable[idx], true));
					g.fillRect(gx + 1, gy + 1, gw - 2, gh - 2);
				}
			}
		}
	}
	
	public int getIndexAt(int x, int y) {
		Insets i = getInsets();
		int w = getWidth() - i.left - i.right;
		int h = getHeight() - i.top - i.bottom;
		x -= i.left; if (x < 0 || x >= w) return -1;
		y -= i.top; if (y < 0 || y >= h) return -1;
		return (rows * y / h) * columns + (columns * x / w);
	}
	
	private final MouseAdapter mouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			setSelectedIndex(getIndexAt(e.getX(), e.getY()));
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			setSelectedIndex(getIndexAt(e.getX(), e.getY()));
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
				int i = getIndexAt(e.getX(), e.getY());
				if (i >= 0 && i < colorTable.length) {
					Color c = JColorChooser.showDialog(
						ColorTablePanel.this,
						("Color #" + i),
						new Color(colorTable[i], true)
					);
					if (c != null) {
						setColor(i, c.getRGB());
					}
				}
			}
		}
	};
}
