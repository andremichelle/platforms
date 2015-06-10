package applications;

import defrac.display.Stage;
import defrac.display.event.UIEventManager;
import defrac.geom.Point;
import platformer.Platformer;
import platformer.glare.GlareLineProgram;
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

			final Platformer platformer = new Platformer( mapData, 32, 16 );

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
		private static final int Width = 32;
		private static final int Height = 32;

		private final Point mouse;
		private final MapTileLayer tiles;
		private final TilesRayCast caster;

		private float x0 = 32 * 13;
		private float y0 = 32 * 7;
		private float x1 = 0;
		private float y1 = 0;

		public PathSprite( @Nonnull final Stage stage, @Nonnull final Platformer platformer, @Nonnull final MapTileLayer tiles )
		{
			this.tiles = tiles;

			caster = new TilesRayCast( tiles, platformer.tileWidth(), platformer.tileHeight() );

			mouse = new Point();

			final UIEventManager eventManager = stage.eventManager();
			stage.globalEvents().onEnterFrame.add( ( evt ) -> {
				eventManager.pointerPos( mouse, 0 );
				x1 = ( int ) mouse.x - Width / 2;
				y1 = ( int ) mouse.y - platformer.tileHeight() + Height / 2;
			} );
			stage.globalEvents().onPointerDown.add( ( evt ) -> {
				{
					float tmp;
					tmp = x0;
					x0 = x1;
					x1 = tmp;
					tmp = y0;
					y0 = y1;
					y1 = tmp;
				}
			} );
		}

		@Override
		public float x()
		{
			return x0;
		}

		@Override
		public float y()
		{
			return y0;
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
			drawPhases( context, x0, y0 );
			drawPhases( context, x1, y1 );

			final float distance = caster.solve( this, x1 - x0, y1 - y0 );

			if( distance <= 1.0f )
			{
				drawPhases( context, x0 + distance * ( x1 - x0 ), y0 + distance * ( y1 - y0 ), 1f );
			}
		}

		private void drawPhases( @Nonnull final RendererContext context, final float x, final float y )
		{
			drawPhases( context, x, y, 1f );
		}

		private void drawPhases( @Nonnull final RendererContext context, final float x, final float y, final float alpha )
		{
			context.glare().getProgram( GlareLineProgram.class ).color( 1f, 0f, 0f, alpha ).outlineRect(
					x - context.offsetX(),
					y - context.offsetY() - Height + context.tileHeight(),
					Width, Height );
		}
	}
}