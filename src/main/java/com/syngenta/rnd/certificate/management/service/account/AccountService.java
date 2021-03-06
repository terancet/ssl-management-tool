package com.syngenta.rnd.certificate.management.service.account;

import com.syngenta.rnd.certificate.management.dao.UserRepository;
import com.syngenta.rnd.certificate.management.dao.UserRoleRepository;
import com.syngenta.rnd.certificate.management.model.dto.UserRegistrationRequest;
import com.syngenta.rnd.certificate.management.model.entity.UserEntity;
import com.syngenta.rnd.certificate.management.model.entity.UserRoleEntity;
import com.syngenta.rnd.certificate.management.service.keypair.KeyPairService;
import com.syngenta.rnd.certificate.management.service.security.JWTAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Session;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final KeyPairService keyPairService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTAuthenticationService jwtAuthenticationService;

    @Transactional
    public String saveUserInformation(UserRegistrationRequest userRegistrationRequest) {
        ByteArrayOutputStream keyPairOutPutStream = keyPairService.createKeyPairAsByteOutputStream();
        UserEntity userEntity = createUserEntity(userRegistrationRequest, keyPairOutPutStream);
        userEntity.setUserPassword(passwordEncoder.encode(userRegistrationRequest.getPassword()));
        userRepository.save(userEntity);
        return jwtAuthenticationService.generateTokenForRegister(userRegistrationRequest);
    }

    public Account initializeLetsEncryptAccount(String userName) {
        return userRepository.findByUserName(userName)
                .map(this::toLetsEncryptAccount)
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find registered user for user name %s", userName)));
    }

    @SneakyThrows
    private Account toLetsEncryptAccount(UserEntity userEntity) {
        KeyPair keyPair = keyPairService.loadKeyPair(userEntity.getKeyPair());
        Session session = new Session("acme://letsencrypt.org/staging");
        Account account = createAccount(keyPair, session);
        log.info("Registered a new user, URL: {}", account.getLocation());
        return account;
    }

    @SneakyThrows
    private Account createAccount(KeyPair keyPair, Session session) {
        return new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(keyPair)
                .create(session);
    }

    private UserEntity createUserEntity(UserRegistrationRequest userRegistrationRequest, ByteArrayOutputStream byteArrayOutputStream) {
        UserEntity userEntity = new UserEntity();
        String userRole = userRegistrationRequest.getUserRole();
        String keyPairAsString = new String(byteArrayOutputStream.toByteArray());
        userEntity.setUserName(userRegistrationRequest.getUserName());
        userEntity.setUserPassword(userRegistrationRequest.getPassword());
        UserRoleEntity userRoleEntity = userRoleRepository.findByUserRole(userRole)
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find the given user role '%s'", userRole)));
        userEntity.setUserRoleEntity(userRoleEntity);
        userEntity.setKeyPair(keyPairAsString);
        return userEntity;
    }

    public String loginUser(UserRegistrationRequest userRegistrationRequest) {
        userRepository.findByUserName(userRegistrationRequest.getUserName())
                .orElseThrow(() -> new RuntimeException("Cannot find user"));
        return jwtAuthenticationService.generateTokenForLogin(userRegistrationRequest);
    }
}
