package cd.arnett.advent.events.annotations.data;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used Alongside ModuleDataProvider annotation to mark a method as providing a HashSet variable
 * for the event to store and save automatically.<br>
 * This takes another ModuleData annotation for the type of data stored in the set.<br>
 * The return of the method marked is used only for a default value and will be overwritten
 * if a value is already saved in the yaml
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface HashSetModuleData
{

    /**
     * @return Module Data annotation calss type which will be used for the type of element in the set.
     * I.E. passing BooleanModuleData.class will make this a HashSet of booleans
     */
    Class<? extends Annotation> type();
}
