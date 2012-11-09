
package com.engineering.framework;

import java.io.InputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Matrix;
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

	// bullet
	private Thread sim = null;
	private DiscreteDynamicsWorld dynamicWorld;
	
	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(50, 50, 100);

	private Object3D ball = null, ground = null, camaro = null, snooker = null, slot = null;
	private RigidBody ball_body, ground_body;
	
	private int fps = 0;

	private Light sun = null;

	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");

		super.onCreate(savedInstanceState);
		mGLView = new GLSurfaceView(getApplication());

		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
				// back to Pixelflinger on some device (read: Samsung I7500)
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

	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public boolean onTouchEvent(MotionEvent me) {

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			Logger.log("ACTION_DOWN: " + me.getX() + "," + me.getY());
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			Logger.log("ACTION_UP: " + me.getX() + "," + me.getY());
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_MOVE) {
			Logger.log("ACTION_MOVE: " + me.getX() + "," + me.getY());
			return true;
		}

		try {
			Thread.sleep(15);
		} catch (Exception e) {
		}

		return super.onTouchEvent(me);
	}

	protected boolean isFullscreenOpaque() {
		return true;
	}

	class MyRenderer implements GLSurfaceView.Renderer {

		private long time = System.currentTimeMillis();

		public MyRenderer() {
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {

			// jpct
			if (fb != null) {
				fb.dispose();
			}
			fb = new FrameBuffer(gl, w, h);
			world = new World();
			
			//world.setAmbientLight(250, 250, 250);

			sun = new Light(world);
			sun.setIntensity(250, 250, 250);
			sun.setPosition(new SimpleVector(0, -100, 0));

			MemoryHelper.compact();

			// bullet
			BroadphaseInterface broadphase = new DbvtBroadphase();
			DefaultCollisionConfiguration config = new DefaultCollisionConfiguration();
			CollisionDispatcher dispatcher = new CollisionDispatcher(config);
			SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
			dynamicWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, config); 
			dynamicWorld.setGravity(new Vector3f(0,-10,0));
			dynamicWorld.getDispatchInfo().allowedCcdPenetration = 0f;

			float mass;
			Vector3f inertia;
			CollisionShape shape;
			RigidBodyConstructionInfo rbcInfo;
			Transform xform = new Transform();
			Quat4f rot = new Quat4f();

			// ground
			float width = 10f, height = 0.5f;

			ground = Primitives.getBox(width, height/width);
			ground.rotateY((float)Math.PI / 4f);
			ground.rotateMesh();
			ground.getRotationMatrix().setIdentity();
			ground.calcTextureWrapSpherical();
			ground.strip();
			ground.build();
			ground.translate(0, 0, 0);
			world.addObject(ground);

			xform.setIdentity(); // position
			xform.origin.set(0, 0, 0);
			QuaternionUtil.setEuler(rot, 0, 0, (float)Math.PI/45); // yaw pitch roll
			xform.setRotation(rot);
			
			mass = 0f;
			inertia = new Vector3f(0,0,0);
			shape = new BoxShape(new Vector3f(width,height,width));
			rbcInfo = new RigidBodyConstructionInfo(mass, new JPCTBulletMotionState(ground, xform), shape, inertia);
			ground_body = new RigidBody(rbcInfo);
			dynamicWorld.addRigidBody(ground_body);
			
			// ball
			//ball = Primitives.getCube(2f);
			ball = Primitives.getSphere(2f);
			ball.rotateY((float)Math.PI / 4f);
			ball.rotateMesh();
			ball.getRotationMatrix().setIdentity();
			ball.calcTextureWrapSpherical();
			ball.strip();
			ball.build();
			ball.translate(0, 0, 0);
			world.addObject(ball);

			xform.setIdentity(); // position
			xform.origin.set(0, 500, 0);
			mass = 40f;
			inertia = new Vector3f(0,0,0);
			//shape = new BoxShape(new Vector3f(2,2,2));
			shape = new SphereShape(2f);
			shape.calculateLocalInertia(mass, inertia);
			rbcInfo = new RigidBodyConstructionInfo(mass, new JPCTBulletMotionState(ball, xform), shape, inertia);
			ball_body = new RigidBody(rbcInfo);
			//ball_body.setRestitution(0.5f);
			//ball_body.setFriction(0.5f);
			//ball_body.setDamping(0.5f,  0.5f);
			//ball_body.setUserPointer(ball);
			dynamicWorld.addRigidBody(ball_body);
			
			// model
			TextureManager tm = TextureManager.getInstance();
			//Texture image_jpg = new Texture(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.image)));
			
			// camaro
			/*
			Texture camaro_skin = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.camaro)), 512, 512));
			tm.addTexture("camaro.jpg", camaro_skin);

			InputStream camaro_obj = getResources().openRawResource(R.raw.camaro_obj);
			InputStream camaro_mtl = getResources().openRawResource(R.raw.camaro_mtl);
			camaro = Loader.loadOBJ(camaro_obj, camaro_mtl, 1f)[0];
			*/
			
			// slot
			Texture slotfrut_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.slotfrut)), 64, 64));
			tm.addTexture("SLOTFRUT.JPG", slotfrut_jpg);
			
			Texture slotwd1_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.slotwd1)), 64, 64));
			tm.addTexture("SLOTWD1.JPG", slotwd1_jpg);
			
			Texture slotref_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.slotref)), 64, 64));
			tm.addTexture("SLOTREF.JPG", slotref_jpg);
			
			Texture slottop_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.slottop)), 64, 64));
			tm.addTexture("SLOTTOP.JPG", slottop_jpg);
			
			Texture slottop2_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.slottop2)), 64, 64));
			tm.addTexture("SLOTTOP2.JPG", slottop2_jpg);
			
			Texture slotmid_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.slotmid)), 64, 64));
			tm.addTexture("SLOTMID.JPG", slotmid_jpg);
			
			Texture slotlogo_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.slotlogo)), 64, 64));
			tm.addTexture("SLOTLOGO.JPG", slotlogo_jpg);
			
			InputStream slot_obj = getResources().openRawResource(R.raw.slot_obj);
			InputStream slot_mtl = getResources().openRawResource(R.raw.slot_mtl);
			slot = Loader.loadOBJ(slot_obj, slot_mtl, 1f)[0];

			slot.rotateX((float)Math.PI);
			slot.rotateMesh();
			slot.getRotationMatrix().setIdentity();

			slot.scale(5f);
			slot.strip();
			slot.build();
			slot.translate(25f, 0, 0);
			world.addObject(slot);
			
			// snooker
			Texture wood1_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.wood1)), 64, 64));
			tm.addTexture("WOOD1.jpg", wood1_jpg);
			
			Texture wood2_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.wood2)), 64, 64));
			tm.addTexture("WOOD2.jpg", wood2_jpg);
			
			Texture poolstic_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.poolstic)), 64, 64));
			tm.addTexture("POOLSTIC.jpg", poolstic_jpg);
			
			Texture green_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.green)), 64, 64));
			tm.addTexture("GREEN.jpg", green_jpg);
			
			Texture dot_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.dot)), 64, 64));
			tm.addTexture("DOT.jpg", dot_jpg);

			Texture poolb_jpg = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.poolb)), 64, 64));
			tm.addTexture("POOLB.jpg", poolb_jpg);

			InputStream snooker_obj = getResources().openRawResource(R.raw.snooker_obj);
			InputStream snooker_mtl = getResources().openRawResource(R.raw.snooker_mtl);
			snooker = Loader.loadOBJ(snooker_obj, snooker_mtl, 1f)[0];

			snooker.rotateX((float)Math.PI);
			snooker.rotateY((float)Math.PI / 2);
			snooker.rotateMesh();
			snooker.getRotationMatrix().setIdentity();

			snooker.scale(5f);
			snooker.strip();
			snooker.build();
			snooker.translate(-25f, 0, 0);
			world.addObject(snooker);
			
			// camera
			Camera cam = world.getCamera();
			cam.setPosition(0, -50, -100);
			cam.lookAt(ground.getTransformedCenter());

			// physics
			sim = new Thread(new SimThread());
			sim.start();

		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		}

		public void onDrawFrame(GL10 gl) {

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
	
	class SimThread implements Runnable {
		
		public void run() {
			
			try {
				Thread.sleep(1000);
				Logger.log("simulation start");
				Clock clock = new Clock();
				while (true) {
					float ms = clock.getTimeMicroseconds();
					clock.reset();
					dynamicWorld.stepSimulation( ms / 1000000f);

					int num_manifolds = dynamicWorld.getDispatcher().getNumManifolds();
					for (int i=0;i<num_manifolds;i++) {
						PersistentManifold contact_manifold = dynamicWorld.getDispatcher().getManifoldByIndexInternal(i);
						CollisionObject obj0 = (CollisionObject)contact_manifold.getBody0();
						CollisionObject obj1 = (CollisionObject)contact_manifold.getBody1();				
						int num_contacts = contact_manifold.getNumContacts();
//						Logger.log("num_manifolds: " + num_manifolds);
//						Logger.log("num_contacts: " + num_contacts);
						for (int j=0;j<num_contacts;j++) {
							ManifoldPoint pt = contact_manifold.getContactPoint(j);
							if (pt.getDistance() < 0.0f) {
//								Logger.log("pt.getDistance(): " + pt.getDistance());
								/*
								Vector3f ptA = new Vector3f(), ptB = new Vector3f(), normal_b = new Vector3f();
								pt.getPositionWorldOnA(ptA);
								pt.getPositionWorldOnB(ptB);
								normal_b = pt.normalWorldOnB;
								*/
								if ((obj0 == ball_body && obj1 == ground_body) || (obj0 == ground_body && obj1 == ball_body)) {
									Logger.log("ball and ground collided!");
								}
							}
						}

					}
					
					Thread.sleep(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	class JPCTBulletMotionState extends MotionState {
		public final Transform centerOfMassOffset = new Transform();
	  private Object3D obj3d;
	  
	  public JPCTBulletMotionState(Object3D obj)
	  {
	    obj3d = obj;
	    centerOfMassOffset.setIdentity();
	  }
	  
		public JPCTBulletMotionState(Object3D obj, Transform startTrans)
	  {
	    obj3d = obj;
	    setGraphicFromTransform(startTrans);
			centerOfMassOffset.setIdentity();
		}  
	  
		public JPCTBulletMotionState(Object3D obj, Transform startTrans, Transform centerOfMassOffset)
	  {
	    obj3d = obj;
	    setGraphicFromTransform(startTrans);
			this.centerOfMassOffset.set(centerOfMassOffset);
		}

	  public Transform getWorldTransform(Transform worldTrans){
	    setTransformFromGraphic(worldTrans);
	    return worldTrans;
	  }
	  
	  public void setWorldTransform(Transform worldTrans)
	  {
	    setGraphicFromTransform(worldTrans);
	  }
	  
	  
	  private void setTransformFromGraphic(Transform tran)
	  {

		  SimpleVector pos = obj3d.getTransformedCenter();
		  
			tran.origin.set(pos.x, -pos.y, pos.z);

			Matrix matrixGfx = obj3d.getRotationMatrix();
			MatrixUtil.setFromOpenGLSubMatrix(tran.basis, matrixGfx.getDump());

			Quat4f tmp = new Quat4f(), rot = new Quat4f();
		    tran.getRotation(rot);
		    tmp = rot;
		    rot.x = -tmp.x;
		    rot.y = tmp.y;
		    rot.z = -tmp.z;
		    tran.setRotation(rot);

	  }
	  
	  private void setGraphicFromTransform(Transform tran)
	  {

	    SimpleVector pos = obj3d.getTransformedCenter();

	    obj3d.translate(tran.origin.x - pos.x,
			  (-tran.origin.y) - pos.y, 
			  (tran.origin.z) - pos.z);

	    Quat4f tmp = new Quat4f(), rot = new Quat4f();
	    tran.getRotation(rot);
	    tmp = rot;
	    rot.x = -tmp.x;
	    rot.y = tmp.y;
	    rot.z = -tmp.z;
	    tran.setRotation(rot);

	    float[] dump = obj3d.getRotationMatrix().getDump(); 
	    Matrix matrixGfx = new Matrix();
	    MatrixUtil.getOpenGLSubMatrix(tran.basis, dump);
	    matrixGfx.setDump(dump);
	    obj3d.setRotationMatrix(matrixGfx);

	  }

	}

	public class JPCTDebugDraw extends IDebugDraw {

		  private int _debugMode;
		  private World _world;
		  private FrameBuffer _buffer;
		  
		  private SimpleVector Vec3fToSVec(Vector3f vector)
		  {
		    return new SimpleVector(vector.x, -vector.y, -vector.z);
		  }
		  
		  public JPCTDebugDraw(World w, FrameBuffer b)
		  {
		    _world = w;
		    _buffer = b;
		  }

		  public void setDebugMode(int debugMode)
		  {
		    _debugMode = debugMode;
		  }

		  public int getDebugMode()
		  {
		    return _debugMode;
		  }

		  public void drawLine(Vector3f from, Vector3f to, Vector3f color)
		  { 
		  }
		  		  
		  public void draw3dText(Vector3f location, String textString)
		  {
		  }
		  		  
		  public void drawContactPoint(Vector3f pointOnB, Vector3f normalOnB, float distance, int lifeTime, Vector3f color)
		  {
		  }
		  		  
		  public void reportErrorWarning(String warningString)
		  {
		  }

		}
	
}
