package com.eshu.OnlineShopping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Maps the public /product-images/** URL prefix straight onto the local
 * directory ProductService writes uploaded product photos to
 * (file.product.path), so an <img src="{apiBase}/product-images/xyz.jpg">
 * just works without a dedicated download controller/endpoint.
 *
 * This is a separate WebMvcConfigurer from SecurityConfig deliberately -
 * SecurityConfig already implements WebMvcConfigurer for the seller-access
 * interceptor, and Spring merges configurers from every bean of that type,
 * so there's no need to crowd an unrelated concern into that class.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.product.path}")
    private String productImageDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = productImageDirectory.endsWith("/") || productImageDirectory.endsWith("\\")
                ? productImageDirectory
                : productImageDirectory + "/";
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations("file:" + location);
    }
}
