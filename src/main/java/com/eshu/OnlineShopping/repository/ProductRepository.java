package com.eshu.OnlineShopping.repository;

import com.eshu.OnlineShopping.enums.ProductCategory;
import com.eshu.OnlineShopping.model.Product;
import jdk.jfr.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(nativeQuery = true, value = "SELECT * from products WHERE category IN :categories")
    public Page<Product> productWithCategory(@Param("categories") List<String> category, Pageable pageable);

    @Query(""" 
        SELECT p FROM Product p WHERE (:minPrice IS NULL OR p.price >= :minPrice) AND (:maxPrice IS NULL OR p.price <= :maxPrice) AND ( :productName IS NULL OR LOWER(p.name) LIKE CONCAT('%', LOWER(CAST(:productName AS string)), '%') ) AND (:categories IS NULL OR p.category IN :categories) AND (:manufacturer IS NULL OR p.manufacturer IN :manufacturer) AND ( p.listingStatus IS NULL OR p.listingStatus = com.eshu.OnlineShopping.enums.ProductListingStatus.LIVE OR p.availabilityStatus = com.eshu.OnlineShopping.enums.AvailabilityStatus.AVAILABLE ) 
        """)
    public List<Product> searchProduct( @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice, @Param("productName") String productName, @Param("categories") List<ProductCategory> categories, @Param("manufacturer") List<String> manufacturer, Pageable pageable );


    Optional<Product> findByIdAndSellerIdId(Integer productId, Integer sellerId);

    Optional<List<Product>> findBySellerIdId(Integer sellerId);

    /**
     * Used for the seller dashboard's "out of stock" panel. Queried
     * directly against Product (not via OrderItem) so a product shows up
     * here as soon as its stock hits 0, regardless of whether it's ever
     * actually been ordered.
     */
    List<Product> findBySellerIdIdAndQuantity(Integer sellerId, Integer quantity);
}
