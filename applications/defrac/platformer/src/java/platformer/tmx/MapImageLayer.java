package platformer.tmx;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public final class MapImageLayer extends MapLayer
{
	MapImageLayer(
			@Nonnull final String name,
			final int x, final int y,
			final int width, final int height,
			final boolean visible, final float opacity )
	{
		super( name, x, y, width, height, visible, opacity );
	}
}