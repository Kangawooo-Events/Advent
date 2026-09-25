package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called when a player joins the event (registration is seperate)<br>
 * Method MUST have ONE JoinEventData Parameter for the join data
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnJoinEvent { }
