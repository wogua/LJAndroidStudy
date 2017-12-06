package com.lijun.androidstudy.rajawalidemo.photograph;

import android.content.Context;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.lijun.androidstudy.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by lijun on 17-12-5.
 */

public class PhotographRenderer extends Renderer {
    private long mStartTime;
    private Material mMaterial;
    private PlanesGaloreMaterialPlugin mMaterialPlugin;

    public PhotographRenderer(Context context) {
        super(context);
    }

    public PhotographRenderer(Context context, boolean registerForResources) {
        super(context, registerForResources);
    }

    @Override
    protected void initScene() {
        DirectionalLight light = new DirectionalLight(0, 0, 1);

        getCurrentScene().addLight(light);
        getCurrentCamera().setPosition(0, 0, -16);

        final PlanesGalore planes = new PlanesGalore();
        mMaterial = planes.getMaterial();
        mMaterial.setColorInfluence(0);
        try {
            mMaterial.addTexture(new Texture("flickrPics", R.drawable.flickrpics));
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        mMaterialPlugin = planes.getMaterialPlugin();

        planes.setDoubleSided(true);
        planes.setZ(4);
        getCurrentScene().addChild(planes);

        Object3D empty = new Object3D();
        getCurrentScene().addChild(empty);

        CatmullRomCurve3D path = new CatmullRomCurve3D();
        path.addPoint(new Vector3(-4, 0, -20));
        path.addPoint(new Vector3(2, 1, -10));
        path.addPoint(new Vector3(-2, 0, 10));
        path.addPoint(new Vector3(0, -4, 20));
        path.addPoint(new Vector3(5, 10, 30));
        path.addPoint(new Vector3(-2, 5, 40));
        path.addPoint(new Vector3(3, -1, 60));
        path.addPoint(new Vector3(5, -1, 70));

        final SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(path);
        anim.setDurationMilliseconds(20000);
        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);//无限重复往返
        anim.setTransformable3D(getCurrentCamera());
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        getCurrentScene().registerAnimation(anim);//注册动画监听 Scene render()时更新动画状态 -> applyTransformation
        anim.play();

        getCurrentCamera().setLookAt(new Vector3(0, 0, 30));
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        mStartTime = System.currentTimeMillis();
    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        mMaterial.setTime((System.currentTimeMillis() - mStartTime) / 1000f);
        mMaterialPlugin.setCameraPosition(getCurrentCamera().getPosition());
    }
}
