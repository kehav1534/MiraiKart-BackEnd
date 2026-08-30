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

    @Query(nativeQuery = false, value = "SELECT p FROM Product p WHERE (:minPrice is NULL OR p.price >= :minPrice) AND (:maxPrice is NULL OR p.price <= :maxPrice) and (:productName is NULL or LOWER(p.name) LIKE LOWER(CONCAT('%',:productName,'%'))) AND (:categories is NULL or p.category in :categories) AND (:manufacturer is NULL or p.manufacturer in :manufacturer) AND(p.listingStatus is NULL or p.listingStatus = LIVE or p.availabilityStatus = AVAILABLE)")
    public List<Product> searchProduct(Integer minPrice, Integer maxPrice, String productName, List<ProductCategory> categories, List<String> manufacturer, Pageable pageable);
    //for now status works even if it's null.

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
