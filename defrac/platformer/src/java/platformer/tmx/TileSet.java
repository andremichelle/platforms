package platformer.tmx;

import defrac.display.Texture;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * @author Andre Michelle
 */
public final class TileSet
{
	private final Texture[] textures;
	private final HashMap<Integer, TileAnimation> animations;
	public final int tileWidth;
	public final int tileHeight;
	public final int margin; // not used yet
	public final int spacing; // not used yet
	public final int firstGID;
	public final String name; // not used yet

	TileSet(
			@Nonnull final String name,
			@Nonnull final Texture[] textures,
			@Nonnull final HashMap<Integer, TileAnimation> animations,
			final int tileWidth,
			final int tileHeight,
			final int margin,
			final int spacing,
			final int firstGID )
	{
		this.textures = textures;
		this.animations = animations;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.margin = margin;
		this.spacing = spacing;
		this.firstGID = firstGID;
		this.name = name;
	}

	@Nullable
	public Texture getTileAt( final int index, final int time )
	{
		if( 0 > index || index >= textures.length )
			return null;

		final TileAnimation tileAnimation = animations.get( index );

		if( null == tileAnimation )
			return textures[ index ];

		return textures[ tileAnimation.getCurrentIndex( time ) ];
	}

	@Override
	public String toString()
	{
		return "[TileSet" +
				" textures: " + textures.length +
				", animations: " + animations +
				", tileWidth: " + tileWidth +
				", tileHeight: " + tileHeight +
				", margin: " + margin +
				", spacing: " + spacing +
				", firstGID: " + firstGID +
				", name: '" + name + '\'' +
				"]";
	}
}