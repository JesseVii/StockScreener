package jessevii.stockscreener.objects;

import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Stock {
	public String ticker;
	private int updateMs;
	private long lastUpdate;
	public boolean dontUpdate;
	public double high, low;
	public StockQuote quote, lastQuote;
	public yahoofinance.Stock stock;
	private final List<StockListener> listeners = new ArrayList<>();
	private static final List<Stock> list = new ArrayList<>();
	
	private Stock(String ticker, int updateMs) {
		this.ticker = ticker.toUpperCase();
		this.updateMs = updateMs;
		resetHighAndLow();
		list.add(this);
	}
	
	public double getPreviousClosePrice() {
		update();
		return quote.getPreviousClose().doubleValue();
	}
	
	public double getPrice() {
		update();
		return quote.getPrice().doubleValue();
	}
	
	public double getDailyChange() {
		update();
		return getChange(quote.getPreviousClose().doubleValue());
	}
	
	public double getChange(double startingPrice) {
		update();
		return getChange(startingPrice, getPrice());
	}
	
	public static double getChange(double start, double end) {
		return ((end - start) / start) * 100;
	}
	
	public Color getColor(double startingPrice, boolean isShort) {
		double price = getPrice();
		if (price > startingPrice) {
			return Color.GREEN;
		} else if (price < startingPrice) {
			return Color.RED;
		} else {
			return Color.GRAY;
		}
	}
	
	public static Color getColor(double change) {
		if (change > 0) {
			return Color.GREEN;
		} else if (change < 0) {
			return Color.RED;
		} else {
			return Color.GRAY;
		}
	}
	
	public static Color getColor(double start, double end, boolean isShort) {
		if (end > start) {
			return Color.GREEN;
		} else if (end < start) {
			return Color.RED;
		} else {
			return Color.GRAY;
		}
	}
	
	public void addListener(StockListener listener) {
		listeners.add(listener);
	}
	
	public void resetHighAndLow() {
		high = Integer.MIN_VALUE;
		low = Integer.MAX_VALUE;
	}
	
	public boolean update() {
		if (dontUpdate) {
			return quote != null;
		}
		
		if (Math.abs(System.currentTimeMillis() - lastUpdate) >= updateMs) {
			try {
				StockQuote last = lastQuote;
				lastQuote = quote;
				stock = YahooFinance.get(ticker);
				quote = stock.getQuote();
				if (quote.getPrice().doubleValue() > high) high = quote.getPrice().doubleValue();
				if (quote.getPrice().doubleValue() < low) low = quote.getPrice().doubleValue();
				
				if (lastQuote == null || lastQuote.getPrice().doubleValue() != quote.getPrice().doubleValue()) {
					for (StockListener listener : listeners) {
						listener.priceChanged();
					}
				} else {
					lastQuote = last;
				}
			} catch (Exception e) {
				if (lastUpdate == 0) {
					return false;
				}
			}
			
			lastUpdate = System.currentTimeMillis();
		}
		
		return quote != null;
	}
	
	/**
	 * Gets the stock object or creates a new one
	 * @param updateSeconds How often to update the stock quote in seconds
	 */
	public static Stock get(String ticker, double updateSeconds) {
		for (Stock stock : list) {
			if (stock.ticker.equals(ticker)) {
				stock.updateMs = (int)(updateSeconds * 1000);
				return stock;
			}
		}
		
		Stock stock = new Stock(ticker, (int)(updateSeconds * 1000));
		if (stock.update()) {
			return stock;
		} else {
			return null;
		}
	}
	
	public static boolean exists(String ticker) {
		try {
			YahooFinance.get(ticker).getQuote();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static class StockListener {
		public void priceChanged() {
			
		}
	}
}
