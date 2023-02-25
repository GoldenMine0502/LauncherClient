package net.technicpack.launchercore.auth;

import net.technicpack.launchercore.exception.AuthenticationException;
import net.technicpack.minecraftcore.microsoft.auth.UserModel;

public interface IUserType {
    String getUserType();

    String getId();

    String getUsername();

    String getDisplayName();

    String getAccessToken();

    String getSessionId();

    String getMCUserType();

    String getUserProperties();

    boolean isOffline();

    void login(UserModel userModel) throws AuthenticationException;
}
