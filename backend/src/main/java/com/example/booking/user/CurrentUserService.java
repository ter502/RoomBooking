package com.example.booking.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

    // 第 1 階段的開發用假使用者；第 2 階段改成從登入資料取得
    private static final String DEV_SUB = "dev-user";

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser currentUser() {
        return appUserRepository.findByKeycloakSub(DEV_SUB)
                .orElseGet(() -> appUserRepository.save(
                        new AppUser(DEV_SUB, "dev@example.com", "Dev User")));
    }
}