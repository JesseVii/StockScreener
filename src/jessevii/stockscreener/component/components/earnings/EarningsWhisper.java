package jessevii.stockscreener.component.components.earnings;

import jessevii.stockscreener.objects.Time;
import jessevii.stockscreener.utils.Utils;

import java.util.*;

public class EarningsWhisper {
	private static int done;
	private static final List<String> allTickers = new ArrayList<>();
	public static List<String> anticipatedTickers = new ArrayList<>();
	
	@SuppressWarnings("removal")
	public static List<Earning> getEarnings(int days) {
		allTickers.clear();
		List<List<Earning>> earnings = new ArrayList<>();
		for (int i3 = 0; i3 < days; i3++) {
			earnings.add(new ArrayList<>());
		}
		
		done = 0;
		List<Thread> threads = new ArrayList<>();
		for (int i2 = 0; i2 < days; i2++) {
			final int day = i2;
			new Thread() {
				public void run() {
					threads.add(this);
					try {
						String html = Utils.getHtml("https://www.earningswhispers.com/calendar?sb=p&d=" + day + "&t=all&v=s");

						String tickersText = html.substring(html.indexOf("meta name=\"description\" content=\"The most anticipated earnings releases scheduled for") + 86, 
						html.indexOf("The Whisper Calendar also contains the Earnings Whisper Score, investor sentiment data, volatility, and more. \" /></head>"));
						String[] spaceSplit = tickersText.split(" ");
						
						String date = spaceSplit[0] + " " + spaceSplit[1] + " " + spaceSplit[2] + " " + spaceSplit[3];
						tickersText = tickersText.substring(date.length() + 1);
						
						date = date.substring(date.indexOf(",") + 2);
						String day2 = date.split(",")[0].split(" ")[1];
						String month = date.split(" ")[0];
						String year = date.split(",")[1].replace(" ", "");
						
						String[][] list = {new String[]{"January", "1"}, new String[]{"February", "2"}, new String[]{"March", "3"}, new String[]{"April", "4"},
						new String[]{"May", "5"}, new String[]{"June", "6"}, new String[]{"July", "7"}, new String[]{"August", "8"}, new String[]{"September", "9"},
						new String[]{"October", "10"}, new String[]{"November", "11"}, new String[]{"December", "12"}};
						for (String[] s : list) {
							if (month.equals(s[0])) {
								month = s[1];
								break;
							}
						}
						
						date = day2 + "." + month + "." + year;
						
						List<String> names = new ArrayList<>();
						List<String> tickers = new ArrayList<>();
						List<String> epss = new ArrayList<>();
						List<String> revenues = new ArrayList<>();
						List<String> times = new ArrayList<>();
						HashMap<String, String> growths = new HashMap<>();
						HashMap<String, String> surprises = new HashMap<>();
						
						//Get anticipated tickers
						for (String name : tickersText.split(",")) {
							if (!name.contains(")")) {
								continue;
							}
							
							name = name.substring(0, name.indexOf(")")) + ")";
							if (name.startsWith(" ")) {
								name = name.substring(1);
							}
							
							String ticker = name.substring(name.indexOf("(")).replace("(", "").replace(")", "");
							if (!anticipatedTickers.contains(ticker)) {
								anticipatedTickers.add(ticker);
							}
						}
						
						//Get tickers
						int index2 = html.indexOf("<div id=\"O-");
						while (index2 >= 0) {
						    String ticker = html.substring(index2 + 11, index2 + 30);
							ticker = ticker.split("\"")[0];

							if (!ticker.contains("html><html xmlns=") && !allTickers.contains(ticker)) {
								tickers.add(ticker);
								allTickers.add(ticker);
							}
							
						    index2 = html.indexOf("<div id=\"O-", index2 + 1);
						}
						
						//Get names
						for (String ticker : tickers) {
							try {
								String search = "<div class=\"company\" onclick=\"javascript:location.href='stocks/" + ticker.toLowerCase();
								if (!html.contains(search)) {
									search = "<div class=\"company\" onclick=\"javascript:location.href='epsdetails/" + ticker.toLowerCase();
								}
								
								int index = html.indexOf(search);
								index += (search).length() + 3;
								String name = html.substring(index, index + 100);
								name = name.split("<")[0];
								names.add(name);
							} catch (Exception e) {
								names.add("-");
							}
						}
						
						//Get eps's or growths and surprises if the earning is released
						for (String ticker : tickers) {
							int index = html.indexOf("<div class=\"estimate\" onclick=\"javascript:location.href='stocks/" + ticker.toLowerCase());
							if (html.contains("showgrowth(\"rr-" + ticker.toUpperCase() + "\",\"")) {
								index = html.indexOf("showgrowth(\"rr-" + ticker.toUpperCase() + "\",\"");
								index += 18 + ticker.length();
								String growth = html.substring(index, index + 10).split("\"")[0];
								index = html.indexOf("showsurprise(\"rs-" + ticker.toUpperCase() + "\",\"");
								index += 20 + ticker.length();
								String surprise = html.substring(index, index + 10).split("\"")[0];

								epss.add("-");
								growths.put(ticker, growth);
								surprises.put(ticker, surprise);
								continue;
							}
							
							String text = html.substring(index, index + 125);
							String eps = "-";
							try {
								eps = text.substring(text.indexOf("$"), text.indexOf("</div>")).replace("$", "").replace(")", "");
							} catch (Exception ignored) {}
							epss.add(eps);
						}
						
						//Get revenues
						for (String ticker : tickers) {
							try {
								int index = html.indexOf("<div class=\"revest\" onclick=\"javascript:location.href='stocks/" + ticker.toLowerCase());
								if (html.contains("<div class=\"revactual green\" onclick=\"javascript:location.href='epsdetails/" + ticker.toLowerCase())) {
									index = html.indexOf("<div class=\"revactual green\" onclick=\"javascript:location.href='epsdetails/" + ticker.toLowerCase());
								}
								
								String text = html.substring(index, index + 125);
								String revenue = "-";
								try {
									revenue = text.substring(text.indexOf("$"), text.indexOf("</div>")).replace("$", "").replace(")", "").replace(" ", "");
								} catch (Exception ignored) {}
								revenues.add(revenue);
							} catch (Exception e) {
								revenues.add("-");
							}
						}
						
						//Get times
						for (String ticker : tickers) {
							if (html.contains("showgrowth(\"rr-" + ticker.toUpperCase())) {
								times.add("-");
								continue;
							}
							
							int index = html.indexOf("<div class=\"time\" onclick=\"javascript:location.href='stocks/" + ticker.toLowerCase());
							String text = html.substring(index, index + 125);
							String time = text.substring(text.indexOf("'\">") + 3, text.indexOf("</div>"));
							
							try {
								TimeZone tz = TimeZone.getTimeZone("EST");
								Calendar c = Calendar.getInstance(tz);
								int hourDiff = Math.abs(Time.getTime().hour - (c.get(Calendar.HOUR_OF_DAY) + 1));
								int hours = Integer.parseInt(time.split(":")[0]) + hourDiff;
								String minutes = time.split(" ")[0].split(":")[1];
								if (time.contains("PM")) {
									hours += 12;
									if (hours > 23) {
										hours -= 24;
									}
								}
								time = hours + ":" + minutes;
							} catch (Exception ignored) {

							}
							
							times.add(time);
						}
						
						for (int i = 0; i < tickers.size(); i++) {
							Earning earning = new Earning();
							earning.name = names.get(i);
							earning.ticker = tickers.get(i);
							earning.date = date;
							earning.eps = epss.get(i);
							earning.revenue = revenues.get(i);
							earning.time = times.get(i);
							
							if (earning.time.equals("-")) {
								earning.growth = growths.get(earning.ticker);
								earning.surprise = surprises.get(earning.ticker);
							}
							
							try {
								earning.timeHour = Integer.parseInt(earning.time.split(":")[0]);
								earning.timeMinute = Integer.parseInt(earning.time.split(":")[1].split(" ")[0]);
							} catch (Exception ignored) {
								
							}

							earnings.get(day).add(earning);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					done++;
				}
			}.start();
		}
		
		while(days != done) {
			Utils.sleep(50);
		}
		
		List<Earning> list = new ArrayList<>();
		for (List<Earning> l : earnings) {
			list.addAll(l);
		}
		
		for (Thread thread : threads) {
			thread.suspend();
		}
		
		return list;
	}
	
	public static class Earning {
		public String ticker, name, time, eps, revenue, date, growth, surprise;
		public int timeHour, timeMinute;
		
		public Earning() {}

		@Override
		public String toString() {
			return name + "(" + ticker + ") " + date + " - " + time + " EPS: " + eps + " Revenue: " + revenue;
		}
	}
}
