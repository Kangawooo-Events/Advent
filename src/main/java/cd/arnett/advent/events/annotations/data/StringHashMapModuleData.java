package cd.arnett.advent.events.annotations.data;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used Alongside ModuleDataProvider annotation to mark a method as providing a HashMap with string keys variable
 * for the event to store and save automatically.<br>
 * This takes another ModuleData annotation for the type of data stored as values in the map.<br>
 * The return of the method marked is NOT USED, this is purely for declaring automatically saved data.
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface StringHashMapModuleData
{
    Class<? extends Annotation> value();
}
