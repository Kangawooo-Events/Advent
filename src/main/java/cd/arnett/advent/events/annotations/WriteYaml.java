package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as ones that return a YamlConfiguration with its data written to it
 * Method MUST have a return type of YamlConfiguration to send back data which was saved
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteYaml {

}
