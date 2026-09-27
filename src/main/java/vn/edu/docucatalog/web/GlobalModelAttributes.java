package vn.edu.docucatalog.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.edu.docucatalog.security.CustomUserDetails;

@ControllerAdvice
public class GlobalModelAttributes {
    @ModelAttribute("appName")
    public String appName() {
        return "DocuCatalog";
    }

    @ModelAttribute("currentUser")
    public CustomUserDetails currentUser(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details ? details : null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }

    @ModelAttribute("canCatalog")
    public boolean canCatalog(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_CATALOGER");
    }

    @ModelAttribute("canAcquire")
    public boolean canAcquire(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_ACQUISITION");
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
