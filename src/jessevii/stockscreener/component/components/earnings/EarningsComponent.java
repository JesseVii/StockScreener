package jessevii.stockscreener.component.components.earnings;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.settings.Setting;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.utils.ChartList;
import jessevii.stockscreener.utils.ChartList.ListObject;
import jessevii.stockscreener.utils.ComponentUtil;
import jessevii.stockscreener.utils.Location;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EarningsComponent extends Component {
	public static EarningsComponent instance;
	public static List<EarningsWhisper.Earning> earnings = new ArrayList<>();
	public List<ListObject> tickerListObjects = new ArrayList<>();
	public ListObject lastSearch;
	public Color lastSearchColor;
	public static Setting showOnlyPopular = new Setting(Setting.Mode.BOOLEAN, "Only popular", false);
	
	public EarningsComponent() {
		super("Earnings", true);
		instance = this;
	}

	@Override
	public void onInit(String... params) {
		ChartList chartList = new ChartList(this);
		
		JCheckBox showOnlyPopularCheckbox = ComponentUtil.createCheckBox(showOnlyPopular.name, 1, 1, 1, 1, 12);
		showOnlyPopularCheckbox.setSelected(showOnlyPopular.booleanValue());
		showOnlyPopularCheckbox.setBackground(this.panel.getBackground());
		onResize(() -> showOnlyPopularCheckbox.setBounds(this.panel.getWidth() - 100, 2, 100, 25));
		showOnlyPopularCheckbox.addActionListener(e -> {
			repaint(chartList);
			instance.lastYAdd = 0;
			instance.yAdd = 0;
			instance.updateXAndYAdd(null);
		});

		this.dontScroll.add(showOnlyPopularCheckbox);
		this.panel.add(showOnlyPopularCheckbox);
		showOnlyPopular.addCheckBox(showOnlyPopularCheckbox);
		
		JTextField ticker = ComponentUtil.createTextField("", 5, 5, 100, 30, 1, 14, false);
		dontScroll.add(ticker);
		ComponentUtil.setTextFieldOnlyUppercase(ticker);
		this.panel.add(ticker);
		
		JButton search = ComponentUtil.createButton("Search", 110, 5, 60, 30, 1, 14, false);
		dontScroll.add(search);
		search.addActionListener(e -> {
			for (ListObject object : tickerListObjects) {
				if (object.text.equals(ticker.getText())) {
					if (lastSearch != null) {
						lastSearch.color = lastSearchColor;
					}

					lastSearchColor = object.color;
					lastSearch = object;
					object.color = Color.CYAN;
					instance.lastYAdd = 0;
					instance.yAdd = -object.y + 100;
					instance.updateXAndYAdd(null);
					chartList.repaint();
				}
			}
		});
		this.panel.add(search);
		
		chartList.underline = true;
		chartList.borderSize = 2;
		chartList.height = 25;
		chartList.width = 110;
		chartList.heightSpace = 8;
		chartList.xAdd = 5;
		chartList.fontSize = 15;
		chartList.yAdd = 50;
		chartList.location = Location.LEFT;
		
		if (earnings.isEmpty()) {
			earnings = EarningsWhisper.getEarnings(14);
		}
		
		repaint(chartList);
	}
	
	public void repaint(ChartList chartList) {
		chartList.reset();

		for (EarningsWhisper.Earning earning : earnings) {
			if (showOnlyPopular.booleanValue() && !EarningsWhisper.anticipatedTickers.contains(earning.ticker)) {
				continue;
			}
			
			Color tickerColor = Color.GRAY;
			ListObject tickerObject = new ListObject(earning.ticker, true, 50, tickerColor);
			tickerListObjects.add(tickerObject);
			chartList.add(tickerObject);
			chartList.add(new ListObject(earning.name, 160, Color.ORANGE));
			chartList.add(new ListObject(earning.revenue, 65, Color.YELLOW));

			if (earning.growth != null) {
				try {
					chartList.add(new ListObject(earning.growth.replace("-", ""), 50, Stock.getColor(0, Double.parseDouble(earning.growth.replace("%", "")), true)));
				} catch (Exception e) {
					chartList.add(new ListObject("-", 50));
				}
				
				try {
					chartList.add(new ListObject(earning.surprise.replace("-", ""), 50, Stock.getColor(0, Double.parseDouble(earning.surprise.replace("%", "")), true)));
				} catch (Exception e) {
					chartList.add(new ListObject("-", 50));
				}
			} else {
				chartList.add(new ListObject(earning.eps, 40));
				chartList.add(new ListObject(earning.date + " " + earning.time, 250, Color.CYAN));
			}
			
			chartList.newLine();
		}
		
		chartList.repaint();
	}
}
