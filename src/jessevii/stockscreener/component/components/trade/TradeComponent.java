package jessevii.stockscreener.component.components.trade;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.history.History;
import jessevii.stockscreener.component.components.history.HistoryComponent;
import jessevii.stockscreener.component.components.positions.Position;
import jessevii.stockscreener.component.components.trade.Trade.SellObject;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.objects.Time;
import jessevii.stockscreener.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TradeComponent extends Component {
	public String ticker;
	public ChartList chartList;
	public long id;
	public Stock stock;
	public JTextField shares, usd;
	public static TradeComponent instance;

	public TradeComponent() {
		super("Trade", false);
		instance = this;
	}
	
	@Override
	public void onInit(String... params) {
		chartList = new ChartList(this);
		chartList.borderSize = 0;
		chartList.height = 30;
		chartList.width = 500;
		chartList.heightSpace = 8;
		chartList.xAdd = 5;
		chartList.fontSize = 15;
		chartList.yAdd = 150;
		chartList.location = Location.CENTER;
		
		JTextField tickerTextField = ComponentUtil.createTextField("", 5, 5, 150, 40, 1, 20, false);
		ComponentUtil.setTextFieldOnlyUppercase(tickerTextField);
		this.panel.add(tickerTextField);
		
		JButton search = ComponentUtil.createButton("Search", 158, 5, 70, 40, 1, 15, false);
		search.addActionListener(e -> {
			ticker = tickerTextField.getText();
			long ms = System.currentTimeMillis();
			id = ms;

			if (Stock.exists(ticker)) {
				new Thread(() -> {
					while (ms == id) {
						if (instance.toggled) {
							stock = Stock.get(ticker, 2);
							instance.panel.repaint();
						}

						Utils.sleep(1500);
					}
				}).start();
			}
		});
		this.panel.add(search);
		
		JTextField manualDate = ComponentUtil.createTextField("Manual date yyyy-MM-dd-HH-mm-ss", 5, 5, 150, 40, 1, 20, false);
		onResize(() -> manualDate.setBounds((this.panel.getWidth() / 2) - (400 / 2), this.panel.getHeight() - 200, 400, 40));
		this.panel.add(manualDate);
		
		JTextField manualPrice = ComponentUtil.createTextField("Manual price", 5, 5, 150, 40, 1, 20, false);
		onResize(() -> manualPrice.setBounds((this.panel.getWidth() / 2) - (400 / 2), this.panel.getHeight() - 250, 400, 40));
		this.panel.add(manualPrice);
		
		JCheckBox virtualTradeBox = ComponentUtil.createCheckBox("Virtual Trade", 180, 490, 118, 18, 15);
		onResize(() -> virtualTradeBox.setBounds((this.panel.getWidth() / 2) - (118 / 2), this.panel.getHeight() - 133, 118, 15));
		this.panel.add(virtualTradeBox);
		
		JButton buy = ComponentUtil.createButton("Buy", 5, 5, 70, 40, 1, 35, false);
		buy.setForeground(Color.GREEN);
		onResize(() -> buy.setBounds(5, this.panel.getHeight() - 58, (this.panel.getWidth() / 2) - 10, 50));
		buy.addActionListener(e -> {
			try {
				int amount = Integer.parseInt(shares.getText());
				Position position = Trade.buy(amount, stock.ticker, manualPrice.getText().contains("Manual") ? stock.getPrice() : Double.parseDouble(manualPrice.getText()), virtualTradeBox.isSelected());
				position.add();
				History history = new History(position, true);
				Position newPosition = Position.get(position.ticker);
				history.id = newPosition.id;

				if (!manualDate.getText().contains("Manual")) {
					history.time = Time.parseTime(manualDate.getText());
				}
				history.add();

				HistoryComponent.instance.update();

				if (position.isVirtual) {
					setMessage("Virtually bought " + position.shares + " shares of " + stock.ticker, Color.GREEN);
				} else {
					setMessage("Bought " + position.shares + " shares of " + stock.ticker, Color.GREEN);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				setMessage("Error buying stock", Color.RED);
			}
		});
		this.panel.add(buy);
		
		JButton sell = ComponentUtil.createButton("Sell", 5, 5, 70, 40, 1, 35, false);
		sell.setForeground(Color.RED);
		onResize(() -> sell.setBounds(buy.getWidth() + 10, this.panel.getHeight() - 58, (this.panel.getWidth() / 2) - 10, 50));
		sell.addActionListener(e -> {
			try {
				int amount = Integer.parseInt(shares.getText());
				Position position = Position.get(stock.ticker);
				if (amount > position.shares) {
					setMessage("You don't own this many shares", Color.RED);
					return;
				}

				SellObject sellObject = Trade.sell(amount, manualPrice.getText().contains("Manual") ? stock.getPrice() : Double.parseDouble(manualPrice.getText()));
				position.remove(sellObject.shares);
				new History(stock.ticker, false, sellObject.shares, sellObject.price, position.isVirtual, position.isShort(), position.id, manualDate.getText().contains("Manual") ? Time.getTime() : Time.parseTime(manualDate.getText())).add();
				HistoryComponent.instance.update();
				setMessage("Sold " + sellObject.shares + " shares of " + stock.ticker, Color.GREEN);
			} catch (Exception e2) {
				setMessage("Error selling stock", Color.RED);
			}
		});
		this.panel.add(sell);
		
		shares = ComponentUtil.createTextField("Shares", -1, -1, -1, -1, 1, 35, true);
		shares.setForeground(Color.WHITE);
		onResize(() -> shares.setBounds(5, this.panel.getHeight() - 111, (this.panel.getWidth() / 2) - 10, 50));
		shares.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (!Utils.isInteger(shares.getText())) {
					shares.setText("");
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (stock != null) {
					if (Utils.isInteger(shares.getText())) {
						usd.setText("" + ((int)((Integer.parseInt(shares.getText())) * stock.getPrice())));	
					} else {
						usd.setText("0");
					}
				}
			}
			
			public void keyTyped(KeyEvent e) {}
		});
		this.panel.add(shares);
		
		usd = ComponentUtil.createTextField("USD", -1, -1, -1, -1, 1, 35, true);
		usd.setForeground(Color.WHITE);
		onResize(() -> usd.setBounds(shares.getWidth() + 10, this.panel.getHeight() - 111, (this.panel.getWidth() / 2) - 10, 50));
		usd.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (!Utils.isInteger(usd.getText())) {
					usd.setText("");
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (stock != null) {
					if (Utils.isInteger(usd.getText())) {
						shares.setText("" + (int)((Integer.parseInt(usd.getText()) / stock.getPrice())));
					} else {
						shares.setText("0");
					}
				}
			}
			
			public void keyTyped(KeyEvent e) {}
		});
		this.panel.add(usd);
	}

	@Override
	public void onPaint(Graphics2D g) {
		if (stock != null) {
			Position position = Position.get(ticker);
			RenderUtil.drawString(g, RenderUtil.toStringRbg(Color.WHITE) + stock.stock.getName() + RenderUtil.toStringRbg(Color.GRAY) + " (" + Utils.formatDecimal(stock.getPrice(), 2) + Utils.getCurrencySymbol(stock.stock.getCurrency()) + ")", -1, 100, 20);
			RenderUtil.drawString(g, RenderUtil.toStringRbg(Color.WHITE) + "Todays change: " + RenderUtil.toStringRbg(stock.getColor(stock.getPreviousClosePrice(), false)) + Utils.formatDecimal(stock.getDailyChange(), 3) + "%", -1, 125, 20);
			RenderUtil.drawString(g, RenderUtil.toStringRbg(Color.white) + "Owned shares: " + RenderUtil.toStringRbg(Color.YELLOW) + (position == null ? 0 : position.shares), -1, 175, 20);
			if (position != null) {
				RenderUtil.drawString(g, RenderUtil.toStringRbg(Color.white) + "Gain: " + RenderUtil.toStringRbg(stock.getColor(position.price, position.isShort())) + Utils.formatDecimal(stock.getChange(position.price), 3) + "%", -1, 200, 20);
			}
		}
	}
}
