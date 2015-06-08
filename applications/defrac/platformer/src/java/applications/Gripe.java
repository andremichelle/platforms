package applications;

import defrac.display.DisplayObject;
import defrac.display.Stage;
import defrac.display.Stats;
import defrac.util.KeyCode;
import platformer.Platformer;
import platformer.glare.GlareTextureProgramAperture;
import platformer.filter.MonitorFilter;
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
public final class Gripe
{
	private static final MonitorFilter MonitorFilter = new MonitorFilter();

	public Gripe( @Nonnull final Stage stage )
	{
		final String levelFile = "fuzed.json"; // checks unreasonable objects drawing
		System.out.println( "loading " + levelFile );

		MapResources.load( levelFile ).onSuccess( mapData -> {
			System.out.println( "loaded " + mapData );

			final Platformer platformer = new Platformer( mapData, 24, 14 );

			final GlareTextureProgramAperture orificeProgram =
					platformer.glare().getProgram( GlareTextureProgramAperture.class );

			final SpriteLayer spriteLayer = new SpriteLayer( platformer );
			final GripeSprite sprite = new GripeSprite( platformer, stage );
			spriteLayer.addSprite( sprite );

			for( final MapLayer mapLayer : mapData.mapLayers )
			{
				final Class<? extends MapLayer> layerClass = mapLayer.getClass();

				if( MapTileLayer.class.equals( layerClass ) )
				{
					final TileRenderer renderer;
					if( "hidden".equals( mapLayer.name ) )
						renderer = new TileRenderer( platformer, ( MapTileLayer ) mapLayer, orificeProgram );
					else
						renderer = new TileRenderer( platformer, ( MapTileLayer ) mapLayer );

					platformer.addRenderer( renderer );
				}

				if( "sprites".equals( mapLayer.name ) )
				{
					platformer.addRenderer( spriteLayer );
				}
			}

			final DisplayObject stageObject = stage.addChild( platformer.createDisplayObject() ).
					moveTo( 0f, 64f ).
					filter( MonitorFilter );

			stage.addChild( TinyConsole.get() ).moveTo( platformer.pixelWidth() - TinyConsole.Width, 0f );
			stage.addChild( new Stats() );

			platformer.restartTime();
			stage.backgroundColor( 0xFF000000 );

			System.out.println( "all set... (use cursor keys to navigate)" );

			stage.globalEvents().onKeyUp.add( event -> {
				if( event.keyCode == KeyCode.ESCAPE )
				{
					if( null == stageObject.filter() )
						stageObject.filter( MonitorFilter );
					else
						stageObject.filter( null );
				}
			} );

			stage.globalEvents().onEnterFrame.add( ( ignore ) -> {
				platformer.center( sprite );
				orificeProgram.setCircle(
						sprite.x() - platformer.offsetX() + sprite.width() / 2,
						sprite.y() - platformer.offsetY(),
						48f );
			} );
		} );
	}
}