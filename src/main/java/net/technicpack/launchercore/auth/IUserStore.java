package net.technicpack.launchercore.auth;

import java.util.Collection;

public interface IUserStore {
    void addUser(IUserType user);

    void removeUser(String username);

    IUserType getUser(String username);

    String getClientToken();

    Collection<String> getUsers();

    Collection<IUserType> getSavedUsers();

    void setLastUser(String username);

    String getLastUser();
}
