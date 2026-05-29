package com.skillbridge.backend.user.client;

import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.common.exception.ResourceNotFoundException;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import com.skillbridge.backend.user.client.dto.ClientProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClientProfile createOrUpdate(UUID userId, ClientProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.CLIENT) {
            throw new UnauthorizedException("Only clients can create a client profile");
        }

        ClientProfile profile = clientProfileRepository.findByUserId(userId)
                .orElse(ClientProfile.builder().user(user).build());

        profile.setBusinessName(request.getBusinessName());
        profile.setCategory(request.getCategory());
        profile.setCity(request.getCity());
        profile.setContactNumber(request.getContactNumber());
        profile.setWebsite(request.getWebsite());

        return clientProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public ClientProfile getProfile(UUID userId) {
        return clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }
}