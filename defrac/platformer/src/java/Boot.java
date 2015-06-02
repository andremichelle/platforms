import defrac.app.Bootstrap;
import defrac.app.GenericApp;
import defrac.display.Stats;
import defrac.display.event.UIEventManager;
import defrac.event.Events;
import defrac.util.KeyCode;
import platformer.DebugScreen;
import platformer.renderer.TileRenderer;
import platformer.tmx.MapResources;
import platformer.utils.TinyConsole;

import static java.lang.Math.rint;

/**
 * 'Platformer' is a defrac project to render tmx maps built with Tiled.
 *
 * @see <a href="http://www.defrac.com">defrac</a>
 * @see <a href="http://doc.mapeditor.org/reference/tmx-map-format/">tmx-map-format</a>
 * @see <a href="http://www.mapeditor.org">Tiled</a>
 *
 * It is a fun-project for which I do not have the time to ensure a rock solid development.
 * The goal is however to write a super-mario like jump'n'run game,
 * cause I still love that way more than all the fancy 3d-games.
 *
 * The rendering is entirely done in GL (with a minimal number of draw calls).
 *
 * Performance notes:
 * Do not use transparent tiles. Just leave them empty in Tiled.
 * Use as few as possible different tilesets. Each switch will cause a draw call to the GPU.
 *
 * Notes:
 * Defrac enables this project to run in IOS, Android, HTML and natively!
 * Make sure you have java 1.8 installed.
 *
 * TODO-LIST
 * [X] Load and parse tmx-format
 * [X] Render tiles on screen
 * [X] Render layers with opacity
 * [X] Render animations
 * [X] Support tile-flipping (horizontally, vertically, diagonally)
 * [X] Draw objects by GL (for debugging, will not be optimised)
 * [ ] Implement polygon (closed)
 * [ ] Implement map background color (waiting for a defrac update which allows to decode web colors e.g. #FFF)
 * [ ] Find the correct offset(x,y) for tilesets that are not the same tile-size as the map tile-size (render-ordering?)
 * [ ] Remove transparent color (shader or boot)
 * [ ] Implement all possible render orderings
 * [ ] Implement all possible orientations
 * [ ] Add Image-Layer support
 * [ ] Add sprite support within rendering (z-Sorting)
 * [ ] Optimise animation lookup > too much GC waste in java.util.HashMap
 * [ ] Implement margin, spacing for tilesets (need test-files)
 * [ ] Implement rotation of objects
 * [ ] Implement draworder of objects
 * [ ] Expose all meta tmx properties of objects
 * [ ] Convert tmx format to a more common and accessible structure to allow different map formats
 * [ ] Implement Physics
 *
 * @author Andre Michelle (andre.michelle@gmail.com)
 */
public final class Boot extends GenericApp
{
	public static void main( final String[] args )
	{
		Bootstrap.run( new Boot() );
	}

	private static final double Drag = 0.1;
	private static final double Force = 1.0;

	// Simple easing
	private double positionX = 0.0;
	private double positionY = 0.0;
	private double velocityX = 0.0;
	private double velocityY = 0.0;

	@Override
	protected void onStart()
	{
		// Test-Files
		//
		final String levelFile = "mario-1-1.json"; // checks unreasonable objects drawing
//		final String levelFile = "zelda.json"; // checks a big map
//		final String levelFile = "fuzed.json"; // issues with tiles that have a different size (pickups) + transparency

		System.out.println( "loading " + levelFile );

		MapResources.load( levelFile ).onSuccess( map -> {
			System.out.println( "loaded " + map );

			final DebugScreen screen = new DebugScreen( map, 32, 14 );

			stage().addChild( screen.displayObject() ).moveTo( 0f, 64f );
			stage().addChild( TinyConsole.get() ).moveTo( screen.pixelWidth() - TinyConsole.Width, 0f );
			stage().addChild( new Stats() );

			final TileRenderer renderer = ( TileRenderer ) screen.getLayerByName( "solid" ); // mario-1-1.json

			if( null != renderer )
			{
				renderer.rowRenderer( (index) -> {
					// TODO Sprite rendering with z-sorting
					// This is called before a row is rendered.
					// If you draw your sprites located in this row now,
					// they will be overridden by the next row (painter's algorithm).
					// Collisions should do the rest.
				} );
			}

			System.out.println( "all set... (use cursor keys to navigate)" );

			screen.restartTime();

			final UIEventManager eventManager = stage().eventManager();

			Events.onEnterFrame.add( ( ignore ) -> {

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

				final boolean moved = screen.moveTo( ( int ) rint( positionX ), ( int ) rint( positionY ) );

				if( !moved )
				{
					positionX = screen.offsetX();
					positionY = screen.offsetY();
				}
			} );
		} );
	}
}