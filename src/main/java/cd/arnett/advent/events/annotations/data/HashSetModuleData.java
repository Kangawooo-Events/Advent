package cd.arnett.advent.events.annotations.data;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used Alongside ModuleDataProvider annotation to mark a method as providing a HashSet variable
 * for the event to store and save automatically.<br>
 * This takes another ModuleData annotation for the type of data stored in the set.<br>
 * The return of the method marked is NOT USED, this is purely for declaring automatically saved data.
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface HashSetModuleData
{
    Class<? extends Annotation> value();
}
