package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called when a player's registration is removed.<br>
 * Method MUST have ONE PlayerProfile Parameter for the player being removed
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnRegistrationRemove { }
