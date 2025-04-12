package ru.udaltsov.unit.services;

import org.junit.jupiter.api.Test;
import ru.udaltsov.application.services.EncryptionService;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptonSeviceTest {
    private final EncryptionService encryptionService = new EncryptionService();

    @Test
    public void encryptedThenDecryptedShouldBeCorrect() {
        var message = "Hello World!";

        var encrypted = encryptionService.encrypt(message);
        var decrypted = encryptionService.decrypt(encrypted);

        assertEquals(message, decrypted);
    }
}
