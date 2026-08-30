package com.eshu.OnlineShopping.controllers;

import com.eshu.OnlineShopping.dto.BuyNowDto;
import com.eshu.OnlineShopping.dto.CheckoutDto;
import com.eshu.OnlineShopping.model.Order;
import com.eshu.OnlineShopping.security.AuthenticatedAccountResolver;
import com.eshu.OnlineShopping.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gated to ROLE_USER or ROLE_SELLER by SecurityConfig - a seller's own
 * login can shop too. Every method resolves "which buyer" strictly from
 * the verified JWT via AuthenticatedAccountResolver#getCurrentShoppingUserId
 * - never from a client-supplied id - matching the pattern already used
 * for /cart/apis and /seller/apis.
 */
@RestController
@RequestMapping("/order/apis")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    AuthenticatedAccountResolver authenticatedAccountResolver;

    /** Places an order for everything currently in the caller's cart, then empties it. */
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody CheckoutDto checkoutDto){
        Order order = orderService.placeOrderFromCart(
                authenticatedAccountResolver.getCurrentShoppingUserId(),
                checkoutDto.getPaymentMode());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /** Places a single-product order directly, bypassing the cart entirely. */
    @PostMapping("/buyNow")
    public ResponseEntity<Order> buyNow(@RequestBody BuyNowDto buyNowDto){
        Order order = orderService.buyNow(
                authenticatedAccountResolver.getCurrentShoppingUserId(),
                buyNowDto.getProductId(),
                buyNowDto.getQuantity(),
                buyNowDto.getPaymentMode());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/myOrders")
    public ResponseEntity<List<Order>> myOrders(@RequestParam(required = false, defaultValue = "50") int limit){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.getMyOrders(authenticatedAccountResolver.getCurrentShoppingUserId(), limit));
    }
}
