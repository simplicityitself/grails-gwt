package org.codehaus.groovy.grails.plugins.gwt

import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator
import org.hibernate.validator.engine.ConstraintViolationImpl
import org.hibernate.validator.engine.PathImpl
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

import javax.validation.ConstraintViolation
import javax.validation.Path
import java.lang.annotation.ElementType

class RfValidationService extends ServiceLayerDecorator {

    static transactional = false

    def messageSource

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T domainObject) {
        try {
            if (!domainObject.validate()) {
                List<ConstraintViolation<T>> violations = []
                Errors errors = domainObject.errors
                Locale locale = resolveLocale()
                for (FieldError fieldError : errors.fieldErrors) {
                    ConstraintViolation<T> violation = convertFieldError(fieldError, locale, domainObject)
                    violations << violation
                }
                return violations
            }
        } catch (Exception e) {
            // Nothing to do.. validate method does not exists. Thats ok.
        }
        return super.validate(domainObject)
    }

    private <T> ConstraintViolation<T> convertFieldError(FieldError fieldError, Locale locale, T domainObject) {
        String messageTemplate = fieldError.codes?.length > 0 ? fieldError.codes[0] : fieldError.code
        String interpolatedMessage = messageSource.getMessage(fieldError, locale)
        T rootBean = domainObject
        Path path = PathImpl.createPathFromString(fieldError.field)
        Object invalidValue = fieldError.rejectedValue
        Class<T> rootBeanClass = domainObject.class
        ConstraintViolation<T> violation = new ConstraintViolationImpl<T>(messageTemplate, interpolatedMessage,
                rootBeanClass, rootBean, domainObject,
                invalidValue, path, null, ElementType.FIELD)
        return violation
    }

    private Locale resolveLocale() {
        return (Locale) RequestContextHolder.requestAttributes.getAttribute(GwtRequestController.GWT_LANGUAGE,
                RequestAttributes.SCOPE_REQUEST)
    }

}