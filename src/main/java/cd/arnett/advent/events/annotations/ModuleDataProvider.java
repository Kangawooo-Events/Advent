package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used Alongside ModuleDataProvider annotation to mark a method as providing a boolean variable
 * for the event to store and save automatically.<br>
 * The return of the method marked is used only for a default value and will be overwritten
 * if a value is already saved in the yaml
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDataProvider {

    /**
     * @return The string key this will be stored under, MUST be Yaml compatible
     */
    String key();

    /**
     * @return name of the Yaml File this will be store under (will be created if not present).
     * Do NOT include the .yaml
     */
    String fileBasename() default "event";
}
