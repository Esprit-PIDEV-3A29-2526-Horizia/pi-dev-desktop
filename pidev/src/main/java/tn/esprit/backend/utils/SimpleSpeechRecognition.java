package tn.esprit.backend.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class SimpleSpeechRecognition {

    public static CompletableFuture<String> recognize(String languageCode) {
        CompletableFuture<String> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                String culture = switch (languageCode) {
                    case "fr-FR" -> "fr-FR";
                    case "en-US" -> "en-US";
                    case "ar-SA" -> "ar-SA";
                    default -> "fr-FR";
                };

                // Commande PowerShell avec capture des erreurs
                String command = "powershell -Command \"" +
                        "$ErrorActionPreference = 'Stop'; " +
                        "[Console]::OutputEncoding = [Text.Encoding]::UTF8; " +
                        "try { " +
                        "   $recognizer = New-Object System.Speech.Recognition.SpeechRecognitionEngine([System.Globalization.CultureInfo]::GetCultureInfo('" + culture + "')); " +
                        "   $recognizer.SetInputToDefaultAudioDevice(); " +
                        "   $grammar = New-Object System.Speech.Recognition.DictationGrammar; " +
                        "   $recognizer.LoadGrammar($grammar); " +
                        "   $result = $recognizer.Recognize(); " +
                        "   if ($result -ne $null) { $result.Text } else { '' } " +
                        "} catch { " +
                        "   'Erreur: ' + $_.Exception.Message " +
                        "}\" 2>&1";

                Process process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                int exitCode = process.waitFor();
                String result = output.toString().trim();

                if (exitCode == 0 && !result.isEmpty() && !result.startsWith("Erreur:")) {
                    future.complete(result);
                } else {
                    System.err.println("Erreur reconnaissance : " + result);
                    future.complete("");
                }
            } catch (Exception e) {
                e.printStackTrace();
                future.complete("");
            }
        }).start();

        return future;
    }
}