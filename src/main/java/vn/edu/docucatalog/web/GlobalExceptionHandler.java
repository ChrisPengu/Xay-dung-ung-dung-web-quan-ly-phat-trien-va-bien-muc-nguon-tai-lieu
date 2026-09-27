package vn.edu.docucatalog.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(ResourceNotFoundException ex, HttpServletRequest request, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error/404";
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String business(BusinessException ex, HttpServletRequest request, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error/400";
    }
}
