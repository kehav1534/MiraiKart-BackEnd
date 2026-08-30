package com.eshu.OnlineShopping.converters;

import com.eshu.OnlineShopping.dto.CartItemDto;
import com.eshu.OnlineShopping.model.CartItem;

public class CartItemConverter {
    public static CartItem convertCartItemDtoIntoCartItem(CartItemDto cartItemDto){

        CartItem newItem = new CartItem();
        newItem.setQuantity(cartItemDto.getQuantity());
        return newItem;
    }
}

