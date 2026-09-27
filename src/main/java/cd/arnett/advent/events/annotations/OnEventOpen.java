package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called when the event opens.<br>
 *  Method MUST have NO parameters
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnEventOpen { }
