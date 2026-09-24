package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a PUBLIC method as providing a data object to the event.<br>
 *  Method MUST have NO parameters, An accoumpaning ModuleData annotation specifying the type,
 *  and a return type the same as the specified data type using the ModuleData annotations.
 *  This data can be accessed in the Base Event's getModuleData(String name) function as a generic object.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDataProvider
{
    /**
     * @return Yaml string key to store under
     */
    String value();
}
