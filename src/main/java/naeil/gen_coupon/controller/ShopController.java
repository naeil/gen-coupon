package naeil.gen_coupon.controller;

import naeil.gen_coupon.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping
    public ResponseEntity<?> getShopList(){
        return ResponseEntity.ok().body(shopService.getShopList());
    }
}
