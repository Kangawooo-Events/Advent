package cd.arnett.advent.events.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TrackedPlayerLists.class)
/**
 * Marks ArrayList<UUID> fields to be saved and written to automatically as part of the yaml
 */
public @interface TrackedPlayerList
{
    /**
     * @return Yaml string key to store under
     */
    String value();
}
