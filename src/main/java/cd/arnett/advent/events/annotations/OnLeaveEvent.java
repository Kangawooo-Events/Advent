package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called when a player leaves the event. <br>
 * Method MUST have ONE Player Parameter for the player leaving
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnLeaveEvent { }
