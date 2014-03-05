/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CalendarSerializer implements JsonSerializer<Calendar>, JsonDeserializer<Calendar> {

    private static final DateSerializer mInternalSerializer = new DateSerializer();

    @Override
    public Calendar deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        Date date = mInternalSerializer.deserialize(element, Date.class, ctx);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    @Override
    public JsonElement serialize(Calendar calendar, Type type, JsonSerializationContext ctx) {
        return mInternalSerializer.serialize(calendar.getTime(), Date.class, ctx);
    }

}
