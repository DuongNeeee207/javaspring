package com.dt.khohang.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.dt.khohang.entity.Order;
import com.dt.khohang.entity.Product;
import com.dt.khohang.service.LogService;

/**
 * AOP Aspect – Ghi log tự động các hoạt động quan trọng trong hệ thống.
 *
 * Yêu cầu đề bài:
 * - Ghi log khi đăng nhập
 * - Ghi log khi đặt hàng
 * - Ghi log khi thêm sản phẩm
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    private final LogService logService;

    public LoggingAspect(LogService logService) {
        this.logService = logService;
    }

    // ======================== POINTCUTS ========================

    @Pointcut("execution(* com.dt.khohang.service.ProductService.createProduct(..))")
    public void productCreatePointcut() {}

    @Pointcut("execution(* com.dt.khohang.service.ProductService.updateProduct(..))")
    public void productUpdatePointcut() {}

    @Pointcut("execution(* com.dt.khohang.service.ProductService.deleteProduct(..))")
    public void productDeletePointcut() {}

    @Pointcut("execution(* com.dt.khohang.service.OrderService.createOrder(..))")
    public void orderCreatePointcut() {}

    @Pointcut("execution(* com.dt.khohang.service.OrderService.approveOrder(..))")
    public void orderApprovePointcut() {}

    @Pointcut("execution(* com.dt.khohang.service.OrderService.shipOrder(..))")
    public void orderShipPointcut() {}

    @Pointcut("execution(* com.dt.khohang.service.OrderService.cancelOrder(..))")
    public void orderCancelPointcut() {}

    @Pointcut("execution(* com.dt.khohang.service.InventoryService.addStock(..))")
    public void inventoryAddStockPointcut() {}

    @Pointcut("execution(* com.dt.khohang.service.CustomUserDetailsService.loadUserByUsername(..))")
    public void userLoginPointcut() {}

    // ======================== ADVICES ========================

    /**
     * Log khi thêm sản phẩm mới
     */
    @AfterReturning(pointcut = "productCreatePointcut()", returning = "result")
    public void logProductCreate(JoinPoint joinPoint, Object result) {
        String user = getCurrentUsername();
        Product product = (Product) result;
        String detail = "Thêm sản phẩm mới: \"" + product.getName() + "\" (SKU: " + product.getSku() + ")";
        logger.info("[AOP] {} - {} - User: {}", "THÊM SẢN PHẨM", detail, user);
        logService.addLog("THÊM SẢN PHẨM", detail, user);
    }

    /**
     * Log khi cập nhật sản phẩm
     */
    @AfterReturning(pointcut = "productUpdatePointcut()", returning = "result")
    public void logProductUpdate(JoinPoint joinPoint, Object result) {
        String user = getCurrentUsername();
        Product product = (Product) result;
        String detail = "Cập nhật sản phẩm: \"" + product.getName() + "\" (ID: " + product.getId() + ")";
        logger.info("[AOP] {} - {} - User: {}", "CẬP NHẬT SP", detail, user);
        logService.addLog("CẬP NHẬT SP", detail, user);
    }

    /**
     * Log khi xóa sản phẩm
     */
    @AfterReturning(pointcut = "productDeletePointcut()")
    public void logProductDelete(JoinPoint joinPoint) {
        String user = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        String detail = "Xóa sản phẩm ID: " + args[0];
        logger.info("[AOP] {} - {} - User: {}", "XÓA SẢN PHẨM", detail, user);
        logService.addLog("XÓA SẢN PHẨM", detail, user);
    }

    /**
     * Log khi đặt hàng
     */
    @AfterReturning(pointcut = "orderCreatePointcut()", returning = "result")
    public void logOrderCreate(JoinPoint joinPoint, Object result) {
        String user = getCurrentUsername();
        Order order = (Order) result;
        String detail = "Đặt đơn hàng mới: " + order.getOrderCode()
                + " | Tổng tiền: " + String.format("%,.0f đ", order.getTotalAmount())
                + " | " + order.getOrderDetails().size() + " sản phẩm";
        logger.info("[AOP] {} - {} - User: {}", "ĐẶT HÀNG", detail, user);
        logService.addLog("ĐẶT HÀNG", detail, user);
    }

    /**
     * Log khi duyệt đơn
     */
    @AfterReturning(pointcut = "orderApprovePointcut()", returning = "result")
    public void logOrderApprove(JoinPoint joinPoint, Object result) {
        String user = getCurrentUsername();
        Order order = (Order) result;
        String detail = "Duyệt đơn hàng: " + order.getOrderCode();
        logger.info("[AOP] {} - {} - User: {}", "DUYỆT ĐƠN", detail, user);
        logService.addLog("DUYỆT ĐƠN", detail, user);
    }

    /**
     * Log khi giao hàng
     */
    @AfterReturning(pointcut = "orderShipPointcut()", returning = "result")
    public void logOrderShip(JoinPoint joinPoint, Object result) {
        String user = getCurrentUsername();
        Order order = (Order) result;
        String detail = "Xuất kho giao hàng: " + order.getOrderCode();
        logger.info("[AOP] {} - {} - User: {}", "GIAO HÀNG", detail, user);
        logService.addLog("GIAO HÀNG", detail, user);
    }

    /**
     * Log khi hủy đơn
     */
    @AfterReturning(pointcut = "orderCancelPointcut()", returning = "result")
    public void logOrderCancel(JoinPoint joinPoint, Object result) {
        String user = getCurrentUsername();
        Order order = (Order) result;
        String detail = "Hủy đơn hàng: " + order.getOrderCode() + " (đã hoàn kho)";
        logger.info("[AOP] {} - {} - User: {}", "HỦY ĐƠN", detail, user);
        logService.addLog("HỦY ĐƠN", detail, user);
    }

    /**
     * Log khi nhập thêm hàng vào kho
     */
    @AfterReturning(pointcut = "inventoryAddStockPointcut()", returning = "result")
    public void logAddStock(JoinPoint joinPoint, Object result) {
        String user = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        String detail = "Nhập thêm hàng vào kho ID: " + args[0] + " | Số lượng thêm: +" + args[1];
        logger.info("[AOP] {} - {} - User: {}", "NHẬP KHO", detail, user);
        logService.addLog("NHẬP KHO", detail, user);
    }

    /**
     * Log khi người dùng đăng nhập (loadUserByUsername được gọi)
     */
    @AfterReturning(pointcut = "userLoginPointcut()", returning = "result")
    public void logUserLogin(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        String username = (String) args[0];
        String detail = "Người dùng đăng nhập: " + username;
        logger.info("[AOP] {} - {}", "ĐĂNG NHẬP", detail);
        logService.addLog("ĐĂNG NHẬP", detail, username);
    }

    // ======================== HELPER ========================

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "system";
    }
}
