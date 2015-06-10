package applications;

import defrac.display.Stage;
import platformer.Platformer;
import platformer.glare.GlareRectangleProgram;
import platformer.renderer.ObjectsRenderer;
import platformer.renderer.RendererContext;
import platformer.renderer.Sprite;
import platformer.renderer.SpriteLayer;
import platformer.renderer.TileRenderer;
import platformer.tmx.MapLayer;
import platformer.tmx.MapObjectGroupLayer;
import platformer.tmx.MapResources;
import platformer.tmx.MapTileLayer;
import platformer.utils.TilesRayCast;
import platformer.utils.TinyConsole;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public final class Physics
{
	public Physics( @Nonnull final Stage stage )
	{
		final String levelFile = "physics.json";
		System.out.println( "loading " + levelFile );

		MapResources.load( levelFile ).onSuccess( mapData -> {
			System.out.println( "loaded " + mapData );

			MapTileLayer blocks = null;

			final Platformer platformer = new Platformer( mapData, 32, 24 );

			for( final MapLayer mapLayer : mapData.mapLayers )
			{
				final Class<? extends MapLayer> layerClass = mapLayer.getClass();

				if( MapTileLayer.class.equals( layerClass ) )
				{
					if( "blocks".equals( mapLayer.name ) )
						blocks = ( MapTileLayer ) mapLayer;

					platformer.addRenderer( new TileRenderer( platformer, ( MapTileLayer ) mapLayer ) );
				}
				else if( MapObjectGroupLayer.class.equals( layerClass ) )
				{
					platformer.addRenderer( new ObjectsRenderer( platformer, ( MapObjectGroupLayer ) mapLayer ) );
				}
			}

			final SpriteLayer spriteLayer = new SpriteLayer( platformer );
			spriteLayer.addSprite( new PathSprite( stage, platformer, blocks ) );
			platformer.addRenderer( spriteLayer );

			stage.addChild( TinyConsole.get().moveTo( platformer.pixelWidth() - TinyConsole.Width, 0f ) );
			stage.addChild( platformer.createDisplayObject() );

			platformer.restartTime();

			System.out.println( "all set..." );
		} );
	}

	private static class PathSprite implements Sprite
	{
		private static final int Width = 16;
		private static final int Height = 16;

		private final TilesRayCast caster;

		private float sx = 32 * 13;
		private float sy = 32 * 7;
		private float vx;
		private float vy;

		public PathSprite( @Nonnull final Stage stage, @Nonnull final Platformer platformer, @Nonnull final MapTileLayer tiles )
		{
			caster = new TilesRayCast( tiles, platformer.tileWidth(), platformer.tileHeight() );

			vx = -4f;
			vy = -4f;

			stage.globalEvents().onEnterFrame.add( e -> {
				float remainingTime = 1f;

				vy += 0.1;

				while( 0.0f < remainingTime )
				{
					final float distance = caster.solve( PathSprite.this, vx, vy );

					if( distance >= 1f )
						break;

					// Move to touch point
					sx += vx * distance;
					sy += vy * distance;

					// Resolve Collision
					final int border = caster.lastBorder();

					switch( border )
					{
						case TilesRayCast.Left:
						case TilesRayCast.Right:
							vx = -vx;
							break;

						case TilesRayCast.Top:
						case TilesRayCast.Bottom:
							vy = -vy;
							break;
					}

					remainingTime -= distance * remainingTime;
				}

				sx += vx * remainingTime;
				sy += vy * remainingTime;

				platformer.center( PathSprite.this );
			} );
		}

		@Override
		public float x()
		{
			return sx;
		}

		@Override
		public float y()
		{
			return sy;
		}

		@Override
		public int width()
		{
			return Width;
		}

		@Override
		public int height()
		{
			return Height;
		}

		@Override
		public void requestRender( @Nonnull final RendererContext context )
		{
			context.glare().getProgram( GlareRectangleProgram.class ).color( 0.5f, 0.5f, 0.5f, 1f ).rect(
					sx - context.offsetX(),
					sy - context.offsetY() - Height + context.tileHeight(),
					Width, Height );
		}
	}
}