package projects;

import defrac.display.Stage;
import defrac.display.Stats;
import defrac.display.Texture;
import defrac.display.TextureData;
import defrac.geom.Point;
import defrac.resource.TextureDataResource;
import platformer.PlatformerStage;
import platformer.renderer.ObjectsRenderer;
import platformer.renderer.Sprite;
import platformer.renderer.TileRenderer;
import platformer.tmx.MapLayer;
import platformer.tmx.MapObjectGroupLayer;
import platformer.tmx.MapResources;
import platformer.tmx.MapTileLayer;
import platformer.utils.TinyConsole;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Andre Michelle
 */
public final class DepthSortingTest
{
	private float pathPhase = 0.0f;

	public DepthSortingTest( @Nonnull final Stage stage )
	{
		final String levelFile = "depth-sorting.json"; // checks unreasonable objects drawing
		System.out.println( "loading " + levelFile );

		MapResources.load( levelFile ).onSuccess( mapData -> {
			System.out.println( "loaded " + mapData );

			final PlatformerStage platformerStage = new PlatformerStage( mapData, 10, 10 );

			for( final MapLayer mapLayer : mapData.mapLayers )
			{
				final Class<? extends MapLayer> layerClass = mapLayer.getClass();

				if( MapTileLayer.class.equals( layerClass ) )
					platformerStage.addRenderer( new TileRenderer( platformerStage, ( MapTileLayer ) mapLayer ) );
				else if( MapObjectGroupLayer.class.equals( layerClass ) )
					platformerStage.addRenderer( new ObjectsRenderer( platformerStage, ( MapObjectGroupLayer ) mapLayer ) );
			}

			stage.addChild( TinyConsole.get() ).moveTo( platformerStage.pixelWidth() - TinyConsole.Width, 0f );
			stage.addChild( platformerStage.displayObject() ).moveTo( 0f, 64f );
			stage.addChild( new Stats() );

			final TileRenderer renderer = ( TileRenderer ) platformerStage.getLayerByName( "solids" ); // depth-sorting.json
			assert null != renderer : "Map does not contain TileLayer solids.";
			final CharacterSprite sprite = new CharacterSprite( platformerStage );

			renderer.addSprite( sprite );

			platformerStage.restartTime();
			stage.backgroundColor( 0xFF000000 );

			System.out.println( "all set... (use cursor keys to navigate)" );

			final Point[] path = new Point[]{
					new Point( 2 * mapData.tileWidth, 7 * mapData.tileHeight - 16 ),
					new Point( 7 * mapData.tileWidth, 7 * mapData.tileHeight - 16 ),
					new Point( 7 * mapData.tileWidth, 2 * mapData.tileHeight + 16 ),
					new Point( 2 * mapData.tileWidth, 2 * mapData.tileHeight + 16 )
			};

			stage.globalEvents().onEnterFrame.add( ( ignore ) -> {

				final int pathIndex = ( int ) ( pathPhase * path.length );
				final Point a = path[ pathIndex ];
				final Point b = path[ ( pathIndex + 1 ) % path.length ];
				final float localPhase = pathPhase * path.length - pathIndex;

				sprite.moveTo( ( int ) ( a.x + localPhase * ( b.x - a.x ) ), ( int ) ( a.y + localPhase * ( b.y - a.y ) ) );

				pathPhase += 0.002f;
				pathPhase -= Math.floor( pathPhase );
			} );
		} );
	}

	private static class CharacterSprite implements Sprite
	{
		private static final int Size = 32;

		private final PlatformerStage stage;
		private final Texture[][] sheet;

		private int x;
		private int y;

		private int directionIndex;
		private boolean moving;

		public CharacterSprite( @Nonnull final PlatformerStage stage )
		{
			this.stage = stage;

			sheet = new Texture[ 4 ][ 3 ];

			TextureDataResource.from( "character.png" ).listener( new TextureDataResource.SimpleListener()
			{
				@Override
				public void onResourceComplete( @Nonnull final TextureDataResource resource, @Nonnull final TextureData content )
				{
					for( int directionIndex = 0 ; directionIndex < 4 ; ++directionIndex )
					{
						sheet[ directionIndex ] = new Texture[ 3 ];

						for( int frameIndex = 0 ; frameIndex < 3 ; ++frameIndex )
						{
							sheet[ directionIndex ][ frameIndex ] = new Texture( content, frameIndex * Size, directionIndex * Size, Size, Size );
						}
					}
				}
			} ).load();
		}

		public void moveTo( final int x, final int y )
		{
			final int deltaX = x - this.x;
			final int deltaY = y - this.y;

			if( 0 != deltaX )
			{
				directionIndex = deltaX < 0 ? 1 : 2;
				moving = true;
			}
			else if( 0 != deltaY )
			{
				directionIndex = deltaY < 0 ? 3 : 0;
				moving = true;
			}

			this.x = x;
			this.y = y;
		}

		public void stop()
		{
			moving = false;
		}

		@Override
		public int x()
		{
			return x;
		}

		@Override
		public int y()
		{
			return y;
		}

		@Override
		public int width()
		{
			return Size;
		}

		@Override
		public int height()
		{
			return Size;
		}

		@Nullable
		@Override
		public Texture texture()
		{
			return sheet[ directionIndex ][ moving ? ( stage.currentTime() / 150 ) % 3 : 1 ];
		}
	}
}