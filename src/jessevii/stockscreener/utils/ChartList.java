package jessevii.stockscreener.utils;

import jessevii.stockscreener.component.components.chart.ChartComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ChartList {
	public List<ListObject> listObjects = new ArrayList<>();
	private final List<Component> jComponents = new ArrayList<>();
	private int line, lineSize;
	private final jessevii.stockscreener.component.Component component;
	public Color backgroundColor;
	public Color borderColor = Color.BLACK;
	public int borderSize = 2;
	public int fontSize = 10;
	public int width = 65;
	public int height = 50;
	public int widthSpace = 10;
	public int heightSpace = 10;
	public int xAdd = 10;
	public int yAdd = 10;
	public boolean underline = false;
	public Location location = Location.CENTER;

	public ChartList(jessevii.stockscreener.component.Component component) {
		this.component = component;
		backgroundColor = this.component.panel.getBackground();
	}
	
	/**
	 * Adds new ListObject to the current line
	 */
	public void add(ListObject listObject) {
		listObject.line = line;
		listObject.lineSize = lineSize;
		if (listObject.width == 0) {
			listObject.width = width;
		}
		
		listObjects.add(listObject);
		lineSize++;
	}
	
	/**
	 * Starts drawing the next listObjects below
	 */
	public void newLine() {
		line++;
		lineSize = 0;
	}
	
	/**
	 * Resets this object to default values
	 */
	public void reset() {
		listObjects.clear();
		line = 0;
		lineSize = 0;
	}
	
	/**
	 * Repaints it and the panel
	 */
	public void repaint() {
		//Remove old components
		for (Component component : jComponents) {
			this.component.panel.remove(component);
		}
		jComponents.clear();
		
		//Make the new components
		int x = xAdd;
		ListObject last = null;
		for (ListObject listObject : listObjects) {
			if (last != null) {
				if (last.line == listObject.line) {
					x += last.width + widthSpace;
				} else {
					x = xAdd;
					
					if (this.underline) {
						JPanel underline = ComponentUtil.createPanel(-10, (((listObject.line * (height + heightSpace)) + yAdd) + component.yAdd) - 5, 4200, borderSize, borderSize, borderColor, backgroundColor);
						jComponents.add(underline);
						this.component.panel.add(underline);
					}
				}
			}
			last = listObject;
			
			int size = borderSize;
			if (underline) {
				size = 0;
			}
			
			JPanel objectPanel = ComponentUtil.createPanel(x, ((listObject.line * (height + heightSpace)) + yAdd) + component.yAdd, listObject.width, height, size, borderColor, backgroundColor);
			listObject.x = objectPanel.getX();
			listObject.y = objectPanel.getY();
			JLabel label = ComponentUtil.createLabel(listObject.text, 0, 0, 58008, 58008, fontSize);
			label.setForeground(listObject.color);
			if (location == Location.CENTER) {
				objectPanel.setLayout(new GridBagLayout());
			} else if (location == Location.LEFT) {
				objectPanel.setLayout(new GridLayout(3, 1, 0, -objectPanel.getHeight() + 2));
			}
			objectPanel.add(label);
			
			//Add click listener to JPanel and make it go to chart if its clicked and ticker is true in listObject
			if (listObject.ticker) {
				objectPanel.addMouseListener(new MouseAdapter() {
					@Override
		            public void mousePressed(MouseEvent e) {
						component.disable();
						ChartComponent.instance.enable(listObject.text);
					}
				});
			}
			
			this.component.panel.add(objectPanel);
			jComponents.add(objectPanel);
		}
		
		//Repaint panel
		this.component.panel.revalidate();
		this.component.panel.repaint();
	}
	
	public static class ListObject {
		public String text;
		public Color color = Color.WHITE;
		public boolean ticker;
		public int line, lineSize;
		public int width;
		public int x, y;

		public ListObject(String text, boolean ticker, int width, Color color) {
			this.text = text;
			this.ticker = ticker;
			this.width = width;
			this.color = color;
		}
		
		public ListObject(String text, int width) {
			this.text = text;
			this.width = width;
		}
		
		public ListObject(String text, int width, Color color) {
			this.text = text;
			this.width = width;
			this.color = color;
		}
	}
}
