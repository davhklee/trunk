
/*
 * MessageField.java
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

import net.rim.device.api.system.Application;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.decor.*;

/**
 * A TextField that shows only the last 20 messages, the latest one on top.
 */
public final class MessageField extends TextField {

    //==========================================================================
    //  fields

    private static final int MAX_MESSAGES = 20;

    private static Font _font;
    static {
        try {
            _font = FontFamily.forName("BBAlpha Sans").getFont(Font.PLAIN, 11);
        } catch (ClassNotFoundException e) {
            _font = Font.getDefault();
        }
    }

    private final String[] _msgs = new String[MAX_MESSAGES];

    //==========================================================================
    //  public methods

    public MessageField() {
        super(FIELD_LEFT | FIELD_TOP | NO_EDIT_MODE_INPUT | NON_FOCUSABLE);
        setFont(_font);
        setPadding(5, 5, 5, 5);
        setMargin(10, 20, 0, 20);
        setBorder(BorderFactory.createRoundedBorder(new XYEdges(5,5,5,5)));
        setBackground(BackgroundFactory.createSolidBackground(0xFFFFCC));
    }

    public void addMessage(String msg) {
        // prefix the message with a bullet or an arrow
        if (msg.startsWith(">")) {
            msg = "\u2192 " + msg.substring(1);  // \u2192 = right arrow
        } else {
            msg = "\u2022 " + msg; // \u2022 = bullet
        }

        // find first non-empty space
        int p;
        for (p = 0; p < MAX_MESSAGES; ++p) {
            if (_msgs[p] == null) break;
        }
        if (p == MAX_MESSAGES) {
            // discard first message
            for (int i = 1; i < MAX_MESSAGES; ++i) {
                _msgs[i-1] = _msgs[i];
            }
            --p;
        }
        _msgs[p] = msg;
        combineMessages();
    }

    //==========================================================================
    //  private methods

    private void combineMessages() {
        StringBuffer out = new StringBuffer();
        for (int i = MAX_MESSAGES; --i >= 0; ) {
            if (_msgs[i] == null) continue;
            out.append(_msgs[i]).append("\n");
        }
        out.append("\u2022 ...");
        final String msg = out.toString();

        // update the message later
        Application.getApplication().invokeLater(new Runnable() {
            public void run() {
                // make sure the app has not exited yet
                if (MyApp.getInstance() == null) return;
                setText(msg);
            }
        });
    };

}

