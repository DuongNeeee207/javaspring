package com.dt.khohang.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dt.khohang.entity.Order;
import com.dt.khohang.entity.Product;
import com.dt.khohang.entity.User;
import com.dt.khohang.repository.UserRepository;
import com.dt.khohang.service.OrderService;
import com.dt.khohang.service.ProductService;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService,
                           ProductService productService,
                           UserRepository userRepository) {
        this.orderService = orderService;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @org.springframework.web.bind.annotation.ModelAttribute
    public void addCommonAttributes(Authentication authentication, Model model) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("role", isAdmin ? "ADMIN" : "CUSTOMER");
        }
    }

    /**
     * Trang đặt hàng (checkout) – Khách chọn sản phẩm và số lượng
     */
    @GetMapping("/orders/checkout")
    public String checkoutPage(@RequestParam(required = false) Long productId,
                               Authentication authentication,
                               Model model) {

        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        model.addAttribute("selectedProductId", productId);

        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("phone", user.getPhone());
                model.addAttribute("address", user.getAddress());
            }
        }

        return "checkout";
    }

    /**
     * Xử lý tạo đơn hàng mới
     */
    @PostMapping("/orders/create")
    public String createOrder(@RequestParam List<Long> productIds,
                              @RequestParam List<Integer> quantities,
                              @RequestParam String shippingAddress,
                              @RequestParam String phone,
                              @RequestParam(required = false) String note,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User customer = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

            // Lọc bỏ sản phẩm có số lượng <= 0
            List<Long> filteredIds = new ArrayList<>();
            List<Integer> filteredQtys = new ArrayList<>();
            for (int i = 0; i < productIds.size(); i++) {
                if (quantities.get(i) != null && quantities.get(i) > 0) {
                    filteredIds.add(productIds.get(i));
                    filteredQtys.add(quantities.get(i));
                }
            }

            if (filteredIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất 1 sản phẩm!");
                return "redirect:/orders/checkout";
            }

            Order order = orderService.createOrder(customer, shippingAddress, phone, note, filteredIds, filteredQtys);
            redirectAttributes.addFlashAttribute("success",
                    "Đặt hàng thành công! Mã đơn: " + order.getOrderCode()
                            + " | Tổng tiền: " + String.format("%,.0f đ", order.getTotalAmount()));
            return "redirect:/customer/orders";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/checkout";
        }
    }

    /**
     * Trang lịch sử đơn hàng của khách hàng
     */
    @GetMapping("/customer/orders")
    public String customerOrders(Authentication authentication, Model model) {
        User customer = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (customer != null) {
            List<Order> orders = orderService.findByCustomerId(customer.getId());
            model.addAttribute("orders", orders);
        }
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", "CUSTOMER");
        return "customer_orders";
    }

    /**
     * Hủy đơn hàng cho khách (nếu đơn còn ở trạng thái PENDING)
     */
    @PostMapping("/customer/orders/cancel/{id}")
    public String customerCancelOrder(@PathVariable Long id,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + id));

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && (currentUser == null || order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId()))) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đơn hàng này!");
                return "redirect:/customer/orders";
            }

            if (!"PENDING".equals(order.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy đơn khi đang ở trạng thái Chờ duyệt!");
                return "redirect:/customer/orders";
            }

            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng #" + order.getOrderCode() + " thành công và hoàn trả kho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/customer/orders";
    }
}
