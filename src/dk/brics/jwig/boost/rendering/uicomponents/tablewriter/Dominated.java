package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking a table feature dominated. Apply the annotation to {@link TableFeature#getLine}
 *
 * TODO: Replace by interface to avoid Java gotchas in TableWriter
 * @author schwarz
 * @created 30-09-2009 17:59:15
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Dominated {
    //
}
