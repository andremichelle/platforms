package platformer.gl;

import defrac.display.Texture;
import defrac.display.TextureData;
import defrac.gl.GL;
import defrac.gl.GLBuffer;
import defrac.gl.GLMatrix;
import defrac.gl.GLSubstrate;
import defrac.util.Color;

import javax.annotation.Nonnull;

/**
 * A Renderer that draws images to the current GLSurface.
 * All draws will be written as geometry and only send to the GPU when the alpha value or texture changes.
 * TODO ReInit when GL context is broken (never happened while testing, but is assumed on certain platforms)
 *
 * @author Andre Michelle
 */
public final class GLRenderer
{
	private static final int BufferNumProperties = 4;
	private static final int BufferNumVertices = 6;
	private static final int BufferImageSize = BufferNumProperties * BufferNumVertices;
	private static final int BufferSizeFloat = 4;

	private final GLMatrix glMatrix;
	private final float[] buffer;

	private final GLTextureCache textureCache;

	// GLContext
	private GLBuffer glBuffer;
	private GLSubstrate glSubstrate = null;

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
	private GLRenderStrategy renderStrategy;

	/**
	 * @param capacity The number of textures that can be stored as geometry
	 */
	public GLRenderer( final int capacity )
	{
		glMatrix = new GLMatrix();

		textureCache = new GLTextureCache();

		bufferSize = BufferNumProperties * BufferNumVertices * capacity;

		buffer = new float[ bufferSize ];

		alpha = 1f;

		renderStrategy = GLRenderStrategy.Default.get();

		System.out.println( "glBuffer: " + ( ( bufferSize * BufferSizeFloat ) >> 10 ) + "kb" );
	}

	public void setRenderStrategy( @Nonnull final GLRenderStrategy strategy )
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
	 * @param glSubstrate     The current GL
	 * @param width           The width of the view-port
	 * @param height          The height of the view-port
	 * @param backgroundColor The color of the background
	 */
	public void begin( @Nonnull final GLSubstrate glSubstrate, final float width, final float height, final int backgroundColor )
	{
		assert !drawPhase : "draw phase out of order";

		this.glSubstrate = glSubstrate;

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
	public GLRenderer alpha( final float value )
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
	 * @param flippedDiagonally   True will switch the image diagonally
	 * @param flippedHorizontally True will switch the image horizontally
	 * @param flippedVertically   True will switch the image vertically
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

		glSubstrate = null;
		textureData = null;
		bufferPointer = 0;
		drawPhase = false;
	}

	private void beginGL( final float width, final float height )
	{
		if( initGL )
		{
			System.out.println( "init gl" );

			initGL = false;
			drawable = false;

			glSubstrate.disable( GL.DITHER );
			glSubstrate.disable( GL.STENCIL_TEST );
			glSubstrate.disable( GL.CULL_FACE );
			glSubstrate.disable( GL.DEPTH_TEST );
			glSubstrate.depthFunc( GL.ALWAYS );
			glSubstrate.enable( GL.BLEND );
			glSubstrate.blendEquationSeparate( GL.FUNC_ADD, GL.FUNC_ADD );
			glSubstrate.blendFuncSeparate( GL.ONE, GL.ONE_MINUS_SRC_ALPHA, GL.ONE, GL.ONE );

			GLRenderStrategy.Default.get().initProgram( glSubstrate );
			GLRenderStrategy.Orifice.get().initProgram( glSubstrate );
		}

		if( null == glBuffer )
		{
			glBuffer = glSubstrate.createBuffer();
			glSubstrate.bindBuffer( GL.ARRAY_BUFFER, glBuffer );
			glSubstrate.bufferData( GL.ARRAY_BUFFER, buffer, 0, bufferSize, GL.STATIC_DRAW );
		}
		else
		{
			glSubstrate.bindBuffer( GL.ARRAY_BUFFER, glBuffer );
		}

		drawable = true;

		glMatrix.identity();
		glMatrix.ortho( 0f, width, height, 0f, 0f, 1f );

		glSubstrate.viewport( 0, 0, ( int ) width, ( int ) height );
		glSubstrate.clearColor( background[ 0 ], background[ 1 ], background[ 2 ], background[ 3 ] );
		glSubstrate.clear( GL.COLOR_BUFFER_BIT );

		glSubstrate.enable( GL.BLEND );
		glSubstrate.blendFunc( GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA );

		glSubstrate.enableVertexAttribArray( 0 );
		glSubstrate.vertexAttribPointer( 0, 2, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 0 );
		glSubstrate.enableVertexAttribArray( 1 );
		glSubstrate.vertexAttribPointer( 1, 2, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 2 * BufferSizeFloat );

		textureData = null;
		bufferPointer = 0;
	}

	private void call()
	{
		assert drawPhase : "draw phase out of order";

		if( null == glSubstrate || !drawable )
		{
			bufferPointer = 0;
			return;
		}

		if( 0 == bufferPointer )
			return;

		renderStrategy.initDraw( glSubstrate, glMatrix, alpha );

		glSubstrate.activeTexture( GL.TEXTURE0 );
		glSubstrate.bindTexture( GL.TEXTURE_2D, textureCache.getOrCreateTexture( glSubstrate, textureData ) );

		glSubstrate.bindBuffer( GL.ARRAY_BUFFER, glBuffer );
		glSubstrate.bufferSubData( GL.ARRAY_BUFFER, 0, buffer, 0, bufferPointer );
		glSubstrate.drawArrays( GL.TRIANGLES, 0, bufferPointer / BufferNumProperties );

		bufferPointer = 0;
		drawCalls++;
	}
}