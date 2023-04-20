package me.zootsuitproductions.cubicworlds;

import org.joml.Vector3d;

public class AxisTransformation {
  private Vector3d transformX;
  private Vector3d transformY;
  private Vector3d transformZ;

  public AxisTransformation(Vector3d transformationX, Vector3d transformationY, Vector3d transformationZ) {
    this.transformX = transformationX;
    this.transformY = transformationY;
    this.transformZ = transformationZ;
  }

  // transform a coordinate
  public Vector3d apply(Vector3d coordinate) {
    double x = coordinate.x;
    double y = coordinate.y;
    double z = coordinate.z;
    return new Vector3d(
        transformX.x * x + transformX.y * y + transformX.z * z,
        transformY.x * x + transformY.y * y + transformY.z * z,
        transformZ.x * x + transformZ.y * y + transformZ.z * z
    );
  }

  //
  public Vector3d unapply(Vector3d transformedCoord) {
    double x = transformedCoord.x;
    double y = transformedCoord.y;
    double z = transformedCoord.z;
    return new Vector3d(
        -transformX.x * x + -transformX.y * y + -transformX.z * z,
        -transformY.x * x + -transformY.y * y + -transformY.z * z,
        -transformZ.x * x + -transformZ.y * y + -transformZ.z * z
    );
  }

  // Transformations for each of the cube faces

  public static final AxisTransformation TOP = new AxisTransformation(
      new Vector3d(1.0, 0.0, 0.0),
      new Vector3d(0.0, 1.0, 0.0),
      new Vector3d(0.0, 0.0, 1.0)
  );

  public static final AxisTransformation BOTTOM = new AxisTransformation(
      new Vector3d(1.0, 0.0, 0.0),
      new Vector3d(0.0, -1.0, 0.0),
      new Vector3d(0.0, 0.0, -1.0)
  );

  public static final AxisTransformation RIGHT = new AxisTransformation(
      new Vector3d(1.0, 0.0, 0.0),
      new Vector3d(0.0, 0.0, 1.0),
      new Vector3d(0.0, -1.0, 0.0)
  );

  public static final AxisTransformation LEFT = new AxisTransformation(
      new Vector3d(1.0, 0.0, 0.0),
      new Vector3d(0.0, 0.0, -1.0),
      new Vector3d(0.0, 1.0, 0.0)
  );

  public static final AxisTransformation FRONT = new AxisTransformation(
      new Vector3d(0.0, -1.0, 0.0),
      new Vector3d(1.0, 0.0, 0.0),
      new Vector3d(0.0, 0.0, 1.0)
  );

  public static final AxisTransformation BACK = new AxisTransformation(
      new Vector3d(0.0, 1.0, 0.0),
      new Vector3d(-1.0, 0.0, 0.0),
      new Vector3d(0.0, 0.0, 1.0)
  );
}
