package mypackage;

import net.rim.blackberry.api.bbm.platform.BBMPlatformApplication;
import net.rim.blackberry.api.bbm.platform.BBMPlatformException;
import net.rim.blackberry.api.bbm.platform.BBMPlatformContext;
import net.rim.blackberry.api.bbm.platform.BBMPlatformContextListener;
import net.rim.blackberry.api.bbm.platform.BBMPlatformManager;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannel;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannelListener;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformConnection;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformData;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformIncomingJoinRequest;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformOutgoingJoinRequest;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSession;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformSessionListener;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContact;
import net.rim.blackberry.api.bbm.platform.profile.BBMPlatformContactList;
import net.rim.blackberry.api.bbm.platform.profile.PresenceListener;
import net.rim.blackberry.api.bbm.platform.profile.UserProfile;
import net.rim.blackberry.api.bbm.platform.service.ContactListService;
import net.rim.blackberry.api.bbm.platform.service.MessagingService;
import net.rim.blackberry.api.bbm.platform.service.MessagingServiceListener;
import net.rim.blackberry.api.bbm.platform.service.UIService;
import net.rim.blackberry.api.bbm.platform.ui.chat.MessageSender;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.StringUtilities;


/**
 * This class extends the UiApplication class, providing a
 * graphical user interface.
 */
public class MyApp extends UiApplication implements Runnable, BBMBridgeCallback
{

    private MyScreen _myscreen = null;

    private static final long GUID = 0xaaaaaaaabbbbbbbbL;
    
    public static void delay(long time) {
        try { Thread.sleep(time); } catch(InterruptedException e) {}
    }
    
    public static void doDialog(final String msg) {
        MyApp app = getInstance();
        if (app == null) return;
        // a Dialog must be opened on the event thread
        app.invokeLater(new Runnable() {
            public void run() {
                Dialog.inform(msg);
            }
        });
    }
    
    public static void addMessage(String msg) {
    	MyApp app = getInstance();
        if (app != null) app._myscreen.addMessage(msg);
    }
    
    public MyApp() {
        RuntimeStore.getRuntimeStore().put(GUID, this);
        _myscreen = new MyScreen();
        pushScreen(_myscreen);
        new Thread(this).start();
    }
    
    public static MyApp getInstance() {
        return (MyApp) RuntimeStore.getRuntimeStore().get(GUID);
    }
    
    public static void main(String[] args) {
    	MyApp app = getInstance();   // Has an instance of the app been created?
        if (app != null) {
            app.requestForeground();        // If so, just place it in the foreground.
        } else {
            app = new MyApp();       // If not, create a new instance.
            app.enterEventDispatcher();
        }
    }

    private BBMBridge _bbm;
    
    public void run() {
        delay(1000);  // wait for 1 second before starting BBMBridge (just for UI's sake)
        _bbm = new BBMBridge(this);
        _bbm.start();  // connect to BBM

        // BBMBridge will call onInitialized() once the initialization is finished
    }
    
    public void invitePlayerToJoin() {
        final String joinMessage = "Join me in Let Me See!";
        _bbm.inviteFriendsToJoin(joinMessage);
    }

    public void invitePlayerToDownload() {
        _bbm.inviteFriendsToDownload();
    }
    
    
    
    
    
    // callbacks

    private MessageSender _me = null;
    private BBMPlatformContact _him = null;
    public static final String URL_MSG_TYPE = "URL";
    public static final String GPS_MSG_TYPE = "GPS";
    
    public void onInitialized(boolean success) {
    	if (!success) return;
    	_me = new MessageSender(_bbm.getUserName());
    }
    public void onContactJoined(final BBMPlatformContact contact) {
    	
        invokeLater(new Runnable() {  // a dialog must be opened in the event thread
            public void run() {
                String msg = contact.getDisplayName() + " has accepted your invitation. Start a game?";
                int response = Dialog.ask(Dialog.D_YES_NO, msg);
                if (response == Dialog.NO) {
                    _bbm.removeChannelContact(contact);
                    return;
                }
                _him = contact;
                //_bbm.sendMessageToAll(URL_MSG_TYPE, "http://www.yahoo.com");
            }
        });
    	
    }
    public void onContactLeft(BBMPlatformContact contact) {
    	if (_him == contact) {
            doDialog(contact.getDisplayName() + " has left the game.");
            _him = null;
        } else if (_him == null) {
        }
    	
    }
    public void onJoining(BBMPlatformContact contact) {
    	_him = contact;
    }
    public void onMessageReceived(BBMPlatformContact contact, String type, String message) {
    	if (type.equals(URL_MSG_TYPE)) {
    		_myscreen.openURL(message);
    	} else if (type.equals(GPS_MSG_TYPE)) {
        	try {
        		String[] tokens = split(message, ",");
        		_myscreen.centerMap(Double.parseDouble(tokens[0]),Double.parseDouble(tokens[1]));
        	} catch (Exception e) {
        		// parse exception 
        	}
    	}
    }
    public void exitApp() {
        RuntimeStore.getRuntimeStore().remove(GUID);
        invokeLater(new Runnable() {
            public void run() {
                popScreen(_myscreen);
                System.exit(0);  // When System.exit() is called, BBM will remove all listeners
            }
        });
    }

    public void sendmsg(String type, String msg) {
    	_bbm.sendMessageToAll(type, msg);
    }


    public static String[] split(String strString, String strDelimiter) {
        String[] strArray;
        int iOccurrences = 0;
        int iIndexOfInnerString = 0;
        int iIndexOfDelimiter = 0;
        int iCounter = 0;

        //Check for null input strings.
        if (strString == null) {
            throw new IllegalArgumentException("Input string cannot be null.");
        }
        //Check for null or empty delimiter strings.
        if (strDelimiter.length() <= 0 || strDelimiter == null) {
            throw new IllegalArgumentException("Delimeter cannot be null or empty.");
        }

        //strString must be in this format: (without {} )
        //"{str[0]}{delimiter}str[1]}{delimiter} ... 
        // {str[n-1]}{delimiter}{str[n]}{delimiter}"

        //If strString begins with delimiter then remove it in order
        //to comply with the desired format.

        if (strString.startsWith(strDelimiter)) {
            strString = strString.substring(strDelimiter.length());
        }

        //If strString does not end with the delimiter then add it
        //to the string in order to comply with the desired format.
        if (!strString.endsWith(strDelimiter)) {
            strString += strDelimiter;
        }

        //Count occurrences of the delimiter in the string.
        //Occurrences should be the same amount of inner strings.
        while((iIndexOfDelimiter = strString.indexOf(strDelimiter,
               iIndexOfInnerString)) != -1) {
            iOccurrences += 1;
            iIndexOfInnerString = iIndexOfDelimiter +
                strDelimiter.length();
        }

        //Declare the array with the correct size.
        strArray = new String[iOccurrences];

        //Reset the indices.
        iIndexOfInnerString = 0;
        iIndexOfDelimiter = 0;

        //Walk across the string again and this time add the 
        //strings to the array.
        while((iIndexOfDelimiter = strString.indexOf(strDelimiter,
               iIndexOfInnerString)) != -1) {

            //Add string to array.
            strArray[iCounter] = strString.substring(iIndexOfInnerString,iIndexOfDelimiter);

            //Increment the index to the next character after 
            //the next delimiter.
            iIndexOfInnerString = iIndexOfDelimiter +
                strDelimiter.length();

            //Inc the counter.
            iCounter += 1;
        }

        return strArray;
    }

    
    
}

