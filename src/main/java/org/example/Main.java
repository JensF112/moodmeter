package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.pi4j.Pi4J;
import com.pi4j.boardinfo.util.BoardInfoHelper;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.util.Console;


public class Main {

    private static final int PIN_BUTTON = 24; // PIN 18 = BCM 24
    private static final int PIN_LED = 22; // PIN 15 = BCM 22

    private static int pressCount = 0;

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/deine_datenbank";
    private static final String JDBC_USER = "dein_benutzer";
    private static final String JDBC_PASSWORD = "dein_passwort";
    /**
     * This  application blinks a led and counts the number the button is pressed. The blink speed increases with each
     * button press, and after 5 presses the application finishes.
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] args) throws Exception {

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // 1. JDBC-Treiber laden (optional ab JDBC 4.0)
            // Class.forName("org.postgresql.Driver");

            // 2. Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            System.out.println("Verbindung zur Datenbank hergestellt!");

        } catch (SQLException e) {
            System.err.println("Datenbank-Fehler aufgetreten: " + e.getMessage());
        }


        // Create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate stdin/stdout code)
        final var console = new Console();

        // Print program title/header
        console.title("<-- The Pi4J Project -->", "Minimal Example project");

        // ************************************************************
        //
        // WELCOME TO Pi4J:
        //
        // Here we will use this getting started example to
        // demonstrate the basic fundamentals of the Pi4J library.
        //
        // This example is to introduce you to the boilerplate
        // logic and concepts required for all applications using
        // the Pi4J library.  This example will do use some basic I/O.
        // Check the pi4j-examples project to learn about all the I/O
        // functions of Pi4J.
        //
        // ************************************************************

        // ------------------------------------------------------------
        // Initialize the Pi4J Runtime Context
        // ------------------------------------------------------------
        // Before you can use Pi4J you must initialize a new runtime
        // context.
        //
        // The 'Pi4J' static class includes a few helper context
        // creators for the most common use cases.  The 'newAutoContext()'
        // method will automatically load all available Pi4J
        // extensions found in the application's classpath which
        // may include 'Platforms' and 'I/O Providers'
        var pi4j = Pi4J.newAutoContext();

        // ------------------------------------------------------------
        // Output Pi4J Context information
        // ------------------------------------------------------------
        // The created Pi4J Context initializes platforms, providers
        // and the I/O registry. To help you to better understand this
        // approach, we print out the info of these. This can be removed
        // from your own application.
        // OPTIONAL
        //PrintInfo.printLoadedPlatforms(console, pi4j);
        //PrintInfo.printDefaultPlatform(console, pi4j);
        //PrintInfo.printProviders(console, pi4j);

        // ------------------------------------------------------------
        // Output Pi4J Board information
        // ------------------------------------------------------------
        // When the Pi4J Context is initialized, a board detection is
        // performed. You can use this info in case you need board-specific
        // functionality.
        // OPTIONAL
        console.println("Board model: " + pi4j.boardInfo().getBoardModel().getLabel());
        console.println("Operating system: " + pi4j.boardInfo().getOperatingSystem());
        console.println("Java versions: " + pi4j.boardInfo().getJavaInfo());
        // This info is also available directly from the BoardInfoHelper,
        // and with some additional realtime data.
        console.println("Board model: " + BoardInfoHelper.current().getBoardModel().getLabel());
        console.println("Raspberry Pi model with RP1 chip (Raspberry Pi 5): " + BoardInfoHelper.usesRP1());
        console.println("OS is 64-bit: " + BoardInfoHelper.is64bit());
        console.println("JVM memory used (MB): " + BoardInfoHelper.getJvmMemory().getUsedInMb());
        console.println("Board temperature (°C): " + BoardInfoHelper.getBoardReading().getTemperatureInCelsius());

        // Here we will create the I/O interface for a LED with minimal code.
        var led = pi4j.digitalOutput().create(PIN_LED);

        // The button needs a bit more configuration, so we use a config builder.
        var buttonConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("button")
                .name("Press button")
                .address(PIN_BUTTON)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3000L);
        var button = pi4j.create(buttonConfig);
        button.addListener(e -> {
            if (e.state() == DigitalState.LOW) {
                pressCount++;
                console.println("Button was pressed for the " + pressCount + "th time");
            }
        });

        // OPTIONAL: print the registry
        //PrintInfo.printRegistry(console, pi4j);

        while (true) {
            if (led.state() == DigitalState.HIGH) {
                console.println("LED low");
                led.low();
            } else {
                console.println("LED high");
                led.high();
            }
            Thread.sleep(500 / (pressCount + 1));
        }

        // ------------------------------------------------------------
        // Terminate the Pi4J library
        // ------------------------------------------------------------
        // We we are all done and want to exit our application, we must
        // call the 'shutdown()' function on the Pi4J static helper class.
        // This will ensure that all I/O instances are properly shutdown,
        // released by the the system and shutdown in the appropriate
        // manner. Terminate will also ensure that any background
        // threads/processes are cleanly shutdown and any used memory
        // is returned to the system.

        // Shutdown Pi4J
      //  pi4j.shutdown();
    }
}