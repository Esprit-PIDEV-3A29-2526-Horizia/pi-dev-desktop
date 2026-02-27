package tn.esprit.api.chatbot;


public interface ChatbotCallback {

    void onSuccess(String response);

    void onError(String error);
}