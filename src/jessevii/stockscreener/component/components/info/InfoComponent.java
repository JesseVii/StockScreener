package jessevii.stockscreener.component.components.info;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.history.History;
import jessevii.stockscreener.component.components.positions.Position;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.utils.RenderUtil;
import jessevii.stockscreener.utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfoComponent extends Component {
	public double alltimeGain, alltimeVirtualGain;
	public double todaysGain, todaysVirtualGain;
	public double todaysGainPercentage, todaysVirtualGainPercentage;
	public int tradesMade;
	public List<TradeYear> tradeYears = new ArrayList<>();
	
	public InfoComponent() {
		super("Info", true);
	}
	
	@Override
	public void onEnabled(String... params) {
		alltimeGain = History.calculateGain(History.list, h -> !h.isVirtual, p -> !p.isVirtual);
		alltimeVirtualGain = History.calculateGain(History.list, h -> h.isVirtual, p -> p.isVirtual);
		
		List<String> ids = new ArrayList<>();
		List<Integer> years = new ArrayList<>();
		for (History history : History.list) {
			if (history.isVirtual) {
				continue;
			}
			
			if (!ids.contains(history.id)) {
				ids.add(history.id);
			}
			
			if (!years.contains(history.time.year)) {
				years.add(history.time.year);
			}
		}
		
		tradesMade = ids.size();
		Collections.reverse(years);
		
		for (int year : years) {
			TradeYear tradeYear = new TradeYear();
			tradeYear.gain = History.calculateGain(History.list, h -> h.time.year == year && !h.isVirtual, p -> true);
			tradeYear.virtualGain = History.calculateGain(History.list, h -> h.time.year == year && h.isVirtual, p -> true);
			tradeYear.year = year;
			
			List<String> tradeYearIds = new ArrayList<>();
			for (History history : History.list) {
				if (history.isVirtual) {
					continue;
				}
				
				if (history.time.year == year && !tradeYearIds.contains(history.id)) {
					tradeYearIds.add(history.id);
				}
			}
			
			tradeYear.trades = tradeYearIds.size();
			
			tradeYears.add(tradeYear);
		}
		
		int virtualTotal = 0;
		int total = 0;
		for (Position position : Position.list) {
			Stock stock = Stock.get(position.ticker, 5);
			
			if (position.isVirtual) {
				virtualTotal += stock.getPrice() * position.shares;
				todaysVirtualGain += (stock.getPrice() - stock.getPreviousClosePrice()) * position.shares;
			} else {
				total += stock.getPrice() * position.shares;
				todaysGain += (stock.getPrice() - stock.getPreviousClosePrice()) * position.shares;
			}
		}
		
		todaysGainPercentage = (todaysGain / (double)total) * 100;
		todaysVirtualGainPercentage = (todaysVirtualGain / (double)virtualTotal) * 100;
		
		this.yAdd = -20;
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		int font = 25;
		RenderUtil.drawString(g, "-:255, 255, 255:-All time gain: " + RenderUtil.getStringColorForNumber(alltimeGain) + ((int)alltimeGain > 0 ? "+" : "") + (int)alltimeGain + "$", -1, 50 + ((font + 5) * 0), font, true);
		RenderUtil.drawString(g, "-:255, 255, 255:-Today's gain: " + RenderUtil.getStringColorForNumber(todaysGain) + ((int)todaysGain > 0 ? "+" : "") + (int)todaysGain + "$ (" + Utils.formatDecimal(todaysGainPercentage, 2) + "%)", -1, 50 + ((font + 5) * 1), font, true);
		RenderUtil.drawString(g, "-:255, 255, 255:-Trades: " + RenderUtil.toStringRbg(Color.CYAN) + tradesMade, -1, 50 + ((font + 5) * 2), font, true);
		
		int amount = 5;
		for (TradeYear tradeYear : tradeYears) {
			RenderUtil.drawString(g, "-:255, 255, 255:---------------------- " + RenderUtil.toStringRbg(Color.ORANGE) + tradeYear.year + " -:255, 255, 255:----------------------", -1, 50 + ((font + 5) * amount), font, true);
			amount++;
			RenderUtil.drawString(g, "-:255, 255, 255:-Gain: " + RenderUtil.getStringColorForNumber(tradeYear.gain) + ((int)tradeYear.gain > 0 ? "+" : "") + (int)tradeYear.gain + "$", -1, 50 + ((font + 5) * amount), font, true);
			amount++;
			RenderUtil.drawString(g, "-:255, 255, 255:-Trades: " + RenderUtil.toStringRbg(Color.CYAN) + tradeYear.trades, -1, 50 + ((font + 5) * amount), font, true);
			
			amount += 2;
		}
	}
	
	public static class TradeYear {
		public int year;
		public int trades;
		public double gain, virtualGain;
	}
}
