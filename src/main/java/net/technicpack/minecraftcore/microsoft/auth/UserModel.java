package net.technicpack.minecraftcore.microsoft.auth;


import net.technicpack.launchercore.auth.IUserStore;
import net.technicpack.launchercore.auth.IUserType;
import net.technicpack.launchercore.exception.AuthenticationException;
import net.technicpack.launchercore.exception.ResponseException;
import net.technicpack.launchercore.exception.SessionException;

import javax.swing.*;
import java.util.Collection;

public class UserModel {
    private IUserType mCurrentUser = null;
    private final IUserStore mUserStore;
    private final MicrosoftAuthenticator microsoftAuthenticator;

    public UserModel(IUserStore userStore, MicrosoftAuthenticator microsoftAuthenticator) {
        this.mCurrentUser = null;
        this.mUserStore = userStore;
        this.microsoftAuthenticator = microsoftAuthenticator;
    }

    public IUserType getCurrentUser() {
        return this.mCurrentUser;
    }

    public void setCurrentUser(IUserType user) {
        this.mCurrentUser = user;

        if (user != null)
            setLastUser(user);
//        this.triggerAuthListeners();
    }

    public void startupAuth() {
        IUserType user = getLastUser();

        if (user == null) {
            setCurrentUser(null);
            return;
        }

        try {
            user.login(this);
            addUser(user);
            setCurrentUser(user);
        } catch (SessionException | ResponseException ex) {
            setCurrentUser(null);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        } catch (AuthenticationException ex) {
            //TODO: This should handle offline users for Microsoft accounts
//            setCurrentUser(mojangAuthenticator.createOfflineUser(user.getDisplayName()));
        }
    }

    public Collection<IUserType> getUsers() {
        return mUserStore.getSavedUsers();
    }

    public IUserType getLastUser() {
        return mUserStore.getUser(mUserStore.getLastUser());
    }

    public IUserType getUser(String username) {
        return mUserStore.getUser(username);
    }

    public void addUser(IUserType user) {
        mUserStore.addUser(user);
    }

    public void removeUser(IUserType user) {
        mUserStore.removeUser(user.getUsername());
    }

    public void setLastUser(IUserType user) {
        mUserStore.setLastUser(user.getUsername());
    }

    public MicrosoftAuthenticator getMicrosoftAuthenticator() {
        return microsoftAuthenticator;
    }
}
