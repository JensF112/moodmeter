package org.example;

import com.pi4j.io.gpio.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Main {

    private static final int[] BUTTON_PINS = {1, 4, 5, 6}; // GPIO Pins für die Knöpfe
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/datenbank";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "admin";

    public static void main(String[] args) throws InterruptedException {

        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalInput[] buttons = new GpioPinDigitalInput[BUTTON_PINS.length];

        // Knöpfe initialisieren
        for (int i = 0; i < BUTTON_PINS.length; i++) {
            buttons[i] = gpio.provisionDigitalInputPin(RaspiPin.getPinByAddress(BUTTON_PINS[i]), "Button " + i, PinPullResistance.PULL_DOWN);
            final int buttonId = i; // Für den Event-Listener

            buttons[i].addListener((GpioPinListenerDigitalBinding) event -> {
                if (event.getState() == PinState.HIGH) {
                    recordButtonClick(buttonId);
                }
            });
        }
        System.out.println("Moodmeter läuft... (Strg+C zum Beenden)");
        Thread.sleep(Long.MAX_VALUE); // Programm am Laufen halten
    }
    private static void recordButtonClick(int buttonId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO button_clicks (button_id, timestamp) VALUES (?, ?)")) {

            statement.setInt(1, buttonId);
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
            System.out.println("Button " + buttonId + " gedrückt und gespeichert.");
        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern in die Datenbank: " + e.getMessage());
        }
    }
}