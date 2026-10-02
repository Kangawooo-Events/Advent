package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called when a player joins the event (registration is seperate).
 * This is called specifically when the player has already left the previous event and has not yet been set
 * to be playing in the event they are joining, I.E. when they are in limbo<br>
 * Method MUST have ONE Player Parameter for the palyer joining
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnJoinEvent {

    JoinTaskPriority priority() default JoinTaskPriority.NORMAL;

}
