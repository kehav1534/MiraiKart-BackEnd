package com.eshu.OnlineShopping.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * One uploaded image belonging to a Product. The actual file bytes live on
 * disk (see ProductService's storage helpers, under the file.product.path
 * directory); this row just remembers the public URL it was served at
 * (imageUrl, e.g. "/product-images/&lt;uuid&gt;.jpg") so the catalog/detail
 * pages can render an &lt;img src=...&gt; directly.
 *
 * The back-reference to Product is @JsonIgnore'd rather than
 * @JsonBackReference because nothing ever needs to walk image -> product in
 * JSON (Product -> images is the only direction the frontend cares about),
 * and JsonIgnore is simpler than reasoning about the managed/back-reference
 * pairing for a relationship that's only ever serialized one way.
 */
@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
