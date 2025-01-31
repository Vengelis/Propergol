package fr.vengelis.propergol.core.utils;


import fr.vengelis.propergol.core.language.LanguageManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;

public class SystemInfoUtil {

    public static String getIpAddress() {
        try {
            InetAddress endpoint = InetAddress.getLocalHost();
            return endpoint.getHostAddress();
        } catch (Exception e) {
            return LanguageManager.translate("no-host-found");
        }
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return LanguageManager.translate("no-host-found");
        }
    }

    public static String getMacAddress() {
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                if (network.getHardwareAddress() != null) {
                    byte[] mac = network.getHardwareAddress();
                    StringBuilder macAddress = new StringBuilder();
                    for (byte b : mac) {
                        macAddress.append(String.format("%02X:", b));
                    }
                    if (!macAddress.isEmpty()) {
                        macAddress.deleteCharAt(macAddress.length() - 1); // Retire le dernier ":"
                    }
                    return macAddress.toString();
                }
            }
        } catch (Exception e) {
            return LanguageManager.translate("no-host-found");
        }
        return LanguageManager.translate("no-host-found");
    }

    public static String getHWID() {
        try {
            String base = getMacAddress() + getMotherboardSerial() + getMachineUUID();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            return LanguageManager.translate("no-host-found");
        }
    }

    private static String getMachineUUID() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                return executeCommand("wmic csproduct get uuid");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                return executeCommand("cat /var/lib/dbus/machine-id");
            }
        } catch (Exception ignored) {}
        return "Unknown-UUID";
    }

    private static String getMotherboardSerial() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                return executeCommand("wmic baseboard get serialnumber");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                return executeCommand("sudo dmidecode -s baseboard-serial-number");
            }
        } catch (Exception ignored) {}
        return "Unknown-MB";
    }

    private static String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            return "Error";
        }
    }

    public static void main(String[] args) {
        System.out.println("Adresse IP: " + getIpAddress());
        System.out.println("Nom de la machine: " + getHostname());
        System.out.println("Adresse MAC: " + getMacAddress());
        System.out.println("HWID: " + getHWID());
    }
}
