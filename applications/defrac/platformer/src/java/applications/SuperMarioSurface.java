package applications;

import defrac.ui.GLSurface;
import defrac.ui.event.KeyEvent;
import platformer.Platformer;
import platformer.gl.MonitorFilter;
import platformer.renderer.ObjectsRenderer;
import platformer.renderer.TileRenderer;
import platformer.tmx.MapLayer;
import platformer.tmx.MapObjectGroupLayer;
import platformer.tmx.MapResources;
import platformer.tmx.MapTileLayer;

import javax.annotation.Nonnull;

/**
 * This is not working right now.
 * It is supposed to run without any DisplayList in future,
 * but there are too many dependencies to a stage in the texture management for now.
 *
 * @author Andre Michelle
 */
public final class SuperMarioSurface
{
	private static final MonitorFilter MonitorFilter = new MonitorFilter();
	private static final double Drag = 0.1;
	private static final double Force = 1.0;

	// Simple view-port easing
	private double positionX = 0.0;
	private double positionY = 0.0;
	private double velocityX = 0.0;
	private double velocityY = 0.0;

	public SuperMarioSurface( @Nonnull final GLSurface surface )
	{
		final String levelFile = "mario-1-1.json"; // checks unreasonable objects drawing
		System.out.println( "loading " + levelFile );

		MapResources.load( levelFile ).onSuccess( mapData -> {
			System.out.println( "loaded " + mapData );

			final Platformer platformer = new Platformer( mapData, 24, 14 );

			for( final MapLayer mapLayer : mapData.mapLayers )
			{
				final Class<? extends MapLayer> layerClass = mapLayer.getClass();

				if( MapTileLayer.class.equals( layerClass ) )
					platformer.addRenderer( new TileRenderer( platformer, ( MapTileLayer ) mapLayer ) );
				else if( MapObjectGroupLayer.class.equals( layerClass ) )
					platformer.addRenderer( new ObjectsRenderer( platformer, ( MapObjectGroupLayer ) mapLayer ) );
			}

			surface.renderer( platformer.createGLSurfaceRenderer() );
			surface.onResume();

			platformer.restartTime();

			System.out.println( "all set... (use cursor keys to navigate)" );

			surface.keyListener( ( view, keyEvent ) -> {
				if( keyEvent.type == KeyEvent.TYPE_DOWN )
				{
					// ...
				}
				return false;
			} );

			// TODO Get enterFrame?

			/*
			surface.globalEvents().onEnterFrame.add( ( ignore ) -> {

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

				final boolean moved = platformer.moveTo( ( int ) rint( positionX ), ( int ) rint( positionY ) );

				if( !moved )
				{
					positionX = platformer.offsetX();
					positionY = platformer.offsetY();
				}
			} );*/
		} );
	}
}