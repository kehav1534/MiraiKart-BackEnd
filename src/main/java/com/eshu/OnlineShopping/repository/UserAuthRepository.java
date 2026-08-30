package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Integer> {
    UserAuth findByEmail(String email);

    boolean existsByEmail(String email);
}
