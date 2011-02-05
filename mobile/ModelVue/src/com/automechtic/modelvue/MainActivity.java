package com.automechtic.modelvue;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.FloatMath;
import android.view.MotionEvent;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

public class MainActivity extends Activity {

	private static MainActivity master = null;

	static final private int history_max = 10;
	static final private String keystore = "com.automechtic.modelvue";
	static final private String keyhistory = keystore + ".history";
	private SharedPreferences prefs = null;
	private List<String> history = null;

	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(0, 0, 0);

	private float touchTurn = 0;
	private float touchTurnUp = 0;
	private float camera_init = 5;
	private float camera_scale = 0;
	
	static final private int touch_none = 0;
	static final private int touch_pan = 1;
	static final private int touch_zoom = 2;
	private int touch_mode = touch_none;

	private float xpos = -1;
	private float ypos = -1;
	private float old_dist = -1;
	private float new_dist = -1;
	private int finger = -1;

	private Object3D camaro = null;
	private int fps = 0;

	private Light sun = null;

	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		mGLView = new GLSurfaceView(getApplication());

		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});

		renderer = new MyRenderer();
		mGLView.setRenderer(renderer);
		setContentView(mGLView);
		
		prefs = this.getSharedPreferences(keystore, Context.MODE_PRIVATE);
		history = Arrays.asList(TextUtils.split(prefs.getString(keyhistory, "camaro.obj"), ","));

	}

	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}

	protected void onResume() {
		super.onResume();
		mGLView.onResume();
	}

	protected void onStop() {
		super.onStop();
	}

	private void openListener() {
		String model = "tank.obj";
		if (history.size() > history_max - 1) {
			history.remove(0);
		}
		history.add(model);
		prefs.edit().putString(keyhistory, TextUtils.join(",", history));
		prefs.edit().commit();
	}
	
	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private float distance(float dx, float dy, float dz) {
		return(FloatMath.sqrt(dx * dx + dy * dy + dz * dz));
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		
		switch(event.getActionMasked()) {
		
		case MotionEvent.ACTION_DOWN:
			xpos = event.getX();
			ypos = event.getY();
			touch_mode = touch_pan;
			return(true);
			
		case MotionEvent.ACTION_UP:
			xpos = -1;
			ypos = -1;
			touchTurn = 0;
			touchTurnUp = 0;
			touch_mode = touch_none;
			return(true);
			
		case MotionEvent.ACTION_MOVE:
			if (touch_mode == touch_pan) {
				float dx = event.getX() - xpos;
				float dy = event.getY() - ypos;
				xpos = event.getX();
				ypos = event.getY();
				touchTurn = dx / -100f;
				touchTurnUp = dy / -100f;
			} else if (touch_mode == touch_zoom) {
				float dx = event.getX() - event.getX(finger);
				float dy = event.getY() - event.getY(finger);
				new_dist = distance(dx, dy, 0);
				if (new_dist > 10f) {
					camera_scale = old_dist / new_dist;
				}
			}
			return(true);

		case MotionEvent.ACTION_POINTER_DOWN:
			finger = event.getActionIndex();
			float dx = event.getX() - event.getX(finger);
			float dy = event.getY() - event.getY(finger);
			old_dist = distance(dx, dy, 0);
			if (old_dist > 10f) {
				touch_mode = touch_zoom;
			}
			return(true);
			
		case MotionEvent.ACTION_POINTER_UP:
			finger = -1;
			xpos = event.getX();
			ypos = event.getY();
			touch_mode = touch_pan;
			return(true);
		
		} // switch

		try {
			Thread.sleep(15);
		} catch (Exception e) {
			// No need for this...
		}

		return super.onTouchEvent(event);
	}

	protected boolean isFullscreenOpaque() {
		return true;
	}

	class MyRenderer implements GLSurfaceView.Renderer {

		private long time = System.currentTimeMillis();

		public MyRenderer() {
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			if (fb != null) {
				fb.dispose();
			}
			fb = new FrameBuffer(gl, w, h);

			if (master == null) {

				world = new World();
				world.setAmbientLight(20, 20, 20);

				sun = new Light(world);
				sun.setIntensity(250, 250, 250);
				
				Texture camaro_skin = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.camaro)), 512, 512));
				TextureManager.getInstance().addTexture("camaro.jpg", camaro_skin); 
				InputStream camaro_obj = getResources().openRawResource(R.raw.camaro_obj);
				InputStream camaro_mtl = getResources().openRawResource(R.raw.camaro_mtl);
				camaro = Loader.loadOBJ(camaro_obj, camaro_mtl, 1f)[0];
				
				world.addObject(camaro);

				Camera cam = world.getCamera();
				cam.moveCamera(Camera.CAMERA_MOVEOUT, camera_init);
				cam.lookAt(camaro.getTransformedCenter());

				SimpleVector sv = new SimpleVector();
				sv.set(camaro.getTransformedCenter());
				sv.y -= 100;
				sv.z -= 100;
				sun.setPosition(sv);
				MemoryHelper.compact();

				if (master == null) {
					Logger.log("Saving master Activity!");
					master = MainActivity.this;
				}
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		}

		public void onDrawFrame(GL10 gl) {
			if (touchTurn != 0) {
				camaro.rotateY(touchTurn);
				touchTurn = 0;
			}

			if (touchTurnUp != 0) {
				camaro.rotateX(touchTurnUp);
				touchTurnUp = 0;
			}
			
			if (camera_scale != 0) {
				if (camera_scale > 0) {
					Camera cam = world.getCamera();
					if (camera_scale > 1) {
						cam.moveCamera(Camera.CAMERA_MOVEOUT, 1);
					} else {
						cam.moveCamera(Camera.CAMERA_MOVEIN, 1);
					}
				}
				camera_scale = 0;
			}

			fb.clear(back);
			world.renderScene(fb);
			world.draw(fb);
			fb.display();

			if (System.currentTimeMillis() - time >= 1000) {
				Logger.log(fps + "fps");
				fps = 0;
				time = System.currentTimeMillis();
			}
			fps++;
		}
	}
}

