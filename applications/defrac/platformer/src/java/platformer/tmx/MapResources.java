package platformer.tmx;

import defrac.concurrent.Future;
import defrac.concurrent.Promise;
import defrac.concurrent.Promises;
import defrac.display.Texture;
import defrac.display.TextureData;
import defrac.geom.Point;
import defrac.json.JSON;
import defrac.json.JSONArray;
import defrac.json.JSONObject;
import defrac.lang.Lists;
import defrac.resource.ResourceGroup;
import defrac.resource.StringResource;
import defrac.resource.TextureDataResource;
import defrac.util.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static defrac.lang.Preconditions.checkNotNull;

/**
 * Loads and parses the tmx format in json.
 * All resources as images will be loaded as well.
 *
 * @author Andre Michelle
 */
public final class MapResources
{
	@Nonnull
	public static Future<MapData> load( @Nonnull final String path )
	{
		final Promise<MapData> promise = Promises.create();

		JSON.parse( StringResource.from( path ) ).
				onFailure( Throwable::printStackTrace ).
				onSuccess( json -> {
					final JSONObject jsonLevel = checkNotNull( json.asObject() );

					final JSONArray jsonTilesets = checkNotNull( jsonLevel.getArray( "tilesets" ) );

					final ResourceGroup<TextureData> resourceGroup = collectResourceGroup( jsonTilesets );

					if( 0 == resourceGroup.size() )
					{
						promise.success( decode( jsonLevel, Lists.newArrayList( 0 ) ) );
					}
					else
					{
						resourceGroup.listener( new ResourceGroup.SimpleListener<TextureData>()
						{
							@Override
							public void onResourceGroupComplete(
									@Nonnull final ResourceGroup<TextureData> resourceGroup,
									@Nonnull final List<TextureData> textures )
							{
								promise.success( decode( jsonLevel, textures ) );
							}

							@Override
							public void onResourceGroupError(
									@Nonnull final ResourceGroup<TextureData> resourceGroup,
									@Nonnull final Throwable reason )
							{
								promise.failure( reason );
							}
						} );

						resourceGroup.load();
					}
				} );

		return promise.future();
	}

	@Nonnull
	private static ResourceGroup<TextureData> collectResourceGroup( @Nonnull final JSONArray jsonTilesets )
	{
		final ResourceGroup<TextureData> textureResources = new ResourceGroup<>( jsonTilesets.length() );

		for( JSON json : jsonTilesets )
		{
			final JSONObject jsonTileset = checkNotNull( json.asObject() );

			final String image = checkNotNull( jsonTileset.getString( "image" ) );

			textureResources.add( TextureDataResource.from( image ) );
		}

		return textureResources;
	}

	@Nonnull
	@SuppressWarnings( "SpellCheckingInspection" )
	private static MapData decode( @Nonnull final JSONObject jsonObject, @Nonnull final List<TextureData> textures )
	{
		final int version = jsonObject.getInt( "version" );
		final int width = jsonObject.getInt( "width" );
		final int height = jsonObject.getInt( "height" );
		final int tileWidth = jsonObject.getInt( "tilewidth" );
		final int tileHeight = jsonObject.getInt( "tileheight" );
		final int nextObjectId = jsonObject.getInt( "nextobjectid" );
		final int backgroundColor = Color.valueOf( jsonObject.optString( "backgroundcolor", "#FF000000" ) );
		final MapOrientation mapOrientation = orientation( checkNotNull( jsonObject.getString( "orientation" ) ) );
		final MapRenderOrder mapRenderOrder = renderOrder( checkNotNull( jsonObject.getString( "renderorder" ) ) );
		final MapLayer[] tileMapLayers = decodeLayers( checkNotNull( jsonObject.getArray( "layers" ) ) );
		final TileSet[] tileSets = decodeTileSets( checkNotNull( jsonObject.getArray( "tilesets" ) ), textures );

		return new MapData( mapOrientation, mapRenderOrder, tileMapLayers, tileSets, version, width, height, tileWidth, tileHeight, nextObjectId, backgroundColor );
	}

	@Nonnull
	private static MapOrientation orientation( @Nonnull final String name )
	{
		switch( name )
		{
			case "orthogonal":
				return MapOrientation.Orthogonal;
		}

		throw new RuntimeException( "Orientation (" + name + ") not implemented." );
	}

	@Nonnull
	private static MapRenderOrder renderOrder( @Nonnull final String name )
	{
		switch( name )
		{
			case "right-down":
				return MapRenderOrder.RightDown;
			case "left-down":
				return MapRenderOrder.LeftDown;
			case "right-up":
				return MapRenderOrder.RightUp;
			case "left-up":
				return MapRenderOrder.LeftUp;
		}

		throw new RuntimeException( "Unknown RenderOrder (" + name + ")." );
	}

	@Nonnull
	private static MapLayer[] decodeLayers( @Nonnull final JSONArray jsonArray )
	{
		final MapLayer[] mapLayers = new MapLayer[ jsonArray.length() ];

		int index = 0;

		for( final JSON json : jsonArray )
			mapLayers[ index++ ] = decodeLayer( checkNotNull( json.asObject() ) );

		return mapLayers;
	}

	@Nonnull
	@SuppressWarnings( "SpellCheckingInspection" )
	private static MapLayer decodeLayer( @Nonnull final JSONObject jsonObject )
	{
		final int x = jsonObject.getInt( "x" );
		final int y = jsonObject.getInt( "y" );
		final int width = jsonObject.getInt( "width" );
		final int height = jsonObject.getInt( "height" );
		final boolean visible = jsonObject.getBoolean( "visible" );
		final float opacity = jsonObject.getFloat( "opacity" );
		final String name = checkNotNull( jsonObject.getString( "name" ) );
		final String type = checkNotNull( jsonObject.getString( "type" ) );

		switch( type )
		{
			case "tilelayer":
				return new MapTileLayer(
						name, decodeDataIntegerArray( checkNotNull( jsonObject.getArray( "data" ) ) ), x, y, width, height, visible, opacity
				);

			case "objectgroup":

				return new MapObjectGroupLayer(
						name, decodeObjects( checkNotNull( jsonObject.getArray( "objects" ) ) ), x, y, width, height, visible, opacity
				);

			case "imagelayer":
				return new MapImageLayer( name, x, y, width, height, visible, opacity );
		}

		throw new RuntimeException( "Unknown layer type (" + type + ")." );
	}

	@Nonnull
	private static List<MapObject> decodeObjects( @Nonnull final JSONArray jsonArray )
	{
		final ArrayList<MapObject> mapObjects = Lists.newArrayList( jsonArray.length() );

		for( final JSON json : jsonArray )
		{
			final MapObject mapObject = decodeObject( checkNotNull( json.asObject() ) );

			if( null != mapObject )
			{
				mapObjects.add( mapObject );
			}
		}

		return mapObjects;
	}

	@Nullable
	@SuppressWarnings( "SpellCheckingInspection" )
	private static MapObject decodeObject( @Nonnull final JSONObject jsonObject )
	{
		final int x = jsonObject.getInt( "x" );
		final int y = jsonObject.getInt( "y" );
		final int width = jsonObject.getInt( "width" );
		final int height = jsonObject.getInt( "height" );
		final boolean visible = jsonObject.getBoolean( "visible" );
		final String name = checkNotNull( jsonObject.getString( "name" ) );

		if( jsonObject.contains( "polyline" ) )
		{
			return decodePolyLine( name, checkNotNull( jsonObject.getArray( "polyline" ) ), x, y, width, height, visible, false );
		}
		else if( jsonObject.contains( "polygon" ) )
		{
			return decodePolyLine( name, checkNotNull( jsonObject.getArray( "polygon" ) ), x, y, width, height, visible, true );
		}
		else if( jsonObject.contains( "ellipse" ) )
		{
			return new ObjectEllipse( x, y, width, height, visible, name );
		}

		return new ObjectRectangle( x, y, width, height, visible, name );
	}

	@Nonnull
	private static MapObject decodePolyLine(
			@Nonnull final String name,
			@Nonnull final JSONArray jsonArray,
			final int x, final int y,
			final int width, final int height,
			final boolean visible,
			final boolean closed )
	{
		final int n = jsonArray.length();

		final Point[] points = new Point[ n + ( closed ? 1 : 0 ) ];

		for( int i = 0 ; i < n ; ++i )
		{
			final JSON json = jsonArray.get( i );
			assert json != null;

			final JSONObject jsonPoint = json.asObject();
			assert jsonPoint != null;

			points[ i ] = new Point( jsonPoint.getInt( "x" ), jsonPoint.getInt( "y" ) );
		}

		if( closed )
		{
			points[ n ] = points[ 0 ];
		}

		return new ObjectPolyLine( x, y, width, height, visible, name, points );
	}

	@Nonnull
	private static TileSet[] decodeTileSets( @Nonnull final JSONArray jsonArray, @Nonnull final List<TextureData> textures )
	{
		final TileSet[] tileSets = new TileSet[ jsonArray.length() ];

		int index = 0;

		for( final JSON json : jsonArray )
		{
			final JSONObject jsonObject = json.asObject();

			if( null == jsonObject )
				throw new RuntimeException( "Broken Json" );

			tileSets[ index ] = decodeTileSet( jsonObject, textures.get( index ) );

			index++;
		}

		return tileSets;
	}

	@Nonnull
	@SuppressWarnings( "SpellCheckingInspection" )
	private static TileSet decodeTileSet( @Nonnull final JSONObject jsonObject, @Nonnull final TextureData textureData )
	{
		final int imageWidth = jsonObject.getInt( "imagewidth" );
		final int imageHeight = jsonObject.getInt( "imageheight" );
		final int tileWidth = jsonObject.getInt( "tilewidth" );
		final int tileHeight = jsonObject.getInt( "tileheight" );
		final int margin = jsonObject.getInt( "margin" );
		final int spacing = jsonObject.getInt( "spacing" );
		final int firstGID = jsonObject.getInt( "firstgid" );
		final String name = checkNotNull( jsonObject.getString( "name" ) );
		final JSONObject tiles = jsonObject.optObject( "tiles" );

		final HashMap<Integer, TileAnimation> animations = new HashMap<>();

		if( tiles != JSONObject.EMPTY )
		{
			for( final String tileId : tiles.keySet() )
			{
				final JSONObject meta = tiles.getObject( tileId );
				assert null != meta;

				if( meta.contains( "animation" ) )
				{
					animations.put( Integer.parseInt( tileId, 10 ), decodeAnimation( checkNotNull( meta.getArray( "animation" ) ) ) );
				}
			}
		}

		final int numTilesH = imageWidth / tileWidth;
		final int numTilesV = imageHeight / tileHeight;

		final int numTiles = numTilesH * numTilesV;

		final Texture[] textures = new Texture[ numTiles ];

		for( int i = 0 ; i < numTiles ; ++i )
		{
			final int x = ( i % numTilesH ) * tileWidth;
			final int y = ( i / numTilesH ) * tileHeight;

			textures[ i ] = new Texture( textureData, x, y, tileWidth, tileHeight );
		}

		return new TileSet( name, textures, animations, tileWidth, tileHeight, margin, spacing, firstGID );
	}

	@Nonnull
	@SuppressWarnings( "SpellCheckingInspection" )
	private static TileAnimation decodeAnimation( @Nonnull final JSONArray jsonArray )
	{
		int relativeTime = 0;

		final TileAnimation.Builder builder = new TileAnimation.Builder( jsonArray.length() );

		for( final JSON json : jsonArray )
		{
			final JSONObject jsonObject = json.asObject();
			assert null != jsonObject;

			final int duration = jsonObject.getInt( "duration" );
			final int tileId = jsonObject.getInt( "tileid" );

			if( 0 == duration )
			{
				builder.add( Integer.MAX_VALUE, tileId );

				return builder.build();
			}
			else
			{
				builder.add( relativeTime += duration, tileId );
			}
		}

		return builder.build();
	}

	@Nonnull
	private static int[] decodeDataIntegerArray( @Nonnull final JSONArray dataJSON )
	{
		final int numData = dataJSON.length();

		final int[] data = new int[ numData ];

		for( int i = 0 ; i < numData ; ++i )
			data[ i ] = ( int ) ( dataJSON.getLong( i ) & 0xFFFFFFFFL ); // Avoid int overflow

		return data;
	}
}