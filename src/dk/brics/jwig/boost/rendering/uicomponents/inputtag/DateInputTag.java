package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateInputTag extends InputTag {

    /**
     * Date input tag without a relation to another object
     *
     * @see InputTag#InputTag(String, String, Object, dk.brics.jwig.boost.rendering.uicomponents.Attribute...)
     */
    public DateInputTag(String variableName, HTML5Date value,
                        Attribute... attributes) {
        this(variableName, value, null, attributes);
    }

    /**
     * Date input tag with a relation to another object
     *
     * @see InputTag#InputTag(String, String, Object, Object, Attribute...)
     */
    public DateInputTag(String variableName, HTML5Date value, Object relator,
                        Attribute... attributes) {
        super("date", variableName,
                value, relator, Attribute.addClass(attributes, "jwb_date"));
    }

    /**
     * A java.util.Calendar instance wrapped in a serializer/deserializer for HTML5 wire formats.
     */
    public static class HTML5Date {

        private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"/* HTML5 Date format*/);
        private Calendar calendar;

        /**
         * Constructor, calendar may be null.
         */
        public HTML5Date(Calendar calendar) {
            this.calendar = calendar;
        }

        private static java.util.Date parse(String string) throws ParseException {
            return dateFormat.parse(string);
        }

        public static HTML5Date valueOf(String v) {
            Calendar calendar;
            try {
                Date date = parse(v);
                calendar = Calendar.getInstance();
                calendar.setTime(date);
                return new HTML5Date(calendar);
            } catch (ParseException e) {
                calendar = null;
            }
            return new HTML5Date(calendar);
        }

        public Calendar getCalendar() {
            return calendar;
        }

        @Override
        public String toString() {
            return calendar == null ? "" : dateFormat.format(calendar.getTime());
        }
    }
}
