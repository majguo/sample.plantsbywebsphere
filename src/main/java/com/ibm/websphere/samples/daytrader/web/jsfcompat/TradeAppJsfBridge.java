package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.web.mvc.CompatibilitySessionFacade;

import jakarta.servlet.ServletException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Component("tradeapp")
@SessionScope
public class TradeAppJsfBridge extends JsfFacesSupport implements Serializable {

    private static final long serialVersionUID = 2L;

    private final TradeServices tradeServices;
    private final CompatibilitySessionFacade sessionFacade;

    @NotBlank
    private String userID = "uid:0";

    @NotBlank
    private String password = "";

    @NotBlank
    private String cpassword;

    private String results;

    @NotBlank
    private String fullname;

    @NotBlank
    private String address;

    @Email
    private String email;

    @NotBlank
    private String ccn;

    @NotBlank
    private String money;

    public TradeAppJsfBridge(TradeServices tradeServices, CompatibilitySessionFacade sessionFacade) {
        this.tradeServices = tradeServices;
        this.sessionFacade = sessionFacade;
    }

    public String login() {
        try {
            AccountDataBean accountData = tradeServices.login(userID, password);
            AccountProfileDataBean accountProfileData = tradeServices.getAccountProfileData(userID);
            if (accountData != null) {
                sessionFacade.establishSession(request(), userID);
                setResults("Ready to Trade");
                setAddress(accountProfileData.getAddress());
                setCcn(accountProfileData.getMaskedCreditCard());
                setEmail(accountProfileData.getEmail());
                setFullname(accountProfileData.getFullName());
                setPassword("");
                setCpassword("");
                return "Ready to Trade";
            }
            Log.log("TradeAppJsfBridge.login(...)", "Error finding account for user " + userID,
                    "user entered a bad username or the database is not populated");
        } catch (Exception exception) {
            Log.error(exception, "TradeAppJsfBridge.login():", "Error logging in " + userID, "forwarding to welcome page");
        }

        setResults("Could not find account");
        return "welcome";
    }

    public String register() {
        try {
            if (!password.equals(cpassword) || password.length() < 1) {
                setResults("Registration operation failed, your passwords did not match");
                return "Registration operation failed";
            }

            AccountDataBean accountData = tradeServices.register(userID, password, fullname, address, email, ccn, new BigDecimal(money));
            if (accountData == null) {
                setResults("Registration operation failed;");
                return "Registration operation failed";
            }

            login();
            setResults("Registration operation succeeded;  Account " + accountData.getAccountID() + " has been created.");
            return "Registration operation succeeded";
        } catch (Exception exception) {
            Log.error(exception, "TradeAppJsfBridge.register():", "Error registering user " + userID, "forwarding to register page");
            setResults("Registration operation failed;");
            return "Registration operation failed";
        }
    }

    public String updateProfile() {
        boolean doUpdate = true;
        if (!password.equals(cpassword)) {
            results = "Update profile error: passwords do not match";
            doUpdate = false;
        }

        AccountProfileDataBean accountProfileData = new AccountProfileDataBean(userID, password, fullname, address, email, ccn);
        try {
            if (doUpdate) {
                tradeServices.updateAccountProfile(accountProfileData);
                results = "Account profile update successful";
            }
        } catch (IllegalArgumentException exception) {
            setResults("invalid argument, check userID is correct, and the database is populated" + userID);
            Log.error(exception, "TradeAppJsfBridge.updateProfile(...)",
                    "illegal argument, information should be in exception string",
                    "treating this as a user error and forwarding to account page");
        } catch (Exception exception) {
            Log.error(exception, "TradeAppJsfBridge.updateProfile():", "Error updating profile " + userID,
                    "forwarding to account page");
        }
        return "Go to account";
    }

    public String logout() {
        try {
            setResults("");
            tradeServices.logout(userID);
        } catch (IllegalArgumentException exception) {
            setResults("illegal argument:" + exception.getMessage());
            Log.error(exception, "TradeAppJsfBridge.logout(...)",
                    "illegal argument, information should be in exception string",
                    "treating this as a user error and forwarding to welcome page");
        } catch (Exception exception) {
            Log.error(exception, "TradeAppJsfBridge.logout():", "Error logging out " + userID,
                    "forwarding to welcome page");
        }

        sessionFacade.invalidateSession(request());
        try {
            request().logout();
        } catch (ServletException exception) {
            Log.error(exception, "TradeAppJsfBridge.logout():", "Error logging out request " + userID,
                    "forwarding to welcome page");
        }
        return "welcome";
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCpassword() {
        return cpassword;
    }

    public void setCpassword(String cpassword) {
        this.cpassword = cpassword;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getResults() {
        String currentResults = results;
        results = "";
        return currentResults;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCcn() {
        return ccn;
    }

    public void setCcn(String ccn) {
        this.ccn = ccn;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}