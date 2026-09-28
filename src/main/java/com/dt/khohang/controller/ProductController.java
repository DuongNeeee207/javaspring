package com.dt.khohang.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dt.khohang.entity.Product;
import com.dt.khohang.service.ProductService;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Trang danh sách sản phẩm (cho cả CUSTOMER và ADMIN xem)
     */
    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String category,
                               Authentication authentication,
                               Model model) {

        List<Product> products;

        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchByName(keyword);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.isBlank()) {
            products = productService.findByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            products = productService.findAll();
        }

        model.addAttribute("products", products);

        // Thông tin user
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("role", isAdmin ? "ADMIN" : "CUSTOMER");
        }

        return "products";
    }
}
