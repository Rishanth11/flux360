package com.rishanth.flux360.service;

import com.rishanth.flux360.entity.PasswordResetToken;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.exception.ResourceNotFoundException;
import com.rishanth.flux360.repository.PasswordResetTokenRepository;
import com.rishanth.flux360.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.rishanth.flux360.exception.BadRequestException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public void sendResetLink(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Delete old token if exists
        tokenRepo.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        tokenRepo.save(resetToken);

        sendEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (resetToken.isExpired()) {
            tokenRepo.delete(resetToken);
            throw new BadRequestException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.delete(resetToken);
    }

    private void sendEmail(String to, String token) {
        String resetUrl = "http://localhost:8080/pages/reset-password.html?token=" + token;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("FLUX360 - Reset Your Password");
        msg.setText("Click to reset your password (valid 30 mins):\n" + resetUrl);
        mailSender.send(msg);
    }
}