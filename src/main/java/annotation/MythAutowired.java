package annotation;

import java.lang.annotation.*;

/**
 * @author myth
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MythAutowired {
    String value() default "";
}
