package cd.arnett.advent.events.annotations.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used Alongside ModuleDataProvider annotation to mark a method as providing a UUID variable
 * for the event to store and save automatically.<br>
 * The return of the method marked is NOT USED, this is purely for declaring automatically saved data.
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface UUIDModuleData {

}
