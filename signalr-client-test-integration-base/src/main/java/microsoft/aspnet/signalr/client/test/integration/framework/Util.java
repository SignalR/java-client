/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.framework;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;


public class Util {
	public static String createSimpleRandomString(Random rndGen, int size) {
		int minChar = ' ';
		int maxChar = '~';

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			int charRand = rndGen.nextInt(maxChar - minChar);
			sb.append((char) (minChar + charRand));
		}

		return sb.toString();
	}

	public static <E> boolean compareLists(List<E> l1, List<E> l2) {
		return compareArrays(l1.toArray(), l2.toArray());
	}

	public static boolean compareArrays(Object[] arr1, Object[] arr2) {
		if (arr1 == null && arr2 == null) {
			return true;
		}

		if (arr1 == null || arr2 == null) {
			return false;
		}

		if (arr1.length != arr2.length) {
			return false;
		}

		for (int i = 0; i < arr1.length; i++) {
			Object o1 = arr1[i];
			Object o2 = arr2[i];

			if (!compare(o1, o2)) {
				return false;
			}
		}
		return true;
	}

	public static <E> String listToString(List<E> list) {
		return arrayToString(list.toArray());
	}

	public static String arrayToString(Object[] arr) {
		if (arr == null) {
			return "<<NULL>>";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("[");

			for (int i = 0; i < arr.length; i++) {
				Object elem = arr[i];
				sb.append(elem.toString());

				if (i != arr.length - 1) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}
	}

	public static String dateToString(Date date) {
		if (date == null) {
			return "NULL";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'", Locale.getDefault());
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		String formatted = dateFormat.format(date);

		return formatted;
	}

	public static boolean compare(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		}

		if (o1 == null || o2 == null) {
			return false;
		}

		return o1.equals(o2);
	}

	public static Date getUTCDate(int year, int month, int day) {

		return getUTCDate(year, month, day, 0, 0, 0);
	}

	public static Date getUTCDate(int year, int month, int day, int hour, int minute, int second) {
		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("utc"));
		int dateMonth = month - 1;
		calendar.set(year, dateMonth, day, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime();
	}

	public static Calendar getUTCCalendar(Date date) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("utc"), Locale.getDefault());
		cal.setTime(date);

		return cal;
	}
}
