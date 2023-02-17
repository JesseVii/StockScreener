package jessevii.stockscreener.component.components.chart;

import jessevii.stockscreener.component.components.stats.StatsComponent;
import jessevii.stockscreener.objects.Time;
import jessevii.stockscreener.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoricalData {
	private static final List<Utils.DoubleString> fetchedEarnings = new ArrayList<>();

	public static List<StockUnit> getData(String ticker, String time) {
		return getData(ticker, time, System.currentTimeMillis());
	}

	public static List<StockUnit> getData(String ticker, String time, long startTime) {
		long dayInMs = 86400000;
		final long oldStartTime = startTime;
		switch (time) {
			case "1m":
				startTime -= dayInMs * 6; //Max 7
				break;
			case "2m":
			case "5m":
			case "15m":
			case "30m":
				startTime -= dayInMs * 58; //Max 60
				break;
			case "1h":
				startTime -= dayInMs * 729; //Max 730
				break;
			case "1d":
			case "1wk":
				startTime = 0; //No limit
				break;
		}

		String endDate = String.valueOf(oldStartTime).substring(0, 10);
		String startDate = String.valueOf(startTime);
		if (startDate.length() > 10) startDate = startDate.substring(0, 10);
		
		String link = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker + "?symbol=" + ticker + "&period1=" + startDate + "&period2=" + endDate + "&useYfid=true&interval=" + time + "&includePrePost=" + ChartComponent.postPreSetting.booleanValue() + "&events=div%7Csplit%7Cearn&lang=en-US&region=FIN&crumb=8YmsVK0vS2R&corsDomain=finance.yahoo.com";
		String html = Utils.getHtml(link);
        List<Long> timeStamps = new ArrayList<>();
        List<Double> opens = new ArrayList<>();
        List<Double> lows = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();
        List<Double> highs = new ArrayList<>();
        List<Double> closes = new ArrayList<>();
        List<Dividend> dividends = new ArrayList<>();
		List<Split> splits = new ArrayList<>();
		List<Earning> earnings = new ArrayList<>();
		List<RenderInfo> renderInfos = new ArrayList<>();
		StockInfo stockInfo = new StockInfo();

		//Get time stamps
        String stampSplit = html.split("timestamp\":\\[")[1];
        for (String stamp : stampSplit.substring(0, stampSplit.indexOf("]")).split(",")) {
        	timeStamps.add(Long.parseLong(stamp + "000"));
        }

		//Get opens
        String openSplit = html.split("open\":\\[")[1];
        for (String stamp : openSplit.substring(0, openSplit.indexOf("]")).split(",")) {
        	if (stamp.equals("null")) {
        		opens.add(opens.get(opens.size() - 1));
        	} else {
        		opens.add(Double.parseDouble(stamp));
        	}
        }

		//Get lows
        String lowSplit = html.split("low\":\\[")[1];
        for (String stamp : lowSplit.substring(0, lowSplit.indexOf("]")).split(",")) {
        	if (stamp.equals("null")) {
        		lows.add(lows.get(lows.size() - 1));
        	} else {
        		lows.add(Double.parseDouble(stamp));
        	}
        }

		//Get volumes
        String volumeSplit = html.split("volume\":\\[")[1];
        for (String stamp : volumeSplit.substring(0, volumeSplit.indexOf("]")).split(",")) {
        	if (stamp.equals("null")) {
        		volumes.add(volumes.get(volumes.size() - 1));
        	} else {
        		volumes.add(Long.parseLong(stamp));
        	}
        }

		//Get highs
        String highSplit = html.split("high\":\\[")[1];
        for (String stamp : highSplit.substring(0, highSplit.indexOf("]")).split(",")) {
        	if (stamp.equals("null")) {
        		highs.add(highs.get(highs.size() - 1));
        	} else {
        		highs.add(Double.parseDouble(stamp));
        	}
        }

		//Get closes
        String closeSplit = html.split("close\":\\[")[1];
        for (String stamp : closeSplit.substring(0, closeSplit.indexOf("]")).split(",")) {
        	if (stamp.equals("null")) {
        		closes.add(closes.get(closes.size() - 1));
        	} else {
        		closes.add(Double.parseDouble(stamp));
        	}
        }

		//Get dividends
		try {
			JSONObject jsonObject = new JSONObject(html).getJSONObject("chart").getJSONArray("result").getJSONObject(0).getJSONObject("events").getJSONObject("dividends");
			for (String key : jsonObject.keySet()) {
				JSONObject jsonDividend = jsonObject.getJSONObject(key);
				Dividend dividend = new Dividend(jsonDividend.getDouble("amount"), new Time(new Date(Long.parseLong(jsonDividend.getLong("date") + "000"))));
				dividends.add(dividend);
				renderInfos.add(dividend);
			}
		} catch (Exception ignored) {}

		//Get splits
		try {
			JSONObject jsonObject = new JSONObject(html).getJSONObject("chart").getJSONArray("result").getJSONObject(0).getJSONObject("events").getJSONObject("splits");
			for (String key : jsonObject.keySet()) {
				JSONObject jsonSplit = jsonObject.getJSONObject(key);
				Split split = new Split(jsonSplit.getInt("numerator"), jsonSplit.getInt("denominator"), jsonSplit.getString("splitRatio"), new Time(new Date(Long.parseLong(jsonSplit.getLong("date") + "000"))));
				splits.add(split);
				renderInfos.add(split);
			}
		} catch (Exception ignored) {}

		//Get earnings from alphavantage
		try {
			//If using 1 or 2 minute chart or showEarnings is disabled then don't
			if (!time.equals("1m") && !time.equals("2m") && ChartComponent.showEarnings.booleanValue()) {
				String earningHtml = "";

				//If earning for this ticker has already been fetched then set the html
				for (Utils.DoubleString fetchedEarning : fetchedEarnings) {
					if (fetchedEarning.first.equals(ticker)) {
						earningHtml = fetchedEarning.second;
						break;
					}
				}

				//Fetch earnings if it's not been fetched yet
				if (earningHtml.isEmpty()) {
					earningHtml = Utils.getHtml("https://www.alphavantage.co/query?function=EARNINGS&symbol=" + ticker + "&apikey=" + StatsComponent.alphavantageApiKey.stringValue());
					fetchedEarnings.add(new Utils.DoubleString(ticker, earningHtml));
				}

				JSONArray jsonArray = new JSONObject(earningHtml).getJSONArray("quarterlyEarnings");
				for (int i = 0; i < jsonArray.length() - 1; i++) {
					JSONObject o = jsonArray.getJSONObject(i);
					Earning earning = new Earning(
							new Time(o.getString("fiscalDateEnding")),
							new Time(o.getString("reportedDate")),
							Utils.getDoubleFromJsonObject(o, "reportedEPS", Earning.nullValue),
							Utils.getDoubleFromJsonObject(o, "estimatedEPS", Earning.nullValue),
							Utils.getDoubleFromJsonObject(o, "surprise", Earning.nullValue),
							Utils.getDoubleFromJsonObject(o, "surprisePercentage", Earning.nullValue));
					earnings.add(earning);
					renderInfos.add(earning);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Add dividends, splits and earnings to stockinfo
		stockInfo.dividends = dividends;
		stockInfo.splits = splits;
		stockInfo.earnings = earnings;
		stockInfo.renderInfo = renderInfos;

        List<StockUnit> units = new ArrayList<>();
        for (int i = 0; i < timeStamps.size(); i++) {
        	StockUnit unit = new StockUnit();
			unit.time = new Time(new Date(timeStamps.get(i)));
        	unit.high = highs.get(i);
        	unit.low = lows.get(i);
        	unit.open = opens.get(i);
        	unit.close = closes.get(i);
        	unit.volume = volumes.get(i);
			unit.stockInfo = stockInfo;
        	
        	if (unit.open != 0) {
            	units.add(unit);
        	}
        }

        return units;
	}

	public static class StockUnit {
		public double open, close, high, low;
		public long volume;
		public StockInfo stockInfo;
		public Time time;

		public StockUnit() {}
		public StockUnit(Time time, double open, double close, double high, double low, long volume) {
			this.time = time;
			this.open = open;
			this.close = close;
			this.high = high;
			this.low = low;
			this.volume = volume;
		}

		@Override
		public String toString() {
			return "DATE: " + time + " OPEN: " + open + " CLOSE: " + close + " HIGH: " + high + " LOW: " + low + " VOLUME: " + volume;
		}
	}

	public static class Dividend extends RenderInfo {
		public double amount;
		public boolean inFuture;
		public Time time;

		public Dividend(double amount, Time time) {
			this.amount = amount;
			this.time = time;
			this.renderInfoTime = this.time;
		}

		@Override
		public String toString() {
			return "AMOUNT: " + amount + " DATE: " + time;
		}
	}

	public static class Split extends RenderInfo {
		public int numerator, denominator;
		public String ratio;
		public Time time;

		public Split(int numerator, int denominator, String ratio, Time time) {
			this.numerator = numerator;
			this.denominator = denominator;
			this.ratio = ratio;
			this.time = time;
			this.renderInfoTime = this.time;
		}

		@Override
		public String toString() {
			return "RATIO: " + ratio + " DATE: " + time + " NUMERATOR: " + numerator + " DENOMINATOR: " + denominator;
		}
	}

	public static class Earning extends RenderInfo {
		public static int nullValue = -823726;
		public Time fiscalDateEnding, reportedDate;
		public double reportedEPS;
		public double estimatedEPS;
		public double surprise;
		public double surprisePercentage;

		public Earning(Time fiscalDateEnding, Time reportedDate, double reportedEPS, double estimatedEPS, double surprise, double surprisePercentage) {
			this.fiscalDateEnding = fiscalDateEnding;
			this.reportedDate = reportedDate;
			this.reportedEPS = reportedEPS;
			this.estimatedEPS = estimatedEPS;
			this.surprise = surprise;
			this.surprisePercentage = surprisePercentage;
			this.renderInfoTime = this.reportedDate;
		}

		@Override
		public String toString() {
			return "FISCALDATEENDING: " + fiscalDateEnding + " REPORTEDDATE: " + reportedDate + " REPORTEDEPS: " + reportedEPS + " ESTIMATEDEPS: " + estimatedEPS + " SURPRISE: " + surprise + " SURPRISEPERCENTAGE: " + surprisePercentage;
		}
	}

	public static class RenderInfo {
		public Time renderInfoTime;

		public RenderInfo(){}
	}

	public static class StockInfo {
		public List<Dividend> dividends = new ArrayList<>();
		public List<Split> splits = new ArrayList<>();
		public List<Earning> earnings = new ArrayList<>();
		public List<RenderInfo> renderInfo = new ArrayList<>();
	}
}
