package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query(value = """
            SELECT o
            FROM Order o
            WHERE o.userId.id = :userId
            ORDER BY o.orderDate DESC
            """)
    List<Order> findByUserIdOrderByOrderDateDesc(@Param("userId") int userId, Pageable pageable);
}
