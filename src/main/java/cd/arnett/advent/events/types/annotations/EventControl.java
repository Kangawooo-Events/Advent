package cd.arnett.advent.events.types.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

/**
 * Marks methods as ones that provide Cattamand controls to the event
 */
public @interface EventControl { }
