package cd.arnett.advent.events.annotations.data;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used Alongside ModuleDataProvider annotation to mark a method as providing a HashMap with string keys variable
 * for the event to store and save automatically.<br>
 * This takes another ModuleData annotation for the type of data stored as values in the map.<br>
 * The return of the method marked is used only for a default value and will be overwritten
 * if a value is already saved in the yaml
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface StringHashMapModuleData
{
    /**
     * @return Module Data annotation class type which will be used for the second type of element in the map.
     * I.E. passing BooleanModuleData.class will make this a Hashmap of String Keys and Booleans
     */
    Class<? extends Annotation> type();
}
