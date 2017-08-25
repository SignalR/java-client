/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Date Serializer/Deserializer to make .NET and Java dates compatible
 */
public class DateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {

    private static final int THREE_MILLISECONDS_DATE_FORMAT_LENGTH = 29;

    /**
     * Deserializes a JsonElement containing an ISO-8601 formatted date
     */
    @Override
    public Date deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        String strVal = element.getAsString();

        try {
            return deserialize(strVal);
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }

    /**
     * Serializes a Date to a JsonElement containing a ISO-8601 formatted date
     */
    @Override
    public JsonElement serialize(Date date, Type type, JsonSerializationContext ctx) {
        JsonElement element = new JsonPrimitive(serialize(date));
        return element;
    }

    /**
     * Deserializes an ISO-8601 formatted date
     */
    public static Date deserialize(String strVal) throws ParseException {
        
        Calendar c = DatatypeConverter.parseDateTime(strVal);
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        return c.getTime();
    }

    /**
     * Serializes a Date object to an ISO-8601 formatted date string
     */
    public static String serialize(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String formatted = dateFormat.format(date);

        return formatted;
    }

}
