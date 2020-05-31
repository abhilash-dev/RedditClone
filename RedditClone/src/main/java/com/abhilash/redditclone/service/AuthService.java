package com.abhilash.redditclone.service;

import com.abhilash.redditclone.dto.RegisterRequest;
import com.abhilash.redditclone.model.NotificationEmail;
import com.abhilash.redditclone.model.User;
import com.abhilash.redditclone.model.VerificationToken;
import com.abhilash.redditclone.repo.UserRepo;
import com.abhilash.redditclone.repo.VerificationTokenRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final VerificationTokenRepo verificationTokenRepo;
    private final MailService mailService;

    @Transactional
    public void signup(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepo.save(user);

        String token = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail("Please activate your account",
                user.getEmail(), "Thank you for signing up to Reddit-Clone, " +
                "Please click on the link / copy the url below to activate your account : " +
                "http://localhost:8080/api/auth/verify/" + token));
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationTokenRepo.save(verificationToken);
        return token;
    }
}