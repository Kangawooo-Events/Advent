package cd.arnett.advent.events.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface TrackedPlayerLists
{
    TrackedPlayerList[] value();
}
