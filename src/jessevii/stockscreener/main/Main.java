package jessevii.stockscreener.main;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.settings.Settings;
import jessevii.stockscreener.utils.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main extends JFrame {
	/** Path to the directory that the program uses for files **/
	public static String path = System.getProperty("user.home") + "/Documents/StockScreener/";
	public static String name = "Stock Screener";
	public static String font = "Inter Medium";
	public static Main instance;

	public static void main(String[] args) {
		FileUtil.createDirectory();
		SwingUtilities.invokeLater(() -> new Main());
	}

	public Main() {		
		instance = this;
		this.setBackground(Color.BLACK);
		this.setTitle(name);
		this.setSize(500, 700);
		this.setLocationRelativeTo(null);
		this.setResizable(true);
		this.setLayout(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setFocusable(true);
		
		//Updates stuff when the JFrame is resized
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				//Resize MainPanel and homeButton for new size
				MainPanel.instance.setBounds(0, 0, instance.getWidth() - 14, instance.getHeight() - 37);
				MainPanel.homeButton.setBounds(MainPanel.homeButton.getX(), instance.getHeight() - 72, MainPanel.homeButton.getWidth(), MainPanel.homeButton.getHeight());
				MainPanel.backButton.setBounds(MainPanel.backButton.getX(), instance.getHeight() - 72, MainPanel.backButton.getWidth(), MainPanel.backButton.getHeight());
				
				//Resize the component buttons, so they will match for the new width
				int x = MainPanel.componentButtonX.intValue();
				int y = MainPanel.componentButtonY.intValue();
				int width = MainPanel.componentButtonWidth.intValue();
				int height = MainPanel.componentButtonHeight.intValue();
				for (Component component : Component.list) {
					component.button.setBounds(x, y, width, height);
					
					x += width + 8.6;
					if (x + width + 16 > Main.instance.getWidth()) {
						y += height + 8.6;
						x = 10;
					}
				}
				
				//Resize Component panels
				for (Component component : Component.list) {
					component.panel.setBounds(0, 0, Main.instance.getWidth() - 16, Main.instance.getHeight() - 75);
				}
				
				//Resize the border panel and call onResize
				if (Component.current != null) {
					Component.current.borderPanel.setBounds(0, Component.current.panel.getHeight() - MainPanel.borderSize.intValue(), Main.instance.getWidth() - 16, MainPanel.borderSize.intValue());
					
					Component.current.onResize(instance.getWidth(), instance.getHeight());
					for (Runnable runnable : Component.current.onResizeRunnables) {
						runnable.run();
					}
				}
				
				//Repaint main panel
				MainPanel.instance.repaint();
			}
		});
		
		//Saves settings and other stuff when the program is closed
		this.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		        Settings.save();
		    }
		});
		
		Component.init();
		this.add(new MainPanel());
		
		//Loads the settings
		Settings.load();
		
		//Sets the JFrame visible
		this.setVisible(true);
	}
}
