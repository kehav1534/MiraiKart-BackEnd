package com.eshu.OnlineShopping.service;

import com.eshu.OnlineShopping.converters.ProductConverter;
import com.eshu.OnlineShopping.dto.ProductDto;
import com.eshu.OnlineShopping.enums.ProductCategory;
import com.eshu.OnlineShopping.enums.ProductListingStatus;
import com.eshu.OnlineShopping.exceptions.NotFoundException;
import com.eshu.OnlineShopping.model.Product;
import com.eshu.OnlineShopping.model.ProductImage;
import com.eshu.OnlineShopping.model.Seller;
import com.eshu.OnlineShopping.repository.ProductRepository;
import com.eshu.OnlineShopping.repository.SellerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SellerRepository sellerRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${supabase.storage.bucket}")
    private String supabaseStorageBucket;

    @Value("${file.product.path}")
    private String productImageDirectory;

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp", "image/gif");
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // 5MB per file
    private static final int MAX_IMAGES_PER_PRODUCT = 8;

    /** sellerId comes from the caller's verified JWT identity, never from the request body. */
    public Product addProduct(ProductDto productDto, int sellerId){
        Product newProduct = ProductConverter.convertProductDtoIntoProduct(productDto);
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(()->new NotFoundException("Seller", sellerId));
        newProduct.setSellerId(seller);
        return productRepository.save(newProduct);
    }

    public String changeProductStatus(int productId, ProductListingStatus status){
        Product product = productRepository.findById(productId).orElseThrow(()-> new NotFoundException("Product", productId));
        if(product.getListingStatus()!= ProductListingStatus.PENDING && product.getListingStatus()!=ProductListingStatus.BLOCKED){
            product.setListingStatus(status);
            productRepository.save(product);
            return "Product status changed successfully.";
        }
        return "Product Status cannot be updated.";
    }

    public String updateProductDetails(int productId, ProductDto productDto){
        Product product = productRepository.findById(productId).orElseThrow(()-> new NotFoundException("Product", productId));
        product.setName((productDto.getName()!=null)?productDto.getName():product.getName());
        product.setDes((productDto.getDes()!=null)?productDto.getDes():product.getDes());
        product.setPrice((productDto.getPrice()!=null)?productDto.getPrice():product.getPrice());
        product.setManufacturer((productDto.getManufacturer()!=null)?productDto.getManufacturer():product.getManufacturer());
        product.setDiscount((productDto.getDiscount() != 0)?productDto.getDiscount():product.getDiscount());
        product.setCategory((productDto.getCategory()!=null)?productDto.getCategory():product.getCategory());
        productRepository.save(product);
        return "Product Details successfully updated.";

    }

    public List<Product> getAllProducts(int pageNo, List<String> categories){
        Pageable pageable =  PageRequest.of(pageNo, 10);
        return (categories.isEmpty())?
                productRepository.findAll(pageable).getContent()
                : productRepository.productWithCategory(categories, pageable).getContent();
    }
    public Product getProductById(int productById){
        return productRepository.findById(productById).orElseThrow(()->new NotFoundException("Product", productById));
    }

    public List<Product> getProduct(String productName, List<String> manufacturer, Integer minPrice, Integer maxPrice, List<ProductCategory> categories, int pageNo){
        Pageable pageable =  PageRequest.of(pageNo, 10);
        return productRepository.searchProduct(minPrice, maxPrice, productName, categories, manufacturer, pageable);
    }

    /**
     * Adds one or more images to a product the caller (verified as sellerId,
     * never a client-supplied value) actually owns. Files are validated
     * (type/size), uploaded to Supabase Storage under productImageDirectory
     * with a random filename (so nothing about the stored name leaks the
     * original), and each gets a ProductImage row pointing at the public
     * URL Supabase Storage serves that object under.
     */
    @Transactional
    public Product addImages(int sellerId, int productId, List<MultipartFile> files){
        Product product = productRepository.findByIdAndSellerIdId(productId, sellerId)
                .orElseThrow(() -> new NotFoundException("Product", productId));

        if(files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)){
            throw new IllegalArgumentException("At least one image file is required.");
        }
        if(product.getImages().size() + files.size() > MAX_IMAGES_PER_PRODUCT){
            throw new IllegalArgumentException(
                    "A product can have at most " + MAX_IMAGES_PER_PRODUCT + " images (" +
                            product.getImages().size() + " already uploaded).");
        }

        for(MultipartFile file : files){
            if(file.isEmpty()) continue;
            validateImage(file);
            String publicUrl = storeImageFile(file);

            ProductImage image = new ProductImage();
            image.setImageUrl(publicUrl);
            image.setProduct(product);
            product.getImages().add(image);
        }

        return productRepository.save(product);
    }

    /** Removes one image from a product the caller owns. orphanRemoval on Product.images deletes the row on save. */
    @Transactional
    public String deleteImage(int sellerId, int productId, int imageId){
        Product product = productRepository.findByIdAndSellerIdId(productId, sellerId)
                .orElseThrow(() -> new NotFoundException("Product", productId));

        ProductImage toRemove = product.getImages().stream()
                .filter(image -> image.getId() == imageId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Image", imageId));

        product.getImages().remove(toRemove);
        productRepository.save(product);

        // Best-effort cleanup: the DB row is already gone even if the remote object delete fails.
        deleteImageFile(toRemove.getImageUrl());

        return "Image removed.";
    }

    private void validateImage(MultipartFile file){
        if(file.getSize() > MAX_IMAGE_BYTES){
            throw new IllegalArgumentException(
                    "\"" + file.getOriginalFilename() + "\" is too large - each image must be 5MB or smaller.");
        }
        String contentType = file.getContentType();
        if(contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())){
            throw new IllegalArgumentException(
                    "\"" + file.getOriginalFilename() + "\" isn't a supported image type. Use PNG, JPEG, GIF, or WEBP.");
        }
    }

    /** Uploads the file to Supabase Storage and returns its public URL. */
    private String storeImageFile(MultipartFile file){
        String objectPath = productImageDirectory + "/" + UUID.randomUUID() + fileExtensionOf(file);

        try {
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseUrl, supabaseStorageBucket, objectPath);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseServiceRoleKey);
            headers.set("apikey", supabaseServiceRoleKey);
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));

            HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl, HttpMethod.POST, request, String.class);

            if(!response.getStatusCode().is2xxSuccessful()){
                throw new RuntimeException("Supabase upload failed with status " + response.getStatusCode());
            }

            return String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseUrl, supabaseStorageBucket, objectPath);
        } catch (IOException e){
            throw new RuntimeException("Could not read uploaded image: " + e.getMessage(), e);
        } catch (RestClientException e){
            throw new RuntimeException("Could not store uploaded image: " + e.getMessage(), e);
        }
    }

    /** Deletes the corresponding object from Supabase Storage given the public URL stored on the image. */
    private void deleteImageFile(String imageUrl){
        if(imageUrl == null || imageUrl.isBlank()) return;
        try {
            String prefix = String.format("%s/storage/v1/object/public/%s/", supabaseUrl, supabaseStorageBucket);
            if(!imageUrl.startsWith(prefix)) return; // not one of ours (e.g. legacy path); nothing to clean up
            String objectPath = imageUrl.substring(prefix.length());

            String deleteUrl = String.format("%s/storage/v1/object/%s", supabaseUrl, supabaseStorageBucket);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseServiceRoleKey);
            headers.set("apikey", supabaseServiceRoleKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, List<String>>> request =
                    new HttpEntity<>(Map.of("prefixes", List.of(objectPath)), headers);

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, request, String.class);
        } catch (Exception e){
            // Swallow: the product/image DB record is already updated; a stray object isn't worth failing the request.
        }
    }

    private String fileExtensionOf(MultipartFile file){
        String original = file.getOriginalFilename();
        int dot = (original == null) ? -1 : original.lastIndexOf('.');
        // Cap the extension length so a pathological filename can't be used to write an oversized name.
        return (dot >= 0 && original.length() - dot <= 10) ? original.substring(dot) : "";
    }
}