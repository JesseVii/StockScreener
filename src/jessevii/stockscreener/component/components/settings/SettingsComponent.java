package jessevii.stockscreener.component.components.settings;

import jessevii.stockscreener.component.Component;
import jessevii.stockscreener.utils.ComponentUtil;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class SettingsComponent extends Component {
	public static HashMap<Setting, java.awt.Component> map = new HashMap<>();
	
	public SettingsComponent() {
		super("Settings", true);
	}
	
	@Override
	public void onInit(String... params) {
		int index = 0;
		Component lastComponent = null;
		for (Setting setting : Setting.list) {
			//Group settings by components
			if (lastComponent != setting.component) {
				if (index == 0) {
					index += 1;
				} else {
					index += 2;
				}
				
				JLabel label = ComponentUtil.createLabel(setting.component != null ? setting.component.name : "Others", 5, (index - 1) * 25, 200, 50, 15);
				label.setForeground(Color.GREEN);
				this.panel.add(label);
			}
			
			//JLabel for the name of the setting
			JLabel label = ComponentUtil.createLabel(setting.name + ":", 5, index * 25, 200, 50, 15);
			label.setForeground(Color.WHITE);
			this.panel.add(label);
			
			//Creates the field where u can change the value.
			int width = 110;
			int x = ((Container)label).getFontMetrics(label.getFont()).stringWidth(setting.name + ":") + 10;
			
			if (setting.mode == Setting.Mode.COLOR) {
				JButton button = ComponentUtil.createButton("", x, 15 + index * 25, width, 20, 1, 15, false);
				button.setBackground(setting.colorValue());

				button.addActionListener(e -> {
					setting.setValue(JColorChooser.showDialog(null, "Choose a color", (Color)setting.getDefaultValue()));
					button.setBackground(setting.colorValue());
				});
				
				this.panel.add(button);
				map.put(setting, button);
			} else {
				JTextField textField = ComponentUtil.createTextField("", x, 15 + index * 25, width, 20, 1, 15, false);
				textField.setText("" + setting.getValue());
				this.panel.add(textField);
				map.put(setting, textField);
			}
			
			//JLabel that shows what type of setting it is
			JLabel typeLabel = ComponentUtil.createLabel(setting.mode.toString(), x + width + 5, index * 25, 200, 50, 15);
			typeLabel.setForeground(Color.WHITE);
			this.panel.add(typeLabel);
			
			index++;
			lastComponent = setting.component;
		}
	}
	
	@Override
	public void onDisabled(String... params) {
		boolean success = true;
		for (Setting setting : map.keySet()) {
			try {
				java.awt.Component component = map.get(setting);
				
				if (setting.mode == Setting.Mode.INTEGER) {
					setting.setValue(Integer.parseInt(((JTextField)component).getText()));
				} else if (setting.mode == Setting.Mode.DOUBLE) {
					setting.setValue(Double.parseDouble(((JTextField)component).getText()));
				} else if (setting.mode == Setting.Mode.BOOLEAN) {
					setting.setValue(Boolean.parseBoolean(((JTextField)component).getText()));
				} else if (setting.mode == Setting.Mode.TEXT) {
					setting.setValue(((JTextField)component).getText());
				}
			} catch (Exception e) {
				e.printStackTrace();
				success = false;
				setMessage("Error saving " + setting.name, Color.RED);
			}
		}
		
		if (success) {
			setMessage("Settings saved successfully", Color.GREEN);
		}
	}
}
