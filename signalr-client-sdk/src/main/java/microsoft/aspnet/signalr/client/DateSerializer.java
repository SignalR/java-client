/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    private static final int NO_MILLISECONDS_DATE_FORMAT_LENGTH = 25;//"yyyy-MM-ddTHH:mm:dd+00:00".length()

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
     * Normalize ISO-8601 formatted date to java format
     */
    public static String normalize(String strVal) {
        String s;

        if (strVal == null || strVal.length() < 19) {//"yyyy-MM-ddTHH:mm:dd".length()
            throw new JsonParseException("Invalid length for: " + strVal);
        }
        
        //normalize time zone
        if ((strVal.lastIndexOf('+') == strVal.length() - 6) ||//"+08:00".length()
                (strVal.lastIndexOf('-') == strVal.length() - 6)) {//"-08:00".length()
            s = strVal;
        } else if (strVal.endsWith("Z")) {
            // Change Z to +00:00 to adapt the string to a format
            // that can be parsed in Java
            s = strVal.substring(0, strVal.length() - 1) + "+00:00";//"Z".length()
        } else {
            s = strVal + "+00:00";
        }

        try {
            //normalize milliseconds
            final int len = s.length();
            if (len >= NO_MILLISECONDS_DATE_FORMAT_LENGTH) {
                if (len == NO_MILLISECONDS_DATE_FORMAT_LENGTH) {
                    // add dot and extra milliseconds characters
                    s = s.substring(0, 19) + ".000" + s.substring(19);
                } else if (len > THREE_MILLISECONDS_DATE_FORMAT_LENGTH) { // yyyy-MM-ddTHH:mm:dd.SSS+00:00
                    // remove the extra milliseconds characters
                    s = s.substring(0, 23) + s.substring(len - 6);
                } else if (len < THREE_MILLISECONDS_DATE_FORMAT_LENGTH) {
                    // add extra milliseconds characters
                    int dif = (THREE_MILLISECONDS_DATE_FORMAT_LENGTH - len);

                    String zeros = "";
                    for (int i = 0; i < dif; i++) {
                        zeros += "0";
                    }
                    s = s.substring(0, 20 + (3 - dif)) + zeros + s.substring(len - 6);
                }

                // Remove the ":" character to adapt the string to a
                // format
                // that can be parsed in Java
                s = s.substring(0, 26) + s.substring(27);
            } else {
                throw new JsonParseException("Invalid length for: " + s);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new JsonParseException("Invalid length for: " + s);
        }
        
        return s;
    }

    /**
     * Deserializes an ISO-8601 formatted date
     */
    public static Date deserialize(String strVal) throws ParseException {
        String s = normalize(strVal);
        
        // Parse the well-formatted date string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = dateFormat.parse(s);

        return date;
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
