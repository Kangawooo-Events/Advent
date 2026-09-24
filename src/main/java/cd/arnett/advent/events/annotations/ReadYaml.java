package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks PUBLIC methods as ones that take a YamlConfiguration and read in data from it
 * Method MUST have Only ONE Parameter and that parameter must be able to take a YamlConfiguration
 * for the yaml section it's reading from
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadYaml {

}
