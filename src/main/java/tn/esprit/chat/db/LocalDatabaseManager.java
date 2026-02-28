package tn.esprit.chat.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import tn.esprit.chat.model.PendingMessage;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class LocalDatabaseManager {

    private static final String DATABASE_NAME = "chat_offline.db";
    private static LocalDatabaseManager instance;
    private Dao<PendingMessage, Integer> messageDao;
    private ConnectionSource connectionSource;

    private LocalDatabaseManager() {
        try {
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + File.separator + ".horizia" + File.separator + DATABASE_NAME;

            new File(userHome + File.separator + ".horizia").mkdirs();

            connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbPath);
            messageDao = DaoManager.createDao(connectionSource, PendingMessage.class);
            TableUtils.createTableIfNotExists(connectionSource, PendingMessage.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized LocalDatabaseManager getInstance() {
        if (instance == null) {
            instance = new LocalDatabaseManager();
        }
        return instance;
    }

    public void addPendingMessage(PendingMessage message) {
        try {
            messageDao.create(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PendingMessage> getPendingMessages() {
        try {
            QueryBuilder<PendingMessage, Integer> qb = messageDao.queryBuilder();
            qb.where().eq("synced", false);
            qb.orderBy("timestamp", true);
            return qb.query();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void markAsSynced(PendingMessage message) {
        message.setSynced(true);
        try {
            messageDao.update(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cleanSyncedMessages() {
        try {
            messageDao.deleteBuilder()
                    .where()
                    .eq("synced", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connectionSource != null) {
                connectionSource.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}