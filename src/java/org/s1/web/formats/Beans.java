package org.s1.web.formats;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * Bean helper methods
 *
 * @author Grigory Pykhov
 */
public class Beans {

    private Beans(){}

    private static final ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = vf.getValidator();

    /**
     *
     * @return Validator
     */
    public static Validator getValidator(){
        return validator;
    }

    /**
     *
     * @param object Bean to check
     * @throws BeanValidationException If got ConstraintViolations
     */
    public static void validate(Object object) throws BeanValidationException{
        Set<ConstraintViolation<Object>> constraintViolations = validator
                .validate(object);
        if(constraintViolations.size()>0){
            throw new BeanValidationException(constraintViolations);
        }
    }

}
