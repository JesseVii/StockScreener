package jessevii.stockscreener.utils;

import jessevii.stockscreener.main.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FileUtil {
	private static final String extension = ".txt";
	private static final String regex = ":";
	
	/**
	 * Creates a file with the given name and if it already exists then it deletes it before
	 */
	public static void createFile(String name) {
		try {
			File file = new File(Main.path + name + extension);
			file.delete();
			file.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads the file with the given name and returns a list of the lines and the content parsed from the line
	 */
	public static List<List<String>> readFile(String name) {
		List<List<String>> list = new ArrayList<>();
		
		try {
			Scanner s = new Scanner(new File(Main.path + name + extension));
			while (s.hasNextLine()) {
				String line = s.nextLine();
				if (!line.isEmpty()) {
					list.add(new ArrayList<>());
					for (String string : line.split(regex)) {
						list.get(list.size() - 1).add(string);
					}
				}
			}
			s.close();
		} catch (Exception ignored) {

		}
		
		return list;
	}
	
	/**
	 * Adds the content to a new line for the file with the given name
	 */
	public static void addToFile(String name, Object[] content) {
		List<String> list = new ArrayList<>();
		for (Object object : content) {
			list.add("" + object);
		}
		
		addToFile(name, list);
	}
	
	/**
	 * Adds the content to a new line for the file with the given name
	 */
	public static void addToFile(String name, String[] content) {
		addToFile(name, Arrays.asList(content));
	}
	
	/**
	 * Adds the content to a new line for the file with the given name
	 */
	public static void addToFile(String name, List<String> content) {
		try {
			File file = new File(Main.path + name + extension);
			if (!file.exists()) {
				file.createNewFile();
			}
			
			String text = "";
			for (String s : content) {
				text += s + regex;
			}
			text = text.substring(0, text.length() - 1);
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
			bw.write(text);
			bw.newLine();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes the line that contains the text from the file with the given name
	 */
	public static void removeFromFile(String name, String text) {
		try {
			ArrayList<String> lines = new ArrayList<String>();
			
			File file = new File(Main.path + name + extension);
			Scanner s = new Scanner(file);
			
			//Read file and add all lines to an array expect the ones that are identical to passed text
			while(s.hasNextLine()) {
				String line = s.nextLine();
				
				if (!line.contains(text)) {
					lines.add(line);
				}
			}
			s.close();
			
			//Make a new file and write all lines in array back to file
			file.delete();
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			for (String line : lines) {
				bw.write(line);
				bw.newLine();
			}
			
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the directory for the program files
	 */
	public static void createDirectory() {
		new File(Main.path.substring(0, Main.path.length() - 1)).mkdir();
	}
} 
