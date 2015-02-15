/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.mocktransport;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import microsoft.aspnet.signalr.client.CalendarSerializer;
import microsoft.aspnet.signalr.client.DateSerializer;

import org.junit.Test;

import com.google.gson.JsonPrimitive;

public class CustomSerializationTests {

    @SuppressWarnings("deprecation")
    @Test
    public void testSerializeDate() throws Exception {

        int year = 1990;
        int month = 11;
        int day = 15;
        int hour = 10;
        int minute = 50;
        int seconds = 30;

        Date date = new Date(Date.UTC(year - 1900, month - 1, day, hour, minute, seconds));

        DateSerializer dateSerializer = new DateSerializer();

        String strDate = dateSerializer.serialize(date, Date.class, null).toString();

        // yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z
        String expectedDate = String.format("\"%s-%d-%sT%s:%s:%s.000Z\"", year, month, day, hour, minute, seconds);

        assertEquals(expectedDate, strDate);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeserializeDate() throws Exception {

        String strDate = "1990-11-15T10:50:30.123Z";

        DateSerializer dateSerializer = new DateSerializer();

        Date date = dateSerializer.deserialize(new JsonPrimitive(strDate), Date.class, null);

        int year = 1990;
        int month = 11;
        int day = 15;
        int hour = 10;
        int minute = 50;
        int seconds = 30;

        assertEquals(year - 1900, date.getYear());
        assertEquals(month - 1, date.getMonth());
        assertEquals(day, date.getDate());
        assertEquals(hour - date.getTimezoneOffset() / 60, date.getHours());
        assertEquals(minute, date.getMinutes());
        assertEquals(seconds, date.getSeconds());
    }

    @Test
    public void testSerializeCalendar() throws Exception {

        int year = 1990;
        int month = 11;
        int day = 15;
        int hour = 10;
        int minute = 50;
        int seconds = 30;
        int milliseconds = 123;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(year, month - 1, day, hour, minute, seconds);
        calendar.set(Calendar.MILLISECOND, milliseconds);

        CalendarSerializer calendarSerializer = new CalendarSerializer();

        String strDate = calendarSerializer.serialize(calendar, Calendar.class, null).toString();

        // yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z
        String expectedDate = String.format("\"%s-%d-%sT%s:%s:%s.%sZ\"", year, month, day, hour, minute, seconds, milliseconds);

        assertEquals(expectedDate, strDate);
    }

    @Test
    public void testDeserializeCalendar() throws Exception {

        String strDate = "1990-11-15T10:50:30.123Z";

        CalendarSerializer calendarSerializer = new CalendarSerializer();

        Calendar calendar = calendarSerializer.deserialize(new JsonPrimitive(strDate), Calendar.class, null);

        int year = 1990;
        int month = 11;
        int day = 15;
        int hour = 10;
        int minute = 50;
        int seconds = 30;
        int milliseconds = 123;

        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month - 1, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
        assertEquals(seconds, calendar.get(Calendar.SECOND));
        assertEquals(milliseconds, calendar.get(Calendar.MILLISECOND));
    }
    
    @Test
    public void testTimeParse() throws Exception {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2014, 3 - 1, 29, 12, 34, 56);

        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56."));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56Z"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.Z"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56+00:00"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.+00:00"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T04:04:56-08:30"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T04:04:56.-08:30"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T21:04:56+08:30"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T21:04:56.+08:30"));

        calendar.set(Calendar.MILLISECOND, 700);
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.7"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.7Z"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.7+00:00"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T04:04:56.7-08:30"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T21:04:56.7+08:30"));

        calendar.set(Calendar.MILLISECOND, 780);
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.78"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.78Z"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.78+00:00"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T04:04:56.78-08:30"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T21:04:56.78+08:30"));

        calendar.set(Calendar.MILLISECOND, 789);
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.789"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.789Z"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.789+00:00"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T04:04:56.789-08:30"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T21:04:56.789+08:30"));

        calendar.set(Calendar.MILLISECOND, 789);
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.7891"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.7891Z"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T12:34:56.7891+00:00"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T04:04:56.7891-08:30"));
        assertEquals(calendar.getTime(), DateSerializer.deserialize("2014-03-29T21:04:56.7891+08:30"));
    }
}
