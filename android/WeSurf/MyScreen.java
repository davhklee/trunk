
package mypackage;

import org.w3c.dom.Document;
import java.util.*;
import java.lang.*;
import net.rim.blackberry.api.bbm.platform.io.BBMPlatformChannel;
import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.browser.field.ContentReadEvent;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.util.StringProvider;
import net.rim.device.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.lbs.maps.ui.*;
import net.rim.device.api.lbs.maps.model.*;
import net.rim.device.api.browser.field2.*;
import net.rim.device.api.gps.LocationInfo;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

/**
 * A class extending the MainScreen class, which provides default standard
 * behavior for BlackBerry GUI applications.
 */
public final class MyScreen extends MainScreen implements FocusChangeListener 
{
	private BrowserField bf;
	private final MessageField mf = new MessageField();
	private TextField ef;
	
	private ButtonField go, back, fwd;
	
	private MapField mapf = new MapField();
	
	private HorizontalFieldManager hManager = new HorizontalFieldManager();
	private LabelField tab1 = new LabelField("Web", LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
	private LabelField tab2 = new LabelField("Map", LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
	private LabelField tab3 = new LabelField("Log", LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
	private LabelField bar1 = new LabelField(" | ", LabelField.NON_FOCUSABLE);
	private LabelField bar2 = new LabelField(" | ", LabelField.NON_FOCUSABLE);
	
	private BrowserFieldConfig config = new BrowserFieldConfig();

	private VerticalFieldManager tab1man;
	private VerticalFieldManager tab2man;
	private VerticalFieldManager tab3man;
	private HorizontalFieldManager nav;
	
	private LocationTracker myloc;
	
	private class popupScreen extends FullScreen {
		
	    public boolean onClose()
	    {
	        this.getUiEngine().popScreen(this);
	        return(true);
	    }
		
	}

	private popupScreen page2;
	private popupScreen page3;	

	private class LocationTracker extends TimerTask {

		private Timer timer;
		private double longitude;
		private double latitude;

		public LocationTracker() {
			timer = new Timer();

			try {
				if (!LocationInfo.isLocationOn()) {
					LocationInfo.setLocationOn();
				}
				
				Criteria myCriteria = new Criteria();
				myCriteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_LOW);
				myCriteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
				myCriteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
				myCriteria.setCostAllowed(true);
				
				LocationProvider.getInstance(myCriteria).setLocationListener(new MyLocationListener(), 1, 1, 1);
			} catch(Exception e) { System.err.println(e.toString()); }
			
			timer.scheduleAtFixedRate(this, 10000, 10000);
		}

		public void run() {
			try {
			MyApp myapp = MyApp.getInstance();
			double latabs = Math.abs(latitude);
			double lonabs = Math.abs(longitude);
			if ((latabs < 0.1) || (lonabs < 0.1)) throw (new Exception("no GPS fix"));
			myapp.sendmsg(myapp.GPS_MSG_TYPE, "" + latitude + "," + longitude + "");
			} catch (Exception e) {
				addMessage("Exception: " + latitude + "," + longitude + "");
			}
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		private class MyLocationListener implements LocationListener {
			public void locationUpdated(LocationProvider provider, Location location)
			{
				if(location != null && location.isValid())
				{
				    QualifiedCoordinates qc = location.getQualifiedCoordinates();

					try {
						LocationTracker.this.longitude = qc.getLongitude();
						LocationTracker.this.latitude = qc.getLatitude();
					} catch(Exception e) { System.err.println("Exception: " + e.toString()); }
				}
				else
				{
				    System.err.println("Exception: location not valid");
				}
			}

			public void providerStateChanged(LocationProvider provider, int newState) {
			}
		}
	}
	
	private class MyProtocolController extends ProtocolController {
		private BrowserField m_bf;
		public MyProtocolController(BrowserField browserField) {
			super(browserField);
			m_bf = browserField;
		}
		public void handleNavigationRequest(BrowserFieldRequest req) throws Exception {			
			if (req.getProtocol().equalsIgnoreCase("rtsp")) {
				Browser.getDefaultSession().displayPage(req.getURL());
			} else {
				super.handleNavigationRequest(req);
			}
		}
	}
	
	private void createtab1() {
		tab1man = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		bf = new BrowserField();
		bf.getConfig().setProperty(BrowserFieldConfig.CONTROLLER, new MyProtocolController(bf));
		ef = new TextField();
		
		go = new ButtonField("Go", ButtonField.CONSUME_CLICK);
		back = new ButtonField("<<", ButtonField.CONSUME_CLICK);
		fwd = new ButtonField(">>", ButtonField.CONSUME_CLICK);

		tab1man.add(ef);
		
		nav = new HorizontalFieldManager();
		
	     FieldChangeListener golist = new FieldChangeListener() {
	         public void fieldChanged(Field field, int context) {
	        	 bf.requestContent(ef.getText());
	         }
	     };
	     go.setChangeListener(golist);
	     nav.add(go);
	     
	     FieldChangeListener backlist = new FieldChangeListener() {
	         public void fieldChanged(Field field, int context) {
	        	 bf.back();
	         }
	     };
	     back.setChangeListener(backlist);
	     nav.add(back);
	     
	     FieldChangeListener fwdlist = new FieldChangeListener() {
	         public void fieldChanged(Field field, int context) {
	        	 bf.forward();
	         }
	     };
	     fwd.setChangeListener(fwdlist);
	     nav.add(fwd);
	     
	     tab1man.add(nav);
	     
		BrowserFieldListener listener = new BrowserFieldListener() {
			public void documentLoaded(BrowserField browserField, Document document) throws Exception
			{
				ef.setText(browserField.getDocumentUrl() + "");
				MyApp myapp = MyApp.getInstance(); 
				myapp.sendmsg(MyApp.URL_MSG_TYPE,browserField.getDocumentUrl());
			}
		};
		bf.addListener( listener );
		bf.requestContent("http://www.google.com");
		tab1man.add(bf);
	}
	
	public MyScreen() {

		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		
		/*
		config.setProperty(BrowserFieldConfig.VIEWPORT_WIDTH, new Integer(this.getWidth()));
		config.setProperty(BrowserFieldConfig.INITIAL_SCALE, new Float(1.0));
		config.setProperty(BrowserFieldConfig.USER_SCALABLE, Boolean.TRUE);
		bf = new BrowserField(config);
		*/
		
		// focus listener
		tab1.setFocusListener(this);
		tab2.setFocusListener(this);
		tab3.setFocusListener(this);
		
		// tab1
		createtab1();
		
		// tab2
		tab2man = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		tab2man.add(mapf);
		
		// tab3
		tab3man = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		tab3man.add(mf);

		hManager.add(tab1);
		hManager.add(bar1);
		hManager.add(tab2);
		hManager.add(bar2);
		hManager.add(tab3);
		add(hManager);
		add(new SeparatorField());
		
		add(tab1man);
		
		page2 = new popupScreen();
		page2.add(tab2man);
		
		page3 = new popupScreen();
		page3.add(tab3man);

		myloc = new LocationTracker();
		
	}

	public void focusChanged(Field field, int eventType) {

		synchronized (Application.getEventLock()) {
			if (eventType == FOCUS_GAINED) {
				if (field == tab1) {
					//Dialog.alert("Web!");
				} else if (field == tab2) {
					//Dialog.alert("Map!");
					this.getUiEngine().pushScreen(page2);
				} else if (field == tab3) {
					//Dialog.alert("Log!");
					this.getUiEngine().pushScreen(page3);
				}
			}
		}

	}

	public void openURL(String url) {
		if (bf.getDocumentUrl().compareTo(url) != 0) {
			bf.requestContent(url);
		}
	}
	
	public void centerMap(double lat, double lon) {
		mapf.getAction().setCenterAndZoom(new MapPoint(lat,lon), 3);
	}
	
	public void addMessage(String msg) {
        mf.addMessage(msg);
    }
	
    public boolean onClose()
    {
        int response = Dialog.ask(Dialog.D_YES_NO,"Are you sure you want exit?");
        if (response == -1)
        {
            return false;
        }
        else
        {
        	MyApp.getInstance().exitApp();  // clean up
        	super.close();
            return true;
        }
    }

    private class DownloadItem extends MenuItem {
        public DownloadItem() {
            super( new StringProvider( "Download" ), 0, 0 );
        }

        public void run() {
            download();
        }
    }
    
    private class InviteItem extends MenuItem {
        public InviteItem() {
            super( new StringProvider( "Invite" ), 0, 0 );
        }

        public void run() {
            invite();
        }
    }

    protected void makeMenu( Menu menu, int instance ) {
    	super.makeMenu(menu, instance);
        MenuItem menuDownload = new DownloadItem();
        menu.add( menuDownload );
        MenuItem menuInvite = new InviteItem();
        menu.add( menuInvite );
    }

    private void download() {
    	MyApp app = MyApp.getInstance();
    	app.invitePlayerToDownload();
    }
    
    private void invite() {
    	MyApp app = MyApp.getInstance();
    	app.invitePlayerToJoin();
    }

}

