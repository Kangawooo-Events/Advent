package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as one which will be called when a player leaves the specified persistence module's persistence
 * (they are already set as absent by the time this is called). <br>
 * The Module itself must be tracked by the event tracker as a persistent module using registerPersistentModuleType()
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnLeavePersistence {

}
