package cd.arnett.advent.events.annotations.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
/**
 * Marks a method as providing a Player List object to the event, the Base event automatically
 * handles the writing and reading of yaml from there<br>
 *
 * Notes the type ArrayList<UUID>
 */
public @interface PlayerIdArrayList
{

}
