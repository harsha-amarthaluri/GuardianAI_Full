package com.guardianai;

import com.guardianai.data.tracking.TrackingStateManager;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TrackingStateTest {

    private TrackingStateManager trackingStateManager;
    private TrackingStateManager.TrackingState lastStateReceived;

    @Before
    public void setUp() {
        trackingStateManager = TrackingStateManager.getInstance();
        trackingStateManager.setState(TrackingStateManager.TrackingState.STOPPED);
        lastStateReceived = null;
    }

    @Test
    public void testTrackingStateTransitions() {
        assertEquals(TrackingStateManager.TrackingState.STOPPED, trackingStateManager.getCurrentState());

        trackingStateManager.addListener(newState -> lastStateReceived = newState);

        trackingStateManager.setState(TrackingStateManager.TrackingState.STARTING);
        assertEquals(TrackingStateManager.TrackingState.STARTING, trackingStateManager.getCurrentState());
        assertEquals(TrackingStateManager.TrackingState.STARTING, lastStateReceived);

        trackingStateManager.setState(TrackingStateManager.TrackingState.RUNNING);
        assertEquals(TrackingStateManager.TrackingState.RUNNING, trackingStateManager.getCurrentState());
        assertEquals(TrackingStateManager.TrackingState.RUNNING, lastStateReceived);

        trackingStateManager.setState(TrackingStateManager.TrackingState.STOPPED);
        assertEquals(TrackingStateManager.TrackingState.STOPPED, trackingStateManager.getCurrentState());
        assertEquals(TrackingStateManager.TrackingState.STOPPED, lastStateReceived);
    }

    @Test
    public void testPermissionRequiredAndErrorStates() {
        trackingStateManager.setState(TrackingStateManager.TrackingState.PERMISSION_REQUIRED);
        assertEquals(TrackingStateManager.TrackingState.PERMISSION_REQUIRED, trackingStateManager.getCurrentState());

        trackingStateManager.setState(TrackingStateManager.TrackingState.ERROR);
        assertEquals(TrackingStateManager.TrackingState.ERROR, trackingStateManager.getCurrentState());
    }
}
