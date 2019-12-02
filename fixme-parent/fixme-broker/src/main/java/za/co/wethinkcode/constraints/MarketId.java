package za.co.wethinkcode.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = {MarketIdValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface MarketId {
    String message() default "MarketId must be a valid market and must be 6 alphanumeric uppercase characters long. ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
