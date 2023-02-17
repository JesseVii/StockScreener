package jessevii.stockscreener.component;

import jessevii.stockscreener.component.components.chart.ChartComponent;
import jessevii.stockscreener.component.components.earnings.EarningsComponent;
import jessevii.stockscreener.component.components.followed.FollowedComponent;
import jessevii.stockscreener.component.components.history.HistoryComponent;
import jessevii.stockscreener.component.components.info.InfoComponent;
import jessevii.stockscreener.component.components.positions.PositionsComponent;
import jessevii.stockscreener.component.components.settings.Setting;
import jessevii.stockscreener.component.components.settings.Setting.SettingChangedListener;
import jessevii.stockscreener.component.components.settings.SettingsComponent;
import jessevii.stockscreener.component.components.stats.StatsComponent;
import jessevii.stockscreener.component.components.trade.TradeComponent;
import jessevii.stockscreener.main.Main;
import jessevii.stockscreener.main.MainPanel;
import jessevii.stockscreener.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Component {
	public String name;
	public JPanel panel, borderPanel;
	public JButton button;
	public boolean toggled, scrollable;
	public int yAdd, lastYAdd;
	public ArrayList<Runnable> onResizeRunnables = new ArrayList<>();
	public List<java.awt.Component> dontScroll = new ArrayList<>();
	private boolean init;
	public static List<Component> list = new ArrayList<Component>();
	public static Component current, last;
	
	public Component(String name, boolean scrollable) {
		list.add(this);
		this.name = name;
		this.scrollable = scrollable;
		
		panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				onPaint((Graphics2D)g);
			}
		};
		
		panel.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent e) {
				onMousePressed(e);
			}
		});
		
		MainPanel.componentBackgroundColor.addListener(new SettingChangedListener() {
			public void valueChanged() {
				panel.setBackground(MainPanel.componentBackgroundColor.colorValue());	
			}
		});
		
		panel.setBounds(0, 0, Main.instance.getWidth() - 16, Main.instance.getHeight() - 75);
		panel.setLayout(null);
		
		JPanel panel2 = new JPanel();
		panel2.setName("overlayborder");
		panel2.setBounds(0, 0, Main.instance.getWidth() - 16, Main.instance.getHeight() - 75);
		MainPanel.borderColor.addListener(new SettingChangedListener() {
			public void valueChanged() {
				panel2.setBorder(BorderFactory.createMatteBorder(0, 0, MainPanel.borderSize.intValue(), 0, MainPanel.borderColor.colorValue()));
			}
		});
		
		borderPanel = panel2;
		panel.add(panel2);
		
		//Add scrolling ability
		if (scrollable) {
			panel.addMouseWheelListener(e -> updateXAndYAdd(e));
		}
	}

	public void onPaint(Graphics2D g) {}
	public void onEnabled(String... params) {}
	public void onDisabled(String... params) {}
	public void onInit(String... params) {}
	public void onResize(int width, int height) {}
	public void onMousePressed(MouseEvent e) {}
	
	public void enable(String... params) {
		if (!init) {
			MainPanel.instance.add(panel);
			MainPanel.instance.repaint();
			init = true;
			onInit(params);
		}
		
		borderPanel.setBounds(0, panel.getHeight() - MainPanel.borderSize.intValue(), Main.instance.getWidth() - 16, MainPanel.borderSize.intValue());
		onEnabled(params);
		this.panel.setVisible(true);
		toggled = true;
		current = this;
		Main.instance.setTitle(Main.name + " (" + this.name + ")");
	}
	
	public void disable(String... params) {
		if (this.scrollable) {
			for (java.awt.Component component : panel.getComponents()) {
				if (!dontScroll.contains(component)) {
					if (component.getName() == null || !component.getName().equals("overlayborder")) {
						component.setBounds(component.getX(), component.getY() - yAdd, component.getWidth(), component.getHeight());	
					}
				}
			}
			yAdd = 0;
			lastYAdd = 0;
		}
		
		onDisabled(params);
		this.panel.setVisible(false);
		toggled = false;
		current = null;
		last = this;
	}
	
	public void onResize(Runnable runnable) {
		this.onResizeRunnables.add(runnable);
		runnable.run();
	}
	
	protected void updateXAndYAdd(MouseWheelEvent e) {
		if (e != null) {
			//Down
			if (e.getWheelRotation() == -1) {
				yAdd += 20;
			}
			
			//Up
			else {
				yAdd -= 20;
			}
		}
		
		for (java.awt.Component component : panel.getComponents()) {
			if (!dontScroll.contains(component)) {
				if (component.getName() == null || !component.getName().equals("overlayborder")) {
					component.setBounds(component.getX(), component.getY() + (yAdd - lastYAdd), component.getWidth(), component.getHeight());	
				}
			}
		}
		
		lastYAdd = yAdd;
		panel.repaint();
	}
	
	public void setMessage(String message, Color color) {
		new Thread(() -> {
			MainPanel.message.setBounds(140, Main.instance.getHeight() - 82, 500, 50);
			MainPanel.message.setVisible(true);
			MainPanel.message.setText(message);
			MainPanel.message.setFont(new Font(Main.font, Font.BOLD, 20));
			MainPanel.message.setForeground(color);

			Utils.sleep(3 * 1000);

			int lastAlpha = MainPanel.message.getForeground().getAlpha();
			while(MainPanel.message.getForeground().getAlpha() > 0) {
				if (lastAlpha != MainPanel.message.getForeground().getAlpha()) {
					break;
				}

				MainPanel.message.setForeground(
						new Color(MainPanel.message.getForeground().getRed(),
								MainPanel.message.getForeground().getGreen(),
								MainPanel.message.getForeground().getBlue(),
								MainPanel.message.getForeground().getAlpha() - 1));
				MainPanel.message.invalidate();
				lastAlpha = MainPanel.message.getForeground().getAlpha();

				Utils.sleep(2);
			}
		}).start();
	}

	public List<Setting> getComponentsSettings() {
		List<Setting> list = new ArrayList<>();
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				if (field.getType() == Setting.class) {
					list.add((Setting)field.get(this));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	//Initializes the components. Called from Main
	public static void init() {
		new ChartComponent();
		new PositionsComponent();
		new FollowedComponent();
		new EarningsComponent();
		new TradeComponent();
		new InfoComponent();
		new StatsComponent();
		
		new HistoryComponent();
		new SettingsComponent();
	}
}
