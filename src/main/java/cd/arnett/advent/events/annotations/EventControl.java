package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as ones that provide a Cattamand control to the event
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventControl {

}
