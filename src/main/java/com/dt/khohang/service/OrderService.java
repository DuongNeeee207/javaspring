package com.dt.khohang.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dt.khohang.entity.Inventory;
import com.dt.khohang.entity.Order;
import com.dt.khohang.entity.OrderDetail;
import com.dt.khohang.entity.Product;
import com.dt.khohang.entity.User;
import com.dt.khohang.repository.InventoryRepository;
import com.dt.khohang.repository.OrderRepository;
import com.dt.khohang.repository.ProductRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Order> findAll() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public long count() {
        return orderRepository.count();
    }

    public long countByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Tạo đơn hàng mới.
     * Tự động trừ kho theo số lượng trong từng OrderDetail.
     */
    @Transactional
    public Order createOrder(User customer, String shippingAddress, String phone, String note,
                             List<Long> productIds, List<Integer> quantities) {

        // Tạo mã đơn hàng: DH-<timestamp>
        String orderCode = "DH-" + System.currentTimeMillis();

        Order order = new Order();
        order.setOrderCode(orderCode);
        order.setCustomer(customer);
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setNote(note);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            int qty = quantities.get(i);

            if (qty <= 0) continue;

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));

            // Kiểm tra & trừ kho
            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho sản phẩm: " + product.getName()));

            if (inventory.getQuantity() < qty) {
                throw new RuntimeException("Sản phẩm \"" + product.getName()
                        + "\" không đủ hàng. Tồn kho: " + inventory.getQuantity() + ", yêu cầu: " + qty);
            }

            inventory.setQuantity(inventory.getQuantity() - qty);
            inventoryRepository.save(inventory);

            // Tạo OrderDetail & thêm vào danh sách products (@JoinTable)
            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(qty);
            detail.setUnitPrice(product.getPrice());
            order.addOrderDetail(detail);
            order.getProducts().add(product);
        }

        order.calculateTotal();
        return orderRepository.save(order);
    }

    /**
     * Admin duyệt đơn hàng: PENDING → APPROVED
     */
    @Transactional
    public Order approveOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));
        order.setStatus("APPROVED");
        return orderRepository.save(order);
    }

    /**
     * Admin giao hàng: APPROVED → SHIPPED
     */
    @Transactional
    public Order shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));
        order.setStatus("SHIPPED");
        return orderRepository.save(order);
    }

    /**
     * Hủy đơn: PENDING/APPROVED → CANCELLED
     * Hoàn lại tồn kho cho từng sản phẩm trong đơn.
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        if ("SHIPPED".equals(order.getStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã giao!");
        }

        // Hoàn lại kho
        for (OrderDetail detail : order.getOrderDetails()) {
            Inventory inventory = inventoryRepository.findByProductId(detail.getProduct().getId())
                    .orElse(null);
            if (inventory != null) {
                inventory.setQuantity(inventory.getQuantity() + detail.getQuantity());
                inventoryRepository.save(inventory);
            }
        }

        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    /**
     * Tính tổng doanh thu đơn hàng đã giao (SHIPPED) hoặc đã duyệt (APPROVED)
     */
    public double getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> "SHIPPED".equals(o.getStatus()) || "APPROVED".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();
    }
}
