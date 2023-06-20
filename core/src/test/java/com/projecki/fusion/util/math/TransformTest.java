package com.projecki.fusion.util.math;

import com.projecki.fusion.util.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since June 18, 2022
 * @author Andavin
 */
public class TransformTest {

    @Test
    void testPositionOffset() {

        Vector3 base = Vector3.ONE;
        Vector3 posOffset1 = Vector3.at(1, 5, 0);
        Vector3 posOffset2 = Vector3.at(4, 1, 5);

        Transform baseTransform = Transform.of(Vector3.ONE);
        Transform offset1 = Transform.of(posOffset1);
        baseTransform.add(offset1);
        assertEquals(base.add(posOffset1), offset1.position());

        Transform offset2 = Transform.of(posOffset2);
        offset1.add(offset2);
        assertEquals(base.add(posOffset1).add(posOffset2), offset2.position());
    }

    @Test
    void testRotationOffset() {

        Vector3 rotOffset1 = Vector3.at(270, 135, 0); // Yaw, Pitch, Roll
        Vector3 rotOffset2 = Vector3.at(90, 45, 0);

        Transform baseTransform = Transform.of(Vector3.ZERO, Vector3.ZERO);
        assertYawPitchEquals(0, 0, baseTransform.forward());
        assertYawPitchEquals(0, -90, baseTransform.up());
        assertYawPitchEquals(90, 0, baseTransform.right());

        Transform offset1 = Transform.of(Vector3.ZERO, rotOffset1);
        baseTransform.add(offset1);
        assertYawPitchEquals(90, 45, offset1.forward());
        assertYawPitchEquals(270, 45, offset1.up());
        assertYawPitchEquals(0, 0, offset1.right());

        Transform offset2 = Transform.of(Vector3.ZERO, rotOffset2);
        offset1.add(offset2);
        assertYawPitchEquals(35.26438968275463, -30, offset2.forward());
        assertYawPitchEquals(324.7356103172453, 30, offset2.up());
        assertYawPitchEquals(270, -45, offset2.right());
    }

    @Test
    void testYawPitchConversion() {

        double yaw = 90;
        double pitch = 85;
        double xz = Math.cos(Math.toRadians(pitch));
        Vector3 direction = Vector3.at(
                -xz * Math.sin(Math.toRadians(yaw)),
                -Math.sin(Math.toRadians(pitch)),
                xz * Math.cos(Math.toRadians(yaw))
        );

        assertYawPitchEquals(yaw, pitch, direction);
    }

    private static void assertYawPitchEquals(double yaw, double pitch, Vector3 vec) {
        assertEquals(yaw, vec.toYaw(), 0.0000000000001);
        assertEquals(pitch, vec.toPitch(), 0.0000000000001);
    }
}
