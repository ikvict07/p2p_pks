package sk.stuba.pks.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class IpUtil {
    public static String getIp() {
        String command = "ipconfig getifaddr en0";
        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            while ((line = errorReader.readLine()) != null) {
                System.err.println("ERROR: " + line);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Command exited with code " + exitCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot get IP address", e);
        }

        return output.toString();
    }

    public static void main(String[] args) {
        System.out.println(getIp());
    }
}