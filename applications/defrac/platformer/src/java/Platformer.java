import defrac.display.Stage;
import defrac.display.Stats;
import defrac.display.event.UIEventManager;
import defrac.util.KeyCode;
import platformer.PlatformerStage;
import platformer.gl.MonitorFilter;
import platformer.renderer.ObjectsRenderer;
import platformer.renderer.TileRenderer;
import platformer.tmx.MapLayer;
import platformer.tmx.MapObjectGroupLayer;
import platformer.tmx.MapResources;
import platformer.tmx.MapTileLayer;
import platformer.utils.TinyConsole;

import javax.annotation.Nonnull;

import static java.lang.Math.rint;

/**
 * @author Andre Michelle
 */
public final class Platformer
{
	private static final double Drag = 0.1;
	private static final double Force = 1.0;

	// Simple easing
	private double positionX = 0.0;
	private double positionY = 0.0;
	private double velocityX = 0.0;
	private double velocityY = 0.0;

	public Platformer( @Nonnull final Stage stage )
	{
		// Test-Files
		//
		final String levelFile = "mario-1-1.json"; // checks unreasonable objects drawing
		//		final String levelFile = "zelda.json"; // checks a big map
		//		final String levelFile = "fuzed.json"; // issues with tiles that have a different size (pickups) + transparency

		System.out.println( "loading " + levelFile );

		MapResources.load( levelFile ).onSuccess( map -> {
			System.out.println( "loaded " + map );

			final PlatformerStage platformerStage = new PlatformerStage( map, 24, 14 );

			for( final MapLayer mapLayer : map.mapLayers )
			{
				final Class<? extends MapLayer> layerClass = mapLayer.getClass();

				if( MapTileLayer.class.equals( layerClass ) )
					platformerStage.addRenderer( new TileRenderer( platformerStage, ( MapTileLayer ) mapLayer ) );
				else if( MapObjectGroupLayer.class.equals( layerClass ) )
					platformerStage.addRenderer( new ObjectsRenderer( platformerStage, ( MapObjectGroupLayer ) mapLayer ) );
			}

			stage.addChild( platformerStage.displayObject() ).moveTo( 0f, 64f ).filter( new MonitorFilter() );
			stage.addChild( TinyConsole.get() ).moveTo( platformerStage.pixelWidth() - TinyConsole.Width, 0f );
			stage.addChild( new Stats() );

			final TileRenderer renderer = ( TileRenderer ) platformerStage.getLayerByName( "solid" ); // mario-1-1.json
			if( null != renderer )
			{
				renderer.rowRenderer( ( index ) -> {
					// TODO Sprite rendering with z-sorting
					// This is called before a row is rendered.
					// If you draw your sprites located in this row now,
					// they will be overridden by the next row (painter's algorithm).
					// Collisions should do the rest.
				} );
			}

			//backgroundColor( 0x0 );
			platformerStage.restartTime();

			System.out.println( "all set... (use cursor keys to navigate)" );

			final UIEventManager eventManager = stage.eventManager();

			stage.globalEvents().onEnterFrame.add( ( ignore ) -> {

				final boolean l = eventManager.isKeyDown( KeyCode.LEFT );
				final boolean r = eventManager.isKeyDown( KeyCode.RIGHT );
				final boolean u = eventManager.isKeyDown( KeyCode.UP );
				final boolean d = eventManager.isKeyDown( KeyCode.DOWN );

				final double forceX;
				final double forceY;

				if( l && !r )
					forceX = -Force;
				else if( r && !l )
					forceX = Force;
				else
					forceX = 0.0;

				if( u && !d )
					forceY = -Force;
				else if( d && !u )
					forceY = Force;
				else
					forceY = 0.0;

				velocityX += forceX;
				velocityY += forceY;

				velocityX -= Drag * velocityX;
				velocityY -= Drag * velocityY;

				positionX += velocityX;
				positionY += velocityY;

				final boolean moved = platformerStage.moveTo( ( int ) rint( positionX ), ( int ) rint( positionY ) );

				if( !moved )
				{
					positionX = platformerStage.offsetX();
					positionY = platformerStage.offsetY();
				}
			} );
		} );
	}
}