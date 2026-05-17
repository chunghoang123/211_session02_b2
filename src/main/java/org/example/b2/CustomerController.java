package org.example.b2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final Map<Long, Customer> customers = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    static class Customer {
        private Long id;
        private String name;
        private String email;

        public Customer() {
        }

        public Customer(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        boolean emailExists = customers.values().stream()
                .anyMatch(c -> c.getEmail() != null && c.getEmail().equals(customer.getEmail()));

        if (emailExists) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        Long id = nextId.getAndIncrement();
        Customer createdCustomer = new Customer(id, customer.getName(), customer.getEmail());
        customers.put(id, createdCustomer);

        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customer) {
        Customer existingCustomer = customers.get(id);

        if (existingCustomer == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());

        return new ResponseEntity<>(existingCustomer, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return Optional.ofNullable(customers.get(id))
                .map(c -> new ResponseEntity<>(c, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
    