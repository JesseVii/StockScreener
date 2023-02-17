package jessevii.stockscreener.component.components.positions;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.utils.ChartList;
import jessevii.stockscreener.utils.ChartList.ListObject;
import jessevii.stockscreener.utils.Location;
import jessevii.stockscreener.utils.Utils;

import java.awt.*;

public class PositionsComponent extends Component {
	public ChartList chartList;
	
	public PositionsComponent() {
		super("Positions", true);
		Position.load();
	}
	
	@Override
	public void onInit(String... params) {
		chartList = new ChartList(this);
		chartList.borderSize = 2;
		chartList.underline = true;
		chartList.height = 25;
		chartList.width = 110;
		chartList.heightSpace = 8;
		chartList.xAdd = 5;
		chartList.fontSize = 16;
		chartList.yAdd = 5;
		chartList.location = Location.LEFT;

		new Thread(() -> {
			while(true) {
				if (this.toggled) {
					render();
				}
				
				Utils.sleep(100);
			}
		}).start();
	}
	
	public void render() {
		chartList.reset();
		
		for (Position position : Position.list) {
			Stock stock = Stock.get(position.ticker, 3);
			chartList.add(new ListObject(position.ticker, true, 60, (position.isVirtual ? Color.CYAN : Color.YELLOW)));
			chartList.add(new ListObject((Utils.formatDecimal(stock.getDailyChange(), 2) + "%").replace("-", ""), 50, stock.getColor(stock.getPreviousClosePrice(), false)));
			chartList.add(new ListObject((Utils.formatDecimal(Stock.getChange(position.price, stock.getPrice()), 2) + "%").replace("-", ""), 65, stock.getColor(position.price, position.isShort())));
			chartList.add(new ListObject("" + (int)(position.shares * stock.getPrice()) + Utils.getCurrencySymbol(stock.stock.getCurrency()), 60));
			chartList.add(new ListObject("" + position.shares, 50));
			chartList.add(new ListObject(stock.stock.getName(), 500, Color.GRAY));
			
			chartList.newLine();
		}
		
		chartList.repaint();
	}
}
