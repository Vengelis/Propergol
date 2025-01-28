package fr.vengelis.propergol.core.utils;

import fr.vengelis.propergol.core.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;

public class VersionChecker {

    private static final String VERSION_URL = "https://raw.githubusercontent.com/Vengelis/Propergol/refs/heads/master/publishedversion.txt";

    public static void check() {
        String currentVersion = getCurrentVersion();
        String publishedVersion = getPublishedVersion();

        ConsoleLogger.printLine(Level.INFO, "Checking updates...");
        if (currentVersion == null || publishedVersion == null) {
            ConsoleLogger.printLine(Level.SEVERE, "Unable to determine versions for comparison. Github link is unreachable.");
            return;
        }

        if (currentVersion.equals(publishedVersion)) {
            ConsoleLogger.printLine(Level.INFO, "Afterburner is up to date.");
        } else {
            ConsoleLogger.printLineBox(Level.WARNING, "A new version is available: " + publishedVersion);
        }
    }

    private static String getCurrentVersion() {
        return Core.getVersion();
    }

    private static String getPublishedVersion() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(VERSION_URL).openStream()))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}