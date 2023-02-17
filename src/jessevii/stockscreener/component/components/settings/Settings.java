package jessevii.stockscreener.component.components.settings;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.component.components.settings.Setting.Mode;
import jessevii.stockscreener.utils.FileUtil;
import jessevii.stockscreener.utils.Utils;

import java.lang.reflect.Field;
import java.util.List;

public class Settings {
	/**
	 * Saves the settings to the file
	 */
	public static void save() {
		FileUtil.createFile("Settings");
		for (Setting setting : Setting.list) {
			FileUtil.addToFile("Settings", new String[]{setting.name, "" + setting.getValue()});
		}
	}
	
	/**
	 * Loads the settings and sets the values for the Setting objects
	 */
	public static void load() {
		for (List<String> line : FileUtil.readFile("Settings")) {
			try {
				Setting setting = get(line.get(0));
				
				if (setting.mode == Mode.BOOLEAN) {
					setting.setValue(Boolean.parseBoolean(line.get(1)));
				} else if (setting.mode == Mode.INTEGER) {
					setting.setValue(Integer.parseInt(line.get(1)));
				} else if (setting.mode == Mode.DOUBLE) {
					setting.setValue(Double.parseDouble(line.get(1)));
				} else if (setting.mode == Mode.TEXT) {
					setting.setValue(line.get(1));
				} else if (setting.mode == Mode.COLOR) {
					setting.setValue(Utils.decodeHex(line.get(1)));
				}
			} catch (Exception e) {
				System.out.println("Error loading one setting");
				e.printStackTrace();
			}
		}
		
		//Sets components for settings
		for (Component component : Component.list) {
			for (Field field : component.getClass().getFields()) {
				try {
					Class<?> myType = Setting.class;
		
					if (field.getType().isAssignableFrom(myType)) {
						Setting setting = (Setting)field.get(component);
						setting.component = component;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Gets the setting with the given name
	 */
	public static Setting get(String name) {
		for (Setting setting : Setting.list) {
			if (setting.name.equals(name)) {
				return setting;
			}
		}
		
		return null;
	}
}
