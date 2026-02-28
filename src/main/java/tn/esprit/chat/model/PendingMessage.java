package tn.esprit.chat.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.sql.Timestamp;

@DatabaseTable(tableName = "offline_messages")
public class PendingMessage {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String senderUsername;

    @DatabaseField
    private String recipient; // "ALL" pour le groupe, ou nom d'utilisateur

    @DatabaseField
    private String content;

    @DatabaseField
    private Timestamp timestamp;

    @DatabaseField
    private boolean isGroupMessage;

    @DatabaseField
    private boolean synced = false;

    public PendingMessage() {}

    public PendingMessage(String sender, String recipient, String content, boolean isGroup) {
        this.senderUsername = sender;
        this.recipient = recipient;
        this.content = content;
        this.isGroupMessage = isGroup;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.synced = false;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isGroupMessage() { return isGroupMessage; }
    public void setGroupMessage(boolean groupMessage) { isGroupMessage = groupMessage; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}