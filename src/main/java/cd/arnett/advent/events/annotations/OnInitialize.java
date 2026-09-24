package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

/**
 * Marks PUBLIC methods as one which will be called when the event object is initialized
 * (before registration with the EventManager).<br>
 * Method MUST have NO parameters
 */
public @interface OnInitialize { }
