package com.eshu.OnlineShopping.controllers;


import com.eshu.OnlineShopping.dto.CartItemDto;
import com.eshu.OnlineShopping.model.CartItem;
import com.eshu.OnlineShopping.security.AuthenticatedAccountResolver;
import com.eshu.OnlineShopping.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gated to ROLE_USER or ROLE_SELLER by SecurityConfig - a seller's own
 * login can shop too. The cart touched is always the caller's own
 * shopping profile, resolved via
 * AuthenticatedAccountResolver#getCurrentShoppingUserId - never a userId
 * taken from the request - so one account can never read or modify
 * another's cart.
 */
@RestController
@RequestMapping("/cart/apis")
public class CartController {

    @Autowired
    CartService cartService;

    @Autowired
    AuthenticatedAccountResolver authenticatedAccountResolver;

    @PostMapping("/addItem")
    public String addItemIntoCart(@RequestBody CartItemDto cartItemDto){
        return cartService.addItem(cartItemDto, authenticatedAccountResolver.getCurrentShoppingUserId());
    }

    @GetMapping("/getCart")
    public List<CartItem> getCart(){
        return cartService.getCart(authenticatedAccountResolver.getCurrentShoppingUserId());
    }

    /** quantity here is a delta (positive to increase, negative to decrease) - mirrors the seller's PATCH /updateStock pattern. */
    @PatchMapping("/updateItem")
    public String updateItemQuantity(@RequestBody CartItemDto cartItemDto){
        return cartService.updateCartQty(
                authenticatedAccountResolver.getCurrentShoppingUserId(),
                cartItemDto.getProduct(),
                cartItemDto.getQuantity());
    }

    @DeleteMapping("/removeItem")
    public String removeItemFromCart(@RequestParam int productId){
        return cartService.removeItemFromCart(authenticatedAccountResolver.getCurrentShoppingUserId(), productId);
    }

}
