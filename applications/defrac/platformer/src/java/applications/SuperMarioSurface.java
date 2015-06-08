package applications;

import defrac.ui.GLSurface;
import platformer.Platformer;
import platformer.renderer.ObjectsRenderer;
import platformer.renderer.TileRenderer;
import platformer.tmx.MapLayer;
import platformer.tmx.MapObjectGroupLayer;
import platformer.tmx.MapResources;
import platformer.tmx.MapTileLayer;

import javax.annotation.Nonnull;

/**
 * No display-list here. Just checking if that works as well.
 * No events yets.
 *
 * @author Andre Michelle
 */
public final class SuperMarioSurface
{
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
		} );
	}
}