package com.tiendaropa.controller;

import com.tiendaropa.model.Product;
import com.tiendaropa.service.ProductService;
import com.tiendaropa.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FileStorageService fileStorageService;

    // ========== ENDPOINTS PÚBLICOS (Solo lectura) ==========
    
    @GetMapping("/api/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== ENDPOINTS PROTEGIDOS (Solo ADMIN) ==========
    
    @PostMapping("/api/admin/products")
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setDescription(description);
            product.setCategory(category);
            product.setStock(stock);

            // Guardar la imagen si se proporciona
            if (image != null && !image.isEmpty()) {
                String imageUrl = fileStorageService.storeFile(image);
                product.setImageUrl(imageUrl);
            }

            Product savedProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Error al crear producto: " + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/api/admin/products/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        return productService.getProductById(id)
                .map(existingProduct -> {
                    try {
                        existingProduct.setName(name);
                        existingProduct.setPrice(price);
                        existingProduct.setDescription(description);
                        existingProduct.setCategory(category);
                        existingProduct.setStock(stock);

                        // Actualizar imagen si se proporciona una nueva
                        if (image != null && !image.isEmpty()) {
                            // Eliminar imagen anterior si existe
                            if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty()) {
                                fileStorageService.deleteFile(existingProduct.getImageUrl());
                            }
                            String imageUrl = fileStorageService.storeFile(image);
                            existingProduct.setImageUrl(imageUrl);
                        }

                        Product updatedProduct = productService.createProduct(existingProduct);
                        return ResponseEntity.ok(updatedProduct);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("{\"message\": \"Error al actualizar producto: " + e.getMessage() + "\"}");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> {
                    try {
                        // Eliminar imagen asociada si existe
                        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                            fileStorageService.deleteFile(product.getImageUrl());
                        }
                        productService.deleteProduct(id);
                        return ResponseEntity.ok().body("{\"message\": \"Producto eliminado exitosamente\"}");
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("{\"message\": \"Error al eliminar producto: " + e.getMessage() + "\"}");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/admin/products/upload-image")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Archivo vacío\"}");
            }

            String fileName = fileStorageService.storeFile(file);
            return ResponseEntity.ok("{\"imageUrl\": \"" + fileName + "\"}");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Error al guardar imagen: " + e.getMessage() + "\"}");
        }
    }
}