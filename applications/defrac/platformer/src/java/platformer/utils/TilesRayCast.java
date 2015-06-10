package platformer.utils;

import defrac.geom.Point;
import platformer.renderer.Sprite;
import platformer.tmx.MapTileLayer;

import javax.annotation.Nonnull;

/**
 * Dynamic Collision Detection by RayCasting.
 * Sprite size must be lowerEqual tile-size.
 *
 * @author Andre Michelle
 */
public final class TilesRayCast
{
	public static final int Top = 0;
	public static final int Right = 1;
	public static final int Bottom = 2;
	public static final int Left = 3;

	private static final class State
	{
		private float min;
		private int tile;
		private int border;
	}

	private final MapTileLayer layer;
	private final float tileWidth;
	private final float tileHeight;

	private final State state;

	public TilesRayCast( @Nonnull final MapTileLayer layer, final float tileWidth, final float tileHeight )
	{
		this.layer = layer;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;

		state = new State();
	}

	public float solve( @Nonnull final Sprite sprite, final float velocityX, final float velocityY )
	{
		state.min = Float.MAX_VALUE;
		state.tile = -1;
		state.border = -1;

		final float px = sprite.x();
		final float py = sprite.y();

		final double vn = Math.sqrt( velocityX * velocityX + velocityY * velocityY );
		final float vx = ( float ) ( velocityX / vn );
		final float vy = ( float ) ( velocityY / vn );

		final Point tr = new Point( px + sprite.width(), py + tileHeight - sprite.height() );
		final Point tl = new Point( px, py + tileHeight - sprite.height() );
		final Point br = new Point( px + sprite.width(), py + tileHeight );
		final Point bl = new Point( px, py + tileHeight );

		if( 0.0 < vy ) // down
		{
			castDown( bl.x, bl.y, vx, vy );

			if( !( 0.0 == vx && 0 == br.x % tileWidth ) ) // allow gaps in sprite size
				castDown( br.x, br.y, vx, vy );
		}
		else if( 0.0 > vy ) // up
		{
			castUp( tl.x, tl.y, vx, vy );

			if( !( 0.0 == vx && 0 == br.x % tileWidth ) ) // allow gaps in sprite size
				castUp( tr.x, tr.y, vx, vy );
		}

		if( 0.0 < vx ) // right
		{
			castRight( tr.x, tr.y, vx, vy );

			if( !( 0.0 == vy && 0 == br.y % tileHeight ) ) // allow gaps in sprite size
				castRight( br.x, br.y, vx, vy );
		}
		else if( 0.0 > vx ) // left
		{
			castLeft( tl.x, tl.y, vx, vy );

			if( !( 0.0 == vy && 0 == br.y % tileHeight ) ) // allow gaps in sprite size
				castLeft( bl.x, bl.y, vx, vy );
		}

		if( state.min < Float.MAX_VALUE )
		{
			final float distance = ( float ) Math.sqrt( state.min );

			return ( float ) ( distance / vn );
		}

		return Float.MAX_VALUE;
	}

	public int lastTile()
	{
		return state.tile;
	}

	public int lastBorder()
	{
		return state.border;
	}

	private void castLeft( final float px, final float py, final float vx, final float vy )
	{
		final float over = -vy / vx;

		int rx = ( int ) ( px / tileWidth );

		while( 0 < rx ) // left
		{
			final float ax = rx * tileWidth;
			final float ay = py - ( ax - px ) * over;

			final int tile = layer.getTileAt( --rx, ( int ) ( ay / tileHeight ) );

			if( -1 == tile )
				break;

			if( 0 != tile )
			{
				final float dx = ax - px;
				final float dy = ay - py;
				final float dd = dx * dx + dy * dy;

				if( dd < state.min )
				{
					state.min = dd;
					state.tile = tile;
					state.border = Right;
				}
				break;
			}
		}
	}

	private void castUp( final float px, final float py, final float vx, final float vy )
	{
		final float over = -vx / vy;

		int ry = ( int ) ( py / tileHeight );

		while( 0 < ry ) // down
		{
			final float ay = ry * tileHeight;
			final float ax = px - ( ay - py ) * over;

			final int tile = layer.getTileAt( ( int ) ( ax / tileWidth ), --ry );

			if( -1 == tile )
				break;

			if( 0 != tile )
			{
				final float dx = ax - px;
				final float dy = ay - py;
				final float dd = dx * dx + dy * dy;

				if( dd < state.min )
				{
					state.min = dd;
					state.tile = tile;
					state.border = Bottom;
				}
				break;
			}
		}
	}

	private void castDown( final float px, final float py, final float vx, final float vy )
	{
		if( 0 == py % tileHeight )
		{
			final int rx = ( int ) ( px / tileWidth );
			final int ry = ( int ) ( py / tileHeight );

			final int tile = layer.getTileAt( rx, ry );

			if( 0 < tile )
			{
				state.min = 0;
				state.tile = tile;
				state.border = Top;
				return;
			}
		}

		final float over = -vx / vy;

		int ry = ( int ) ( py / tileHeight );

		while( ++ry < layer.height ) // down
		{
			final float ay = ry * tileHeight;
			final float ax = px - ( ay - py ) * over;

			final int tile = layer.getTileAt( ( int ) ( ax / tileWidth ), ry );

			if( -1 == tile )
				break;

			if( 0 != tile )
			{
				final float dx = ax - px;
				final float dy = ay - py;
				final float dd = dx * dx + dy * dy;

				if( dd < state.min )
				{
					state.min = dd;
					state.tile = tile;
					state.border = Top;
				}
				break;
			}
		}
	}

	private void castRight( final float px, final float py, final float vx, final float vy )
	{
		if( 0 == px % tileWidth )
		{
			final int rx = ( int ) ( px / tileWidth );
			final int ry = ( int ) ( py / tileHeight );

			final int tile = layer.getTileAt( rx, ry );

			if( 0 < tile )
			{
				state.min = 0;
				state.tile = tile;
				state.border = Left;
				return;
			}
		}

		final float over = -vy / vx;

		int rx = ( int ) ( px / tileWidth );

		while( ++rx < layer.width )
		{
			final float ax = rx * tileWidth;
			final float ay = py - ( ax - px ) * over;

			final int tile = layer.getTileAt( rx, ( int ) ( ay / tileHeight ) );

			if( -1 == tile )
				break;

			if( 0 != tile )
			{
				final float dx = ax - px;
				final float dy = ay - py;
				final float dd = dx * dx + dy * dy;

				if( dd < state.min )
				{
					state.min = dd;
					state.tile = tile;
					state.border = Left;
				}
				break;
			}
		}
	}
}