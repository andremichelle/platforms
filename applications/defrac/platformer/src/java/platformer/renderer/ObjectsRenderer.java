package platformer.renderer;

import defrac.display.graphics.Graphics;
import defrac.geom.Point;
import platformer.DebugScreen;
import platformer.tmx.MapLayer;
import platformer.tmx.MapObject;
import platformer.tmx.MapObjectGroupLayer;
import platformer.tmx.ObjectEllipse;
import platformer.tmx.ObjectPolyLine;
import platformer.tmx.ObjectRectangle;

import javax.annotation.Nonnull;

/**
 * Supposed for debugging. Not optimised.
 *
 * @author Andre Michelle
 */
public final class ObjectsRenderer implements Renderer
{
	private final DebugScreen screen;
	private final MapObjectGroupLayer layer;

	private final Graphics graphics;

	public ObjectsRenderer( @Nonnull final DebugScreen screen, @Nonnull final MapObjectGroupLayer layer )
	{
		this.screen = screen;
		this.layer = layer;

		graphics = new Graphics( screen.pixelWidth(), screen.pixelHeight() );
	}

	@Override
	public void renderLayer()
	{
		if( !layer.visible )
			return;

		graphics.clearRect( 0, 0, screen.pixelWidth(), screen.pixelHeight() );

		for( MapObject mapObject : layer.mapObjects )
		{
			if( null == mapObject )
				continue;

			final Class<? extends MapObject> objectClass = mapObject.getClass();

			if( ObjectPolyLine.class.equals( objectClass ) )
			{
				render( ( ObjectPolyLine ) mapObject );
			}
			else if( ObjectRectangle.class.equals( objectClass ) )
			{
				render( ( ObjectRectangle ) mapObject );
			}
			else if( ObjectEllipse.class.equals( objectClass ) )
			{
				render( ( ObjectEllipse ) mapObject );
			}
		}

		screen.imageRenderer().draw( graphics.texture(), 0, 0, screen.pixelWidth(), screen.pixelHeight() );
	}

	@Nonnull
	@Override
	public MapLayer layer()
	{
		return layer;
	}

	private void render( @Nonnull final ObjectRectangle objectRectangle )
	{
		defineStroke();

		graphics.strokeRect(
				objectRectangle.x - screen.offsetX() + 0.5f,
				objectRectangle.y - screen.offsetY() + 0.5f,
				objectRectangle.width, objectRectangle.height );
		graphics.stroke();
	}

	private void render( @Nonnull final ObjectEllipse objectEllipse )
	{
		defineStroke();

		final int width = objectEllipse.width;
		final int height = objectEllipse.height;

		graphics.roundRect(
				objectEllipse.x - screen.offsetX() + 0.5f,
				objectEllipse.y - screen.offsetY() + 0.5f,
				width, height,
				width * 0.5f, height * 0.5f );

		graphics.stroke();
	}

	private void render( @Nonnull final ObjectPolyLine objectPolyLine )
	{
		final Point[] points = objectPolyLine.points;
		final int n = points.length;

		if( 2 > n )
			return;

		defineStroke();

		final float x = objectPolyLine.x - screen.offsetX() + 0.5f;
		final float y = objectPolyLine.y - screen.offsetY() + 0.5f;

		final Point first = points[ 0 ];
		graphics.moveTo( x + first.x, y + first.y );

		for( int i = 1 ; i < n ; ++i )
		{
			final Point point = points[ i ];

			graphics.lineTo( x + point.x, y + point.y );
		}

		graphics.stroke();
	}

	private void defineStroke()
	{
		graphics.beginPath();
		graphics.globalAlpha( 1f );
		graphics.lineWidth( 1f );
		graphics.strokeStyle( 1f, 0.6f, 0f, 1f );
	}
}