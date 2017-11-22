package com.lijun.viewexplosion.factory;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.lijun.viewexplosion.particle.Particle;

import java.util.Random;


/**
 * Created by Administrator on 2015/11/29 0029.
 */
public abstract class ParticleFactory {
    public abstract Particle[][] generateParticles(Bitmap bitmap, Rect bound);

    static String[] particles = {"com.lijun.viewexplosion.factory.BooleanFactory", "com.lijun.viewexplosion.factory.ExplodeParticleFactory",
            "com.lijun.viewexplosion.factory.FallingParticleFactory", "com.lijun.viewexplosion.factory.FlyawayFactory",
            "com.lijun.viewexplosion.factory.InnerFallingParticleFactory", "com.lijun.viewexplosion.factory.VerticalAscentFactory"};

    public static ParticleFactory getRandomParticle(int seed) {
        ParticleFactory particleFactory = null;

        Random rd = new Random(seed);
        int a = rd.nextInt(particles.length);

        try {
            Class clazz = Class.forName(particles[a]);
            particleFactory = (ParticleFactory) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (particleFactory == null) {
            particleFactory = new FallingParticleFactory();
        }
        return particleFactory;
    }
}
