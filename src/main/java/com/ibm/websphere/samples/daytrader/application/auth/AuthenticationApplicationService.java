package com.ibm.websphere.samples.daytrader.application.auth;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.persistence.jdbc.KeySequenceJdbcRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.AccountDataJpaRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.AccountProfileJpaRepository;

@Service
public class AuthenticationApplicationService {

    private final AccountProfileJpaRepository accountProfileRepository;
    private final AccountDataJpaRepository accountRepository;
    private final KeySequenceJdbcRepository keySequenceRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationApplicationService(
            AccountProfileJpaRepository accountProfileRepository,
            AccountDataJpaRepository accountRepository,
            KeySequenceJdbcRepository keySequenceRepository,
            PasswordEncoder passwordEncoder) {
        this.accountProfileRepository = accountProfileRepository;
        this.accountRepository = accountRepository;
        this.keySequenceRepository = keySequenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AccountDataBean getAccountData(String userID) {
        AccountProfileDataBean profile = getRequiredProfile(userID);
        AccountDataBean account = profile.getAccount();
        account.setProfileID(profile.getUserID());
        return account;
    }

    @Transactional(readOnly = true)
    public AccountProfileDataBean getAccountProfileData(String userID) {
        return getRequiredProfile(userID);
    }

    @Transactional
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) {
        AccountProfileDataBean profile = getRequiredProfile(profileData.getUserID());
        profile.setAddress(profileData.getAddress());
        profile.setPassword(passwordEncoder.encode(profileData.getPassword()));
        profile.setFullName(profileData.getFullName());
        profile.setCreditCard(maskCreditCard(profileData.getCreditCard()));
        profile.setEmail(profileData.getEmail());
        return profile;
    }

    @Transactional
    public AccountDataBean login(String userID, String password) {
        AccountProfileDataBean profile = getRequiredProfile(userID);
        AccountDataBean account = profile.getAccount();
        if (!passwordMatches(profile, password)) {
            throw new IllegalArgumentException("Login failed for user " + userID);
        }
        if (isLegacyPlaintextPassword(profile)) {
            profile.setPassword(passwordEncoder.encode(password));
        }
        account.login(password, candidate -> true);
        account.setProfileID(profile.getUserID());
        return account;
    }

    @Transactional
    public void logout(String userID) {
        getRequiredProfile(userID).getAccount().logout();
    }

    @Transactional
    public AccountDataBean register(
            String userID,
            String password,
            String fullname,
            String address,
            String email,
            String creditcard,
            BigDecimal openBalance) {
        if (accountProfileRepository.existsById(userID)) {
            return null;
        }

        AccountProfileDataBean profile = new AccountProfileDataBean(
            userID,
            passwordEncoder.encode(password),
            fullname,
            address,
            email,
            maskCreditCard(creditcard));
        AccountDataBean account = new AccountDataBean(0, 0, null, new Timestamp(System.currentTimeMillis()), openBalance, openBalance, userID);
        account.setAccountID(keySequenceRepository.nextValue("account", 1));
        profile.setAccount(account);
        account.setProfile(profile);

        accountProfileRepository.save(profile);
        accountRepository.save(account);
        account.setProfileID(profile.getUserID());
        return account;
    }

    private AccountProfileDataBean getRequiredProfile(String userID) {
        return accountProfileRepository.findById(userID)
                .orElseThrow(() -> new IllegalStateException("No such user: " + userID));
    }

    private boolean passwordMatches(AccountProfileDataBean profile, String password) {
        String storedPassword = profile.getPassword();
        if (storedPassword == null) {
            return false;
        }
        if (isLegacyPlaintextPassword(profile)) {
            return storedPassword.equals(password);
        }
        return passwordEncoder.matches(password, storedPassword);
    }

    private boolean isLegacyPlaintextPassword(AccountProfileDataBean profile) {
        String storedPassword = profile.getPassword();
        return storedPassword != null && !storedPassword.startsWith("$2a$")
                && !storedPassword.startsWith("$2b$")
                && !storedPassword.startsWith("$2y$");
    }

    private String maskCreditCard(String creditCard) {
        if (creditCard == null || creditCard.isBlank()) {
            return "";
        }
        String digits = creditCard.replaceAll("\\s+", "");
        if (digits.length() <= 4) {
            return "****";
        }
        return "****-****-****-" + digits.substring(digits.length() - 4);
    }
}