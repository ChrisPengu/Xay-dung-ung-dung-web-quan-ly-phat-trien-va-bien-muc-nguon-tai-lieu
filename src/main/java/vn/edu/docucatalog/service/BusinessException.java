package vn.edu.docucatalog.service;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
