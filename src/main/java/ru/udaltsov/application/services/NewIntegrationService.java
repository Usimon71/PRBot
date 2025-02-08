package ru.udaltsov.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.udaltsov.application.MessageSender;

@Service
public class NewIntegrationService {
    private final MessageSender _messageSender;

    @Autowired
    public NewIntegrationService(MessageSender messageSender) {
        _messageSender = messageSender;
    }
}
