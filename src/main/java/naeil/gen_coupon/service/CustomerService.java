package naeil.gen_coupon.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.response.CustomerDTO;
import naeil.gen_coupon.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;

    public List<CustomerDTO> findAll() {
        List<CustomerDTO> customers = customerRepository.findAll().stream()
            .map(CustomerDTO::toDTO)
            .toList();
        return customers;
    }    
}
