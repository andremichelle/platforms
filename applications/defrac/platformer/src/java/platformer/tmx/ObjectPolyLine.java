package platformer.tmx;

import defrac.geom.Point;

/**
 * @author Andre Michelle
 */
public final class ObjectPolyLine extends MapObject
{
	public final Point[] points;

	ObjectPolyLine(
			final int x,
			final int y,
			final int width,
			final int height,
			final boolean visible,
			final String name,
			final Point[] points )
	{
		super( x, y, width, height, visible, name );

		this.points = points;
	}

	@Override
	public String toString()
	{
		return "[ObjectPolyLine" +
				" x: " + x +
				", y: " + y +
				", width: " + width +
				", height: " + height +
				", visible: " + visible +
				", name: " + name +
				", numPoints: " + points.length +
				"]";
	}
}