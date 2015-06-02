package platformer.tmx;

/**
 * @author Andre Michelle
 */
public abstract class MapObject
{
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	public final boolean visible;
	public final String name;

	protected MapObject( final int x, final int y, final int width, final int height, final boolean visible, final String name )
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.visible = visible;
		this.name = name;
	}
}