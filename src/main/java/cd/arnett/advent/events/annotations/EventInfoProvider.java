package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a PUBLIC method as providing info for an event.<br>
 *  Method MUST have NO parameters and a return of HashMap(String, Object) where the key is the title
 *  of the info entry and the object is info itself which will be converted to string later
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventInfoProvider
{
    /**
     * @return name of the info section
     */
    String name();

    /**
     * @return is this section long enough that it should only be in its own command
     */
    boolean separate() default false;
}
