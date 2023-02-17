package jessevii.stockscreener.component.components.settings;

import jessevii.stockscreener.component.Component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Setting {
	private Object value, defaultValue;
	public Mode mode;
	public String name;
	public String[] description;
	public Component component;
	public JCheckBox checkBox;
	public List<SettingChangedListener> valueChangedListeners = new ArrayList<>();
	public static ArrayList<Setting> list = new ArrayList<>();
	
	public Setting(Mode mode, String name, Object defaultValue, String... description) {
		list.add(this);
		this.mode = mode;
		this.name = name;
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.description = description;
	}
	
	public boolean booleanValue() {
		return (boolean)value;
	}
	
	public int intValue() {
		return (int)value;
	}
	
	public double doubleValue() {
		return (double)value;
	}
	
	public String stringValue() {
		return (String)value;
	}
	
	public Color colorValue() {
		return (Color)value;
	}
	
	public void setValue(Object value) {
		this.value = value;
		
		for (SettingChangedListener listener : valueChangedListeners) {
			listener.valueChanged();
		}
	}
	
	public Object getValue() {
		if (mode == Mode.COLOR) {
			return "#" + Integer.toHexString(colorValue().getRGB());
		}
		
		return this.value;
	}
	
	public Object getDefaultValue() {
		return this.defaultValue;
	}
	
	public void addListener(SettingChangedListener listener) {
		valueChangedListeners.add(listener);
		listener.valueChanged();
	}
	
	/**
	 * Connects the given checkbox to this setting so when the value is changed
	 * It will change the settings value as well. (Only for boolean mode)
	 */
	public void addCheckBox(JCheckBox checkBox) {
		this.checkBox = checkBox;
		checkBox.addActionListener(e -> setValue(checkBox.isSelected()));
	}
	
	public enum Mode {
		BOOLEAN(),
		INTEGER(),
		DOUBLE(),
		TEXT(),
		COLOR();
	}
	
	public static class SettingChangedListener {
		public void valueChanged() {}
	}
}
