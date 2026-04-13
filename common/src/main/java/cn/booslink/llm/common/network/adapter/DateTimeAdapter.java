package cn.booslink.llm.common.network.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

public class DateTimeAdapter implements JsonDeserializer<DateTime>, JsonSerializer<DateTime> {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            ISODateTimeFormat.dateTime(),
            ISODateTimeFormat.dateHourMinuteSecond(),
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormat.forPattern("yyyy-MM-dd")
    };

    @Override
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String dateString = json.getAsString();
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        // Try to parse with different date formats
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return formatter.parseDateTime(dateString);
            } catch (IllegalArgumentException e) {
                // Continue to next format
            }
        }
        // If all formats fail, try parsing as timestamp (long)
        try {
            long timestamp = Long.parseLong(dateString);
            return new DateTime(timestamp); // Convert seconds to milliseconds if needed
        } catch (NumberFormatException e) {
            throw new JsonParseException("Unable to parse date: " + dateString, e);
        }
    }

    @Override
    public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        // Serialize to ISO 8601 format
        return new JsonPrimitive(ISODateTimeFormat.dateTime().print(src));
    }
}
