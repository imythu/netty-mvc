package annotation;

import java.lang.annotation.*;

/**
 * @author myth
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MythRequestMapping {
    String value() default "";
}
