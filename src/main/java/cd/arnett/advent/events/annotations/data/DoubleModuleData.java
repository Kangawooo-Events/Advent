package cd.arnett.advent.events.annotations.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used Alongside ModuleDataProvider annotation to mark a method as providing a double variable
 * for the event to store and save automatically.<br>
 * The return of the method marked is used only for a default value and will be overwritten
 * if a value is already saved in the yaml
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleModuleData {

}
