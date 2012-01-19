package com.ristar.guitality;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

//import android.app.Activity;
import android.hardware.SensorManager;
//import android.os.Bundle;
import android.widget.Toast;

public class GuitalityActivity extends RistarBaseGame implements IAccelerometerListener, IOnSceneTouchListener, IOnAreaTouchListener {
    /** Called when the activity is first created. */

	
	//ширина и высота камеры
	private static final int CAMERA_WIDTH=320;
	private static final int CAMERA_HEIGHT=240;
	
	//атлас ресурсов
	private BitmapTextureAtlas mBitmapTextureAtlas;
	
	//наши рожи как регионы
	private TiledTextureRegion mTrollFaceRegion;
	private TiledTextureRegion mFuckThatFaceRegion;
	
	private int mFaceCount = 0;
	
	//мир
	private PhysicsWorld mPhysicsWorld;
	
	//гравитация по осям
	private float mGravityX;
	private float mGravityY;
	
	private Scene mScene;
	/*
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    
    }*/

	@Override
	public Engine onLoadEngine() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Бугагашенька", Toast.LENGTH_LONG).show();
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		return new Engine(engineOptions);
	}

	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("img/");
		this.mFuckThatFaceRegion= BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "fuck_that.png", 0, 0, 2, 1); // 64x32
		this.mTrollFaceRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "troll_face.png", 0, 32, 2, 1); // 64x32
		this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
		
	}

	@Override
	public Scene onLoadScene() {
		// TODO Auto-generated method stub		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		this.mScene = new Scene();
		this.mScene.setBackground(new ColorBackground(0, 0, 0));
		this.mScene.setOnSceneTouchListener(this);

		final Shape ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
		final Shape roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
		final Shape left = new Rectangle(0, 0, 2, CAMERA_HEIGHT);
		final Shape right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.mScene.setOnAreaTouchListener(this);

		return this.mScene;
	}

	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerometerSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerometerSensor();
	}
	
	@Override
	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
		// TODO Auto-generated method stub
		this.mGravityX = pAccelerometerData.getX();
		this.mGravityY = pAccelerometerData.getY();

		final Vector2 gravity = Vector2Pool.obtain(this.mGravityX, this.mGravityY);
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			ITouchArea pTouchArea, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		// TODO Auto-generated method stub
		if(pSceneTouchEvent.isActionDown()) {
			final AnimatedSprite face = (AnimatedSprite) pTouchArea;
			this.jumpFace(face);
			return true;
		}

		return false;
	}

	private void jumpFace(AnimatedSprite face) {
		// TODO Auto-generated method stub
		final Body faceBody = (Body)face.getUserData();

		final Vector2 velocity = Vector2Pool.obtain(this.mGravityX * -50, this.mGravityY * -50);
		faceBody.setLinearVelocity(velocity);
		Vector2Pool.recycle(velocity);
		
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {
				this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				return true;
			}
		}
		return false;
	}

	private void addFace(float pX, float pY) {
		// TODO Auto-generated method stub
		this.mFaceCount++;

		final AnimatedSprite face;
		final Body body;

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

		if(this.mFaceCount % 2 == 1){
			face = new AnimatedSprite(pX, pY, this.mFuckThatFaceRegion);
			body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
		} else {
			face = new AnimatedSprite(pX, pY, this.mTrollFaceRegion);
			body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
		}

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));

		face.animate(new long[]{200,200}, 0, 1, true);
		face.setUserData(body);
		this.mScene.registerTouchArea(face);
		this.mScene.attachChild(face);
	}
}