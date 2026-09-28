package com.dt.khohang.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dt.khohang.service.InventoryService;
import com.dt.khohang.service.OrderService;
import com.dt.khohang.service.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    public HomeController(ProductService productService,
                          InventoryService inventoryService,
                          OrderService orderService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication authentication, Model model) {
        String username = "Khách";
        String role = "CUSTOMER";
        boolean isAdmin = false;

        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
            isAdmin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            role = isAdmin ? "ADMIN" : "CUSTOMER";
        }

        // Nếu là khách hàng, chuyển hướng ngay đến trang Danh mục sản phẩm để mua sắm
        if (!isAdmin) {
            return "redirect:/products";
        }

        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("isAdmin", true);

        // KPI stats từ database thực tế
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("totalStockValue", String.format("%,.0f đ", inventoryService.getTotalStockValue()));
        model.addAttribute("totalOrders", orderService.count());
        model.addAttribute("pendingOrders", orderService.countByStatus("PENDING"));
        model.addAttribute("lowStockAlertCount", inventoryService.countLowStock());

        // Dữ liệu chi tiết
        model.addAttribute("lowStockItems", inventoryService.findLowStock());
        model.addAttribute("recentOrders", orderService.findAll());
        model.addAttribute("products", productService.findAll());

        return "home";
    }
}