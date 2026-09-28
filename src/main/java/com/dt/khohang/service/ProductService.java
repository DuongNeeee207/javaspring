package com.dt.khohang.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dt.khohang.entity.Inventory;
import com.dt.khohang.entity.Product;
import com.dt.khohang.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Thêm sản phẩm mới và tự động tạo bản ghi Inventory tương ứng
     */
    @Transactional
    public Product createProduct(Product product, int initialQty, int minThreshold, String location) {
        // Tạo Inventory liên kết 1-1
        Inventory inventory = new Inventory();
        inventory.setQuantity(initialQty);
        inventory.setMinThreshold(minThreshold);
        inventory.setLocation(location);
        product.setInventory(inventory);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updated) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + id));

        existing.setName(updated.getName());
        existing.setSku(updated.getSku());
        existing.setCategory(updated.getCategory());
        existing.setPrice(updated.getPrice());
        existing.setDescription(updated.getDescription());
        existing.setImageUrl(updated.getImageUrl());
        existing.setUnit(updated.getUnit());

        return productRepository.save(existing);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public long count() {
        return productRepository.count();
    }
}
