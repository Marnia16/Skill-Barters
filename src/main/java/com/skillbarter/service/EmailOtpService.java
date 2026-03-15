package com.skillbarter.service;

import javax.mail.*;
import javax.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * CONCEPT 7 + 6: Sends OTP emails via Gmail SMTP on a background thread.
 * Stores OTPs in memory with a 2-minute expiry window.
 *
 * SETUP REQUIRED:
 *   1. Add javax.mail dependency to pom.xml (see bottom of this file)
 *   2. Set your Gmail address in SENDER_EMAIL
 *   3. Set your Gmail App Password in SENDER_PASSWORD
 *      (Gmail → Settings → Security → 2FA → App Passwords → generate one)
 */
public class EmailOtpService {

    // ── CONFIG — change these two values ──────────────────────
    private static final String SENDER_EMAIL    = "trini160306@gmail.com";
    private static final String SENDER_PASSWORD = "qube irmg fuee kwcc";
    // ──────────────────────────────────────────────────────────

    private static final int    OTP_LENGTH      = 6;
    private static final int    OTP_EXPIRY_MINS = 2;

    // In-memory store: email → {otp, expiry}
    private static final Map<String, OtpEntry> otpStore = new HashMap<>();
    private static final SecureRandom rng = new SecureRandom();

    // ── Generate + send OTP ────────────────────────────────────
    /**
     * Generates a 6-digit OTP, stores it, then sends it on a
     * background thread so the UI doesn't freeze.
     * @param toEmail    recipient's email
     * @param onSuccess  called on EDT when email is sent
     * @param onFailure  called on EDT with error message
     */
    public static void sendOtp(String toEmail,
                                Runnable onSuccess,
                                java.util.function.Consumer<String> onFailure) {
        String otp = generateOtp();
        otpStore.put(toEmail.toLowerCase(), new OtpEntry(otp,
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINS)));

        // CONCEPT 7: Background thread for email sending
        Thread t = new Thread(() -> {
            try {
                sendEmail(toEmail, otp);
                if (onSuccess != null)
                    javax.swing.SwingUtilities.invokeLater(onSuccess);
            } catch (Exception ex) {
                System.err.println("[OTP] Send failed: " + ex.getMessage());
                if (onFailure != null)
                    javax.swing.SwingUtilities.invokeLater(
                        () -> onFailure.accept("Failed to send OTP: " + ex.getMessage()));
            }
        }, "OtpSender");
        t.setDaemon(true);
        t.start();
    }

    // ── Verify OTP ─────────────────────────────────────────────
    public static VerifyResult verify(String email, String enteredOtp) {
        String key = email.toLowerCase();
        OtpEntry entry = otpStore.get(key);
        if (entry == null)
            return VerifyResult.NOT_FOUND;
        if (LocalDateTime.now().isAfter(entry.expiry)) {
            otpStore.remove(key);
            return VerifyResult.EXPIRED;
        }
        if (!entry.otp.equals(enteredOtp.trim()))
            return VerifyResult.WRONG;
        otpStore.remove(key); // one-time use
        return VerifyResult.SUCCESS;
    }

    public enum VerifyResult { SUCCESS, WRONG, EXPIRED, NOT_FOUND }

    // ── Internal helpers ───────────────────────────────────────
    private static String generateOtp() {
        int n = rng.nextInt(900000) + 100000; // 100000–999999
        return String.valueOf(n);
    }

    private static void sendEmail(String to, String otp) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SENDER_EMAIL, "Skill Barters"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject("Your Skill Barters Verification Code");
        msg.setContent(buildHtmlEmail(otp), "text/html; charset=utf-8");
        Transport.send(msg);
        System.out.println("[OTP] Sent to: " + to);
    }

    private static String buildHtmlEmail(String otp) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#0E0602;font-family:sans-serif'>" +
               "<div style='max-width:480px;margin:40px auto;background:#1A0A02;border:1px solid rgba(196,154,108,.3);" +
               "border-radius:12px;padding:36px 32px;text-align:center'>" +
               "<h1 style='font-size:22px;color:#EDD9C0;letter-spacing:2px;margin-bottom:6px'>Skill Barters</h1>" +
               "<p style='font-size:11px;color:#A0714F;letter-spacing:3px;margin-bottom:28px'>SKILL IN VALUE OUT</p>" +
               "<div style='width:60px;height:1px;background:#C49A6C;margin:0 auto 28px;opacity:.5'></div>" +
               "<p style='color:#A0714F;font-size:13px;margin-bottom:16px'>Your verification code is</p>" +
               "<div style='background:#0E0602;border:1px solid rgba(196,154,108,.3);border-radius:10px;" +
               "padding:18px;letter-spacing:14px;font-size:32px;font-weight:700;color:#C49A6C;margin-bottom:20px'>" +
               otp + "</div>" +
               "<p style='color:#A0714F;font-size:12px;margin-bottom:6px'>This code expires in <strong style='color:#C49A6C'>2 minutes</strong>.</p>" +
               "<p style='color:rgba(160,113,79,.5);font-size:11px'>If you didn't request this, please ignore this email.</p>" +
               "</div></body></html>";
    }

    // ── Inner classes ──────────────────────────────────────────
    private static class OtpEntry {
        final String otp;
        final LocalDateTime expiry;
        OtpEntry(String otp, LocalDateTime expiry) { this.otp=otp; this.expiry=expiry; }
    }
}

/*
 * ── ADD THIS TO pom.xml dependencies ──────────────────────────
 *
 * <dependency>
 *     <groupId>com.sun.mail</groupId>
 *     <artifactId>javax.mail</artifactId>
 *     <version>1.6.2</version>
 * </dependency>
 *
 * ─────────────────────────────────────────────────────────────
 */
