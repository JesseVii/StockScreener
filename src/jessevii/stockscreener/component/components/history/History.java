package jessevii.stockscreener.component.components.history;

import jessevii.stockscreener.component.components.positions.Position;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.objects.Time;
import jessevii.stockscreener.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class History {
	public String ticker, id;
	public boolean isBought, isVirtual, isShort;
	public int shares;
	public double price;
	public Time time;
	public static List<History> list = new ArrayList<>();
	
	public History(String ticker, boolean isBought, int shares, double price, boolean isVirtual, boolean isShort, String id, Time time) {
		this.ticker = ticker;
		this.isBought = isBought;
		this.shares = shares;
		this.price = price;
		this.isVirtual = isVirtual;
		this.isShort = isShort;
		this.id = id;
		this.time = time;
	}
	
	public History(Position position, boolean isBought) {
		this.ticker = position.ticker;
		this.isBought = isBought;
		this.shares = position.shares;
		this.price = position.price;
		this.isVirtual = position.isVirtual;
		this.isShort = position.isShort();
		this.id = position.id;
		this.time = Time.getTime();
	}
	
	/**
	 * Adds this history to the file
	 */
	public void add() {
		FileUtil.addToFile("History", new Object[]{ticker, isBought, shares, price, isVirtual, isShort, id, time.toStringDash()});
		list.add(this);
	}
	
	/**
	 * Loads the histories from the file and sets the list
	 */
	public static void load() {
		list.clear();
		for (List<String> split : FileUtil.readFile("History")) {
			History history = new History(
					split.get(0), 
					Boolean.parseBoolean(split.get(1)), 
					Integer.parseInt(split.get(2)), 
					Double.parseDouble(split.get(3)), 
					Boolean.parseBoolean(split.get(4)), 
					Boolean.parseBoolean(split.get(5)), 
					split.get(6),
					Time.parseTime(split.get(7)));
			list.add(history);
		}
	}
	
	public static double calculatePercentageGain(List<History> list) {
		double bought = 0;
		double sold = 0;
		for (History history : list) {
			if (history.isBought) {
				bought += history.shares * history.price;
			} else {
				sold += history.shares * history.price;
			}
		}
		
		return Stock.getChange(bought, sold);
	}

	public static int calculateGain(List<History> list) {
		return calculateGain(list, h -> true, null);
	}
	
	public static int calculateGain(List<History> list, Predicate<History> filter, Predicate<Position> positionFilter) {
		double bought = 0;
		double sold = 0;
		List<String> calculatedIds = new ArrayList<>();
		
		for (History history : list) {
			if (filter.test(history)) {
				if (!history.isShort) {
					if (history.isBought) {
						bought += history.shares * history.price;
					} else {
						sold += history.shares * history.price;
					}
				} else {
					if (history.isBought) {
						sold += history.shares * history.price;
					} else {
						bought += history.shares * history.price;
					}
				}
				
				if (!calculatedIds.contains(history.id)) {
					calculatedIds.add(history.id);
				}
			}
		}
		
		if (positionFilter != null) {
			for (Position position : Position.list) {
				if (positionFilter.test(position) && calculatedIds.contains(position.id)) {
					if (!position.isShort()) {
						sold += position.shares * Stock.get(position.ticker, 5).getPrice();
					} else {
						bought += position.shares * Stock.get(position.ticker, 5).getPrice();
					}
				}
			}
		}
		
		return (int)(sold - bought);
	}
}
