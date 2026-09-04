package com.guardianai.data.tracking;

import java.util.ArrayList;
import java.util.List;

public class TrackingStateManager {

    public enum TrackingState {
        STOPPED,
        STARTING,
        RUNNING,
        PAUSED,
        PERMISSION_REQUIRED,
        OFFLINE,
        ERROR
    }

    public interface StateListener {
        void onStateChanged(TrackingState newState);
    }

    private static TrackingStateManager instance;
    private TrackingState currentState = TrackingState.STOPPED;
    private final List<StateListener> listeners = new ArrayList<>();

    public static synchronized TrackingStateManager getInstance() {
        if (instance == null) {
            instance = new TrackingStateManager();
        }
        return instance;
    }

    public synchronized TrackingState getCurrentState() {
        return currentState;
    }

    public synchronized void setState(TrackingState newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            notifyListeners(newState);
        }
    }

    public synchronized void addListener(StateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            listener.onStateChanged(currentState);
        }
    }

    public synchronized void removeListener(StateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(TrackingState state) {
        for (StateListener listener : new ArrayList<>(listeners)) {
            listener.onStateChanged(state);
        }
    }
}
