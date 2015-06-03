package platformer.renderer;

import defrac.display.Texture;
import platformer.gl.GlRenderer;
import platformer.tmx.MapLayer;
import platformer.tmx.TileFlags;
import platformer.tmx.MapTileLayer;
import platformer.tmx.TileSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Renders all tiles of the map within the view-port.
 *
 * Accepts a RowRenderer to draw sprites to enable z-sorting
 *
 * @author Andre Michelle
 */
public final class TileRenderer implements Renderer
{
	public interface RowRenderer
	{
		void renderRow( final int index );
	}

	private final RendererContext context;
	private final MapTileLayer layer;

	@Nullable
	private RowRenderer rowRenderer = null;

	public TileRenderer( @Nonnull final RendererContext context, @Nonnull final MapTileLayer layer )
	{
		this.context = context;
		this.layer = layer;
	}

	@Override
	public void renderLayer()
	{
		if( !layer.visible )
		{
			return;
		}

		final GlRenderer glRenderer = context.imageRenderer().alpha( layer.opacity );

		final int numRows = context.height() != layer.height ? context.height() + 1 : context.height();
		final int numCols = context.width() != layer.width ? context.width() + 1 : context.width();

		final int tileWidth = context.tileWidth();
		final int tileHeight = context.tileHeight();

		final TileSet[] tileSets = context.tileSets();

		final int currentTime = context.currentTime();

		final int mapWidth = context.mapWidth();
		final int mapHeight = context.mapHeight();

		final int offsetX = context.offsetX();
		final int offsetY = context.offsetY();

		final int offsetTileX = offsetX / tileWidth;
		final int offsetTileY = offsetY / tileHeight;

		final int[] data = layer.data;

		final int maxTileX = Math.min( offsetTileX + numCols, mapWidth );
		final int maxTileY = Math.min( offsetTileY + numRows, mapHeight );

		for( int tileY = offsetTileY ; tileY < maxTileY ; ++tileY )
		{
			final int dataOffsetH = tileY * mapWidth;

			if( null != rowRenderer )
				rowRenderer.renderRow( tileY );

			row:
			for( int tileX = offsetTileX ; tileX < maxTileX ; ++tileX )
			{
				final int index = tileX + dataOffsetH;

				final int dataEntry = data[ index ];

				final int globalTileId = dataEntry & TileFlags.FlippedMask;

				for( int i = tileSets.length - 1 ; -1 < i ; --i )
				{
					final TileSet tileSet = tileSets[ i ];

					if( tileSet.firstGID <= globalTileId )
					{
						final Texture texture = tileSet.getTileAt( globalTileId - tileSet.firstGID, currentTime );

						if( null != texture )
						{
							final boolean flipDiagonally = 0 != ( dataEntry & TileFlags.FlipDiagonally );
							final boolean flipHorizontally = 0 != ( dataEntry & TileFlags.FlipHorizontally );
							final boolean flipVertically = 0 != ( dataEntry & TileFlags.FlipVertically );

							glRenderer.draw(
									texture,
									tileX * tileWidth - offsetX,
									tileY * tileHeight - offsetY,
									tileSet.tileWidth, tileSet.tileHeight,
									flipDiagonally, flipHorizontally, flipVertically );
						}

						continue row;
					}
				}
			}
		}
	}

	@Nonnull
	@Override
	public MapLayer layer()
	{
		return layer;
	}

	public void rowRenderer( @Nullable final RowRenderer value )
	{
		rowRenderer = value;
	}

	@Override
	public String toString()
	{
		return "[TileLayerRenderer" +
				" layer: " + layer +
				", rowRenderer: " + rowRenderer +
				"]";
	}
}