package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

/**
 * Marks methods as ones that take a YamlConfiguration and read in data from it
 */
public @interface ReadYaml {

}
