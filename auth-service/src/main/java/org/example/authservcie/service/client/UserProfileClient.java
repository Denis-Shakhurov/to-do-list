package org.example.authservcie.service.client;

import org.example.authservcie.dto.CreateUserProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserProfileClient {
    @PostMapping("/users/")
    void createUser(@RequestBody CreateUserProfileRequest request);
}
