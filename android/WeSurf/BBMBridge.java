
/*
 * BBMBridge.java
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

import net.rim.blackberry.api.bbm.platform.*;
import net.rim.blackberry.api.bbm.platform.io.*;
import net.rim.blackberry.api.bbm.platform.profile.*;
import net.rim.blackberry.api.bbm.platform.service.*;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;

/**
 * All the calls to and callbacks from the BBM API are handled in this class.
 * This class contains a minimal amount of application logic.
 * It also holds a single BBM API channel.
 *
 * The singleton instance of TicTacToeApp makes calls to this class and receives callbacks
 * from this class through the interface BBMBridgeCallback.
 *
 * See TicTacToeApp.java for an overview description of how an application can use
 * the BBM API to communicate among devices.
 */
public class BBMBridge {

    //====================================================================================
    //  BBMPlatformApplication

    /**
     * An UUID is used to uniquely identify the application in the test environment
     * before the application is available in AppWorld. If the application exists in
     * AppWorld, the UUID will not be used to identify the application.
     *
     * To run this app, you should generate a different UUID, because there is a
     * limit to the number of users who can share the same UUID.
     * Search for "UUID Generator" on the web for instructions to generate a UUID.
     */
    private static final String UUID = "3333444455556666777788889999aaaabbbb";

    /**
     * BBMPlatformApplication serves to provide certain properties of the application
     * to the BBM Social Platform. It is used as a parameter inBBMPlatformManager.register().
     *
     * If your application wants to be invoked in a non-default way, e.g. when
     * you have multiple entry points, you need to subclass from BBMPlatformApplication
     * and override certain methods.
     */
    private final BBMPlatformApplication _bbmApp = new BBMPlatformApplication(UUID);

    //====================================================================================
    //  BBMPlatformContextListener

    private final BBMPlatformContextListener _contextListener = new BBMPlatformContextListener() {
        public void accessChanged(final boolean isAccessAllowed, int code) {
            // call onAccessChanged() in a background thread to make UI responsive
            new Thread(new Runnable() {
                public void run() {
                    onAccessChanged(isAccessAllowed);
                }
            }).start();
        }
    };

    //====================================================================================
    //  MessagingServiceListener

    private final MessagingServiceListener _messagingListener = new MessagingServiceListener() {
        // The user accepted an invitation from a remote user.
        // A new channel is created by the BBM Platform and passed to the user application.
        public void channelCreated(BBMPlatformChannel channel) {
            logDelay(">MessagingServiceListener.channelCreated()");
            if (_channel != null && channel != _channel) {
                _channel.destroy();  // abandon the existing channel
            }
            _channel = channel;
        }
        // BBM created a channel because the user joined the app in BBM
        public void channelCreated(BBMPlatformChannel channel, int menuItemId) {
            channelCreated(channel);
        }
        // We must provide a BBMPlatformChannelListener when the BBM Platform creates a channel.
        public BBMPlatformChannelListener getChannelListener(BBMPlatformChannel channel) {
            logDelay(">MessagingServiceListener.getChannelListener()");
            return _channelListener;
        }

        // We do not use BBMPlatformSession in this app, but we have to implement these methods.
        public void sessionCreated(BBMPlatformSession session) {
            logDelay(">MessagingServiceListener.sessionCreated()");
        }
        public void sessionEnded(BBMPlatformContact contact, BBMPlatformSession session) {
            logDelay(">MessagingServiceListener.sessionEnded()");
        }
        public BBMPlatformSessionListener getSessionListener(BBMPlatformSession session) {
            logDelay(">MessagingServiceListener.getSessionListener()");
            return null;
        }

        // Called when a file transfer has failed
        public void fileTransferFailed(String path, BBMPlatformContact contact, int code) {
            logDelay(">MessagingServiceListener.fileTransferFailed(): " + translateIOErrorCode(code));
        }

        public void onContactReachable(BBMPlatformContact contact) {
            logDelay(">MessagingServiceListener.onContactReachable(): " + contact.getDisplayName());
        }
        public void onMessagesExpired(BBMPlatformContact contact, BBMPlatformData[] data) {
            logDelay(">MessagingServiceListener.onMessagesExpired(): " + contact.getDisplayName());
        }

        // I tried to join a public connection and the host accepts my request
        public void joinRequestAccepted(BBMPlatformOutgoingJoinRequest request, String param) {
            BBMPlatformContact contact = request.getHost();
            logDelay(">MessagingServiceListener.joinRequestAccepted(): " + contact.getDisplayName());
        }

        // I tried to join a public connection but the host declines my request
        public void joinRequestDeclined(BBMPlatformOutgoingJoinRequest request, int code) {
            logDelay(">MessagingServiceListener.joinRequestDeclined(): " + request.getHost().getPPID()
                     + " - " + translateIOErrorCode(code));
        }
    };

    //====================================================================================
    //  BBMPlatformChannelListener

    private final BBMPlatformChannelListener _channelListener = new BBMPlatformChannelListener() {
        // I sent some invitations
        public void contactsInvited(BBMPlatformConnection connection, BBMPlatformContactList contactList) {
            log(">ChannelListener.contactsInvited(): " + contactList.size() + " contacts");
        }

        // someone invited me and I joined, or I joined someone's channel
        public void contactsJoined(BBMPlatformConnection connection, BBMPlatformContactList contactList, final String cookie, int type) {
            // just get the first contact in the contactList
            BBMPlatformContact contact = (BBMPlatformContact) contactList.getAll().nextElement();
            log(">ChannelListener.contactsJoined(): " + contact.getDisplayName());
            if (type == CONTACT_INVITING_ME) {          // someone invited me and I joined!
                _callback.onJoining(contact);
            } else if (type == CONTACT_INVITED_BY_ME) { // I invited someone who then joined!
                _callback.onContactJoined(contact);
            }
        }

        // someone I invited declined to join
        public void contactDeclined(BBMPlatformConnection connection, BBMPlatformContact contact) {
            log(">ChannelListener.contactDeclined(): " + contact.getDisplayName());
        }

        // someone who was in the channel left
        public void contactLeft(BBMPlatformConnection connection, BBMPlatformContact contact) {
            log(">ChannelListener.contactLeft(): " + contact.getDisplayName());
            _callback.onContactLeft(contact);
        }
        // someone sent me some data in the channel
        public void dataReceived(BBMPlatformConnection connection, BBMPlatformContact sender, BBMPlatformData data) {
            String msg = data.getDataAsString();
            String type = data.getContentType();
            log(">ChannelListener.dataReceived(): " + type + " / \"" + msg + '"');  // Type / "Message"
            _callback.onMessageReceived(sender, type, msg);
        }

        // someone tries to join my public channel
        public void joinRequestReceived(BBMPlatformConnection connection, BBMPlatformIncomingJoinRequest request, String param) {
            logDelay(">ChannelListener.joinRequestReceived(): " + request.getRequester().getDisplayName());
            request.accept(null);  // automatically accept all public requests
            log("BBMPlatformIncomingJoinRequest.accept()");
        }
        // someone tried to join my public channel but I have not accepted it, and then the join request is canceled
        public void joinRequestCanceled(BBMPlatformConnection connection, BBMPlatformIncomingJoinRequest request, int code) {
            log(">ChannelListener.joinRequestCanceled(): " + request.getRequester().getDisplayName()
                + " - " + translateIOErrorCode(code));
        }
    };

    //====================================================================================
    //  PresenceListener

    private final PresenceListener _presenceListener = new PresenceListener() {
        // called when this user has updated some presence information
        public void userUpdated(UserProfile user, int eventType) {
            log(">PresenceListener.userUpdated(): " + translatePresenceEvent(eventType));
        }
        // called when a contact has updated some presence information
        public void contactUpdated(BBMPlatformContact contact, int eventType) {
            log(">PresenceListener.contactUpdated(): " + contact.getDisplayName()
                + " - " + translatePresenceEvent(eventType));
        }
    };

    //====================================================================================
    //  fields

    // host application
    private final BBMBridgeCallback _callback;

    // BBM Objects
    private BBMPlatformContext _context;
    private UserProfile _profile;
    private ContactListService _contacts;
    private MessagingService _messaging;
    private UIService _ui;
    private BBMPlatformChannel _channel;

    //====================================================================================
    //  initialization

    public BBMBridge(BBMBridgeCallback callback) {
        _callback = callback;
    }

    public void start() {

        // After constructing a BBMPlatformApplication, pass it to BBMPlatformManager.register()
        // to obtain a BBMPlatformContext. The BBMPlatformContext serves as the application's
        // doorway to BBM's functions.
        try {
            _context = BBMPlatformManager.register(_bbmApp);
            logDelay("BBMPlatformManager.register()");
        } catch (ControlledAccessException e) {
            log("BBMPlatformManager.register(): " + e.getMessage());
            return;
        }

        // A BBMPlatformContextListener should be given to the BBMPlatformContext object
        // to detect when access to BBM is granted.
        _context.setListener(_contextListener);
        logDelay("BBMPlatformContext.setChangeListener()");

        boolean allowed = _context.isAccessAllowed();
        log("BBMPlatformContext.isAccessAllowed(): " + translateBoolean(allowed));
        if (!allowed) {
            int error = _context.getAccessErrorCode();
            // The user chose not to connect the app to BBM. We'll prompt the user again.
            if (error == BBMPlatformContext.ACCESS_BLOCKED_BY_USER) {
                _context.requestUserPermission();
            }
        }
    }

    private void onAccessChanged(boolean isAccessAllowed) {
        synchronized (this) {
            // access to BBM not allowed?
            if (!isAccessAllowed) {
                int error = _context.getAccessErrorCode();
                if (error == BBMPlatformContext.ACCESS_REREGISTRATION_REQUIRED) {
                    Application.getApplication().invokeAndWait(new Runnable() {
                        public void run() {
                            Dialog.inform("The application needs to restart.");
                            _callback.exitApp();
                        }
                    });
                    return;
                }
                _profile = null;
                _channel = null;
                _callback.onInitialized(false);  // initialization fails
                return;
            }

            // check if we have already finished with the initialization
            if (_profile != null) {
                return;
            }

            // We can now get UserProfile, ContactListService, MessagingService,
            // UIService, etc, through the BBMPlatformContext.
            _profile = _context.getUserProfile();
        }

        logDelay("BBMPlatformContext.getUserProfile()");
        logDelay("UserProfile.getDisplayName(): " + translateString(getUserName()));
        logDelay("UserProfile.getPersonalMessage(): " + translateString(_profile.getPersonalMessage()));
        logDelay("UserProfile.getStatusMessage(): " + translateString(_profile.getStatusMessage()));

        _contacts = _context.getContactListService();
        logDelay("BBMPlatformContext.getContactListService()");
        _contacts.setPresenceListener(_presenceListener);
        logDelay("ContactListService.setPresenceListener()");

        _ui = _context.getUIService();
        logDelay("BBMPlatformContext.getUIService()");

        _messaging = _context.getMessagingService();
        logDelay("BBMPlatformContext.getMessagingService()");
        _messaging.setServiceListener(_messagingListener);
        logDelay("MessagingService.setServiceListener()");

        // Once we set the service listener, we create a channel.
        if (_channel == null) {
            _channel = _messaging.createChannel(_channelListener);
            log("MessagingService.createChannel()");
        }

        _callback.onInitialized(true);  // initialization succeeds
    }

    //====================================================================================
    //  public methods (called by TicTacToeApp)

    public void registerIcons(int[] iconIds, String[] imageFiles) {
        UserProfileBox profileBox = _profile.getProfileBox();
        // just check if the last icon is registered to see if all the icons have been registered
        logDelay("UserProfileBox.isIconRegistered()");
        if (profileBox.isIconRegistered(iconIds[iconIds.length - 1])) {
            return;
        }
        for(int i = 0; i < iconIds.length; i++) {
            int iconId = iconIds[i];
            logDelay("UserProfileBox.registerIcon()");
            EncodedImage image = EncodedImage.getEncodedImageResource(imageFiles[i]);
            profileBox.registerIcon(iconId, image);
        }
    }

    public String getUserName() {
        return _profile.getDisplayName();
    }

    public void changePersonalMessage(String message) {
        // check if the message needs to be changed
        if (_profile.getPersonalMessage().equals(message)) return;
        log("UserProfile.setPersonalMessage(): \"" + message + '"');
        // change the message: BBM will ask if the user actually wants to change
        _profile.setPersonalMessage(message);
    }

    public void inviteFriendsToDownload() {
        log("BBMPlatformChannel.sendDownloadInvitation()");
        _messaging.sendDownloadInvitation();
    }

    public void inviteFriendsToJoin(String msg) {
        log("BBMPlatformChannel.sendInvitation(): \"" + msg + '"');
        String param = "";
        long expiry = 0;
        _channel.sendInvitation(msg, param, expiry);
        // BBMPlatformChannelListener.contactsInvited() will be called when the
        // user has confirmed inviting certain contacts.
        // BBMPlatformChannelListener.contactsJoined() will be called when a
        // contact accepts the invitation.
    }

    public void hostPublicGame() {
        log("BBMPlatformChannel.setPublic()");
        _channel.setPublic();
    }

    public void joinPublicGame(int pin, String ppid) {
        String param = null;
        _messaging.sendJoinRequest(pin, ppid, param);
        log("MessagingService.sendJoinRequest(): " + Integer.toHexString(pin));
    }

    public void sendMessageToAll(String type, String message) {
        if (isChannelEmpty()) {
            log("(No one has joined the Channel)");
            log("type: " + type);
            log("msg: " + message);
            return;
        }
        BBMPlatformData data = new BBMPlatformData(type, message);
        try {
            _channel.sendData(data, _channel.getContactList());
            log("BBMPlatformChannel.sendData(): " + type + " / \"" + message + '"');
        } catch (BBMPlatformException e) {
            log("BBMPlatformChannel.sendData(): " + e.getMessage());
        }
    }

    public void sendMessageToContact(BBMPlatformContact contact, String type, String message) {
        if (!_channel.getContactList().contains(contact)) {
            log("(Cannot send message to " + contact.getDisplayName() + ")");
            return;
        }
        BBMPlatformData data = new BBMPlatformData(type, message);
        try {
            _channel.sendData(data, contact);
            log("BBMPlatformChannel.sendData(): " + type + " / \"" + message + '"');
        } catch (BBMPlatformException e) {
            log("BBMPlatformChannel.sendData(): " + e.getMessage());
        }
    }

    public void chatWithChannelContact(String message) {
        BBMPlatformContactList contactList = getChannelContactList();
        if (contactList == null) {
            log("(No one in the Channel)");
            return;
        }
        log("UIService.startBBMChat()");
       _ui.startBBMChat(contactList, message);
    }

    public void chatWithOthers(String message) {
        log("UIService.startBBMChat()");
        _ui.startBBMChat(message);
    }

    public void sendFileToContact(String filePath, String caption) {
        // Ask the user to select a contact, and then send the file
        _messaging.sendFile(filePath, caption);
        log("MessagingService.sendFile(): " + filePath);
    }

    public void removeChannelContact(BBMPlatformContact contact) {
        log("BBMPlatformChannel.remove()");
        _channel.remove(contact);
    }

    public void removeChannelContacts() {
        log("BBMPlatformChannel.removeAllContacts()");
        _channel.removeAllContacts();
    }

    public boolean isChannelEmpty() {
        return _channel.getContactList().size() == 0;
    }

    public void addUserProfileString(final int iconID, final String str) {
        try {
            UserProfileBox box = _profile.getProfileBox();
            log("UserProfile.getProfileBox()");
            box.addItem(iconID, str);
            log("UserProfileBox.addItem()");
        } catch (UserProfileBoxAccessException e) {
            log("Changing UserProfileBox failed");
        }
    }

    public String getPPID() {
        return _profile.getPPID();
    }

    //====================================================================================
    //  private methods

    private BBMPlatformContactList getChannelContactList() {
        if (_channel == null) return null;
        BBMPlatformContactList list = _channel.getContactList();
        return (list.size() == 0) ? null : list;
    }

    private static void log(String str) {
        MyApp.addMessage(str);
    }

    private static void logDelay(String str) {
        log(str);
        MyApp.delay(200);  // delay a fifth of a second for UI's sake
    }

    private static String translateIOErrorCode(int code) {
        switch (code) {
            case IOErrorCode.SUCCESS: return "Success";
            case IOErrorCode.DOWNLOAD_INVITATION_LIMIT_REACHED: return "Download Invitation: limit reached";
            case IOErrorCode.JOIN_REQUEST_DECLINED_BY_HOST: return "Join Request Declined: by host";
            case IOErrorCode.JOIN_REQUEST_DECLINED_PUBLIC_CONNECTION_NOT_FOUND: return "Join Request Declined: public connection not found";
            case IOErrorCode.JOIN_REQUEST_DECLINED_INVALID_HOST_PPID: return "Join Request Declined: invalid host PPID";
            case IOErrorCode.JOIN_REQUEST_DECLINED_APPLICATION_NOT_RUNNING: return "Join Request Declined: application not running";
            case IOErrorCode.JOIN_REQUEST_DECLINED_PUBLIC_CONNECTION_IS_FULL: return "Join Request Declined: public connection full";
            case IOErrorCode.JOIN_REQUEST_CANCELED_BY_REQUESTER: return "Join Request Canceled: by requester";
            case IOErrorCode.JOIN_REQUEST_CANCELED_REQUESTER_LEFT: return "Join Request Canceled: Requester left ";
            case IOErrorCode.FILE_TRANSFER_FILE_NOT_FOUND: return "File not found";
            case IOErrorCode.FILE_TRANSFER_USER_CANCELED: return "User canceled";
            case IOErrorCode.FILE_TRANSFER_FILE_SIZE_EXCEEDED: return "Size exceeded";
            case IOErrorCode.FILE_TRANSFER_BAD_FILE_TYPE: return "Bad file type";
            case IOErrorCode.FILE_TRANSFER_BAD_CONTACT: return "Bad contact";
            case IOErrorCode.FILE_TRANSFER_FILE_FORWARD_LOCKED: return "File is forward-locked";
            case IOErrorCode.FILE_TRANSFER_EMPTY_FILE: return "File is empty";
            default: return "Unknown IOErrorCode " + code;
        }
    }

    private static String translatePresenceEvent(int eventType) {
        switch (eventType) {
            case PresenceListener.EVENT_TYPE_DISPLAY_PICTURE: return "Display picture changed";
            case PresenceListener.EVENT_TYPE_DISPLAY_NAME: return "Display name changed";
            case PresenceListener.EVENT_TYPE_STATUS: return "Status changed";
            case PresenceListener.EVENT_TYPE_PERSONAL_MESSAGE: return "Personal message changed";
            case PresenceListener.EVENT_TYPE_INSTALL_APP: return "App installed";
            case PresenceListener.EVENT_TYPE_UNINSTALL_APP: return "App uninstalled";
            case PresenceListener.EVENT_TYPE_INVITATION_RECEIVED: return "Invitation received";
            default: return "Unknown event " + eventType;
        }
    }

    private static String translateBoolean(boolean b) {
        return b ? "yes" : "no";
    }

    private static String translateString(String str) {
        return str == null || str.length() == 0 ? "(empty)" : str;
    }

}

