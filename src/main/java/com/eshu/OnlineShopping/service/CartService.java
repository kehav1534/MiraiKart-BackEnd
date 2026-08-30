package com.eshu.OnlineShopping.service;

import com.eshu.OnlineShopping.converters.CartItemConverter;
import com.eshu.OnlineShopping.dto.CartItemDto;
import com.eshu.OnlineShopping.model.Cart;
import com.eshu.OnlineShopping.model.CartItem;
import com.eshu.OnlineShopping.model.Product;
import com.eshu.OnlineShopping.model.User;
import com.eshu.OnlineShopping.repository.CartItemRepository;
import com.eshu.OnlineShopping.repository.CartRepository;
import com.eshu.OnlineShopping.repository.ProductRepository;
import com.eshu.OnlineShopping.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserInfoRepository userInfoRepository;

    /** userId comes from the caller's verified JWT identity, never from the request body. */
    public String addItem(CartItemDto cartItemDto, int userId){
        CartItem newItem = CartItemConverter.convertCartItemDtoIntoCartItem(cartItemDto);

        Optional<Product> item = productRepository.findById(cartItemDto.getProduct());
        if(item.isPresent()){
            Cart userCart;
            newItem.setProduct(item.get());
            User user = userInfoRepository.findById(userId).orElse(null);
            if(user!=null && user.getUserCart()==null) {
                userCart = new Cart();
                userCart.setCartUserID(user);
                user.setUserCart(userCart);
                userInfoRepository.save(user);
                }
            else if(user==null) return "Error: User not found. Item cannot be added to cart.";
            newItem.setCart(user.getUserCart());
            CartItem existingItem = cartItemRepository.getUserCartItem(user.getUserCart().getId(), cartItemDto.getProduct());
            if(existingItem!=null){
                existingItem.setQuantity(existingItem.getQuantity()+cartItemDto.getQuantity());
                cartItemRepository.save(existingItem);
            }
            else user.getUserCart().getItems().add(newItem);
            userInfoRepository.save(user);
            return "Item added successfully";
        }
        else return "Error: Item not Found. Cannot be added to cart.";
    }


    public String removeItemFromCart(int userId, int productId){
        User user = userInfoRepository.findById(userId).orElse(null);
        if(user==null || user.getUserCart()==null) return "Error: Item not found in Cart.";

        CartItem delItem = cartItemRepository.getUserCartItem(user.getUserCart().getId(), productId);
        if(delItem!=null){
            cartItemRepository.delete(delItem);
            return "Item Removed from cart successfully!";
        }
        else return "Error: Item not found in Cart.";

    }

    public String updateCartQty(int userId, int productId, int qty){
        User user = userInfoRepository.findById(userId).orElse(null);
        if(user==null || user.getUserCart()==null) return "Error: Item not found. Quantity cannot be updated.";

        CartItem item  = cartItemRepository.getUserCartItem(user.getUserCart().getId(), productId);
        if(item!=null){
            //Add or subtract the quantity, instead of directly updating the value.
            item.setQuantity(item.getQuantity()+qty);
            if(item.getQuantity()<=0){
                removeItemFromCart(userId, productId);
                return "Item removed from cart.";
            }
            cartItemRepository.save(item);
            return item.getProduct().getName()+" quantity is set to : "+item.getQuantity();
        }
        return "Error: Item not found. Quantity cannot be updated.";
    }

    public List<CartItem> getAllCartItems(int userId, int pageNo){
        return cartItemRepository.getUserAllCartItems(userInfoRepository.findById(userId).get().getUserCart().getId(), PageRequest.of(pageNo, 10)).getContent();
    }

    /** Full cart view for the "view cart" page - no pagination, since a cart is never large enough to need it. */
    public List<CartItem> getCart(int userId){
        User user = userInfoRepository.findById(userId).orElse(null);
        if(user==null || user.getUserCart()==null) return Collections.emptyList();
        return new ArrayList<>(user.getUserCart().getItems());
    }

    public String removeAllCartItems(int userId){
        Cart userCart = userInfoRepository.findById(userId).get().getUserCart();
        if(userCart!=null){
            cartRepository.delete(userCart);
            return "Cart Items removed.";
        }
        return "Items not found.";
    }
}
