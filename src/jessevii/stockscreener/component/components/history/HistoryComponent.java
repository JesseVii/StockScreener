package jessevii.stockscreener.component.components.history;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.positions.Position;
import jessevii.stockscreener.main.MainPanel;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.objects.Time;
import jessevii.stockscreener.utils.ComponentUtil;
import jessevii.stockscreener.utils.RenderUtil;
import jessevii.stockscreener.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryComponent extends Component {
	public List<java.awt.Component> components = new ArrayList<>();
	public static HistoryComponent instance;

	public JPanel historyPanel;
	public List<History> tradeHistories = new ArrayList<>();
	public String gainString;
	public int daysHeld;
	
	public HistoryComponent() {
		super("History", true);
		instance = this;
		History.load();
	}
	
	@Override
	public void onInit(String... params) {
		historyPanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				
				RenderUtil.overridePanel = this;
				RenderUtil.drawString(g2d, gainString, -1, 35, 30);
				RenderUtil.drawString(g2d, "-:255, 255, 255:-Days held: " + RenderUtil.toStringRbg(Color.CYAN) + daysHeld, -1, 70, 30);
				
				for (int i = 0; i < tradeHistories.size(); i++) {
					History history = tradeHistories.get(i);
					String time = history.time.toString();
					if (history.id.contains("unrealized")) {
						time = "(UNREALIZED)";
					}
					
					RenderUtil.drawString(g2d, "-:45, 215, 224:-" + time + "-:255, 255, 255:- " + getTextForHistory(history), -1, 125 + (i * 15), 13);
				}
				
				RenderUtil.overridePanel = null;
			}
		};
		
		historyPanel.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		    	historyPanel.setVisible(false);
		    	panel.repaint();
		    }
		});
		
		onResize(() -> historyPanel.setBounds((this.panel.getWidth() / 2) - 200, (this.panel.getHeight() / 2) - 200, 400, 400));
		historyPanel.setBackground(MainPanel.componentBackgroundColor.colorValue());
		historyPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, MainPanel.borderColor.colorValue()));
		historyPanel.setVisible(false);
		this.dontScroll.add(historyPanel);
		this.panel.add(historyPanel);
		
		update();
	}
	
	public void update() {
		for (java.awt.Component component : components) {
			this.panel.remove(component);
		}
		components.clear();
		
		int index = 1;
		List<History> list = new ArrayList<>(History.list);
		Collections.reverse(list);
		for (History history : list) {
 			JLabel timeLabel = ComponentUtil.createLabel("", 5, index * 20, 500, 20, 13);
 			timeLabel.setText(history.time.year + "." + Utils.formatNumberTo0Start(history.time.month) + "." + Utils.formatNumberTo0Start(history.time.day) + "." + Utils.formatNumberTo0Start(history.time.hour) + "." + Utils.formatNumberTo0Start(history.time.minute) + ": ");
 			if (Position.hasPosition(history.ticker)) {
 				timeLabel.setForeground(Color.ORANGE);
 			}
 			
 			JLabel label = ComponentUtil.createLabel("", 130, index * 20, 500, 20, 13);
 			label.setForeground(Color.WHITE);
 			if (history.isVirtual) label.setForeground(new Color(31, 218, 224));
 			
 			String text = getTextForHistory(history);
			label.setText(label.getText() + text);
			
			label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                	tradeHistories.clear();
                	for (History history2 : History.list) {
                		if (history2.id.equals(history.id)) {
                			tradeHistories.add(history2);
                		}
                	}
                	
                	//Add unrealised gains for not sold shares in history
                	int sharesNotSold = 0;
                	for (History history2 : tradeHistories) {
                		if (history2.isBought) {
                			sharesNotSold += history2.shares;
                		} else {
                			sharesNotSold -= history2.shares;
                		}
                	}
                	
                	if (sharesNotSold > 0) {
                		History check = tradeHistories.get(0);
                		tradeHistories.add(new History(check.ticker, false, sharesNotSold, Stock.get(check.ticker, 0.1).getPrice(), check.isVirtual, check.isShort, check.id + "unrealized", Time.getTime()));
                	}
                	
                	int gain = History.calculateGain(tradeHistories);
                	double percentageGain = History.calculatePercentageGain(tradeHistories);
                	String color = RenderUtil.toStringRbg(Stock.getColor(percentageGain));
                	gainString = "-:255, 255, 255:-Gain: " + color + gain + "$ (" + Utils.formatDecimal(percentageGain, 3) + "%)";
                	daysHeld = (int)(tradeHistories.get(0).time.difference(tradeHistories.get(tradeHistories.size() - 1).time) / 86400000L);
                	
                	historyPanel.setVisible(true);
                	panel.repaint();
                }
            });
			
			components.add(timeLabel);
			components.add(label);
 			this.panel.add(timeLabel);
 			this.panel.add(label);
			
			index++;
		}
		
		for (java.awt.Component component : this.panel.getComponents()) {
			if (component.getName() == null || !component.getName().equals("overlayborder")) {
				component.setBounds(component.getX(), component.getY() - 13, component.getWidth(), component.getHeight());	
			}
		}	
	}
	
	public String getTextForHistory(History history) {
		String text = "";
		if (history.id.contains("unrealized")) {
			text += RenderUtil.toStringRbg(Color.YELLOW);
		}
		
		text += "Sold";
		if (history.isBought) text = "Bought";
		text += " " + history.shares + " ";
		if (history.isShort) text += "shorts";
		else text += "shares";
		text += " of " + history.ticker + " for " + history.price + "/share";

		return text;
	}
}
