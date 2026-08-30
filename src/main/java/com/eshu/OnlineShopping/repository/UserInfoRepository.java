package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<User, Integer> {
    boolean existsByContactNo(String contactNo);

    /** Resolves the profile row belonging to a given login (JWT subject). */
    Optional<User> findByUserAuth_Email(String email);

    /** Resolves the auto-provisioned shopping profile for a seller account, if one has been created yet. */
    Optional<User> findByLinkedSellerId(Integer sellerId);
}
