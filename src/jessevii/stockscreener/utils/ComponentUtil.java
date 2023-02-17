package jessevii.stockscreener.utils;

import jessevii.stockscreener.main.Main;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ComponentUtil {
	public static JButton selected;
	public static int selectedBorderSize;
	
	public static JPanel createPanel(int x, int y, int width, int height, int borderSize, Color borderColor, Color backgroundColor) {
		JPanel panel = new JPanel();
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, borderColor));
		panel.setBackground(backgroundColor);
		
		return panel;
	}
	
	public static JButton createButton(String name, int x, int y, int width, int height, int borderSize, int fontSize, boolean highlight) {
		JButton button = new JButton(name);
		
		button.setBackground(Themes.BUTTON_BACKGROUND);
		button.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Themes.BUTTON_BORDER));
		button.setForeground(Themes.BUTTON_TEXT);
		button.setBounds(x, y, width, height);
		button.setFont(new Font(Main.font, Font.BOLD, fontSize));
		button.setFocusPainted(false);
		
		//Change border color when hovering over
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				if (!button.equals(selected)) {
					button.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Themes.BUTTON_BORDER_HOVER));
				}
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				if (!button.equals(selected)) {
					button.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Themes.BUTTON_BORDER));
				}
			}
		});
		
		//Highlight selected button
		if (highlight) {
			button.addActionListener(e -> {
				if (selected != null) {
					selected.setBorder(BorderFactory.createMatteBorder(selectedBorderSize, selectedBorderSize, selectedBorderSize, selectedBorderSize, Themes.BUTTON_BORDER));
				}

				button.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Themes.BUTTON_SELECTED));
				selected = button;
				selectedBorderSize = borderSize;
			});
		}
		
		return button;
	}
	
	public static JTextField createTextField(String name, int x, int y, int width, int height, int borderSize, int fontSize, boolean onlyNumbers) {
		JTextField textField = new JTextField(name);
		
		textField.setBackground(Themes.BUTTON_BACKGROUND);
		textField.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Themes.BUTTON_BORDER));
		textField.setForeground(Themes.BUTTON_TEXT);
		textField.setBounds(x, y, width, height);
		textField.setFont(new Font(Main.font, Font.BOLD, fontSize));
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setCaretColor(Color.WHITE);
		
		//Change border color when hovering over
		textField.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				textField.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Themes.BUTTON_BORDER_HOVER));
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				textField.setBorder(BorderFactory.createMatteBorder(borderSize, borderSize, borderSize, borderSize, Themes.BUTTON_BORDER));
			}
		});
		
		//If onlyNumbers is true then cancel all events trying to input letters
		if (onlyNumbers) {
			textField.addKeyListener(new KeyAdapter() {
		        public void keyTyped(KeyEvent evt) {
		        	if (!Character.isDigit(evt.getKeyChar()) && evt.getKeyChar() != '-') {
		        		evt.consume();
		        	}
		        }
		    });
		}
		
		return textField;
	}
	
	public static JComboBox<String> createComboBox(int x, int y, int width, int height, int fontSize) {
		JComboBox<String> box = new JComboBox<>();
		
		box.setBackground(Themes.BUTTON_BACKGROUND);
		box.setForeground(Themes.BUTTON_TEXT);
		box.setBounds(x, y, width, height);
		box.setFont(new Font(Main.font, Font.BOLD, fontSize));
		box.setFocusable(false);
		
		return box;
	}
	
	public static JCheckBox createCheckBox(String name, int x, int y, int width, int height, int fontSize) {
		JCheckBox box = new JCheckBox(name);
		
		box.setHorizontalTextPosition(SwingConstants.LEFT);
		box.setBackground(Themes.BUTTON_BACKGROUND);
		box.setForeground(Themes.BUTTON_TEXT);
		box.setBounds(x, y, width, height);
		box.setFont(new Font(Main.font, Font.BOLD, fontSize));
		box.setFocusable(false);
		
		return box;
	}
	
	public static JLabel createLabel(String name, int x, int y, int width, int height, int fontSize) {
		JLabel label = new JLabel(name);
		
		label.setHorizontalTextPosition(SwingConstants.LEFT);
		label.setBackground(Themes.BUTTON_BACKGROUND);
		label.setForeground(Themes.BUTTON_TEXT);
		label.setBounds(x, y, width, height);
		label.setFont(new Font(Main.font, Font.BOLD, fontSize));
		label.setFocusable(false);
		
		return label;
	}
	
	/**
	 * Calculate center x with the given width
	 * @param component the component which the x should be centered on
	 */
	public static int calculateCenterX(int width, Component component) {
		return (component.getWidth() / 2) - width / 2;
	}
	
	public static void setTextFieldOnlyUppercase(JTextField textField) {
		AbstractDocument document = (AbstractDocument)textField.getDocument();
        document.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                super.insertString(fb, offset, string.toUpperCase(), attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                super.replace(fb, offset, length, text.toUpperCase(), attrs);
            }

        });
	}
	
	public static int getStringWidth(String string, Graphics g) {
		return SwingUtilities.computeStringWidth(g.getFontMetrics(), string);
	}
	
	public static class Themes {
		public static Color BUTTON_TEXT = new Color(0, 255, 0);
		public static Color BUTTON_BACKGROUND = new Color(30, 30, 30);
		public static Color BUTTON_BORDER = new Color(74, 77, 89);
		public static Color BUTTON_BORDER_HOVER = new Color(255, 0, 0);
		public static Color BUTTON_SELECTED = new Color(0, 255, 0);
	}
}
