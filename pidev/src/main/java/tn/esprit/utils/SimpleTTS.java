package tn.esprit.utils;

import java.io.IOException;

public class SimpleTTS {
    public static void speak(String text) {
        try {
            String safeText = text.replace("'", "''").replace("\"", "`\"");
            String command = "powershell -Command \"Add-Type -AssemblyName System.Speech; " +
                    "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$synth.Speak('" + safeText + "');\"";
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}