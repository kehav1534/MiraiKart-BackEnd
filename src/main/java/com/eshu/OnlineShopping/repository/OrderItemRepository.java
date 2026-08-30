package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.dto.SellerOrderDto;
import com.eshu.OnlineShopping.dto.TopSellingDto;
import com.eshu.OnlineShopping.enums.OrderStatus;
import com.eshu.OnlineShopping.model.OrderItem;
import com.eshu.OnlineShopping.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query(value= """
          SELECT Coalesce( SUM(oi.qty), 0)
          FROM OrderItem oi
          WHERE oi.product.sellerId.id = :sellerId""")
    public long getTotalSales(@Param("sellerId") Integer sellerId);

    @Query(value= """
            SELECT Coalesce(COUNT( DISTINCT oi.orderId.id ), 0)
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND oi.status = com.eshu.OnlineShopping.enums.OrderStatus.DELIVERED
            """)
    public long getTotalOrders(@Param("sellerId") Integer sellerId);

    @Query(value= """
            SELECT COALESCE(SUM( oi.purchasePrice * oi.qty ), 0)
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND oi.status = com.eshu.OnlineShopping.enums.OrderStatus.DELIVERED
            """)
    public BigDecimal getTotalGrossRevenue(@Param("sellerId") Integer sellerId);

    @Query(value= """
            SELECT COALESCE(SUM( oi.purchasePrice * oi.qty ), 0)
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND oi.status = com.eshu.OnlineShopping.enums.OrderStatus.DELIVERED
            AND oi.returnStatus != com.eshu.OnlineShopping.enums.ReturnStatus.REFUNDED
            """)
    public BigDecimal getTotalNetRevenue(@Param("sellerId") Integer sellerId);

    @Query(value = """
            SELECT COALESCE(COUNT( oi ), 0)
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND oi.returnStatus = com.eshu.OnlineShopping.enums.ReturnStatus.REFUNDED
            """)
    public long getTotalReturnedProducts(@Param("sellerId") Integer sellerId);


    ///////////////////////////////---------Product-Analytics-----------///////////////////////

    @Query(value= """
          SELECT Coalesce( SUM(oi.qty), 0)
          FROM OrderItem oi
          WHERE oi.product.sellerId.id = :sellerId
          AND oi.product.id = :productId""")
    public long getProductTotalSales(@Param("sellerId") Integer sellerId,
                                     @Param("productId") Integer productId);

    @Query(value= """
            SELECT Coalesce(COUNT( DISTINCT oi.orderId.id ), 0)
            FROM OrderItem oi
            WHERE oi.product.sellerId.id =:sellerId
            AND oi.product.id = :productId
            AND oi.status = com.eshu.OnlineShopping.enums.OrderStatus.DELIVERED
            """)
    public long getProductOrders(@Param("sellerId") Integer sellerId,
                                 @Param("productId") Integer productId);

    @Query(value= """
            SELECT COALESCE(SUM( oi.purchasePrice * oi.qty ), 0)
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND oi.product.id = :productId
            AND oi.status = com.eshu.OnlineShopping.enums.OrderStatus.DELIVERED
            """)
    public BigDecimal getProductGrossRevenue(@Param("sellerId") Integer sellerId,
                                             @Param("productId") Integer productId);

    @Query(value= """
            SELECT COALESCE(SUM( oi.purchasePrice * oi.qty ), 0)
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND oi.product.id = :productId
            AND oi.status = com.eshu.OnlineShopping.enums.OrderStatus.DELIVERED
            AND oi.returnStatus != com.eshu.OnlineShopping.enums.ReturnStatus.REFUNDED
            """)
    public BigDecimal getProductNetRevenue(@Param("sellerId") Integer sellerId,
                                           @Param("productId") Integer productId);

    @Query(value = """
            SELECT COALESCE(SUM( oi.qty ), 0)
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND oi.product.id = :productId
            AND oi.returnStatus = com.eshu.OnlineShopping.enums.ReturnStatus.REFUNDED
            """)
    public long getReturnedProduct(@Param("sellerId") Integer sellerId, @Param("productId") Integer productId);


    @Query(value= """
            SELECT new com.eshu.OnlineShopping.dto.TopSellingDto(
                oi.product.id,
                oi.product.name,
                COALESCE(SUM(oi.qty), 0)
            )
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            GROUP BY oi.product.id, oi.product.name
            ORDER BY SUM( oi.qty ) DESC
            """)
    public List<TopSellingDto> getTopSellingProducts(@Param("sellerId") Integer sellerId,
                                                      org.springframework.data.domain.Pageable pageable);

    @Query(value = """
            SELECT oi
            FROM OrderItem oi
            WHERE oi.id = :orderId
            AND oi.product.sellerId.id = :sellerId
            """)
    public OrderItem getOrderItemForSeller(@Param("sellerId") Integer sellerId,
                                            @Param("orderId") Integer orderItemId);

    @Query(value = """
            SELECT oi
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND (oi.status = :status OR CAST(:status AS string) IS NULL)
            ORDER BY oi.orderId.orderDate DESC
            """)
    public List<OrderItem> getAllOrderItemForSeller(@Param("sellerId") Integer sellerId,
                                                    @Param("status") OrderStatus status,
                                                    org.springframework.data.domain.Pageable pageable);

    /**
     * Seller-facing order list, flattened straight into SellerOrderDto so the
     * response includes order date/payment info and shipping-relevant buyer
     * details that would otherwise be unreachable (order date) or over-broad
     * (the whole User entity) if OrderItem/Order entities were returned as-is.
     * :status is optional - passing null returns items in every status.
     */
    @Query(value = """
            SELECT new com.eshu.OnlineShopping.dto.SellerOrderDto(
                oi.id, oi.orderId.id, oi.orderId.orderDate, oi.orderId.updationDate,
                oi.orderId.paymentMode, oi.orderId.status,
                oi.product.id, oi.product.name, oi.product.manufacturer,
                oi.qty, oi.purchasePrice, oi.status, oi.returnStatus,
                oi.orderId.userId.id, oi.orderId.userId.userAuth.email,
                oi.orderId.userId.contactNo, oi.orderId.userId.address
            )
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND (oi.status = :status OR CAST(:status AS string) IS NULL)
            ORDER BY oi.orderId.orderDate DESC
            """)
    public List<SellerOrderDto> getSellerOrderDtos(@Param("sellerId") Integer sellerId,
                                                     @Param("status") OrderStatus status,
                                                     org.springframework.data.domain.Pageable pageable);

    /** Same projection as above, scoped to one order line so a seller can only ever fetch their own. */
    @Query(value = """
            SELECT new com.eshu.OnlineShopping.dto.SellerOrderDto(
                oi.id, oi.orderId.id, oi.orderId.orderDate, oi.orderId.updationDate,
                oi.orderId.paymentMode, oi.orderId.status,
                oi.product.id, oi.product.name, oi.product.manufacturer,
                oi.qty, oi.purchasePrice, oi.status, oi.returnStatus,
                oi.orderId.userId.id, oi.orderId.userId.userAuth.email,
                oi.orderId.userId.contactNo, oi.orderId.userId.address
            )
            FROM OrderItem oi
            WHERE oi.product.sellerId.id = :sellerId
            AND oi.id = :orderItemId
            """)
    public Optional<SellerOrderDto> getSellerOrderDto(@Param("sellerId") Integer sellerId,
                                                        @Param("orderItemId") Integer orderItemId);

}
