package ai.nexus.desktop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class MacKeychainCredentialStore implements CredentialStore {

    private static final String SERVICE = "Nexus AI Session";
    private static final String ACCOUNT = "nexus-ai-desktop";

    @Override
    public String get(String key) {
        return run("security", "find-generic-password", "-a", ACCOUNT,
                "-s", SERVICE + " " + key, "-w");
    }

    @Override
    public void put(String key, String value) {
        run("security", "add-generic-password", "-U", "-a", ACCOUNT,
                "-s", SERVICE + " " + key, "-w", value);
    }

    @Override
    public void remove(String key) {
        run("security", "delete-generic-password", "-a", ACCOUNT,
                "-s", SERVICE + " " + key);
    }

    private String run(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished || process.exitValue() != 0) {
                process.destroyForcibly();
                return null;
            }
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            return null;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
