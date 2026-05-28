package com.ibm.websphere.samples.daytrader.application.auth;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.stereotype.Service;
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

    public AuthenticationApplicationService(
            AccountProfileJpaRepository accountProfileRepository,
            AccountDataJpaRepository accountRepository,
            KeySequenceJdbcRepository keySequenceRepository) {
        this.accountProfileRepository = accountProfileRepository;
        this.accountRepository = accountRepository;
        this.keySequenceRepository = keySequenceRepository;
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
        profile.setPassword(profileData.getPassword());
        profile.setFullName(profileData.getFullName());
        profile.setCreditCard(profileData.getCreditCard());
        profile.setEmail(profileData.getEmail());
        return profile;
    }

    @Transactional
    public AccountDataBean login(String userID, String password) {
        AccountProfileDataBean profile = getRequiredProfile(userID);
        AccountDataBean account = profile.getAccount();
        account.login(password);
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

        AccountProfileDataBean profile = new AccountProfileDataBean(userID, password, fullname, address, email, creditcard);
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
}