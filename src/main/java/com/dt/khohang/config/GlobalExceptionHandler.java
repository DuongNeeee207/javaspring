package com.dt.khohang.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Xử lý ngoại lệ tập trung (Global Exception Handling) cho hệ thống Spring MVC
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.error("[Exception] Lỗi thực thi tại {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/home";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, HttpServletRequest request, Model model) {
        logger.error("[Exception] Ngoại lệ chưa xử lý tại {}: ", request.getRequestURI(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }
}
