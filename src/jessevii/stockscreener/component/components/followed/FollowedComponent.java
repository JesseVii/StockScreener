package jessevii.stockscreener.component.components.followed;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.utils.*;
import jessevii.stockscreener.utils.ChartList.ListObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FollowedComponent extends Component {
	public static List<String> followed = new ArrayList<>();
	public String fileName = "Followed";
	public ChartList chartList;
	public boolean firstRender;
	public long lastUpdate;
	
	public FollowedComponent() {
		super("Followed", true);
		
		for (List<String> ticker : FileUtil.readFile(fileName)) {
			followed.add(ticker.get(0));
		}
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
		chartList.yAdd = 55;
		chartList.location = Location.LEFT;
		
		JTextField ticker = ComponentUtil.createTextField("", 5, 5, 150, 40, 1, 20, false);
		dontScroll.add(ticker);
		ComponentUtil.setTextFieldOnlyUppercase(ticker);
		this.panel.add(ticker);
		
		JButton search = ComponentUtil.createButton("Follow", 158, 5, 70, 40, 1, 15, false);
		dontScroll.add(search);
		search.addActionListener(e -> {
			String name = ticker.getText();
			if (followed.contains(name)) {
				FileUtil.removeFromFile(fileName, name);
				followed.remove(name);
				setMessage("Unfollowed " + name, Color.RED);
			} else if (Stock.exists(name)) {
				FileUtil.addToFile(fileName, new String[]{name});
				followed.add(name);
				setMessage("Followed: " + name, Color.GREEN);
			} else {
				setMessage(name + " doesn't exist", Color.RED);
			}
		});
		this.panel.add(search);

		new Thread(() -> {
			while(true) {
				if (this.toggled && Math.abs(lastUpdate - System.currentTimeMillis()) >= 250000) {
					render();
				}
				
				Utils.sleep(1000);
			}
		}).start();
	}
	
	public void render() {
		chartList.reset();

		List<String> tickers = new ArrayList<>(followed);
		for (String ticker : tickers) {
			Stock stock = Stock.get(ticker, 20);
			chartList.add(new ListObject(ticker, true, 57, Color.YELLOW));
			chartList.add(new ListObject((Utils.formatDecimal(stock.getDailyChange(), 2) + "%").replace("-", ""), 60, stock.getColor(stock.getPreviousClosePrice(), false)));
			chartList.add(new ListObject(stock.stock.getName(), 250));
			chartList.add(new ListObject(Utils.formatDecimal(stock.getPrice(), 3) + Utils.getCurrencySymbol(stock.stock.getCurrency()), 100, Color.GRAY));

			chartList.newLine();
			
			if (!firstRender) {
				chartList.repaint();
			}
		}
		
		chartList.repaint();
		firstRender = true;
		lastUpdate = System.currentTimeMillis();
	}
}
