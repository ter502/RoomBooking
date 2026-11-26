package com.example.booking.user;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser currentUser() {
        OidcUser oidcUser = currentOidcUser();
        String sub = oidcUser.getSubject();
        String email = Objects.requireNonNullElse(oidcUser.getEmail(), "");
        String displayName = displayNameOf(oidcUser);

        return appUserRepository.findByKeycloakSub(sub)
                .map(user -> {
                    user.updateProfile(email, displayName);
                    return user;
                })
                .orElseGet(() -> appUserRepository.save(new AppUser(sub, email, displayName)));
    }

    private static OidcUser currentOidcUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return oidcUser;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    private static String displayNameOf(OidcUser user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        if (user.getPreferredUsername() != null) {
            return user.getPreferredUsername();
        }
        return user.getSubject();
    }
}