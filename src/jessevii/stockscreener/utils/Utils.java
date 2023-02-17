package jessevii.stockscreener.utils;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Random;

public class Utils {
	
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class MotionListenerJPanel extends JPanel implements MouseMotionListener {
		public void mouseDragged(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}
	}
	
	public static double getChange(double start, double end) {
	     DecimalFormat df = new DecimalFormat("###.###############");
	     String s = df.format(start - end).replace(",", ".");
	     while(!isNumber(Character.toString(s.charAt(0)))) {
	    	 s = s.substring(1);
	     }
	     
	     if (start > end) {
	    	 s = "-" + s;
	     }
	     
	     return Double.parseDouble(s);
	}
	
	public static boolean isNumber(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static double formatDecimal(double number, int decimal) {
		if (Double.isNaN(number)) {
			return 0;
		}
		
		return Double.parseDouble(String.format("%." + decimal + "f", number).replace(",", "."));
	}
	
	public static String formatToMBT(long value) {
		if (value / 1000000 < 1000) {
			return Utils.formatDecimal(((double)value / (double)1000000), 3) + "M";
		} else if (value / 1000000000 < 1000) {
			return Utils.formatDecimal(((double)value / (double)1000000000), 3) + "B";
		} else {
			return Utils.formatDecimal(((double)value / (double)1000000000000L), 3) + "T";
		}
	}
	
	public static String getCurrencySymbol(String currency) {
		if (currency.equals("USD")) {
			return "$";
		} else if (currency.equals("EUR")) {
			return "€";
		}
		
		return "?";
	}
	
	public static String getHtml(String url) {
		try {
	        URL url2 = new URL(url);
	        BufferedReader in = new BufferedReader(new InputStreamReader(url2.openStream()));

	        String inputLine;
	        String html = "";
	        while ((inputLine = in.readLine()) != null) {
	        	html += inputLine;
	        }
	        
	        in.close();
	        
	        return html;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static String generateRandomString(int lenght) {
	    int leftLimit = 97;
	    int rightLimit = 122;
		Random random = new Random();

		return random.ints(leftLimit, rightLimit + 1)
		  .limit(lenght)
		  .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
		  .toString();
	}
	
	public static Color decodeHex(String hex) {
	    return new Color(
	            Integer.valueOf(hex.substring(3, 5), 16),
	            Integer.valueOf(hex.substring(5, 7), 16),
	            Integer.valueOf(hex.substring(7, 9), 16),
	            Integer.valueOf(hex.substring(1, 3), 16));
	}

	public static double getDoubleFromJsonObject(JSONObject jsonObject, String key, int nullValue) {
		try {
			return jsonObject.getDouble(key);
		} catch (Exception ignored) {
			return nullValue;
		}
	}

	public static String formatNumberTo0Start(int num) {
		String output = Integer.toString(num);
		while (output.length() < 2) output = "0" + output;
		return output;
	}

	public static class DoubleString {
		public String first, second;

		public DoubleString(String first, String second) {
			this.first = first;
			this.second = second;
		}
	}
}
