package cd.arnett.advent.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a PUBLIC method as providing info for an event.<br>
 *  Method MUST have NO parameters
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
    boolean separate();
}
