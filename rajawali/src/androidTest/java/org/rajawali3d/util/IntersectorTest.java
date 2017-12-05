package org.rajawali3d.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.rajawali3d.util.Intersector;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.Plane;

import java.util.Arrays;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
@SmallTest
public class IntersectorTest {

    @Test
    public void testIntersectRayPlane() throws Exception {
        Vector3 rayStart = new Vector3(0,0,0);
        Vector3 rayEnd = new Vector3(1,1,1);
        Plane plane = new Plane();
        Vector3 hitPoint = new Vector3();

        Boolean result = Intersector.intersectRayPlane(rayStart, rayEnd, plane, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 0, hitPoint.x, 1e-14);
    }

    @Test
    public void testIntersectRayTriangle() throws Exception {
        Vector3 rayStart = new Vector3(0,0,0);
        Vector3 rayEnd = new Vector3(1,1,1);
        Vector3 t1 = new Vector3(1,0,0);
        Vector3 t2 = new Vector3(0,1,0);
        Vector3 t3 = new Vector3(0,0,1);
        Vector3 hitPoint = new Vector3();

        // (1,1,1) . (1/3,1/3,1/3) = 0,  x+y+z=1
        Boolean result = Intersector.intersectRayTriangle(rayStart, rayEnd, t1, t2, t3, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", 1.0/3.0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", 1.0/3.0, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", 1.0/3.0, hitPoint.x, 1e-14);
    }

    @Test
    public void testIntersectRaySphere() throws Exception {
        Vector3 rayStart = new Vector3(0,0,0);
        Vector3 rayEnd = new Vector3(1,1,1);
        Vector3 sphereCentre = new Vector3(1,1,1);
        double sphereRadius = 1.0;
        Vector3 hitPoint = new Vector3();

        Boolean result = Intersector.intersectRaySphere(rayStart, rayEnd, sphereCentre, sphereRadius, hitPoint);
        assertTrue(result);
        assertEquals("hitPoint.x", Math.sqrt(3)-1, hitPoint.x, 1e-14);
        assertEquals("hitPoint.y", Math.sqrt(3)-1, hitPoint.x, 1e-14);
        assertEquals("hitPoint.z", Math.sqrt(3)-1, hitPoint.x, 1e-14);
    }
}
