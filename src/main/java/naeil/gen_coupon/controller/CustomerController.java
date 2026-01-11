package naeil.gen_coupon.controller;

import naeil.gen_coupon.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{customerId}/detail")
    public ResponseEntity<?> getCustomerDetail(@PathVariable Integer customerId) {
        return ResponseEntity.ok().body(customerService.getCustomerDetail(customerId));
    }
}
