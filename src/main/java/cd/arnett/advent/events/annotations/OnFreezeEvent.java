package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called when an event is frozen<br>
 * Method MUST have take NO parameters
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnFreezeEvent { }
