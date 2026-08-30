package com.eshu.OnlineShopping.controllers;

import com.eshu.OnlineShopping.dto.*;
import com.eshu.OnlineShopping.enums.OrderStatus;
import com.eshu.OnlineShopping.model.Product;
import com.eshu.OnlineShopping.model.Seller;
import com.eshu.OnlineShopping.security.AuthenticatedAccountResolver;
import com.eshu.OnlineShopping.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Every endpoint here is gated to ROLE_SELLER by SecurityConfig. On top of
 * that role check, each method resolves "which seller" strictly from the
 * verified JWT via AuthenticatedAccountResolver - never from a client-
 * supplied id - so one seller's token can never be used to read or modify
 * another seller's stock, dashboard, or orders.
 */
@RestController
@RequestMapping("/seller/apis")
public class SellerController {

    @Autowired
    SellerService sellerService;

    @Autowired
    AuthenticatedAccountResolver authenticatedAccountResolver;

//    @GetMapping("/getAll")
//    public List<Seller> getAllSellers(){
//        return sellerService.getAllSellerList();
//    }

    @PutMapping("/updateStock")
    public ResponseEntity<String> updateStock(@RequestBody ProductQuantityDto updatedQuantity){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.updateStock(
                        authenticatedAccountResolver.getCurrentSellerId(),
                        updatedQuantity.getProductId(),
                        updatedQuantity.getQuantity())
                );
    }

    @PatchMapping("/updateStock")
    public ResponseEntity<String> adjustStock(@RequestBody ProductQuantityDto updatedQuantity){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.adjustStock(
                        authenticatedAccountResolver.getCurrentSellerId(),
                        updatedQuantity.getProductId(),
                        updatedQuantity.getQuantity())
                );
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getSellerProducts(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.getAllProducts(authenticatedAccountResolver.getCurrentSellerId()));
    }

    @PatchMapping("/updateListingStatus")
    public ResponseEntity<String> updateListingStatus(@RequestBody ProductListingStatusDto dto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.updateListingStatus(
                        authenticatedAccountResolver.getCurrentSellerId(),
                        dto.getProductId(),
                        dto.getStatus())
                );
    }

    @PostMapping("/sellerMetrics")
    public ResponseEntity<SellerMetricsDto> sellerDashBoard(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.sellerDashBoard(authenticatedAccountResolver.getCurrentSellerId()));
    }

    @PostMapping("/productMetrics/{productId}")
    public ResponseEntity<ProductMetricsDto> productDashBoard(@PathVariable Integer productId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.productMetrics(authenticatedAccountResolver.getCurrentSellerId(), productId));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<SellerOrderDto>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false, defaultValue = "200") int limit){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.getOrders(authenticatedAccountResolver.getCurrentSellerId(), status, limit));
    }

    /** Full detail for a single order line, scoped to the caller's own products - a page seller can link to per-order. */
    @GetMapping("/orders/{orderItemId}")
    public ResponseEntity<SellerOrderDto> getOrderDetail(@PathVariable int orderItemId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.getOrderDetail(authenticatedAccountResolver.getCurrentSellerId(), orderItemId));
    }

    /**
     * Changes an order line's status, restricted to the transitions defined in
     * SellerService.ALLOWED_TRANSITIONS (e.g. a DELIVERED or already-CANCELLED
     * item can no longer be touched, and CANCELLED is unreachable once the
     * item has shipped). Invalid transitions come back as 400 with a message
     * naming what is still allowed.
     */
    @PatchMapping("/updateOrderStatus")
    public ResponseEntity<String> updateOrderStatus(@RequestBody OrderStatusUpdateDto dto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sellerService.updateOrderStatus(
                        authenticatedAccountResolver.getCurrentSellerId(),
                        dto.getOrderItemId(),
                        dto.getStatus())
                );
    }

    @PatchMapping("/updateSellerDetails")
    public ResponseEntity<String> updateSellerDetails(@RequestBody SellerDto sellerDto){
        sellerService.updateSellerDetails(authenticatedAccountResolver.getCurrentSellerId(), sellerDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Updated seller details");
    }

}
