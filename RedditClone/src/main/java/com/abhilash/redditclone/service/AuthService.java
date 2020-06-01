package com.abhilash.redditclone.service;

import com.abhilash.redditclone.dto.AuthenticationResponse;
import com.abhilash.redditclone.dto.LoginRequest;
import com.abhilash.redditclone.dto.RegisterRequest;
import com.abhilash.redditclone.exception.InvalidTokenException;
import com.abhilash.redditclone.exception.InvalidUsernameException;
import com.abhilash.redditclone.model.NotificationEmail;
import com.abhilash.redditclone.model.User;
import com.abhilash.redditclone.model.VerificationToken;
import com.abhilash.redditclone.repo.UserRepo;
import com.abhilash.redditclone.repo.VerificationTokenRepo;
import com.abhilash.redditclone.security.JWTProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;

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

    public void verifyAndEnableUser(String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByToken(token).orElseThrow(() -> new InvalidTokenException("Invalid token"));
        fetchUserAndEnable(verificationToken);
    }

    @Transactional
    void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new InvalidUsernameException("Invalid username"));
        user.setEnabled(true);
        userRepo.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);
        return new AuthenticationResponse(token, loginRequest.getUsername());
    }
}