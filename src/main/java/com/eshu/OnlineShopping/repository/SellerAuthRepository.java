package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.model.Seller;
import com.eshu.OnlineShopping.model.SellerAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerAuthRepository extends JpaRepository<SellerAuth, Integer> {
    SellerAuth findByEmail(String email);

    boolean existsByEmail(String email);
}
