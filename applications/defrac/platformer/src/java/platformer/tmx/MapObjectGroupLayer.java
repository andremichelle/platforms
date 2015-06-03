package platformer.tmx;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Andre Michelle
 */
public final class MapObjectGroupLayer extends MapLayer
{
	public final List<MapObject> mapObjects;

	MapObjectGroupLayer(
			@Nonnull final String name,
			@Nonnull final List<MapObject> mapObjects,
			final int x, final int y,
			final int width, final int height,
			final boolean visible, final float opacity )
	{
		super( name, x, y, width, height, visible, opacity );

		this.mapObjects = mapObjects;
	}

	@Override
	public String toString()
	{
		return "[MapObjectGroupLayer" +
				" objects: " + mapObjects +
				"]";
	}
}