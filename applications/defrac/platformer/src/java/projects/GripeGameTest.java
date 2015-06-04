package projects;

import defrac.display.DisplayObject;
import defrac.display.Stage;
import defrac.display.Stats;
import defrac.util.KeyCode;
import platformer.Platformer;
import platformer.gl.MonitorFilter;
import platformer.renderer.SpriteLayer;
import platformer.renderer.TileRenderer;
import platformer.tmx.MapLayer;
import platformer.tmx.MapResources;
import platformer.tmx.MapTileLayer;
import platformer.utils.TinyConsole;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public final class GripeGameTest
{
	private static final MonitorFilter MonitorFilter = new MonitorFilter();

	public GripeGameTest( @Nonnull final Stage stage )
	{
		final String levelFile = "fuzed.json"; // checks unreasonable objects drawing
		System.out.println( "loading " + levelFile );

		MapResources.load( levelFile ).onSuccess( mapData -> {
			System.out.println( "loaded " + mapData );

			final Platformer platformer = new Platformer( mapData, 24, 14 );

			final SpriteLayer spriteLayer = new SpriteLayer( platformer );
			final GripeSprite sprite = new GripeSprite( platformer, stage );
			spriteLayer.addSprite( sprite );

			for( final MapLayer mapLayer : mapData.mapLayers )
			{
				final Class<? extends MapLayer> layerClass = mapLayer.getClass();

				if( MapTileLayer.class.equals( layerClass ) )
					platformer.addRenderer( new TileRenderer( platformer, ( MapTileLayer ) mapLayer ) );

				if( "sprites".equals( mapLayer.name ) )
				{
					platformer.addRenderer( spriteLayer );
				}
			}

			final DisplayObject stageObject = stage.addChild( platformer.displayObject() ).moveTo( 0f, 64f );

			stage.addChild( TinyConsole.get() ).moveTo( platformer.pixelWidth() - TinyConsole.Width, 0f );
			stage.addChild( platformer.displayObject() );
			stage.addChild( new Stats() );

			platformer.restartTime();
			stage.backgroundColor( 0xFF000000 );

			System.out.println( "all set... (use cursor keys to navigate)" );

			stage.globalEvents().onKeyUp.add( event -> {
				if( event.keyCode == KeyCode.SPACE )
				{
					if( null == stageObject.filter() )
						stageObject.filter( MonitorFilter );
					else
						stageObject.filter( null );
				}
			} );

			stage.globalEvents().onEnterFrame.add( ( ignore ) -> platformer.center( sprite ) );
		} );
	}
}