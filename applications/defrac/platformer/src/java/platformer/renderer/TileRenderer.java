package platformer.renderer;

import defrac.display.Texture;
import defrac.lang.Lists;
import platformer.gl.GlRenderer;
import platformer.tmx.MapTileLayer;
import platformer.tmx.TileFlags;
import platformer.tmx.TileSet;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Renders all tiles of the map within the view-port.
 * <p>
 * Accepts a RowRenderer to draw sprites to enable z-sorting
 *
 * @author Andre Michelle
 */
public final class TileRenderer implements Renderer
{
	private final RendererContext context;
	private final MapTileLayer layer;

	private final List<Sprite> sprites;

	public TileRenderer( @Nonnull final RendererContext context, @Nonnull final MapTileLayer layer )
	{
		this.context = context;
		this.layer = layer;

		sprites = Lists.newLinkedList();
	}

	/**
	 * Accepts a sprite to rendered with respect to its depth.
	 *
	 * @param sprite The sprite to be rendered.
	 */
	public void addSprite( @Nonnull final Sprite sprite )
	{
		sprites.add( sprite );
	}

	/**
	 * Removes a sprite.
	 *
	 * @param sprite The sprite to be removed.
	 */
	public void removeSprite( @Nonnull final Sprite sprite )
	{
		sprites.remove( sprite );
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

			if( !sprites.isEmpty() )
				renderSprites( tileY );

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
									( tileY * tileHeight - offsetY ) - tileSet.tileHeight + tileHeight,
									tileSet.tileWidth, tileSet.tileHeight,
									flipDiagonally, flipHorizontally, flipVertically );
						}

						continue row;
					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "[TileRenderer" +
				" layer: " + layer +
				"]";
	}

	private void renderSprites( final int rowIndex )
	{
		Collections.sort( sprites, ( a, b ) -> {
			if( a.y() > b.y() ) return 1;
			if( a.y() < b.y() ) return -1;
			return 0;
		} );

		for( final Sprite sprite : sprites )
			if( rowIndex == sprite.y() / context.tileHeight() + 1 )
				sprite.requestRender( context );
	}
}