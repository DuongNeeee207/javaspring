package com.dt.khohang.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dt.khohang.entity.Inventory;
import com.dt.khohang.entity.Order;
import com.dt.khohang.entity.Product;
import com.dt.khohang.service.InventoryService;
import com.dt.khohang.service.LogService;
import com.dt.khohang.service.OrderService;
import com.dt.khohang.service.ProductService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final LogService logService;

    public AdminController(ProductService productService,
                           InventoryService inventoryService,
                           OrderService orderService,
                           LogService logService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.logService = logService;
    }

    @org.springframework.web.bind.annotation.ModelAttribute
    public void addCommonAttributes(Authentication authentication, Model model) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("isAdmin", true);
            model.addAttribute("role", "ADMIN");
        }
    }

    /**
     * Chuyển hướng /admin về trang tổng quan /home
     */
    @GetMapping("")
    public String adminDashboard() {
        return "redirect:/home";
    }

    // ==================== QUẢN LÝ SẢN PHẨM ====================

    @GetMapping("/products")
    public String adminProducts(@RequestParam(required = false) String keyword,
                                Model model) {
        List<Product> products;
        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchByName(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            products = productService.findAll();
        }
        model.addAttribute("products", products);
        return "admin_products";
    }

    @PostMapping("/products/create")
    public String createProduct(@RequestParam String name,
                                @RequestParam String sku,
                                @RequestParam String category,
                                @RequestParam Double price,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false, defaultValue = "Cái") String unit,
                                @RequestParam(required = false, defaultValue = "0") int initialQty,
                                @RequestParam(required = false, defaultValue = "10") int minThreshold,
                                @RequestParam(required = false, defaultValue = "Kho Tổng A1") String location,
                                RedirectAttributes redirectAttributes) {
        try {
            Product product = new Product(name, sku, category, price, description, imageUrl, unit);
            productService.createProduct(product, initialQty, minThreshold, location);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm \"" + name + "\" thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String sku,
                                @RequestParam String category,
                                @RequestParam Double price,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false, defaultValue = "Cái") String unit,
                                RedirectAttributes redirectAttributes) {
        try {
            Product updated = new Product(name, sku, category, price, description, imageUrl, unit);
            productService.updateProduct(id, updated);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ==================== QUẢN LÝ KHO HÀNG ====================

    @GetMapping("/inventory")
    public String adminInventory(Model model) {
        List<Inventory> inventoryList = inventoryService.findAll();
        model.addAttribute("inventoryList", inventoryList);
        model.addAttribute("lowStockList", inventoryService.findLowStock());
        return "admin_inventory";
    }

    @PostMapping("/inventory/addstock/{id}")
    public String addStock(@PathVariable Long id,
                           @RequestParam int addQty,
                           RedirectAttributes redirectAttributes) {
        try {
            inventoryService.addStock(id, addQty);
            redirectAttributes.addFlashAttribute("success", "Đã nhập thêm " + addQty + " đơn vị vào kho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    @PostMapping("/inventory/update/{id}")
    public String updateInventory(@PathVariable Long id,
                                  @RequestParam(required = false) Integer quantity,
                                  @RequestParam(required = false) Integer minThreshold,
                                  @RequestParam(required = false) String location,
                                  RedirectAttributes redirectAttributes) {
        try {
            inventoryService.updateInventory(id, quantity, minThreshold, location);
            redirectAttributes.addFlashAttribute("success", "Cập nhật tồn kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    // ==================== QUẢN LÝ ĐƠN HÀNG ====================

    @GetMapping("/orders")
    public String adminOrders(@RequestParam(required = false) String status,
                              Model model) {
        List<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderService.findByStatus(status);
            model.addAttribute("filterStatus", status);
        } else {
            orders = orderService.findAll();
        }
        model.addAttribute("orders", orders);
        return "admin_orders";
    }

    @PostMapping("/orders/approve/{id}")
    public String approveOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.approveOrder(id);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt đơn hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/ship/{id}")
    public String shipOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.shipOrder(id);
            redirectAttributes.addFlashAttribute("success", "Đã xuất kho giao hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/cancel/{id}")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng và hoàn kho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // ==================== NHẬT KÝ AOP LOGGING ====================

    @GetMapping("/logs")
    public String adminLogs(Model model) {
        model.addAttribute("logs", logService.getAllLogs());
        return "admin_logs";
    }
}