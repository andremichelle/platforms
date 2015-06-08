package platformer.renderer;

import defrac.display.graphics.Graphics;
import defrac.geom.Point;
import platformer.glare.GlareTextureProgram;
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
	private final RendererContext context;
	private final MapObjectGroupLayer layer;

	private final Graphics graphics;

	public ObjectsRenderer( @Nonnull final RendererContext context, @Nonnull final MapObjectGroupLayer layer )
	{
		this.context = context;
		this.layer = layer;

		graphics = new Graphics( context.pixelWidth(), context.pixelHeight() );
	}

	@Override
	public void renderLayer()
	{
		if( !layer.visible )
			return;

		graphics.clearRect( 0, 0, context.pixelWidth(), context.pixelHeight() );

		for( final MapObject mapObject : layer.mapObjects )
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

		context.glare().getProgram( GlareTextureProgram.class ).
				draw( graphics.texture(), 0, 0, context.pixelWidth(), context.pixelHeight() );
	}

	private void render( @Nonnull final ObjectRectangle objectRectangle )
	{
		defineStroke();

		graphics.strokeRect(
				objectRectangle.x - context.offsetX() + 0.5f,
				objectRectangle.y - context.offsetY() + 0.5f,
				objectRectangle.width, objectRectangle.height );
		graphics.stroke();
	}

	private void render( @Nonnull final ObjectEllipse objectEllipse )
	{
		defineStroke();

		final int width = objectEllipse.width;
		final int height = objectEllipse.height;

		graphics.roundRect(
				objectEllipse.x - context.offsetX() + 0.5f,
				objectEllipse.y - context.offsetY() + 0.5f,
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

		final float x = objectPolyLine.x - context.offsetX() + 0.5f;
		final float y = objectPolyLine.y - context.offsetY() + 0.5f;

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
		graphics.strokeStyle( 0f, 0.6f, 0f, 1f );
	}
}