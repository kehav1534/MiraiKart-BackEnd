package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.model.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    @Query(nativeQuery = true, value = "SELECT * FROM cart_item WHERE product=:productId AND cart=:cartId")
    public CartItem getUserCartItem(@Param("cartId") int cartId,@Param("productId") int productId);

    @Query(nativeQuery = true, value = "SELECT * FROM cart_item WHERE cart = :cartId")
    public Page<CartItem> getUserAllCartItems(@Param("cartId") int cartId, Pageable pageable);
}
