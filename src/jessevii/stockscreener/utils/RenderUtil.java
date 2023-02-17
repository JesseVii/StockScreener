package jessevii.stockscreener.utils;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.main.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class RenderUtil {
	public static JPanel overridePanel;
	
	public static void drawString(Graphics2D g, String text, int x, int y, int size) {
		drawString(g, text, x, y, size, false);
	}
	
	/**
	 * Draws a string to the screen using the passed graphics.
	 * You can pass a rgb color with the string like this -:0, 255, 0:- and it will use the color with the upcoming text until u pass another color.
	 * If you use multiple colors then you must include the first color too
	 * Set x to -1 to draw centered string
	 */
	public static void drawString(Graphics2D g, String text, int x, int y, int size, boolean scroll) {
		if (Component.current != null && scroll) {
			y += Component.current.yAdd;
		}
		
		int widthPlus = 0;
		int textWidth = 0;
		String startRegex = "-:";
		String[] texts = {text};
		
		//Set font
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(new Font(Main.font, Font.PLAIN, size));
		
		//Split the text into list for each given color
		if (text.contains(startRegex)) {
			ArrayList<String> list = new ArrayList<>();
			for (String s : text.split(startRegex)) {
				if (!s.isEmpty()) {
					list.add(startRegex + s);
					
					//Calculate total text width including all parts of the real text
					String parsed = s.substring(s.indexOf(":-") + 2);
					textWidth += g.getFontMetrics().stringWidth(parsed.replace(" &&&", ""));
				}
			}
			
	        String[] temp = new String[list.size()];
	        for (int i = 0; i < list.size(); i++) { 
	            temp[i] = list.get(i); 
	        } 
	  
	        texts = temp;
		}
		
		//Loop through the split color text things and add the width of the text to widthPlus so next piece will be rendered in the correct location
		for (String s : texts) {
			//Set color
			Color color = Color.WHITE;
			if (s.contains(startRegex)) {
				String[] split = s.replace(startRegex, "").substring(0, s.indexOf(":-") - 2).replace(" ", "").split(",");
				color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
			}
			
			//Draw text and add its width to widthPlus
			String parsedText = s;
			if (parsedText.contains(startRegex)) {
				parsedText = parsedText.substring(parsedText.indexOf(":-") + 2);
			}
			
			parsedText = parsedText.replace(" &&&", "");
			g.setColor(color);
			if (x != -1) {
				g.drawString(parsedText, x + widthPlus, y);
			} else {
				JPanel panel = overridePanel;
				if (panel == null) panel = Component.current.panel;
				g.drawString(parsedText, (((panel.getWidth() / 2) - (textWidth / 2)) + widthPlus) - 4, y);
			}
			widthPlus += g.getFontMetrics().stringWidth(parsedText);
		}
	}
	
	public static String toStringRbg(Color color) {
		return "-:" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ":-";
	}
	
	public static void fill(double x, double y, double width, double height, Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
		g.fill(rect);
	}
	
	public static String getStringColorForNumber(double number) {
		if (number > 0) {
			return toStringRbg(Color.GREEN);
		} else if (number < 0) {
			return toStringRbg(Color.RED);
		} else {
			return toStringRbg(Color.GRAY);
		}
	}

	public static String addColors(String text) {
		try {
			String[] split = text.split(":");
			return "-:255, 255, 255:-" + split[0] + ":-:226, 184, 227:-" + split[1] + (split.length >= 3 ? ":" + split[2] : "");
		} catch (Exception e) {
			return text;
		}
	}
}
