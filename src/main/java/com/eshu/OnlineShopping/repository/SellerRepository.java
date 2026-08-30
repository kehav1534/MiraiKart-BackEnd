package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.enums.OrderStatus;
import com.eshu.OnlineShopping.model.Order;
import com.eshu.OnlineShopping.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Integer> {

    boolean existsByEntityName(String entityName);

    boolean existsByContactNo(String contactNo);

    boolean existsById(int sellerId);

    /** Resolves the profile row belonging to a given login (JWT subject). */
    Optional<Seller> findBySellerAuth_Email(String email);

    @Query(value = """
            SELECT oi
            FROM Order o
            JOIN o.orderItems oi
            WHERE oi.product.sellerId.id = :sellerId
            AND (oi.status = :status OR :status IS NOT NULL )
            ORDER BY orderDate DESC
            """)
    List<Order> getSellerOrders(@Param("sellerId") int sellerId, @Param("status") OrderStatus status);
}
