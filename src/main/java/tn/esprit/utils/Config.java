package tn.esprit.utils;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in == null) throw new RuntimeException("config.properties introuvable dans resources");
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Erreur chargement config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}