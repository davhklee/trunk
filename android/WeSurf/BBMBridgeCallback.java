
/*
 * BBMBridgeCallback.java
 *
 * Copyright (C) 1998-2011 Research In Motion Ltd.
 *
 * Note:
 *
 * 1. For the sake of simplicity, this test application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 *
 * 2. The sample serves as a demonstration of principles and is not intended for a
 * full featured application. It makes no guarantees for completeness and is left to
 * the user to use it as sample ONLY.
 */

package mypackage;

import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;

/**
 * This interface defines callback functions for BBMBridge.
 */
public interface BBMBridgeCallback {

    public void onInitialized(boolean success);
    public void onContactJoined(BBMPlatformContact contact);
    public void onContactLeft(BBMPlatformContact contact);
    public void onJoining(BBMPlatformContact contact);
    public void onMessageReceived(BBMPlatformContact contact, String type, String message);
    public void exitApp();

}

