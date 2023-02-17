package jessevii.stockscreener.main;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.settings.Setting;
import jessevii.stockscreener.component.components.settings.Setting.Mode;
import jessevii.stockscreener.component.components.settings.Setting.SettingChangedListener;
import jessevii.stockscreener.utils.ComponentUtil;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
	public static MainPanel instance;
	public static JButton homeButton, backButton;
	public static JLabel message;
	public static Setting componentButtonX = new Setting(Mode.INTEGER, "ButtonX", 10);
	public static Setting componentButtonY = new Setting(Mode.INTEGER, "ButtonY", 10);
	public static Setting componentButtonWidth = new Setting(Mode.INTEGER, "ButtonWidth", 110);
	public static Setting componentButtonHeight = new Setting(Mode.INTEGER, "ButtonHeight", 50);
	public static Setting componentBackgroundColor = new Setting(Mode.COLOR, "Component Background Color", new Color(0, 0, 0));
	public static Setting backgroundColor = new Setting(Mode.COLOR, "Background Color", new Color(0, 0, 0));
	public static Setting borderColor = new Setting(Mode.COLOR, "Border Color", new Color(20, 20, 20));
	public static Setting borderSize = new Setting(Mode.INTEGER, "Border Size", 2);
	
	public MainPanel() {
		instance = this;
		backgroundColor.addListener(new SettingChangedListener() {
			public void valueChanged() {
				instance.setBackground(backgroundColor.colorValue());	
			}
		});

		this.setLayout(null);
		
		homeButton = ComponentUtil.createButton("Home", 3, Main.instance.getHeight() - 72, 65, 30, 1, 15, false);
		homeButton.addActionListener(e -> {
			Main.instance.setTitle(Main.name);
			for (Component component : Component.list) {
				component.button.setVisible(true);
				if (component.toggled) {
					component.disable();
				}
			}
		});
		homeButton.setDoubleBuffered(true);
		this.add(homeButton);
		
		backButton = ComponentUtil.createButton("Back", 70, Main.instance.getHeight() - 72, 65, 30, 1, 15, false);
		backButton.addActionListener(e -> {
			Component last = Component.last;
			if (last != null) {
				for (Component component : Component.list) {
					component.button.setVisible(false);
					if (component.toggled) {
						component.disable();
					}
				}

				last.enable();
			}
		});
		this.setDoubleBuffered(true);
		this.add(backButton);
		
		int x = componentButtonX.intValue();
		int y = componentButtonY.intValue();
		int width = componentButtonWidth.intValue();
		int height = componentButtonHeight.intValue();
		for (Component component : Component.list) {
			JButton button = ComponentUtil.createButton(component.name, x, y, width, height, 1, 18, false);
			button.addActionListener(e -> {
				for (Component component1 : Component.list) {
					component1.button.setVisible(false);
					if (component1.toggled) {
						component1.disable();
					}
				}
				component.enable();
			});
			
			this.add(button);
			component.button = button;
			
			x += width + 8.6;
			if (x + width + 16 > Main.instance.getWidth()) {
				y += height + 8.6;
				x = 10;
			}
		}
		
		message = new JLabel();
		message.setBounds(80, Main.instance.getHeight() - 82, 500, 50);
		message.setVisible(false);
		this.add(message);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
}
