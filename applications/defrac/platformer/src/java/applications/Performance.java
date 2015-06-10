package applications;

import defrac.display.Canvas;
import defrac.display.GLSurface;
import defrac.display.Stage;
import defrac.display.Stats;
import defrac.display.graphics.Graphics;
import defrac.geom.Rectangle;
import defrac.util.Timer;
import platformer.glare.Glare;
import platformer.glare.GlareLineProgram;
import platformer.glare.GlareRectangleProgram;
import platformer.glare.GlareTextureProgram;
import platformer.glare.GlareTextureProgramAperture;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public final class Performance
{
	private static final float SIZE = 512;

	static float ran( final float max )
	{
		return ( float ) ( Math.random() * max );
	}

	static final class RectShape
	{
		final float x;
		final float y;
		final float w;
		final float h;
		final float r;
		final float g;
		final float b;
		final float a;

		RectShape()
		{
			final float x0 = ran( SIZE );
			final float x1 = ran( SIZE );
			final float y0 = ran( SIZE );
			final float y1 = ran( SIZE );

			x = Math.min( x0, x1 );
			y = Math.min( y0, y1 );
			w = Math.abs( x1 - x0 );
			h = Math.abs( y1 - y0 );
			r = ran( 1f );
			g = ran( 1f );
			b = ran( 1f );
			a = ran( 1f );
		}
	}

	private final Rectangle bounds = new Rectangle( 0, 0, SIZE, SIZE );

	private boolean useGL = true;

	public Performance( @Nonnull final Stage stage )
	{
		final int numRect = 2048;

		final RectShape[] shapes = new RectShape[ numRect ];

		for( int i = 0 ; i < numRect ; ++i )
			shapes[ i ] = new RectShape();

		final Canvas canvas = new Canvas( SIZE, SIZE );
		stage.addChild( canvas );
		final Graphics graphics = canvas.graphics();

		final Glare glare = new Glare.Builder().
				addProgram( GlareTextureProgram.class, GlareTextureProgram::new ).
				addProgram( GlareTextureProgramAperture.class, GlareTextureProgramAperture::new ).
				addProgram( GlareLineProgram.class, GlareLineProgram::new ).
				addProgram( GlareRectangleProgram.class, GlareRectangleProgram::new ).
				build();

		final int test = 0;

		stage.addChild( new GLSurface( SIZE, SIZE, ( glSurface, gl, glFrameBuffer, glRenderBuffer, glTexture, v, v1, p, i1, b ) -> {
			glare.prepare( gl, SIZE, SIZE );
			if( useGL )
			{
				switch( test )
				{
					case 0:
						final GlareLineProgram lineProgram = glare.getProgram( GlareLineProgram.class );
						for( int i = 0 ; i < numRect ; ++i )
							drawLine( lineProgram, shapes[ i ] );
						break;
					case 1:
						final GlareRectangleProgram rectProgram = glare.getProgram( GlareRectangleProgram.class );
						for( int i = 0 ; i < numRect ; ++i )
							drawRect( rectProgram, shapes[ i ] );
						break;
				}
			}
			glare.complete();
		} ) ).moveTo( 512, 0 );

		stage.globalEvents().onEnterFrame.add( ( event ) -> {
			graphics.clearRect( bounds );
			if( !useGL )
			{
				if( 0 == test )
				{
					for( int i = 0 ; i < numRect ; ++i )
						drawLine( graphics, shapes[ i ] );
				}
				else if( 1 == test )
				{
					for( int i = 0 ; i < numRect ; ++i )
						drawRect( graphics, shapes[ i ] );
				}
			}
		} );

		stage.addChild( new Stats() );
		stage.globalEvents().onReset.add( (event) -> {
			System.out.println( "reset" );
		} );

		new Timer( 5000, 0 ).listener( new Timer.SimpleListener()
		{
			@Override
			public void onTimerTick( @Nonnull final Timer timer )
			{
				useGL = !useGL;

				System.out.println( "useGL: " + useGL );
			}
		} ).resetAndStart();
	}

	private void drawRect( final GlareRectangleProgram rectProgram, final RectShape shape )
	{
		rectProgram.color( shape.r, shape.g, shape.b, shape.a );
		rectProgram.rect( shape.x, shape.y, shape.w, shape.h );
	}

	private void drawLine( final GlareLineProgram program, final RectShape shape )
	{
		program.color( shape.r, shape.g, shape.b, shape.a );
		program.line( shape.x, shape.y, shape.x + shape.w, shape.y + shape.h );
	}

	private void drawRect( final Graphics graphics, final RectShape shape )
	{
		graphics.fillStyle( shape.r, shape.g, shape.b, shape.a );
		graphics.fillRect( shape.x, shape.y, shape.w, shape.h );
	}

	private void drawLine( final Graphics graphics, final RectShape shape )
	{
		graphics.beginPath();
		graphics.strokeStyle( shape.r, shape.g, shape.b, shape.a );
		graphics.moveTo( shape.x, shape.y );
		graphics.lineTo( shape.x + shape.w, shape.y + shape.h );
		graphics.stroke();
		graphics.closePath();
	}
}