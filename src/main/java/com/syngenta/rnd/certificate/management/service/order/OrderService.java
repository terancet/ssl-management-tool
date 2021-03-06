package com.syngenta.rnd.certificate.management.service.order;

import com.syngenta.rnd.certificate.management.service.keypair.KeyPairService;
import com.syngenta.rnd.certificate.management.dao.OrderDao;
import com.syngenta.rnd.certificate.management.service.polling.PollingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.util.CSRBuilder;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final KeyPairService keyPairService;
    private final OrderDao orderDao;
    private final PollingStrategy<Order> orderPollingStrategy;

    @SneakyThrows
    public Order createAndStoreOrder(String userName, Account account, Set<String> domains) {
        Order order = createOrder(account, domains);
        orderDao.persistOrder(userName, domains, order);
        return order;
    }

    @SneakyThrows
    public void pollOrder(Order order, Collection<String> domains) {
        KeyPair domainKeyPair = keyPairService.createKeyPair();
        CSRBuilder csrb = createSignInRequest(domains, domainKeyPair);
        byte[] encoded = csrb.getEncoded();
        order.execute(encoded);
        orderPollingStrategy.doPoll(order);
    }

    @SneakyThrows
    private static CSRBuilder createSignInRequest(Collection<String> domains, KeyPair domainKeyPair) {
        CSRBuilder certificateSignInRequest = new CSRBuilder();
        certificateSignInRequest.addDomains(domains);
        certificateSignInRequest.sign(domainKeyPair);
        return certificateSignInRequest;
    }

    @SneakyThrows
    private static Order createOrder(Account account, Set<String> domains) {
        return account.newOrder()
                .domains(domains)
                .create();
    }
}
