package jessevii.stockscreener.component.components.positions;

import jessevii.stockscreener.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class Position {
	public String ticker;
	public double price;
	public int shares;
	public boolean isVirtual;
	public String id;
	public static List<Position> list = new ArrayList<>();
	
	public Position(String ticker, double priceBoughtAt, int amountOfShares, boolean virtualTrade, String id) {
		this.ticker = ticker;
		this.price = priceBoughtAt;
		this.shares = amountOfShares;
		this.isVirtual = virtualTrade;
		this.id = id;
	}
	
	public static void load() {
		list.clear();
		for (List<String> split : FileUtil.readFile("Positions")) {
			Position position = new Position(split.get(0), Double.parseDouble(split.get(2)), Integer.parseInt(split.get(1)), Boolean.parseBoolean(split.get(3)), split.get(5));
			if (Boolean.parseBoolean(split.get(4))) {
				position.shares = -position.shares;
			}
			
			list.add(position);
 		}
	}
	
	/*
	 * Adds this position to the file and list
	 * If it already has a position with the same ticker then it will
	 * Remove or add the shares this position has and average the price bought at
	 */
	public void add() {
		String id = this.id;
		int existingShares = -92348214;
		double oldPriceBoughtAt = -1;
		Position pos = null;
		for (Position position : list) {
			if (position.ticker.equals(this.ticker) && position.isVirtual == this.isVirtual) {
				existingShares = position.shares;
				oldPriceBoughtAt = position.price;
				id = position.id;
				pos = position;
			}
		}
		
		int shares = this.shares;
		double avgPrice = this.price;
		if (existingShares != -92348214) {
			FileUtil.removeFromFile("Positions", this.ticker);

			//Calculate averagePrice
			avgPrice = (oldPriceBoughtAt * existingShares + this.price * this.shares) / (existingShares + this.shares);
			shares += existingShares;
		}
		
		FileUtil.addToFile("Positions", new Object[]{ticker, Math.abs(shares), avgPrice, this.isVirtual, shares < 0, id});
		if (pos != null) {
			pos.shares = shares;
			pos.price = avgPrice;
		}
		
		load();
	}
	
	/*
	 * Removes the given amount of shares from this position
	 */
	public void remove(int shares) {
		for (Position position : list) {
			if (position.ticker.equals(this.ticker)) {
				int currentShares = position.shares - shares;
				FileUtil.removeFromFile("Positions", this.ticker);
				
				if (currentShares != 0) {
					FileUtil.addToFile("Positions", new Object[]{this.ticker, Math.abs(currentShares), this.price, this.isVirtual, currentShares < 0, this.id});
				}
				
				load();
				return;
			}
		}
	}
	
	public boolean isShort() {
		return this.shares < 0;
	}
	
	public static Position get(String ticker) {
		for (Position position : list) {
			if (position.ticker.equals(ticker)) {
				return position;
			}
		}
		
		return null;
	}
	
	public static boolean hasPosition(String ticker) {
		return list.stream().anyMatch(p -> p.ticker.equals(ticker));
	}
}