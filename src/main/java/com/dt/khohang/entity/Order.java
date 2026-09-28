package com.dt.khohang.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderCode;

    private LocalDateTime orderDate;

    // Trạng thái: PENDING (Chờ duyệt), APPROVED (Đã duyệt), SHIPPED (Đã xuất kho/giao), CANCELLED (Đã hủy)
    @Column(nullable = false)
    private String status = "PENDING";

    private Double totalAmount = 0.0;

    private String shippingAddress;

    private String phone;

    private String note;

    // Quan hệ Many-to-One với Customer (User)
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // Quan hệ One-to-Many với OrderDetail (Mapping bảng trung gian giữa Order và Product)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // Quan hệ Many-to-Many trực tiếp với Product sử dụng @JoinTable theo yêu cầu đề bài
    @jakarta.persistence.ManyToMany
    @jakarta.persistence.JoinTable(
        name = "order_products",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    public Order() {
    }

    public Order(String orderCode, User customer, String shippingAddress, String phone, String note) {
        this.orderCode = orderCode;
        this.customer = customer;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.note = note;
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    @PrePersist
    public void onCreate() {
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
    }

    public void addOrderDetail(OrderDetail detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
        calculateTotal();
    }

    public void calculateTotal() {
        this.totalAmount = orderDetails.stream()
                .mapToDouble(d -> (d.getUnitPrice() != null ? d.getUnitPrice() : 0.0) * (d.getQuantity() != null ? d.getQuantity() : 0))
                .sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getCreatedAt() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        calculateTotal();
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
