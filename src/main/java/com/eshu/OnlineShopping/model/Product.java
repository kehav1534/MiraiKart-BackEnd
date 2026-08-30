package com.eshu.OnlineShopping.model;
import com.eshu.OnlineShopping.enums.AvailabilityStatus;
import jakarta.validation.constraints.Min;

import com.eshu.OnlineShopping.enums.ProductCategory;
import com.eshu.OnlineShopping.enums.ProductListingStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String des;

    @Column(name = "price")
    @Min(0)
    private BigDecimal price;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "category")
    @Enumerated(value = EnumType.STRING)
    private ProductCategory category;

    @Column(name = "discount")
    @Min(0)
    private int discount;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    @JsonBackReference
    private Seller sellerId;

    @Column(name="listing_status")
    @Enumerated(value = EnumType.STRING)
    private ProductListingStatus listingStatus;

    @Column(name="available_status")
    @Enumerated(value = EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    @Column(name="quantity")
    @Min(0)
    private Integer quantity;

    /**
     * A product's photos. Owned from this side (mappedBy = "product") with
     * orphanRemoval so deleting an image is just removing it from this list
     * and saving the product - no separate delete call needed. Ordered by
     * id so the first-uploaded image (used as the catalog thumbnail) is
     * always images.get(0) rather than whatever order the DB feels like
     * returning.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ProductImage> images = new ArrayList<>();

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    @PrePersist
    @PreUpdate
    private void updateListingStatus() {

        if (quantity == null || quantity == 0) {
            this.availabilityStatus = AvailabilityStatus.OUT_OF_STOCK;
        } else {
            this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        }
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ProductListingStatus getListingStatus() {
        return listingStatus;
    }

    public void setListingStatus(ProductListingStatus status) {
        this.listingStatus = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public Seller getSellerId() {
        return sellerId;
    }

    public void setSellerId(Seller sellerId) {
        this.sellerId = sellerId;
    }

}
