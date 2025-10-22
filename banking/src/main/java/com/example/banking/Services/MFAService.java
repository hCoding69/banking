package com.example.banking.Services;


import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

public class MFAService {

    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    /**
     * Génère un secret MFA unique pour l'utilisateur
     */
    public static String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * Vérifie si le code OTP fourni est valide
     */
    public static boolean verifyOtp(String secret, int otpCode) {
        return gAuth.authorize(secret, otpCode);
    }

    /**
     * Génère l'URL "otpauth://" pour QR Code Google Authenticator
     */
    public static String generateOtpAuthURL(String email, String secret) {
        // Format standard : otpauth://totp/ISSUER:EMAIL?secret=SECRET&issuer=ISSUER
        String issuer = "BankingPlatform";
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                email,
                secret,
                issuer
        );
    }
}