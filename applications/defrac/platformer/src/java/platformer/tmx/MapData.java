package platformer.tmx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * @author Andre Michelle
 */
public final class MapData
{
	public final MapOrientation mapOrientation; // not used yet
	public final MapRenderOrder mapRenderOrder; // not used yet
	public final MapLayer[] mapLayers;
	public final TileSet[] tileSets;
	public final int version; // not used yet
	public final int width;
	public final int height;
	public final int tileWidth;
	public final int tileHeight;
	public final int nextObjectId; // not used yet
	public final int backgroundColor; // not used yet

	MapData(
			@Nonnull final MapOrientation mapOrientation,
			@Nonnull final MapRenderOrder mapRenderOrder,
			@Nonnull final MapLayer[] mapLayers,
			@Nonnull final TileSet[] tileSets,
			final int version,
			final int width, final int height,
			final int tileWidth, final int tileHeight,
			final int nextObjectId,
			final int backgroundColor )
	{
		this.mapOrientation = mapOrientation;
		this.mapRenderOrder = mapRenderOrder;
		this.mapLayers = mapLayers;
		this.tileSets = tileSets;
		this.version = version;
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.nextObjectId = nextObjectId;
		this.backgroundColor = backgroundColor;
	}

	@Nullable
	public MapLayer getLayerByName( @Nonnull final String name )
	{
		for( final MapLayer mapLayer : mapLayers )
		{
			if( name.equals( mapLayer.name ) )
				return mapLayer;
		}

		return null;
	}

	@Override
	public String toString()
	{
		return "[MapData" +
				" version: " + version +
				", width: " + width +
				", height: " + height +
				", tileWidth: " + tileWidth +
				", tileHeight: " + tileHeight +
				", nextObjectId: " + nextObjectId +
				", backgroundColor: " + backgroundColor +
				", orientation: " + mapOrientation +
				", renderOrder: " + mapRenderOrder +
				", layers: " + Arrays.toString( mapLayers ) +
				", tileSets: " + Arrays.toString( tileSets ) +
				"]";
	}
}