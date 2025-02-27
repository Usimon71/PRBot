package ru.udaltsov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(Main.class, args);
    }
}