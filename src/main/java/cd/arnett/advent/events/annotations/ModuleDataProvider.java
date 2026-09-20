package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
/**
 * Marks a method as providing a data object to the event
 */
public @interface ModuleDataProvider
{
    /**
     * @return Yaml string key to store under
     */
    String value();
}
