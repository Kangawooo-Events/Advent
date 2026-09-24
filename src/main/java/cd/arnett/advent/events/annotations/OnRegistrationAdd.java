package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called after a player's registration is added. <br>
 * MUST have ONE PlayerProfile Parameter for the player being added
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnRegistrationAdd { }
