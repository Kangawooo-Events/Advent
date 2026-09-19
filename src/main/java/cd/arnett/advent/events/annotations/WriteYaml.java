package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

/**
 * Marks methods as ones that return a YamlConfiguration with its data written to it
 */
public @interface WriteYaml { }
