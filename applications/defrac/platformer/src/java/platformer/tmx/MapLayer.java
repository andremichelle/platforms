package platformer.tmx;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public abstract class MapLayer extends MapObject
{
	public final float opacity;

	MapLayer(
			@Nonnull final String name,
			final int x, final int y,
			final int width, final int height,
			final boolean visible, final float opacity )
	{
		super( x, y, width, height, visible, name );

		this.opacity = opacity;
	}

	@Override
	public String toString()
	{
		return "[MapLayer" +
				" x: " + x +
				", y: " + y +
				", width: " + width +
				", height: " + height +
				", visible: " + visible +
				", opacity: " + opacity +
				", name: '" + name + '\'' +
				"]";
	}
}