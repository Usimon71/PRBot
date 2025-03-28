package ru.udaltsov.application;

import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    private final AES256TextEncryptor encryptor;

    public EncryptionService() {
        this.encryptor = new AES256TextEncryptor();
        encryptor.setPassword(System.getenv("ENCRYPTION_PASSWORD"));
    }

    public String encrypt(String data) {
        return encryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        return encryptor.decrypt(encryptedData);
    }
}
