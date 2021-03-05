package com.kreative.iconposeur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;

public class ColorTablePanel extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private int rows;
	private int columns;
	private int[] colorTable;
	private int selectedIndex;
	
	public ColorTablePanel(int rows, int columns, int[] colorTable) {
		this.rows = rows;
		this.columns = columns;
		this.colorTable = colorTable;
		this.selectedIndex = -1;
	}
	
	public int getRowCount() {
		return rows;
	}
	
	public void setRowCount(int rows) {
		this.rows = rows;
		this.repaint();
	}
	
	public int getColumnCount() {
		return columns;
	}
	
	public void setColumnCount(int columns) {
		this.columns = columns;
		this.repaint();
	}
	
	public int[] getColorTable() {
		return colorTable;
	}
	
	public void setColorTable(int[] colorTable) {
		this.colorTable = colorTable;
		this.repaint();
	}
	
	public int getColor(int index) {
		return colorTable[index];
	}
	
	public void setColor(int index, int color) {
		this.colorTable[index] = color;
		this.repaint();
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
		this.repaint();
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
					g.setColor(new Color(colorTable[idx]));
					g.fillRect(gx + 1, gy + 1, gw - 2, gh - 2);
				}
			}
		}
	}
}
