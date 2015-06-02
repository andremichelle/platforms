package platformer.tmx;

/**
 * @author Andre Michelle
 */
public final class ObjectEllipse extends MapObject
{
	ObjectEllipse( final int x, final int y, final int width, final int height, final boolean visible, final String name )
	{
		super( x, y, width, height, visible, name );
	}

	@Override
	public String toString()
	{
		return "[ObjectEllipse" +
				" x: " + x +
				", y: " + y +
				", width: " + width +
				", height: " + height +
				", visible: " + visible +
				", name: '" + name + '\'' +
				"]";
	}
}