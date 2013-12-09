package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeInputTag extends InputTag {
    /**
     * HTML5DateTime input tag without a relation to another object
     *
     * @see InputTag#InputTag(String, String, Object, dk.brics.jwig.boost.rendering.uicomponents.Attribute...)
     */
    public DateTimeInputTag(String variableName, HTML5DateTime value,
                            Attribute... attributes) {
        this(variableName, value, null, attributes);
    }


    /**
     * HTML5DateTime input tag with a relation to another object
     *
     * @see InputTag#InputTag(String, String, Object, Object, Attribute...)
     */
    public DateTimeInputTag(String variableName, HTML5DateTime value, Object relator,
                            Attribute... attributes) {
        super("datetime-local", variableName,
                value
                , relator, Attribute.addClass(attributes, "jwb_datetime"));
    }

    /**
     * A java.util.Calendar instance wrapped in a serializer/deserializer for HTML5 wire formats.
     */
    public static class HTML5DateTime {

        private static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"/* HTML5DateTime format*/);
        private static DateFormat dateTimeFormat_space = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"/* HTML5DateTime-like format, compatibility with other formats */);
        private static DateFormat dateTimeFormat_noSeconds = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"/* HTML5DateTime format without seconds*/);
        private static DateFormat dateTimeFormat_noSeconds_space = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm"/* HTML5DateTime-like format without seconds, compatibility with other formats */);
        private static DateFormat defaultOutputDateTimeFormat = dateTimeFormat_noSeconds; // minute resolution
        private final Calendar calendar;

        /**
         * Constructor, calendar may be null.
         */
        public HTML5DateTime(Calendar calendar) {
            this.calendar = calendar;
        }

        private static java.util.Date parse(String string) {
            DateFormat[] formats = new DateFormat[]{dateTimeFormat, dateTimeFormat_noSeconds, dateTimeFormat_space, dateTimeFormat_noSeconds_space};
            // try all formats
            for (DateFormat f : formats) {
                try {
                    return f.parse(string);
                } catch (ParseException e) {
                    continue;
                }
            }
            // no early returns: no successful parsings
            return null;
        }

        public static HTML5DateTime valueOf(String v) {
            final Calendar calendar;
            Date date = parse(v);
            if (date != null) {
                calendar = Calendar.getInstance();
                calendar.setTime(date);
            }else{
                calendar = null;
            }
            return new HTML5DateTime(calendar);
        }

        public Calendar getCalendar() {
            return calendar;
        }

        @Override
        public String toString() {
            return calendar == null ? "" : defaultOutputDateTimeFormat.format(calendar.getTime());
        }

    }
}

