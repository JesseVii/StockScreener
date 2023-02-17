package jessevii.stockscreener.component.components.chart;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.chart.HistoricalData.StockUnit;
import jessevii.stockscreener.component.components.settings.Setting;
import jessevii.stockscreener.main.MainPanel;
import jessevii.stockscreener.objects.Stock;
import jessevii.stockscreener.objects.Stock.StockListener;
import jessevii.stockscreener.objects.Time;
import jessevii.stockscreener.utils.ComponentUtil;
import jessevii.stockscreener.utils.RenderUtil;
import jessevii.stockscreener.utils.Utils;
import yahoofinance.quotes.stock.StockStats;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChartComponent extends Component {
	public static ChartComponent instance;

	public boolean lostFocus;
	public jessevii.stockscreener.utils.Utils.MotionListenerJPanel chartPanel;
	public Stock stock;
	
	public String timeFrame;
	public int chartSize = 10;
	public int dragStartX, dragStartY;
	public int scrolled;
	public double xAdd, yAdd;
	public int mouseX, mouseY;
	public double averageChange, lastFirstPrice;
	public boolean dragging;
	public List<StockUnit> stockUnits;
	public List<Candle> candles = new ArrayList<>();
	public List<Point> drawLinePoints = new ArrayList<>();
	public Candle selectedCandle;
	public Candle dragStart, dragStop;
	public Thread stockUpdateThread;
	
	public JButton searchButton;
	public JTextField tickerField;
	
	public static Setting chartSetting = new Setting(Setting.Mode.BOOLEAN, "Chart", true);
	public static Setting candlesSetting = new Setting(Setting.Mode.BOOLEAN, "Candles", true);
	public static Setting drawLines = new Setting(Setting.Mode.BOOLEAN, "DrawLines", false);
	public static Setting drawLinesWidth = new Setting(Setting.Mode.INTEGER, "DrawLines Width", 2);
	public static Setting movingAverage5Setting = new Setting(Setting.Mode.BOOLEAN, "5-MA", false);
	public static Setting movingAverage25Setting = new Setting(Setting.Mode.BOOLEAN, "25-MA", false);
	public static Setting movingAverage50Setting = new Setting(Setting.Mode.BOOLEAN, "50-MA", false);
	public static Setting movingAverage150Setting = new Setting(Setting.Mode.BOOLEAN, "150-MA", false);
	public static Setting minDayChangeSetting = new Setting(Setting.Mode.BOOLEAN, "Min-Day%", true);
	public static Setting postPreSetting = new Setting(Setting.Mode.BOOLEAN, "Post&Pre", false);
	public static Setting showEarnings = new Setting(Setting.Mode.BOOLEAN, "Earnings", true);
	public static Setting showDividends = new Setting(Setting.Mode.BOOLEAN, "Dividends", true);
	public static Setting showStacked = new Setting(Setting.Mode.BOOLEAN, "Show Stacked", false);
	public static Setting chartBackgroundColor = new Setting(Setting.Mode.COLOR, "Chart Background Color", new Color(14, 14, 14));
	public static Setting cursorColor = new Setting(Setting.Mode.COLOR, "Cursor Color", new Color(0, 255, 255, 65));
	public static Setting cursorDragColor = new Setting(Setting.Mode.COLOR, "Cursor Drag Color", new Color(0, 255, 179, 100));
	public static Setting cursorSize = new Setting(Setting.Mode.INTEGER, "Cursor Size", 2);
	public static Setting movingAverageAlpha = new Setting(Setting.Mode.INTEGER, "Moving Avg alpha", 150);
	
	public ChartComponent() {
		super("Chart", false);
		instance = this;
	}
	
	@Override
	public void onInit(String... params) {
		//Ticker text-field
		JTextField ticker = ComponentUtil.createTextField("", 5, 5, 90, 40, 1, 20, false);
		tickerField = ticker;
		ComponentUtil.setTextFieldOnlyUppercase(ticker);
		onResize(() -> ticker.setBounds(this.panel.getWidth() - 169, ticker.getY(), ticker.getWidth(), ticker.getHeight()));
		
		ticker.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				lostFocus = true;
			}
			
			public void focusGained(FocusEvent e) {}
		});
		
		ticker.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (lostFocus && KeyEvent.getKeyText(e.getKeyCode()).length() == 1) {
					lostFocus = false;
					ticker.setText("");
				}
			}

			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		this.panel.add(ticker);
		
		//Chart panel
		chartPanel = new jessevii.stockscreener.utils.Utils.MotionListenerJPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				onPaintChart((Graphics2D)g);
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				
				boolean isMouseOverChart = false;
				for (Candle candle : candles) {
					if (candle.x < e.getX() && candle.x + candle.width > e.getX() || candle.x == e.getX()) {
						isMouseOverChart = true;
						if (selectedCandle == null && candle.stockUnit != null || candle.stockUnit != null && !selectedCandle.stockUnit.equals(candle.stockUnit)) {
							selectedCandle = candle;
						}
						
						break;
					}
				}
				
				if (!isMouseOverChart) {
					for (Candle candle : candles) {
						if (candle.x < e.getX() + chartSize / 2 && candle.x + candle.width > e.getX() + chartSize / 2) {
							if (candle.stockUnit != null) {
								selectedCandle = candle;
								return;
							}
						}
					}
					
					selectedCandle = null;
				}

				panel.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				
				if (drawLines.booleanValue()) {
					drawLinePoints.add(new Point(mouseX - (int)xAdd, mouseY - (int)yAdd));
					chartPanel.repaint();
					return;
				}
				
				if (dragStart != null) {
					mouseMoved(e);
					if (selectedCandle != null) {
						dragStop = selectedCandle;
						chartPanel.repaint();
					}
					
					selectedCandle = dragStop;
				}

				chartPanel.repaint();
			}
		};
		
		onResize(() -> chartPanel.setBounds(0, 165, this.panel.getWidth(), this.panel.getHeight() - 165));
		chartBackgroundColor.addListener(new Setting.SettingChangedListener() {
			public void valueChanged() {
				chartPanel.setBackground(chartBackgroundColor.colorValue());	
			}
		});
		
		chartPanel.setBorder(BorderFactory.createMatteBorder(MainPanel.borderSize.intValue(), 0, MainPanel.borderSize.intValue(), 0, MainPanel.borderColor.colorValue()));
		chartPanel.addMouseMotionListener(chartPanel);
		chartPanel.setLayout(null);
		
		//Scrolls the chart smaller and bigger
		chartPanel.addMouseWheelListener(e -> {
			//Down
			if (e.getWheelRotation() == -1) {
				//Change volatility for rendering
				if (e.isControlDown()) {
					averageChange += averageChange / 15;
				}

				//Change chart size
				else if (chartSize < 40) {
					chartSize++;
					scrolled = 1;
				}
			}

			//Up
			else {
				//Change volatility for rendering
				if (e.isControlDown()) {
					averageChange -= averageChange / 15;
				}

				//Change chart size
				else if (chartSize > 1) {
					chartSize--;
					scrolled = 2;
				}
			}

			chartPanel.repaint();
		});
		
		//Allows to drag to move the chart
		chartPanel.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent e) {
				if (e.getButton() == 3) {
					dragStart = selectedCandle;
					dragStop = dragStart;
					return;
				}
				
				if (!drawLines.booleanValue()) {
					dragStartX = e.getX();
					dragStartY = e.getY();
					dragging = true;
				}
            }
			
			@Override
            public void mouseReleased(MouseEvent e) {
				if (e.getButton() == 3) {
					dragStart = null;
					dragStop = null;
					chartPanel.repaint();
					return;
				}
				
				if (!drawLines.booleanValue()) {
					xAdd += e.getX() - dragStartX;
					yAdd += e.getY() - dragStartY;
				}
				
				dragging = false;
				if (dragStart != null) dragStart.x -= dragStartX - mouseX;
				chartPanel.repaint();
            }
        });

		this.panel.add(chartPanel);
		
		//Add setting checkboxes
		Object[][] settingCheckboxes = {{chartSetting, 1}, {candlesSetting, 1}, {movingAverage5Setting, 4}, {movingAverage25Setting, 5},
				{movingAverage50Setting, 5}, {movingAverage150Setting, 5}, {minDayChangeSetting, 5}, {postPreSetting, 2}, {drawLines, 3},
				{showEarnings, 0}, {showDividends, 4}, {showStacked, 8}};

		int index2 = 1;
		for (Object[] object : settingCheckboxes) {
			Setting setting = (Setting)object[0];

			String text = setting.name;
			int textWidth = ComponentUtil.getStringWidth(text, chartPanel.getGraphics());
			JCheckBox candlesCheckbox = ComponentUtil.createCheckBox(text, chartPanel.getWidth() - textWidth - 30, (chartPanel.getHeight() - index2 * 18) - 10, textWidth + 25, 20, 13);
			final int index2Final = index2;
			onResize(() -> candlesCheckbox.setBounds(chartPanel.getWidth() - textWidth - 30 - (int)object[1], (chartPanel.getHeight() - index2Final * 18) - 10, textWidth + 50, 20));
			candlesCheckbox.setBackground(chartPanel.getBackground());
			setting.addCheckBox(candlesCheckbox);
			candlesCheckbox.setSelected(setting.booleanValue());

			candlesCheckbox.addActionListener(e -> chartPanel.repaint());
			chartPanel.add(candlesCheckbox);
			index2++;
		}
		
		//Search button
		JButton search = ComponentUtil.createButton("Search", 100, 5, 70, 40, 1, 15, false);
		searchButton = search;
		search.addActionListener(e -> {
			if (stockUpdateThread != null) stockUpdateThread.suspend();
			drawLinePoints.clear();
			xAdd = 0;
			yAdd = 0;
			lastFirstPrice = 0;
			stock = Stock.get(ticker.getText(), 2);
			stockUnits = null;
			averageChange = -1;

			if (stock != null) {
				panel.repaint();
				stock.addListener(new StockListener() {
					@Override
					public void priceChanged() {
						if (stockUnits != null && !stockUnits.isEmpty() && timeFrame.equals("1m")) {
							Time time = Time.getTime();
							StockUnit unit = new StockUnit(time, stockUnits.get(0).close, stock.getPrice(), stock.high, stock.low, stock.quote.getVolume());

							if (stockUnits.get(0).time.equals(time)) {
								stockUnits.remove(0);
							} else {
								stock.resetHighAndLow();
							}

							stockUnits.add(0, unit);
						}

						panel.repaint();
					}
				});

				//Update price
				stockUpdateThread = new Thread(() -> {
					String old = ticker.getText();
					while(ticker.getText() != null && ticker.getText().equals(old)) {
						if (toggled && stock != null) {
							stock.update();
						}

						Utils.sleep(200);
					}
				});
				stockUpdateThread.start();
			}
		});
		onResize(() -> search.setBounds(this.panel.getWidth() - 75, search.getY(), search.getWidth(), search.getHeight()));
		this.panel.add(search);
		
		//Candle time selection buttons
		String[] timeButtons = {"1wk", "1d", "1h", "30m", "15m", "5m", "2m", "1m"};
		int index = 1;
		int y = 0;
		for (String name : timeButtons) {
			JButton button = ComponentUtil.createButton(name, 0, 0, 60, 30, 1, 15, true);
			button.addActionListener(e -> {
				timeFrame = name;
				if (!ticker.getText().isEmpty()) {
					search.doClick();
				}
			});

			if (name.equals("1m")) {
				button.doClick();
			}
			
			final int currentIndex = index;
			final int currentY = y;
			onResize(() -> {
				int y2 = 133 - 32 * currentY;
				int currentIndex2 = currentIndex;
				if (this.panel.getWidth() > 1000) {
					y2 = 133;
					if (currentY > 0) {
						currentIndex2 += 4;
					}
				}
				
				button.setBounds(this.panel.getWidth() - button.getWidth() * currentIndex2 - 3 * currentIndex2, y2, button.getWidth(), button.getHeight());
			});
			this.panel.add(button);
			
			if (button.getX() < this.panel.getWidth() / 2) {
				y = 1;
				index = 0;
			}
			index++;
		}
	}
	
	@Override
	public void onEnabled(String... params) {
		if (params.length > 0) {
			tickerField.setText(params[0]);
			searchButton.doClick();
		}
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		if (stock == null || stock.quote == null) {
			return;
		}

		stock.dontUpdate = true;

		//Price
		Color lastChange = Color.GRAY;
		if (stock.lastQuote != null) {
			lastChange = Stock.getColor(stock.lastQuote.getPrice().doubleValue(), stock.getPrice(), false);
		}
		RenderUtil.drawString(g, RenderUtil.toStringRbg(lastChange) + stock.getPrice(), 5, 128, 25);
		
		//Daily change
		Color changeColor = stock.getColor(stock.getPreviousClosePrice(), false);
		String change = (RenderUtil.toStringRbg(changeColor) + ("" + Utils.formatDecimal(stock.getDailyChange(), 3)).replace("-", "")) + "%";
		RenderUtil.drawString(g, change, 5, 155, 25);
		
		//Draw some info about the stock
		int x = 180;
		int y = 16;
		StockStats stats = stock.stock.getStats();
		g.setColor(MainPanel.borderColor.colorValue());
		g.fillRect(x - 7, 0, MainPanel.borderSize.intValue(), 500);
		int drawn = 0;
		for (int i = 0; i < 15; i++) {
			String text = null;
			if (i == 0) text = stock.stock.getName() + " (" + stock.stock.getSymbol() + ")";
			else if (i == 1 && stats.getPe() != null) text = "P/E: " + Utils.formatDecimal(stats.getPe().doubleValue(), 3);
			else if (i == 2 && stats.getMarketCap() != null) text = "MarketCap: " + Utils.formatToMBT(stats.getMarketCap().longValue());
			else if (i == 3 && stats.getEps() != null) text = "EPS: " + stats.getEps();
			else if (i == 4 && stock.stock.getDividend() != null && stock.stock.getDividend().getAnnualYieldPercent() != null) text = "Dividend yield: " + Utils.formatDecimal(stock.stock.getDividend().getAnnualYieldPercent().doubleValue(), 3) + "%";
			else if (i == 6) text = "Vol: " + Utils.formatToMBT(stock.quote.getVolume());
			else if (i == 7) text = "Avg-Vol: " + Utils.formatToMBT(stock.quote.getAvgVolume());
			else if (i == 8) text = "Market: " + stock.stock.getStockExchange();

			if (text != null) {
				drawn++;
				RenderUtil.drawString(g, RenderUtil.addColors(text), x, y * drawn, 15);
			}
		}

		//Draw info about selectedCandle
		if (selectedCandle != null) {
			x += 180;
			g.setColor(this.panel.getBackground());
			g.fillRect(x - 7, 0, 1000, 100);
			g.setColor(MainPanel.borderColor.colorValue());
			g.fillRect(x - 7, 0, MainPanel.borderSize.intValue(), 500);
			StockUnit u = selectedCandle.stockUnit;
			RenderUtil.drawString(g, u.time.toString(), x, y, 15);
			RenderUtil.drawString(g, RenderUtil.addColors("Open: " + Utils.formatDecimal(u.open, 3)), x, y * 2, 15);
			RenderUtil.drawString(g, RenderUtil.addColors("Close: " + Utils.formatDecimal(u.close, 3)), x, y * 3, 15);
			RenderUtil.drawString(g, RenderUtil.addColors("High: " + Utils.formatDecimal(u.high, 3)), x, y * 4, 15);
			RenderUtil.drawString(g, RenderUtil.addColors("Low: " + Utils.formatDecimal(u.low, 3)), x, y * 5, 15);
			RenderUtil.drawString(g, RenderUtil.addColors("Change: " + Utils.formatDecimal(Stock.getChange(u.open, u.close), 3) + "%"), x, y * 6, 15);
			
			//Draw drag change
			if (dragStart != null && dragStop != null) {				
				x = 465 + 25 * 3;
				g.setColor(MainPanel.borderColor.colorValue());
				g.fillRect(x - 7, 0, MainPanel.borderSize.intValue(), 500);
				RenderUtil.drawString(g, dragStart.stockUnit.time.toString(), x, y, 15);
				RenderUtil.drawString(g, dragStop.stockUnit.time.toString(), x, y * 2, 15);
				RenderUtil.drawString(g, Utils.formatDecimal(dragStart.stockUnit.close, 3) + " > " + Utils.formatDecimal(dragStop.stockUnit.close, 3), x, y * 3, 15);
				RenderUtil.drawString(g, RenderUtil.toStringRbg(Stock.getColor(dragStart.stockUnit.close, dragStop.stockUnit.close, false)) + Utils.formatDecimal(Stock.getChange(dragStart.stockUnit.close, dragStop.stockUnit.close), 3) + "%", x, y * 4, 15);
			}
		}
		
		stock.dontUpdate = false;
	}
	
	public void onPaintChart(Graphics2D g) {
		if (stock == null) {
			return;
		}
		
		if (stockUnits == null) {
			stockUnits = HistoricalData.getData(stock.ticker, timeFrame);
			try {
				HistoricalData.Dividend dividend = new HistoricalData.Dividend(0, new Time(stock.stock.getDividend().getPayDate()));
				dividend.inFuture = true;
				if (dividend.time.getMs() >= stockUnits.get(stockUnits.size() - 1).time.getMs()) {
					stockUnits.get(0).stockInfo.dividends.add(dividend);
					stockUnits.get(0).stockInfo.renderInfo.add(dividend);
				}
			} catch (Exception ignored) {}

			Collections.reverse(stockUnits);
			
			if (averageChange == -1) {
				int amount = 0;
				List<StockUnit> list = new ArrayList<>(stockUnits);
				
				for (StockUnit unit : list) {
					amount++;
					averageChange += Math.abs(Stock.getChange(unit.open, unit.close));
				}
				
				averageChange /= amount;
			}
		}

		List<StockUnit> list = new ArrayList<>(stockUnits);
		candles.clear();
		
		StockUnit lastUnit = null, lastOpenUnit = null;
		int lastOpenX = -1;
		double multiply = 3 / averageChange;
		if (lastFirstPrice != 0) yAdd -= Stock.getChange(lastFirstPrice, list.get(0).close) * (multiply * chartSize);
		int x = ((chartPanel.getWidth() / 2) - 15);
		double y = (chartPanel.getHeight() / 2);

		if (scrolled == 1) {
			xAdd += xAdd / (double)chartSize;
		} else if (scrolled == 2) {
			xAdd -= xAdd / (double)(chartSize + 1);
		}
		scrolled = 0;
		
		x += xAdd;
		y += yAdd;
		int index = 0;
		int lastX = -1;
		double lastY = -1;
		double firstPrice = -1;
		if (dragging) {
			x += mouseX - dragStartX;
			y += mouseY - dragStartY;
		}
		double startY = y;
		double lastChangeFromFirst = 0;
		
		StockUnit cursorUnit = null;
		double cursorUnitChange = 0;
		int cursorUnitX = 0;
		double cursorUnitY = 0;
		
		for (StockUnit unit : list) {
			//Set this ones open to last ones open because if there's gaps in the data the chart will mess up
			try {
				unit.open = list.get(index + 1).close;
			} catch (Exception ignored) {
				
			}
			
			index++;
			if (index == 1) {
				firstPrice = unit.close;
				lastFirstPrice = unit.close;
			}
			
			if (unit.open == unit.close) {
				continue;
			}
			
			double changeFromFirst = Stock.getChange(firstPrice, unit.open) * (multiply * chartSize);
			double change = changeFromFirst - lastChangeFromFirst;
			lastChangeFromFirst = changeFromFirst;
			
			//Update coordinates
			x -= chartSize + (chartSize / 4);
			y = startY - changeFromFirst;
			if (x > this.panel.getWidth() + 300) {
				lastY = y;
				lastX = x;
				continue;
			}
			
			//Render drag thing so u can see how much it moved when dragging mouse
			if (dragStart != null && dragStop != null) {
				int dragX = dragStart.x;
				if (dragging) dragX -= this.dragStartX - mouseX;
				if (x <= dragStop.x && x >= dragX || x >= dragStop.x && x <= dragX) {
					g.setColor(cursorDragColor.colorValue());
					RenderUtil.fill(x, 0, chartSize + (chartSize / 4), 3000, g);
				}
			}
			
			//Render close and open markers and info about it if its 1-minute chart
			if (lastUnit != null && (timeFrame.equals("1m") || timeFrame.equals("2m")) && minDayChangeSetting.booleanValue()) {
				if (unit.time.day != lastUnit.time.day || index == list.size()) {
					if (lastOpenX != -1) {
						double dayChange = Utils.formatDecimal(Stock.getChange(unit.open, lastOpenUnit.open), 3);
						String white = "-:255, 255, 255:-";
						String changeColor = RenderUtil.toStringRbg(Stock.getColor(unit.open, lastOpenUnit.open, false));
						String text = white + lastUnit.time.toStringDayMonthYear() + " (" + changeColor + dayChange + "%" + white + ")";

						int renderX = x + ((lastOpenX - x) / 2) - ComponentUtil.getStringWidth(text.replace(white, "").replace(changeColor, ""), g) / 2;
						if (renderX == -1) renderX = 0;
						RenderUtil.drawString(g, text, renderX, 20, 15);
					}
					
					g.setColor(new Color(186, 135, 26, 150));
					if (index == list.size()) {
						lastOpenX = x;
					} else {
						lastOpenX = x + chartSize + (chartSize / 4);
					}
					RenderUtil.fill(x, 0, chartSize, 2000, g);
					lastOpenUnit = lastUnit;
				}
			}
			
			if (chartSize > 2 && candlesSetting.booleanValue()) {
				g.setColor(new Color(255, 255, 255, 95));

				//Draw high
				if (unit.high > unit.close) {
					if (change < 0) {
						double height = getHeight(unit.close - unit.open, Math.abs(change), unit.high - unit.close);
						RenderUtil.fill(x + (chartSize / 2), (y + change) - height, 1, height, g);
					} else {
						double height = getHeight(unit.open - unit.close, Math.abs(change), unit.high - unit.open);
						RenderUtil.fill(x + (chartSize / 2), y - height, 1, height, g);
					}
				}
				
				//Draw low
				if (unit.low < unit.close) {
					if (change < 0) {
						double height = getHeight(unit.close - unit.open, Math.abs(change), unit.low - unit.open);
						RenderUtil.fill(x + (chartSize / 2), y, 1, -height, g);
					} else {
						double height = getHeight(unit.open - unit.close, Math.abs(change), unit.low - unit.close);
						RenderUtil.fill(x + (chartSize / 2), y + change, 1, -height, g);
					}
				}
			}
			
			//Draw candle
			if (candlesSetting.booleanValue() && chartSetting.booleanValue()) {
				if (change < 0) {
					g.setColor(Color.GREEN);
				} else {
					g.setColor(Color.RED);
				}

				double height = Math.abs(change);
				if (change > 0) {
					RenderUtil.fill(x, y, chartSize, height, g);
				} else {
					RenderUtil.fill(x, y - height, chartSize, height, g);
				}
			}
			
			if (lastX != -1) {
				//Draw lines instead of candles if the setting is off
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (!candlesSetting.booleanValue() && chartSetting.booleanValue()) {
					g.setColor(Color.YELLOW);
					g.drawLine(x, (int)y, lastX, (int)lastY);
				}
			}

			//Add candle
			candles.add(new Candle(unit, x, (int)y, chartSize, -(int)(change)));
			
			//Set cursor unit
			if (selectedCandle != null && unit.time.equals(selectedCandle.stockUnit.time)) {
				cursorUnit = unit;
				cursorUnitChange = change;
				cursorUnitX = x;
				cursorUnitY = y;
			}
			
			if (x < -300) {
				break;
			}
			
			lastUnit = unit;
			lastX = x;
			lastY = y;
		}

		//Draws the cursor
		if (cursorUnit != null) {
			int size = cursorSize.intValue();
			g.setColor(cursorColor.colorValue());
			if (candlesSetting.booleanValue()) {
				RenderUtil.fill(cursorUnitX + chartSize / 2, 0, size, 2000, g);
			} else {
				RenderUtil.fill(cursorUnitX, 0, size, 2000, g);
			}
			
			if (candlesSetting.booleanValue()) {
				if (cursorUnit.open > cursorUnit.close) {
					RenderUtil.fill(0, cursorUnitY, 2000, size, g);
				} else {
					RenderUtil.fill(0, cursorUnitY + cursorUnitChange, 2000, size, g);
				} 
			} else {
				RenderUtil.fill(0, cursorUnitY, 2000, size, g);
			}
			
			selectedCandle.stockUnit = cursorUnit;
			selectedCandle.x = cursorUnitX;
			selectedCandle.y = (int)cursorUnitY;
		}
		
		//Calculate and draw moving average
		for (int i2 = 0; i2 < 4; i2++) {
			Color color = Color.WHITE;
			int amount = 5;
			if (i2 == 1) {
				color = Color.MAGENTA;
				amount = 25;
			}
			if (i2 == 2) {
				amount = 50;
				color = Color.CYAN;
			}
			if (i2 == 3) {
				amount = 150;
				color = Color.PINK;
			}
			
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), movingAverageAlpha.intValue());
			if (amount == 5 && !movingAverage5Setting.booleanValue() || amount == 25 && !movingAverage25Setting.booleanValue() ||
				amount == 50 && !movingAverage50Setting.booleanValue() || amount == 150 && !movingAverage150Setting.booleanValue()) {
				continue;
			}
			
			ArrayList<Double> lastCloses = new ArrayList<>();
			x = ((chartPanel.getWidth() / 2) - 15);
			x += xAdd;
			boolean first = true;
			if (dragging) {
				x += mouseX - dragStartX;
			}
			
			for (StockUnit unit : list) {
				if (unit.open == unit.close) {
					continue;
				}
				
				lastCloses.add(0, unit.close);
				
				if (x < 0) {
					break;
				} else if (x > this.panel.getWidth() && lastCloses.size() >= amount) {
					x -= chartSize + (chartSize / 4);
					lastX = x;
					continue;
				}
				
				if (lastCloses.size() >= amount) {
					double average = 0;
					for (int i = 0; i < amount; i++) {
						average += lastCloses.get(i);
					}
					average /= amount;
					
					double changeFromFirst = Stock.getChange(firstPrice, average) * (multiply * chartSize);
					double y2 = startY - changeFromFirst;
					x -= chartSize + (chartSize / 4);
					if (first) {
						lastY = y2;
						lastX = x;
						first = false;
					}
					
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setColor(color);
					g.drawLine(x, (int)y2, lastX, (int)lastY);

					lastY = y2;
					lastX = x;
				}
			}
		}
		
		//Draw drawn lines
		Point lastPoint = null;
		for (Point point : drawLinePoints) {
			if (lastPoint != null && Math.abs(point.x - lastPoint.x) + Math.abs(point.y - lastPoint.y) < 25) {
				int dragAddX = 0;
				int dragAddY = 0;
				if (dragging) {
					dragAddX += mouseX - dragStartX;
					dragAddY += mouseY - dragStartY;
				}
				
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(drawLinesWidth.intValue()));
				g.drawLine(point.x + (int)xAdd + dragAddX, point.y + (int)yAdd + dragAddY, lastPoint.x + (int)xAdd + dragAddX, lastPoint.y + (int)yAdd + dragAddY);
			}
			
			lastPoint = point;
		}
		
		//Draw drag change in left bottom corner also
		if (dragStart != null && dragStop != null) {
			RenderUtil.drawString(g, RenderUtil.toStringRbg(Stock.getColor(dragStart.stockUnit.close, dragStop.stockUnit.close, false)) + Utils.formatDecimal(Stock.getChange(dragStart.stockUnit.close, dragStop.stockUnit.close), 3) + "%", 3, chartPanel.getHeight() - 8, 15);
		}

		//Add invisible candles for drawing future renderables
		int candleX = ((chartPanel.getWidth() / 2) - 15) + (int)xAdd;
		int candleY = (chartPanel.getHeight() / 2) + (int)yAdd;
		Date date = stockUnits.get(0).time.getDate();
		long candleTimeDifference = stockUnits.get(0).time.difference(stockUnits.get(1).time);
		if (dragging) {
			candleX += mouseX - dragStartX;
			candleY += mouseY - dragStartY;
		}
		for (int i = 0; i < 1500; i++) {
			candleX += chartSize + (chartSize / 4);

			Candle candle = new Candle(null, candleX, candleY, chartSize, 0);
			candle.overwriteTime = new Time(date);
			candles.add(candle);

			date.setTime(date.getTime() + candleTimeDifference);
		}

		//Draw dividends, splits, earnings and other stuff at the bottom
		if (stockUnits.get(0).stockInfo != null) {
			boolean renderedInfo = false;
			List<Integer> renderedXs = new ArrayList<>();
			for (HistoricalData.RenderInfo renderInfo : stockUnits.get(0).stockInfo.renderInfo) {
				Candle best = null;
				long lowestDiff = Long.MAX_VALUE;
				for (Candle candle : candles) {
					long diff = Math.abs(candle.getTime().getMs() - renderInfo.renderInfoTime.getMs());

					if (diff < lowestDiff) {
						lowestDiff = diff;
						best = candle;
					}
				}

				int size = 20;
				int renderInfoY = chartPanel.getHeight() - 10;
				if (showStacked.booleanValue()) {
					for (int renderedX : renderedXs) {
						if (Math.abs(best.x - renderedX) <= 10) {
							renderInfoY -= 23;
						}
					}
				} else if (renderedXs.contains(best.x)) {
					renderInfoY -= 23;
				}

				//Render the symbol
				if (renderInfo instanceof HistoricalData.Dividend) {
					if (!showDividends.booleanValue()) {
						continue;
					}

					RenderUtil.drawString(g, ((HistoricalData.Dividend)renderInfo).inFuture ? "-:235, 80, 211:-D" : "-:45, 156, 78:-D", best.x == -1 ? -2 : best.x, renderInfoY, size, false);
				} else if (renderInfo instanceof HistoricalData.Split) {
					RenderUtil.drawString(g, "-:47, 189, 194:-S", best.x == -1 ? -2 : best.x, renderInfoY, size, false);
				} else if (renderInfo instanceof HistoricalData.Earning) {
					if (!showEarnings.booleanValue()) {
						continue;
					}

					HistoricalData.Earning earning = (HistoricalData.Earning)renderInfo;
					String color = RenderUtil.toStringRbg(Color.GREEN);
					if (earning.estimatedEPS == HistoricalData.Earning.nullValue || earning.reportedEPS == earning.estimatedEPS) {
						color = RenderUtil.toStringRbg(Color.GRAY);
					} else if (earning.estimatedEPS > earning.reportedEPS) {
						color = RenderUtil.toStringRbg(Color.RED);
					}

					RenderUtil.drawString(g, color + "E", best.x == -1 ? -2 : best.x, renderInfoY, size, false);
				}
				renderedXs.add(best.x);

				//Draw info about it if hovering over with mouse
				if (best.x + 5 - 13 < mouseX && best.x + 5 + 13 > mouseX && renderInfoY - 23 < mouseY && renderInfoY + 13 > mouseY && !renderedInfo) {
					if (selectedCandle == null || selectedCandle.x != best.x) {
						g.setColor(new Color(211, 101, 234, 102));
						RenderUtil.fill(best.x + chartSize / 2, 0, 1, 2000, g);
					}

					int renderX = (best.x == -1 ? -2 : best.x) - 45;
					g.setColor(chartBackgroundColor.colorValue());

					if (renderInfo instanceof HistoricalData.Dividend) {
						HistoricalData.Dividend dividend = (HistoricalData.Dividend)renderInfo;

						RenderUtil.fill(renderX, renderInfoY - 83, 120, 60, g);
						if (best.stockUnit != null) {
							RenderUtil.drawString(g, RenderUtil.addColors("Amount: " + dividend.amount + Utils.getCurrencySymbol(stock.stock.getCurrency())), renderX, renderInfoY - 70, 15, false);
							RenderUtil.drawString(g, RenderUtil.addColors("Yield: " + Utils.formatDecimal(Stock.getChange(best.stockUnit.open, best.stockUnit.open + dividend.amount), 3) + "%"), renderX, renderInfoY - 70 + 20, 15, false);
						}
						RenderUtil.drawString(g, RenderUtil.addColors("Date: " + dividend.time.toStringDayMonthYear()), renderX, renderInfoY - 70 + 40, 15, false);
					} else if (renderInfo instanceof HistoricalData.Split) {
						HistoricalData.Split split = (HistoricalData.Split)renderInfo;

						RenderUtil.fill(renderX, renderInfoY - 63, 120, 40, g);
						RenderUtil.drawString(g, RenderUtil.addColors("Ratio: " + split.ratio), renderX, renderInfoY - 50, 15, false);
						RenderUtil.drawString(g, RenderUtil.addColors("Date: " + split.time.toStringDayMonthYear()), renderX, renderInfoY - 50 + 20, 15, false);
					} else if (renderInfo instanceof HistoricalData.Earning) {
						HistoricalData.Earning earning = (HistoricalData.Earning)renderInfo;

						RenderUtil.fill(renderX, renderInfoY - 103, 120, 80, g);
						RenderUtil.drawString(g, RenderUtil.addColors("Date: " + earning.reportedDate.toStringDayMonthYear()), renderX, renderInfoY - 90, 15, false);
						RenderUtil.drawString(g, RenderUtil.addColors("ReportedEPS: " + earning.reportedEPS), renderX, renderInfoY - 90 + 20, 15, false);
						RenderUtil.drawString(g, RenderUtil.addColors("EstimatedEPS: " + (earning.estimatedEPS == HistoricalData.Earning.nullValue ? "None" : earning.estimatedEPS)), renderX, renderInfoY - 90 + 20 * 2, 15, false);
						RenderUtil.drawString(g, RenderUtil.addColors("Surprise: " + (earning.surprisePercentage == HistoricalData.Earning.nullValue ? "None" : earning.surprisePercentage + "%")), renderX, renderInfoY - 90 + 20 * 3, 15, false);
					}

					renderedInfo = true;
				}
			}
		}
	}

	public double getHeight(double change, double height, double change2) {
		return change2 / (change / height);
	}
	
	public static class Candle {
		public int x, y, width, height;
		public StockUnit stockUnit;
		public Time overwriteTime;

		public Candle(StockUnit stockUnit, int x, int y, int width, int height) {
			this.stockUnit = stockUnit;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public Time getTime() {
			if (overwriteTime != null) {
				return overwriteTime;
			} else {
				return stockUnit.time;
			}
		}

		@Override
		public String toString() {
			return "X: " + x + " Y: " + y + " WIDTH: " + width + " HEIGHT: " + height + " STOCKUNIT: " + stockUnit;
		}
	}
}
