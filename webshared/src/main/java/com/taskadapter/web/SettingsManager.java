package com.taskadapter.web;

import java.util.prefs.Preferences;

/**
 * @author Alexey Skorokhodov
 */
public class SettingsManager {
    public static final boolean DEFAULT_LOCAL = true;

    private LocalRemoteModeListener listener;

    private Preferences prefs = Preferences.userNodeForPackage(SettingsManager.class);

    /**
     * Is TA working on the local machine?
     */
    public boolean isLocal() {
        return prefs.getBoolean("TALocal", DEFAULT_LOCAL);
    }

    public void setLocal(boolean local) {
        prefs.putBoolean("TALocal", local);
        if (listener != null) {
            listener.modeChanged(local);
        }
    }

    public void setLocalRemoteListener(LocalRemoteModeListener listener) {
        this.listener = listener;
    }
}