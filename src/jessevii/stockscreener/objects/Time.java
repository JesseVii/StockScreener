package jessevii.stockscreener.objects;

import jessevii.stockscreener.utils.Utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Time {
	public int year, month, day, hour, minute, second;
	long ms = -1;

	public Time(int year, int month, int day, int hour, int minute, int second) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}
	
	public Time(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public Time(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		this.year = calendar.get(Calendar.YEAR);
		this.month = calendar.get(Calendar.MONTH) + 1;
		this.day = calendar.get(Calendar.DAY_OF_MONTH);
		this.hour = calendar.get(Calendar.HOUR_OF_DAY);
		this.minute = calendar.get(Calendar.MINUTE);
		this.second = calendar.get(Calendar.SECOND);
	}

	public Time(Calendar calendar) {
		this.year = calendar.get(Calendar.YEAR);
		this.month = calendar.get(Calendar.MONTH) + 1;
		this.day = calendar.get(Calendar.DAY_OF_MONTH);
		this.hour = calendar.get(Calendar.HOUR_OF_DAY);
		this.minute = calendar.get(Calendar.MINUTE);
		this.second = calendar.get(Calendar.SECOND);
	}

	public Time(String time) {
		String[] split = time.split("-");
		this.year = Integer.parseInt(split[0]);
		this.month = Integer.parseInt(split[1]);
		this.day = Integer.parseInt(split[2]);
	}

	/**
	* @return difference to other time in milliseconds
	* Get in days by diving with 86400000L
	*/
	public long difference(Time other) {
		return Math.abs(this.getMs() - other.getMs());
	}
	
	public long getMs() {
		if (ms != -1) {
			return ms;
		}

		ms = getDate().getTime();
		return ms;
	}

	public Date getDate() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy/HH/mm/ss", Locale.ENGLISH);
			return sdf.parse(this.month + "/" + this.day + "/" + this.year + "/" + this.hour + "/" + this.minute + "/" + this.second);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks if this time is higher than the other time. So if this time is 20 seconds ahead of the other time then it will return true
	 */
 	public boolean isHigher(Time other) {
 		return getIntegerTime() > other.getIntegerTime();
 	}
 	
 	public int getIntegerTime() {
 		return Integer.parseInt(toString().replace("-", ""));
 	}
 	
	public static Time getTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();
		return parseTime(dtf.format(now));
	}
	
	public static Time parseTime(String time) {
		String[] split = time.split("-");
		return new Time(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]), Integer.parseInt(split[5]));
	}
	
	public static String getStringTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
	
	@Override
	public String toString() {
		return "" + Utils.formatNumberTo0Start(day) + "." + Utils.formatNumberTo0Start(month) + "." + year + " " + Utils.formatNumberTo0Start(hour) + ":" + Utils.formatNumberTo0Start(minute);
	}

	public String toStringDayMonthYear() {
		return "" + Utils.formatNumberTo0Start(day) + "." + Utils.formatNumberTo0Start(month) + "." + year;
	}

	public String toStringDash() {
		return year + "-" + Utils.formatNumberTo0Start(month) + "-" + Utils.formatNumberTo0Start(day) + "-" + Utils.formatNumberTo0Start(hour) + "-" + Utils.formatNumberTo0Start(minute) + "-" + Utils.formatNumberTo0Start(second);
	}

	@Override
	public boolean equals(Object otherObject) {
		 Time other = (Time)otherObject;
		 return other.year == this.year && other.month == this.month && other.day == this.day && other.hour == this.hour && other.minute == this.minute;
	}
}
