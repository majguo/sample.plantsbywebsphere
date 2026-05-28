package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.websphere.samples.daytrader.util.Log;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator("loginValidator")
public class LoginValidator implements Validator<Object> {

    private static final String LOGIN_REGEX = "uid:\\d+";
    private static final Pattern PATTERN = Pattern.compile(LOGIN_REGEX);

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String text = value == null ? "" : value.toString();
        Log.trace("LoginValidator.validate", "Validating submitted login name -- " + text);
        Matcher matcher = PATTERN.matcher(text);
        if (!matcher.matches()) {
            FacesMessage message = new FacesMessage(
                    "Username validation failed. Please provide username in this format: uid:#");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
}