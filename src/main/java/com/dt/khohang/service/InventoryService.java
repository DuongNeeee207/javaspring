package com.dt.khohang.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dt.khohang.entity.Inventory;
import com.dt.khohang.repository.InventoryRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> findById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Optional<Inventory> findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    /**
     * Lấy danh sách sản phẩm có tồn kho dưới ngưỡng an toàn (low stock)
     */
    public List<Inventory> findLowStock() {
        return inventoryRepository.findAll().stream()
                .filter(Inventory::isLowStock)
                .collect(Collectors.toList());
    }

    /**
     * Nhập thêm hàng vào kho (tăng số lượng)
     */
    @Transactional
    public Inventory addStock(Long inventoryId, int additionalQty) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho ID: " + inventoryId));
        inventory.setQuantity(inventory.getQuantity() + additionalQty);
        return inventoryRepository.save(inventory);
    }

    /**
     * Trừ kho khi xuất hàng / đặt hàng
     */
    @Transactional
    public void deductStock(Long productId, int qty) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho sản phẩm ID: " + productId));

        if (inventory.getQuantity() < qty) {
            throw new RuntimeException("Không đủ tồn kho. Hiện có: "
                    + inventory.getQuantity() + ", yêu cầu: " + qty);
        }

        inventory.setQuantity(inventory.getQuantity() - qty);
        inventoryRepository.save(inventory);
    }

    /**
     * Hoàn trả lại kho khi hủy đơn
     */
    @Transactional
    public void restoreStock(Long productId, int qty) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho sản phẩm ID: " + productId));
        inventory.setQuantity(inventory.getQuantity() + qty);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory updateInventory(Long id, Integer quantity, Integer minThreshold, String location) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho ID: " + id));

        if (quantity != null) inventory.setQuantity(quantity);
        if (minThreshold != null) inventory.setMinThreshold(minThreshold);
        if (location != null) inventory.setLocation(location);

        return inventoryRepository.save(inventory);
    }

    /**
     * Tổng giá trị ước tính toàn bộ kho
     */
    public double getTotalStockValue() {
        return inventoryRepository.findAll().stream()
                .filter(inv -> inv.getProduct() != null && inv.getProduct().getPrice() != null)
                .mapToDouble(inv -> inv.getProduct().getPrice() * inv.getQuantity())
                .sum();
    }

    public long countLowStock() {
        return findLowStock().size();
    }
}
