package uk.troglodytegames.DustyProton.DualPaletteSwapExample;

import java.util.Random;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.BaseGameActivity;

import uk.troglodytegames.DustyProton.DualPaletteSwapShader.DualPaletteSwap;
import uk.troglodytegames.DustyProton.DualPaletteSwapShader.DualPaletteSwapFragmentShaderCreator;
import android.content.res.Resources.NotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;

public class MainActivity extends BaseGameActivity implements IOnSceneTouchListener, IScrollDetectorListener,
		IPinchZoomDetectorListener {
	/*
	 * These enums are for random selection for sprite touch,
	 * colourSource is because these are the colours in the image and relate
	 * to the naming of the RGBA values stored in this class.
	 * colourReplace relates to the colours we've got declared as replacements
	 */
	public enum colourSource {
		DRED, LRED, BROWN, BLACK
	}

	public enum colourReplace {
		BLUE, GREEN, PINK
	}

	public final String TAG = "DualPaletteSwapExample";
	public LimitedFPSEngine engine;
	public ZoomCamera camera;
	public int camera_width = 720;
	public int camera_height = 480;
	public ScrollDetector mScrollDetector;
	public PinchZoomDetector mPinchZoomDetector;
	public float mPinchZoomStartedCameraZoomFactor;
	public float maxZoom = 0;
	public float zoomDepth = 2;
	private boolean mClicked = false;

	private ITexture mTexture;
	private ITextureRegion mTextureRegion;

	private Sprite mSprite;
	private Sprite mSprite2;

	private DualPaletteSwap mSinglePalette;
	private DualPaletteSwap mDualPalette;
	private DualPaletteSwapFragmentShaderCreator mSinglePaletteCreator;
	private DualPaletteSwapFragmentShaderCreator mDualPaletteCreator;

	/*
	 * see the enum comments, this relates to how many we've got declared
	 */
	int minColourSourceNumber = 0;
	int maxColourSourceNumber = 3;
	int minColourReplaceNumber = 0;
	int maxColourReplaceNumber = 2;
	/*
	 * Red colour
	 */
	float red_r = 1.0f;
	float red_g = 0f;
	float red_b = 0f;
	float red_a = 1.0f;
	float[] red = new float[] { red_r, red_g, red_b, red_a };
	/*
	 * Light red
	 */
	float lred_r = 1.0f;
	float lred_g = 0.4470f;
	float lred_b = 0.4470f;
	float lred_a = 1.0f;
	float[] lred = new float[] { lred_r, lred_g, lred_b, lred_a };
	/*
	 * Brown
	 */
	float brown_r = 0.3686f;
	float brown_g = 0.2078f;
	float brown_b = 0.2078f;
	float brown_a = 1.0f;
	float[] brown = new float[] { brown_r, brown_g, brown_b, brown_a };
	/*
	 * black
	 */
	float black_r = 0f;
	float black_g = 0f;
	float black_b = 0f;
	float black_a = 1.0f;
	float[] black = new float[] { black_r, black_g, black_b, black_a };
	/*
	 * epsilon
	 */
	float epsilon_r = 0.09f;
	float epsilon_g = 0.09f;
	float epsilon_b = 0.09f;
	float epsilon_a = 0.09f;
	float[] epsilon = new float[] { epsilon_r, epsilon_g, epsilon_b, epsilon_a };

	/*
	 * Blue
	 */
	float blue_r = 0.5372f;
	float blue_g = 0.8745f;
	float blue_b = 0.9803f;
	float blue_a = 1.0f;
	float[] blue = new float[] { blue_r, blue_g, blue_b, blue_a };

	/*
	 * Green
	 */
	float green_r = 0.8392f;
	float green_g = 0.8705f;
	float green_b = 0.2666f;
	float green_a = 1.0f;
	float[] green = new float[] { green_r, green_g, green_b, green_a };

	/*
	 * purple
	 */
	float purple_r = 0.7529f;
	float purple_g = 0.1764f;
	float purple_b = 0.9019f;
	float purple_a = 1.0f;
	float[] purple = new float[] { purple_r, purple_g, purple_b, purple_a };

	@Override
	public EngineOptions onCreateEngineOptions() {
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		this.camera_height = metrics.heightPixels;
		this.camera_width = metrics.widthPixels;
		this.camera = new ZoomCamera(0, 0, camera_width, camera_height);
		EngineOptions eOps = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(),
				this.camera);
		return eOps;
	}

	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		this.engine = new LimitedFPSEngine(pEngineOptions, 60);
		return this.engine;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
		this.mTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/fourcolours.png");
		this.mTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);
		this.mTexture.load();
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		try {
			this.zoomDepth = getResources().getInteger(R.integer.zoomDepth);
		} catch (NotFoundException e) {
			this.zoomDepth = 2;
		}
		/*
		 * Create a single and dual palette with the float arrays
		 */

		this.mSinglePaletteCreator = new DualPaletteSwapFragmentShaderCreator();
		this.mSinglePaletteCreator.setFirstPaletteSwap(this.red, this.purple, null);
		this.mSinglePalette = new DualPaletteSwap(this.mSinglePaletteCreator);

		this.mDualPaletteCreator = new DualPaletteSwapFragmentShaderCreator();
		this.mDualPaletteCreator.setFirstPaletteSwap(this.red, this.green, null);
		this.mDualPaletteCreator.setSecondPaletteSwap(this.black, this.blue, null);
		this.mDualPalette = new DualPaletteSwap(this.mDualPaletteCreator);

		/*
		 * If we need to we can do it value by value
		 */
		/*
		this.mSinglePaletteCreator = new DualPaletteSwapFragmentShaderCreator();
		this.mSinglePaletteCreator.setFirstPaletteSwap(this.red_r, this.red_g, this.red_b, this.red_a, this.blue_r,
				this.blue_g, this.blue_b, this.blue_a, 0.009f, 0.009f, 0.009f, 0.009f);
		this.mSinglePalette = new DualPaletteSwap(this.mSinglePaletteCreator);

		this.mDualPaletteCreator = new DualPaletteSwapFragmentShaderCreator();
		this.mDualPaletteCreator.setFirstPaletteSwap(this.red_r, this.red_g, this.red_b, this.red_a, this.green_r, this.green_g, this.green_b, this.green_a, 0.07f, 0.07f, 0.07f, 0.07f);
		this.mDualPaletteCreator.setSecondPaletteSwap(this.black_r, this.black_g, this.black_b, this.black_a, this.blue_r, this.blue_g, this.blue_b, this.blue_a, 0.07f, 0.07f, 0.07f, 0.07f);
		this.mDualPalette = new DualPaletteSwap(this.mDualPaletteCreator);
		*/

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
		Scene mScene = new Scene();
		mScene.setBackground(new Background(0.6509f, 0.8156f, 0.7764f));
		final FPSLogger fpsLogger = new FPSLogger();
		this.getEngine().registerUpdateHandler(fpsLogger);
		pOnCreateSceneCallback.onCreateSceneFinished(mScene);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		pScene.setOnSceneTouchListener(this);
		pScene.setTouchAreaBindingOnActionMoveEnabled(true);
		pScene.setOnAreaTouchTraversalFrontToBack();

		this.mSprite = new Sprite(100f, 100f, this.mTextureRegion, this.getVertexBufferObjectManager()) {

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionUp()) {
					Log.i(TAG, "Sprite1 Touched");
					mSinglePaletteCreator.setFirstPaletteSwap(randomColourSource(), randomColourSwap(), null);
				}
				return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
			}

		};
		this.mSprite2 = new Sprite(150f, 100f, this.mTextureRegion, this.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionUp()) {
					Log.i(TAG, "Sprite2 Touched");
					mDualPaletteCreator.setFirstPaletteSwap(randomColourSource(), randomColourSwap(), null);
					mDualPaletteCreator.setSecondPaletteSwap(randomColourSource(), randomColourSwap(), null);
				}
				return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
			}
		};
		this.mSprite.setShaderProgram(this.mSinglePalette);
		this.mSprite2.setShaderProgram(this.mDualPalette);
		pScene.attachChild(this.mSprite);
		pScene.attachChild(this.mSprite2);
		pScene.registerTouchArea(this.mSprite);
		pScene.registerTouchArea(this.mSprite2);

		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector, TouchEvent pSceneTouchEvent) {
		this.mPinchZoomStartedCameraZoomFactor = this.camera.getZoomFactor();
		this.mClicked = false;
	}

	@Override
	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
		this.camera.setZoomFactor(Math.min(
				Math.max(this.maxZoom, this.mPinchZoomStartedCameraZoomFactor * pZoomFactor), this.zoomDepth));
		this.mClicked = false;
	}

	@Override
	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
		/*
		 * We could have this, but it would only have to mirror onPinchZoom
		 * so why add it?
		 */
		this.mClicked = false;
	}

	@Override
	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
	}

	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
		final float zoomFactor = camera.getZoomFactor();
		float xLocation = -pDistanceX / zoomFactor;
		float yLocation = -pDistanceY / zoomFactor;
		camera.offsetCenter(xLocation, yLocation);
		this.mClicked = false;
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (this.mPinchZoomDetector != null) {
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
			if (this.mPinchZoomDetector.isZooming()) {
				this.mScrollDetector.setEnabled(false);
			} else {
				if (pSceneTouchEvent.isActionDown()) {
					this.mScrollDetector.setEnabled(true);
				}
				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		} else {
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		if (pSceneTouchEvent.isActionUp()) {
			if (this.mClicked) {
				// this.handleActionDown(pScene, pSceneTouchEvent);
			}
			this.mClicked = true;
		}
		return true;
	}

	public float[] randomColourSource() {
		Random rand = new Random();
		int randomNum = rand.nextInt(this.maxColourSourceNumber - this.minColourSourceNumber + 1)
				+ this.minColourSourceNumber;

		switch (randomNum) {
		case 0:
			Log.i(TAG, "Source - Red");
			return this.red;
		case 1:
			Log.i(TAG, "Source - Light Red");
			return this.lred;
		case 2:
			Log.i(TAG, "Source - Brown");
			return this.brown;
		case 3:
			Log.i(TAG, "Source - Black");
			return this.black;
		default:
			Log.i(TAG, "Default: Source - Red");
			return this.red;
		}
	}

	public float[] randomColourSwap() {
		Random rand = new Random();
		int randomNum = rand.nextInt(this.maxColourReplaceNumber - this.minColourReplaceNumber + 1)
				+ this.minColourReplaceNumber;
		switch (randomNum) {
		case 0:
			Log.i(TAG, "Replace with - Green");
			return this.green;
		case 1:
			Log.i(TAG, "Replace with - Blue");
			return this.blue;
		case 2:
			Log.i(TAG, "Replace with - Purple");
			return this.purple;
		default:
			Log.i(TAG, "Default: Replace with - Blue");
			return this.blue;
		}
	}
}
