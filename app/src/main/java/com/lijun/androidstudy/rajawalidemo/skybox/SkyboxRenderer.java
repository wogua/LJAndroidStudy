package com.lijun.androidstudy.rajawalidemo.skybox;


import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.lijun.androidstudy.R;

import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

/**
 * Created by lijun on 17-12-6.
 */

public class SkyboxRenderer extends Renderer {

    public SkyboxRenderer(Context context) {
        super(context);
    }

    public SkyboxRenderer(Context context, boolean registerForResources) {
        super(context, registerForResources);
    }

    @Override
    protected void initScene() {
        getCurrentCamera().setFarPlane(1000);
        /**
         * Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
         */
        try {
            //lijun test
//                getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx,
//                                            R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
            getCurrentScene().setSkybox(R.drawable.vr_wallpaper1);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        getCurrentCamera().rotate(Vector3.Axis.Y, -0.2);
    }

    public void onPicSourceChanged(final int resId){
        try {
            //lijun test
//                getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx,
//                                            R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
            getCurrentScene().updateSkybox(resId);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
