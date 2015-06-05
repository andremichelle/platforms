package platformer.gl;

import defrac.display.Stage;
import defrac.display.Texture;
import defrac.display.TextureData;
import defrac.gl.GL;
import defrac.gl.GLBuffer;
import defrac.gl.GLMatrix;
import defrac.util.Color;

import javax.annotation.Nonnull;

/**
 * A Renderer that draws images to the current GLSurface.
 * All draws will be written as geometry and only send to the GPU when the alpha value or texture changes.
 * TODO ReInit when GL context is broken (never happened while testing, but is assumed on certain platforms)
 *
 * @author Andre Michelle
 */
public final class GlRenderer
{
	private static final int BufferNumProperties = 4;
	private static final int BufferNumVertices = 6;
	private static final int BufferImageSize = BufferNumProperties * BufferNumVertices;
	private static final int BufferSizeFloat = 4;

	private final GLMatrix glMatrix;
	private final float[] buffer;

	// GLContext
	private GLBuffer glBuffer;
	private Stage stage = null;
	private GL gl = null;

	private final int bufferSize;

	// current setting
	private TextureData textureData;
	private int bufferPointer;
	private float alpha;
	private float[] background = new float[ 4 ];

	// statistics
	public int drawCalls;
	public int drawTriangles;

	// current state
	private boolean initGL = true; // must be set to true when GLContext is broken
	private boolean drawPhase;
	private boolean drawable;

	@Nonnull
	private GlRenderStrategy renderStrategy;

	/**
	 * @param capacity The number of textures that can be stored as geometry
	 */
	public GlRenderer( final int capacity )
	{
		glMatrix = new GLMatrix();

		bufferSize = BufferNumProperties * BufferNumVertices * capacity;

		buffer = new float[ bufferSize ];

		alpha = 1f;

		renderStrategy = GlRenderStrategy.Default.get();

		System.out.println( "glBuffer: " + ( ( bufferSize * BufferSizeFloat ) >> 10 ) + "kb" );
	}

	public void setRenderStrategy( @Nonnull final GlRenderStrategy strategy )
	{
		if( strategy != renderStrategy )
		{
			if( 0 < bufferPointer )
				call();

			renderStrategy = strategy;
		}
	}

	/**
	 * Starts the draw phase
	 *
	 * @param stage           The current stage
	 * @param gl              The current GL
	 * @param width           The width of the view-port
	 * @param height          The height of the view-port
	 * @param backgroundColor The color of the background
	 */
	public void begin( @Nonnull final Stage stage, @Nonnull final GL gl, final float width, final float height, final int backgroundColor )
	{
		assert !drawPhase : "draw phase out of order";

		this.stage = stage;
		this.gl = gl;

		drawCalls = 0;
		drawTriangles = 0;

		Color.extract( backgroundColor, background );

		beginGL( width, height );

		drawPhase = true;
	}

	/**
	 * Switches to a new alpha value for upcoming images
	 *
	 * @param value The new alpha value
	 * @return GlRenderer to chain method calls
	 */
	@Nonnull
	public GlRenderer alpha( final float value )
	{
		assert drawPhase : "draw phase out of order";

		if( alpha != value )
		{
			if( 0 < bufferPointer )
				call();

			alpha = value;
		}

		return this;
	}

	/**
	 * Draws a texture
	 *
	 * @param texture The texture to be drawn
	 * @param x       The x-coordinate where to draw
	 * @param y       The y-coordinate where to draw
	 * @param w       The width of the image
	 * @param h       The height of the image
	 */
	public void draw(
			@Nonnull final Texture texture,
			final int x, final int y,
			final int w, final int h )
	{
		draw( texture, x, y, w, h, false, false, false );
	}

	/**
	 * Draws a texture with flipping options
	 * Each image will be created by 6 vertices and two triangles and added as a geometry
	 *
	 * @param texture             The texture to be drawn
	 * @param x                   The x-coordinate where to draw
	 * @param y                   The y-coordinate where to draw
	 * @param w                   The width of the image
	 * @param h                   The height of the image
	 * @param flippedDiagonally   True is the image should be switched diagonally
	 * @param flippedHorizontally True is the image should be switched horizontally
	 * @param flippedVertically   True is the image should be switched vertically
	 */
	public void draw(
			@Nonnull final Texture texture,
			final int x, final int y,
			final int w, final int h,
			final boolean flippedDiagonally,
			final boolean flippedHorizontally,
			final boolean flippedVertically )
	{
		assert drawPhase : "draw phase out of order";

		if( null == textureData )
		{
			assert 0 == bufferPointer;

			textureData = texture.textureData; // begin writing
		}
		else if( texture.textureData != textureData ) // switch texture
		{
			if( 0 < bufferPointer )
				call();

			textureData = texture.textureData;
		}
		else if( bufferPointer + BufferImageSize >= bufferSize ) // avoid overflow
		{
			call();
		}

		float ax = texture.uv00x;
		float ay = texture.uv00y;
		float bx = texture.uv10x;
		float by = texture.uv10y;
		float cx = texture.uv01x;
		float cy = texture.uv01y;
		float dx = texture.uv11x;
		float dy = texture.uv11y;
		float tm;

		/**
		 * This has been found by comparing the results with the Tiled editor.
		 */
		if( flippedDiagonally )
		{
			if( flippedHorizontally )
			{
				// flip vertically
				tm = ay;
				ay = cy;
				cy = tm;
				tm = by;
				by = dy;
				dy = tm;
			}

			if( flippedVertically )
			{
				// flip horizontally
				tm = ax;
				ax = bx;
				bx = tm;
				tm = cx;
				cx = dx;
				dx = tm;
			}

			// flip diagonally
			tm = bx;
			bx = cx;
			cx = tm;
			tm = by;
			by = cy;
			cy = tm;
		}
		else
		{
			if( flippedHorizontally )
			{
				tm = ax;
				ax = bx;
				bx = tm;
				tm = cx;
				cx = dx;
				dx = tm;
			}

			if( flippedVertically )
			{
				tm = ay;
				ay = cy;
				cy = tm;
				tm = by;
				by = dy;
				dy = tm;
			}
		}

		final int x1 = x + w;
		final int y1 = y + h;

		// 0, 0, ax, ay
		buffer[ bufferPointer++ ] = x;
		buffer[ bufferPointer++ ] = y;
		buffer[ bufferPointer++ ] = ax;
		buffer[ bufferPointer++ ] = ay;

		// size, 0, bx, by
		buffer[ bufferPointer++ ] = x1;
		buffer[ bufferPointer++ ] = y;
		buffer[ bufferPointer++ ] = bx;
		buffer[ bufferPointer++ ] = by;

		// 0, size, cx, cy
		buffer[ bufferPointer++ ] = x;
		buffer[ bufferPointer++ ] = y1;
		buffer[ bufferPointer++ ] = cx;
		buffer[ bufferPointer++ ] = cy;

		// size, size, dx, dy
		buffer[ bufferPointer++ ] = x1;
		buffer[ bufferPointer++ ] = y1;
		buffer[ bufferPointer++ ] = dx;
		buffer[ bufferPointer++ ] = dy;

		// 0, size, cx, cy
		buffer[ bufferPointer++ ] = x;
		buffer[ bufferPointer++ ] = y1;
		buffer[ bufferPointer++ ] = cx;
		buffer[ bufferPointer++ ] = cy;

		// size, 0, bx, by
		buffer[ bufferPointer++ ] = x1;
		buffer[ bufferPointer++ ] = y;
		buffer[ bufferPointer++ ] = bx;
		buffer[ bufferPointer++ ] = by;

		drawTriangles += 2;
	}

	/**
	 * Completes a drawing phase
	 */
	public void complete()
	{
		assert drawPhase : "draw phase out of order";

		if( 0 < bufferPointer )
			call();

		gl = null;
		stage = null;
		textureData = null;
		bufferPointer = 0;
		drawPhase = false;
	}

	private void beginGL( final float width, final float height )
	{
		if( initGL )
		{
			initGL = false;
			drawable = false;

			gl.disable( GL.DITHER );
			gl.disable( GL.STENCIL_TEST );
			gl.disable( GL.CULL_FACE );
			gl.disable( GL.DEPTH_TEST );
			gl.depthFunc( GL.ALWAYS );
			gl.enable( GL.BLEND );
			gl.blendEquationSeparate( GL.FUNC_ADD, GL.FUNC_ADD );
			gl.blendFuncSeparate( GL.ONE, GL.ONE_MINUS_SRC_ALPHA, GL.ONE, GL.ONE );

			GlRenderStrategy.Default.get().initProgram( gl );
			GlRenderStrategy.Orifice.get().initProgram( gl );
		}

		if( null == glBuffer )
		{
			glBuffer = gl.createBuffer();
			gl.bindBuffer( GL.ARRAY_BUFFER, glBuffer );
			gl.bufferData( GL.ARRAY_BUFFER, buffer, 0, bufferSize, GL.STATIC_DRAW );
		}
		else
		{
			gl.bindBuffer( GL.ARRAY_BUFFER, glBuffer );
		}

		drawable = true;

		glMatrix.identity();
		glMatrix.ortho( 0f, width, height, 0f, 0f, 1f );

		gl.viewport( 0, 0, ( int ) width, ( int ) height );
		gl.clearColor( background[ 0 ], background[ 1 ], background[ 2 ], background[ 3 ] );
		gl.clear( GL.COLOR_BUFFER_BIT );

		gl.enable( GL.BLEND );
		gl.blendFunc( GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA );

		gl.enableVertexAttribArray( 0 );
		gl.vertexAttribPointer( 0, 2, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 0 );
		gl.enableVertexAttribArray( 1 );
		gl.vertexAttribPointer( 1, 2, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 2 * BufferSizeFloat );

		textureData = null;
		bufferPointer = 0;
	}

	private void call()
	{
		assert drawPhase : "draw phase out of order";

		if( null == stage || null == gl || !drawable )
		{
			bufferPointer = 0;
			return;
		}

		if( 0 == bufferPointer )
			return;

		renderStrategy.initDraw( gl, glMatrix, alpha );

		gl.activeTexture( GL.TEXTURE0 );
		gl.bindTexture( GL.TEXTURE_2D, stage.getOrCreateTexture( textureData ) );

		gl.bindBuffer( GL.ARRAY_BUFFER, glBuffer );
		gl.bufferSubData( GL.ARRAY_BUFFER, 0, buffer, 0, bufferPointer );
		gl.drawArrays( GL.TRIANGLES, 0, bufferPointer / BufferNumProperties );

		bufferPointer = 0;
		drawCalls++;
	}
}