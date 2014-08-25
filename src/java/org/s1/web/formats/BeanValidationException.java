package org.s1.web.formats;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * @author Grigory Pykhov
 */
public class BeanValidationException extends Exception {

    private Set<ConstraintViolation<Object>> violations;

    /**
     *
     * @param violations ConstraintViolations
     */
    public BeanValidationException(Set<ConstraintViolation<Object>> violations) {
        super(getMessage(violations));
        this.violations = violations;
    }

    /**
     *
     * @return ConstraintViolations
     */
    public Set<ConstraintViolation<Object>> getViolations() {
        return violations;
    }

    private static String getMessage(Set<ConstraintViolation<Object>> violations){
        String message = "";
        message+="Got "+violations.size()+" violations:\n";
        int i=1;
        for(ConstraintViolation violation:violations){
            message+=(i++)+") "+violation.getPropertyPath()+" ["+violation.getInvalidValue()+"]: "+violation.getMessage()+"\n";
        }
        return message;
    }
}
