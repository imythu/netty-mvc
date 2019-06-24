package annotation;

import java.lang.annotation.*;

/**
 * @author myth
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MythService {
    String value() default "";
}
