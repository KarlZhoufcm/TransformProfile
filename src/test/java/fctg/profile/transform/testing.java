package fctg.profile.transform;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class testing {

	private static String aaa;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(aaa);
		if (aaa == null) {
			System.out.println("is null");
		}

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");
		String time = now.format(f);
		System.out.println(time);
	}

}
