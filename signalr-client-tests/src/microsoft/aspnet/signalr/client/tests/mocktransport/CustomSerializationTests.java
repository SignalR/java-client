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

        assertNotNull(DateSerializer.deserialize("2014-03-29T00:00:00"));
        assertNotNull(DateSerializer.deserialize("2014-03-29T00:00:00Z"));
        assertNotNull(DateSerializer.deserialize("2014-03-29T00:00:00+00:00"));
        assertNotNull(DateSerializer.deserialize("2014-03-29T00:00:00.Z"));
    }
}
