package com.ibm.websphere.samples.daytrader.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.persistence.jdbc.KeySequenceJdbcRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.AccountDataJpaRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.AccountProfileJpaRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationApplicationServiceTest {

    @Mock
    private AccountProfileJpaRepository accountProfileRepository;

    @Mock
    private AccountDataJpaRepository accountRepository;

    @Mock
    private KeySequenceJdbcRepository keySequenceRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void registerHashesPasswordsAndMasksCreditCardsBeforePersisting() {
        when(accountProfileRepository.existsById("uid:new")).thenReturn(false);
        when(keySequenceRepository.nextValue("account", 1)).thenReturn(100);

        AuthenticationApplicationService service = new AuthenticationApplicationService(
                accountProfileRepository,
                accountRepository,
                keySequenceRepository,
                passwordEncoder);

        AccountDataBean account = service.register(
                "uid:new",
                "secret",
                "User New",
                "addr",
                "mail@example.com",
                "4111111111111111",
                BigDecimal.TEN);

        AccountProfileDataBean profile = account.getProfile();
        assertNotEquals("secret", profile.getPassword());
        assertTrue(passwordEncoder.matches("secret", profile.getPassword()));
        assertEquals("****-****-****-1111", profile.getCreditCard());
        verify(accountProfileRepository).save(profile);
        verify(accountRepository).save(account);
    }

    @Test
    void loginUpgradesLegacyPlaintextPasswordsToHashes() {
        AccountProfileDataBean profile = new AccountProfileDataBean("uid:1", "secret", "User One", "addr", "mail@example.com", "****");
        AccountDataBean account = new AccountDataBean(100, 1, 0, null, null, BigDecimal.TEN, BigDecimal.TEN, "uid:1");
        profile.setAccount(account);
        account.setProfile(profile);
        when(accountProfileRepository.findById("uid:1")).thenReturn(Optional.of(profile));

        AuthenticationApplicationService service = new AuthenticationApplicationService(
                accountProfileRepository,
                accountRepository,
                keySequenceRepository,
                passwordEncoder);

        service.login("uid:1", "secret");

        assertNotEquals("secret", profile.getPassword());
        assertTrue(passwordEncoder.matches("secret", profile.getPassword()));
    }
}