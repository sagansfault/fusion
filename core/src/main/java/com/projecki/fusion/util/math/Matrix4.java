package com.projecki.fusion.util.math;

import com.projecki.fusion.util.Vector3;

/**
 * A 4x4 matrix used for 3-dimensional coordinate
 * plane translations, rotations and scaling.
 * <p>
 *     Inspiration taken largely from WorldEdit and
 *     <a href="http://geom-java.sourceforge.net/index.html">JavaGeom project</a>.
 * </p>
 *
 * @since May 26, 2022
 * @author Andavin
 */
public class Matrix4 {

    private static final Matrix4 UP = of().identity().translate(Vector3.UNIT_Y);
    private static final Matrix4 FORWARD = of().identity().translate(Vector3.UNIT_Z);
    private static final Matrix4 RIGHT = of().identity().translate(Vector3.UNIT_NEGATIVE_X);

    public static Matrix4 of() {
        return new Matrix4();
    }

    public static Matrix4 of(double m00, double m10, double m20, double m30,
                             double m01, double m11, double m21, double m31,
                             double m02, double m12, double m22, double m32,
                             double m03, double m13, double m23, double m33) {
        return new Matrix4(
                m00, m10, m20, m30,
                m01, m11, m21, m31,
                m02, m12, m22, m32,
                m03, m13, m23, m33
        );
    }

    // This is actually incorrect for matrix notation
    // Currently it is mColumnRow when it should be mRowColumn
    private double m00, m10, m20, m30;
    private double m01, m11, m21, m31;
    private double m02, m12, m22, m32;
    private double m03, m13, m23, m33;

    private Matrix4() {
    }

    private Matrix4(double m00, double m10, double m20, double m30,
                    double m01, double m11, double m21, double m31,
                    double m02, double m12, double m22, double m32,
                    double m03, double m13, double m23, double m33) {
        this.m00 = m00;
        this.m10 = m10;
        this.m20 = m20;
        this.m30 = m30;
        this.m01 = m01;
        this.m11 = m11;
        this.m21 = m21;
        this.m31 = m31;
        this.m02 = m02;
        this.m12 = m12;
        this.m22 = m22;
        this.m32 = m32;
        this.m03 = m03;
        this.m13 = m13;
        this.m23 = m23;
        this.m33 = m33;
    }

    Matrix4 identity() {
        // 1 0 0 0
        // 0 1 0 0
        // 0 0 1 0
        // 0 0 0 1
        this.m00 = 1;
        this.m01 = 0;
        this.m02 = 0;
        this.m03 = 0;

        this.m10 = 0;
        this.m11 = 1;
        this.m12 = 0;
        this.m13 = 0;

        this.m20 = 0;
        this.m21 = 0;
        this.m22 = 1;
        this.m23 = 0;

        this.m30 = 0;
        this.m31 = 0;
        this.m32 = 0;
        this.m33 = 1;
        return this;
    }

    void set(Matrix4 o) {
        this.m00 = o.m00;
        this.m01 = o.m01;
        this.m02 = o.m02;
        this.m03 = o.m03;

        this.m10 = o.m10;
        this.m11 = o.m11;
        this.m12 = o.m12;
        this.m13 = o.m13;

        this.m20 = o.m20;
        this.m21 = o.m21;
        this.m22 = o.m22;
        this.m23 = o.m23;

        this.m30 = o.m30;
        this.m31 = o.m31;
        this.m32 = o.m32;
        this.m33 = o.m33;
    }

    double m30() {
        return m30;
    }

    double m31() {
        return m31;
    }

    double m32() {
        return m32;
    }

    public Vector3 translation() {
        return Vector3.at(m30, m31, m32);
    }

    public Vector3 calcRight() {
        Matrix4 m = this.mul(RIGHT, of());
        return Vector3.at(m.m30 - m30, m.m31 - m31, m.m32 - m32);
    }

    public Vector3 calcUp() {
        Matrix4 m = this.mul(UP, of());
        return Vector3.at(m.m30 - m30, m.m31 - m31, m.m32 - m32);
    }

    public Vector3 calcForward() {
        Matrix4 m = this.mul(FORWARD, of());
        return Vector3.at(m.m30 - m30, m.m31 - m31, m.m32 - m32);
    }

    public Matrix4 translate(Vector3 translation) {
        return this.translate(translation.x(), translation.y(), translation.z());
    }

    public Matrix4 translate(double x, double y, double z) {
        this.m30 = m00 * x + m10 * y + m20 * z + m30;
        this.m31 = m01 * x + m11 * y + m21 * z + m31;
        this.m32 = m02 * x + m12 * y + m22 * z + m32;
        this.m33 = m03 * x + m13 * y + m23 * z + m33;
        return this;
    }

    public Matrix4 rotate(Vector3 rotation) {
        return this.rotate(
                -Math.toRadians(rotation.x()),
                Math.toRadians(rotation.y()),
                Math.toRadians(rotation.z())
        );
    }

    public Matrix4 rotate(double angleY, double angleX, double angleZ) {

        double sinX = Math.sin(angleX);
        double cosX = cosFromSinInternal(sinX, angleX);
        double sinY = Math.sin(angleY);
        double cosY = cosFromSinInternal(sinY, angleY);
        double sinZ = Math.sin(angleZ);
        double cosZ = cosFromSinInternal(sinZ, angleZ);

        double nSinY = -sinY;
        double nSinX = -sinX;
        double nSinZ = -sinZ;

        // rotateY
        double nm20 = m00 * sinY + m20 * cosY;
        double nm21 = m01 * sinY + m21 * cosY;
        double nm22 = m02 * sinY + m22 * cosY;
        double nm23 = m03 * sinY + m23 * cosY;
        double nm00 = m00 * cosY + m20 * nSinY;
        double nm01 = m01 * cosY + m21 * nSinY;
        double nm02 = m02 * cosY + m22 * nSinY;
        double nm03 = m03 * cosY + m23 * nSinY;
        // rotateX
        double nm10 = m10 * cosX + nm20 * sinX;
        double nm11 = m11 * cosX + nm21 * sinX;
        double nm12 = m12 * cosX + nm22 * sinX;
        double nm13 = m13 * cosX + nm23 * sinX;

        this.m20 = m10 * nSinX + nm20 * cosX;
        this.m21 = m11 * nSinX + nm21 * cosX;
        this.m22 = m12 * nSinX + nm22 * cosX;
        this.m23 = m13 * nSinX + nm23 * cosX;
        // rotateZ
        this.m00 = nm00 * cosZ + nm10 * sinZ;
        this.m01 = nm01 * cosZ + nm11 * sinZ;
        this.m02 = nm02 * cosZ + nm12 * sinZ;
        this.m03 = nm03 * cosZ + nm13 * sinZ;
        this.m10 = nm00 * nSinZ + nm10 * cosZ;
        this.m11 = nm01 * nSinZ + nm11 * cosZ;
        this.m12 = nm02 * nSinZ + nm12 * cosZ;
        this.m13 = nm03 * nSinZ + nm13 * cosZ;
        return this;
    }

    public Matrix4 scale(Vector3 scale) {
        return this.scale(scale.x(), scale.y(), scale.z());
    }

    public Matrix4 scale(double x, double y, double z) {
        this.m00 = m00 * x;
        this.m01 = m01 * x;
        this.m02 = m02 * x;
        this.m03 = m03 * x;
        this.m10 = m10 * y;
        this.m11 = m11 * y;
        this.m12 = m12 * y;
        this.m13 = m13 * y;
        this.m20 = m20 * z;
        this.m21 = m21 * z;
        this.m22 = m22 * z;
        this.m23 = m23 * z;
        return this;
    }

    /**
     * Multiply this matrix by another and populate the
     * target matrix with the result.
     *
     * @param m The matrix multiplier to multiply by.
     * @param target The matrix to populate with the result.
     * @return The target matrix after it has been populated;
     */
    public Matrix4 mul(Matrix4 m, Matrix4 target) {

        double r00 = m00 * m.m00 + m10 * m.m01 + m20 * m.m02 + m30 * m.m03;
        double r01 = m01 * m.m00 + m11 * m.m01 + m21 * m.m02 + m31 * m.m03;
        double r02 = m02 * m.m00 + m12 * m.m01 + m22 * m.m02 + m32 * m.m03;
        double r03 = m03 * m.m00 + m13 * m.m01 + m23 * m.m02 + m33 * m.m03;
        double r10 = m00 * m.m10 + m10 * m.m11 + m20 * m.m12 + m30 * m.m13;
        double r11 = m01 * m.m10 + m11 * m.m11 + m21 * m.m12 + m31 * m.m13;
        double r12 = m02 * m.m10 + m12 * m.m11 + m22 * m.m12 + m32 * m.m13;
        double r13 = m03 * m.m10 + m13 * m.m11 + m23 * m.m12 + m33 * m.m13;
        double r20 = m00 * m.m20 + m10 * m.m21 + m20 * m.m22 + m30 * m.m23;
        double r21 = m01 * m.m20 + m11 * m.m21 + m21 * m.m22 + m31 * m.m23;
        double r22 = m02 * m.m20 + m12 * m.m21 + m22 * m.m22 + m32 * m.m23;
        double r23 = m03 * m.m20 + m13 * m.m21 + m23 * m.m22 + m33 * m.m23;
        double r30 = m00 * m.m30 + m10 * m.m31 + m20 * m.m32 + m30 * m.m33;
        double r31 = m01 * m.m30 + m11 * m.m31 + m21 * m.m32 + m31 * m.m33;
        double r32 = m02 * m.m30 + m12 * m.m31 + m22 * m.m32 + m32 * m.m33;
        double r33 = m03 * m.m30 + m13 * m.m31 + m23 * m.m32 + m33 * m.m33;

        target.m00 = r00;
        target.m01 = r01;
        target.m02 = r02;
        target.m03 = r03;

        target.m10 = r10;
        target.m11 = r11;
        target.m12 = r12;
        target.m13 = r13;

        target.m20 = r20;
        target.m21 = r21;
        target.m22 = r22;
        target.m23 = r23;

        target.m30 = r30;
        target.m31 = r31;
        target.m32 = r32;
        target.m33 = r33;
        return target;
    }

    public Matrix4 add(Matrix4 a, Matrix4 target) {
        target.m00 = m00 + a.m00;
        target.m10 = m10 + a.m10;
        target.m20 = m20 + a.m20;
        target.m30 = m30 + a.m30;

        target.m01 = m01 + a.m01;
        target.m11 = m11 + a.m11;
        target.m21 = m21 + a.m21;
        target.m31 = m31 + a.m31;

        target.m02 = m02 + a.m02;
        target.m12 = m12 + a.m12;
        target.m22 = m22 + a.m22;
        target.m32 = m32 + a.m32;

        target.m03 = m03 + a.m03;
        target.m13 = m13 + a.m13;
        target.m23 = m23 + a.m23;
        target.m33 = m33 + a.m33;
        return target;
    }

    public Matrix4 sub(Matrix4 s, Matrix4 target) {
        target.m00 = m00 - s.m00;
        target.m10 = m10 - s.m10;
        target.m20 = m20 - s.m20;
        target.m30 = m30 - s.m30;

        target.m01 = m01 - s.m01;
        target.m11 = m11 - s.m11;
        target.m21 = m21 - s.m21;
        target.m31 = m31 - s.m31;

        target.m02 = m02 - s.m02;
        target.m12 = m12 - s.m12;
        target.m22 = m22 - s.m22;
        target.m32 = m32 - s.m32;

        target.m03 = m03 - s.m03;
        target.m13 = m13 - s.m13;
        target.m23 = m23 - s.m23;
        target.m33 = m33 - s.m33;
        return target;
    }

    private static double cosFromSinInternal(double sin, double angle) {
        // sin(x)^2 + cos(x)^2 = 1
        double cos = Math.sqrt(1 - sin * sin);
        double a = angle + Math.PI / 2;
        double b = a - (int) (a / (Math.PI * 2)) * (Math.PI * 2);
        if (b < 0.0) {
            b = Math.PI * 2 + b;
        }

        return b >= Math.PI ? -cos : cos;
    }
}
