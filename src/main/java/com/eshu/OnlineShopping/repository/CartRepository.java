package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    @Query(nativeQuery = true, value = "SELECT * FROM cart WHERE user = :userId")
    public Cart SearchUserCart(@Param("userId") int userId);
}
