package platformer.tmx;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public final class MapTileLayer extends MapLayer
{
	public final int[] data;

	MapTileLayer(
			@Nonnull final String name,
			@Nonnull final int[] data,
			final int x, final int y,
			final int width, final int height,
			final boolean visible, final float opacity )
	{
		super( name, x, y, width, height, visible, opacity );

		this.data = data;
	}

	public int getTileAt( final int x, final int y )
	{
		if( 0 > x || x >= width || 0 > y || y >= height )
			return -1;

		return data[ y * width + x ];
	}

	@Override
	public String toString()
	{
		return "[MapTileLayer" +
				" x: " + x +
				", y: " + y +
				", width: " + width +
				", height: " + height +
				", visible: " + visible +
				", opacity: " + opacity +
				", name: '" + name + '\'' +
				", data: " + data.length +
				"]";
	}
}