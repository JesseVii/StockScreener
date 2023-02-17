package jessevii.stockscreener.component.components.trade;

import jessevii.stockscreener.component.components.positions.Position;
import jessevii.stockscreener.utils.Utils;

public class Trade {
	
	/**
	 * Called when the Buy button is pressed
	 * Returns the position that buys the stock.
	 * If returned position is null it will not buy it
	 */
	public static Position buy(int shares, String ticker, double price, boolean virtualTrade) {
		return new Position(ticker, price, shares, virtualTrade, Utils.generateRandomString(20));
	}
	
	/**
	 * Called when the Sell button is pressed
	 * Returns the SellObject that then sells the amount of stock with the price
	 * if returned SellObject is null it won't sell anything
	 */
	public static SellObject sell(int shares, double price) {
		return new SellObject(shares, price);
	}
	
	public static class SellObject {
		public int shares;
		public double price;
		
		public SellObject(int shares, double price) {
			this.shares = shares;
			this.price = price;
		}
	}
}
