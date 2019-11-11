import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a method for whom we expect groovy's dynamic dispatching to apply.
 * Such group of methods has the same name but different signature so Groovy chooses the best of them in run-time.
 * Do not rename such methods randomly.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@interface DynamicDispatch {
}