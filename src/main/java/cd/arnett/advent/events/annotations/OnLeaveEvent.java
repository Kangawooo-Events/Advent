package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called when a player leaves the event
 * (they are already set as absent by the time this is called). <br>
 * Method MUST have ONE EventLeaveData Parameter for data related to the player leaving
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnLeaveEvent { }
