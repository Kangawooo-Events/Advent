package cd.arnett.advent.events.annotations.data;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

public @interface UUIDHashMapModuleData
{
    Class<? extends Annotation> value();
}
