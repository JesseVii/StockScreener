package jessevii.stockscreener.utils;

import java.awt.*;

public enum Location {
	LEFT(0, 1),
	RIGHT(1, 1),
	TOP(2, 0),
	BOTTOM(2, 2),
	CENTER(2, 1),
	
	TOP_LEFT_CORNER(0, 0),
	TOP_RIGHT_CORNER(1, 0),
	BOTTOM_LEFT_CORNER(0, 2),
	BOTTOM_RIGHT_CORNER(1, 2);

	//0X = LEFT
	//1X = RIGHT
	//2X = CENTER
	
	//0Y = TOP
	//1Y = CENTER
	//2Y = BOTTOM
	private int x, y;
	Location(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point getLocation(int x, int y, int width, int height) {
		int locationX = 0;
		int locationY = 0;
		
		//X
		if (this.x == 0) {
			locationX += x;
		} else if (this.x == 1) {
			locationX += x + width;
		} else if (this.x == 2) {
			locationX += x + width / 2;
		}
		
		//Y
		if (this.y == 0) {
			locationY += y;
 		} else if (this.y == 1) {
 			locationY += y + width / 2;
 		} else if (this.y == 2) {
 			locationY += y + width;
 		}
		
		return new Point(locationX, locationY);
	}
}
