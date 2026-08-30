package com.eshu.OnlineShopping.controllers;


import com.eshu.OnlineShopping.dto.ProductDto;
import com.eshu.OnlineShopping.enums.ProductCategory;
import com.eshu.OnlineShopping.model.Product;
import com.eshu.OnlineShopping.security.AuthenticatedAccountResolver;
import com.eshu.OnlineShopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product/apis")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    AuthenticatedAccountResolver authenticatedAccountResolver;

    // SecurityConfig restricts this endpoint to ROLE_SELLER; the listing is
    // always attached to whichever seller the verified JWT belongs to, never
    // to a seller id the client could supply. Returns the created Product
    // (with its generated id) so the caller can immediately attach images
    // to it via /product/apis/{productId}/images.
    @PostMapping("/addProduct")
    public ResponseEntity<Product> addProduct(@RequestBody ProductDto productDto){
        Product created = productService.addProduct(productDto, authenticatedAccountResolver.getCurrentSellerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Attaches one or more images to a product the caller owns.
     * SecurityConfig restricts this to ROLE_SELLER; ProductService scopes
     * the lookup to the verified seller id, so a seller can never attach
     * images to another seller's listing.
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<Product> uploadImages(@PathVariable int productId,
                                                 @RequestParam("files") List<MultipartFile> files){
        Product updated = productService.addImages(
                authenticatedAccountResolver.getCurrentSellerId(), productId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    /** Removes one image from a product the caller owns. */
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable int productId, @PathVariable int imageId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productService.deleteImage(authenticatedAccountResolver.getCurrentSellerId(), productId, imageId));
    }

    // Public - browsing a single product's detail page doesn't require an account,
    // same as browsing the catalog itself.
    @GetMapping("/{productId}")
    public Product getProduct(@PathVariable int productId){
        return productService.getProductById(productId);
    }

    @GetMapping({"/searchProduct/{pageNo}", "/searchProduct"})
    public List<Product> searchProductWithFilter(@RequestParam(required = false) String search,
                                                 @RequestParam(required = false) Integer minPrice,
                                                 @RequestParam(required = false) Integer maxPrice,
                                                 @RequestParam(required =   false) List<ProductCategory> category,
                                                 @RequestParam(required = false) List<String> manufacturer,
                                                 @PathVariable(required = false) Integer pageNo){
        pageNo = (pageNo==null)?0:pageNo;
        pageNo = pageNo>=1?pageNo-1:pageNo;
        return pageNo>=0?productService.getProduct(search, manufacturer, minPrice, maxPrice, category, pageNo):null;
    }
}
