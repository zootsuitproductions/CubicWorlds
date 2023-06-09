package me.zootsuitproductions.cubicworlds;

import org.joml.Matrix3d;
import org.joml.Vector3d;

public class AxisTransformation {
  private final Matrix3d matrix;
  private Matrix3d inverse;

  public AxisTransformation(double[] matrixValues) {
    matrix = new Matrix3d();
    matrix.set(matrixValues);

    inverse = new Matrix3d();
    inverse.set(matrixValues);
    inverse.invert();
  }

  public Vector3d unapply(Vector3d coordinate) {
    Vector3d copy = new Vector3d(coordinate.x, coordinate.y, coordinate.z);
    return matrix.transform(copy);
  }


  public Vector3d apply(Vector3d transformedCoord) {
    Vector3d copy = new Vector3d(transformedCoord.x, transformedCoord.y, transformedCoord.z);
    return inverse.transform(copy);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AxisTransformation) {
      AxisTransformation other = (AxisTransformation) obj;
      return this.matrix == other.matrix;
    }
    return false;
  }

  public static final AxisTransformation[] transformations = new AxisTransformation[] {
      AxisTransformation.TOP,
      AxisTransformation.FRONT,
      AxisTransformation.BOTTOM,
      AxisTransformation.BACK,
      AxisTransformation.LEFT,
      AxisTransformation.RIGHT
  };

  // Transformations for each of the cube faces
  public static final AxisTransformation TOP = new AxisTransformation(
      new double[]{
          1, 0, 0,
          0, 1, 0,
          0, 0, 1
      }
  );

  public static final AxisTransformation BOTTOM = new AxisTransformation(
      new double[]{
          -1, 0, 0,
          0, -1, 0,
          0, 0, 1
      }
  );

  public static final AxisTransformation RIGHT = new AxisTransformation(
      new double[]{
          1.0, 0.0, 0.0,
          0.0, 0.0, 1.0,
          0.0, -1.0, 0.0
      }
  );

  public static final AxisTransformation LEFT = new AxisTransformation(
      new double[]{
          1.0, 0.0, 0.0,
          0.0, 0.0, -1.0,
          0.0, 1.0, 0.0
      }
  );

  public static final AxisTransformation FRONT = new AxisTransformation(
      new double[]{
          0.0, -1.0, 0.0,
          1.0, 0.0, 0.0,
          0.0, 0.0, 1.0
      }
  );

  //x should be -y

  public static final AxisTransformation BACK = new AxisTransformation(
      new double[]{
          0.0, 1.0, 0.0,
          -1.0, 0.0, 0.0,
          0.0, 0.0, 1.0
      }
  );

}
