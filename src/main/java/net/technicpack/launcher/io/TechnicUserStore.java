package net.technicpack.launcher.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.technicpack.launchercore.auth.IUserStore;
import net.technicpack.launchercore.auth.IUserType;
import net.technicpack.utilslib.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class TechnicUserStore implements IUserStore {
    private String clientToken = UUID.randomUUID().toString();
    private Map<String, IUserType> savedUsers = new HashMap<>();
    private String lastUser;
    private transient File usersFile;

    public TechnicUserStore() {
    }

    public TechnicUserStore(File userFile) {
        this.usersFile = userFile;
    }

    private static Gson gson = new GsonBuilder().registerTypeAdapter(IUserType.class, new IUserTypeInstanceCreator()).setPrettyPrinting().create();

    public static TechnicUserStore load(File userFile) {
        if (!userFile.exists()) {
            Utils.getLogger().log(Level.WARNING, "Unable to load users from " + userFile + " because it does not exist.");
            return new TechnicUserStore(userFile);
        }

        try {
            String json = FileUtils.readFileToString(userFile, StandardCharsets.UTF_8);
            TechnicUserStore newModel = gson.fromJson(json, TechnicUserStore.class);

            if (newModel != null) {
                newModel.setUserFile(userFile);
                return newModel;
            }
        } catch (JsonSyntaxException | IOException e) {
            Utils.getLogger().log(Level.WARNING, "Unable to load users from " + userFile);
        }

        return new TechnicUserStore(userFile);
    }

    public void setUserFile(File userFile) {
        this.usersFile = userFile;
    }

    public void save() {
        String json = gson.toJson(this);

        try {
            FileUtils.writeStringToFile(usersFile, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Utils.getLogger().log(Level.WARNING, "Unable to save users " + usersFile);
        }
    }

    public void addUser(IUserType user) {
        if (savedUsers.containsKey(user.getUsername())) {
            IUserType oldUser = savedUsers.get(user.getUsername());
//            if (oldUser instanceof MojangUser && user instanceof MojangUser) {
//                ((MojangUser) user).mergeUserProperties((MojangUser) oldUser);
//            }
        }
        savedUsers.put(user.getUsername(), user);
        save();
    }

    public void removeUser(String username) {
        savedUsers.remove(username);
        save();
    }

    public IUserType getUser(String accountName) {
        return savedUsers.get(accountName);
    }

    public String getClientToken() {
        return clientToken;
    }

    public Collection<String> getUsers() {
        return savedUsers.keySet();
    }

    public Collection<IUserType> getSavedUsers() {
        return savedUsers.values();
    }

    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
        save();
    }

    public String getLastUser() {
        return lastUser;
    }
}
