package applications;

import defrac.display.Stage;
import defrac.display.Stats;
import defrac.display.graphics.Graphics;
import platformer.Platformer;
import platformer.renderer.ObjectsRenderer;
import platformer.renderer.RendererContext;
import platformer.renderer.Sprite;
import platformer.renderer.SpriteLayer;
import platformer.renderer.TileRenderer;
import platformer.tmx.MapLayer;
import platformer.tmx.MapObjectGroupLayer;
import platformer.tmx.MapResources;
import platformer.tmx.MapTileLayer;
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

			final Platformer platformer = new Platformer( mapData, 32, 16 );

			final CharacterSprite sprite = new CharacterSprite();

			for( final MapLayer mapLayer : mapData.mapLayers )
			{
				final Class<? extends MapLayer> layerClass = mapLayer.getClass();

				if( MapTileLayer.class.equals( layerClass ) )
				{
					platformer.addRenderer( new TileRenderer( platformer, ( MapTileLayer ) mapLayer ) );
				}
				else if( MapObjectGroupLayer.class.equals( layerClass ) )
				{
					platformer.addRenderer( new ObjectsRenderer( platformer, ( MapObjectGroupLayer ) mapLayer ) );
				}
			}

			final SpriteLayer spriteLayer = new SpriteLayer( platformer );
			spriteLayer.addSprite( sprite );
			platformer.addRenderer( spriteLayer );

			stage.addChild( TinyConsole.get() ).moveTo( platformer.pixelWidth() - TinyConsole.Width, 0f );
			stage.addChild( platformer.displayObject() ).moveTo( 0f, 64f );
			stage.addChild( new Stats() );

			platformer.restartTime();

			System.out.println( "all set... (just watch)" );
		} );
	}

	private static class CharacterSprite implements Sprite
	{
		private static final int Size = 32;

		private final Graphics graphics;

		private int x = 144;
		private int y = 64;

		public CharacterSprite()
		{
			graphics = new Graphics( Size, Size );
			graphics.strokeStyle( 1, 0, 0, 1 );
			graphics.rect( 0, 0, Size, Size );
			graphics.stroke();
		}

		public void moveTo( final int x, final int y )
		{
			this.x = x;
			this.y = y;
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

		@Override
		public void requestRender( @Nonnull final RendererContext context )
		{
			context.imageRenderer().draw(
					graphics.texture(),
					x - context.offsetX(),
					y - context.offsetY() - Size + context.tileHeight(),
					Size, Size );
		}
	}
}