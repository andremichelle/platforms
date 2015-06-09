package platformer.glare;

import defrac.display.Texture;
import defrac.display.TextureData;
import defrac.gl.GL;
import defrac.gl.GLProgram;
import defrac.gl.GLShader;
import defrac.gl.GLSubstrate;
import defrac.gl.GLUniformLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static platformer.glare.GlareProgram.createShader;
import static platformer.glare.GlareProgram.linkProgram;

/**
 * @author Andre Michelle
 */
public class GlareTextureProgram implements GlareProgram
{
	protected static final int BufferSizeFloat = 4;
	protected static final int BufferNumProperties = 4;
	protected static final int BufferNumVertices = 6;
	protected static final int BufferTextureSize = BufferNumProperties * BufferNumVertices;

	private final float[] color = new float[]{ 1f, 1f, 1f, 1f };

	private final Glare glare;
	private final String fCode;
	private final String vCode;

	@Nullable
	protected GLProgram program;
	@Nullable
	private GLUniformLocation matrixLocation;
	@Nullable
	private GLUniformLocation textureLocation;
	@Nullable
	private GLUniformLocation colorLocation;
	@Nullable
	private TextureData textureData;

	public GlareTextureProgram( @Nonnull final Glare glare )
	{
		this(
				glare,
				"" +
						"attribute vec2 position;" +
						"attribute vec2 uv;" +
						"uniform mat4 pMatrix;" +
						"varying vec2 vUv;" +
						"void main()" +
						"{" +
						"	vUv = uv;" +
						"	gl_Position = pMatrix * vec4(position, 0, 1);" +
						"}", "" +
						"uniform sampler2D texture;" +
						"uniform vec4 color;" +
						"varying vec2 vUv;" +
						"void main()" +
						"{" +
						"	gl_FragColor = texture2D(texture, vUv) * color;" +
						"}"
		);
	}

	protected GlareTextureProgram( @Nonnull final Glare glare, @Nonnull final String vCode, @Nonnull final String fCode )
	{
		this.glare = glare;
		this.fCode = fCode;
		this.vCode = vCode;
	}

	@Override
	public int bufferCapacity()
	{
		return 512 * BufferTextureSize * BufferSizeFloat;
	}

	@Override
	public final void compile( @Nonnull final GLSubstrate glSubstrate )
	{
		final GLShader vertexShader = createShader( glSubstrate, vCode, GL.VERTEX_SHADER );
		if( null == vertexShader )
			throw new RuntimeException();

		final GLShader fragmentShader = createShader( glSubstrate, fCode, GL.FRAGMENT_SHADER );
		if( null == fragmentShader )
			throw new RuntimeException();

		program = glSubstrate.createProgram();

		if( !linkProgram( glSubstrate, program, vertexShader, fragmentShader, "position", "uv" ) )
			throw new RuntimeException();

		matrixLocation = glSubstrate.getUniformLocation( program, "pMatrix" );
		textureLocation = glSubstrate.getUniformLocation( program, "texture" );
		colorLocation = glSubstrate.getUniformLocation( program, "color" );

		initUniforms( glSubstrate, program );
	}

	@Override
	public final boolean compiled()
	{
		return null != program;
	}

	@Override
	public final void finalizeDraw( @Nonnull final GLSubstrate glSubstrate )
	{
		assert null != matrixLocation && null != textureLocation && null != colorLocation;

		assert null != textureData : "No texture data but a flush requirement?";

		glSubstrate.enable( GL.BLEND );
		glSubstrate.blendFunc( GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA );

		glSubstrate.useProgram( program );
		glSubstrate.uniformMatrix4fv( matrixLocation, false, glare.glMatrix.values );
		glSubstrate.uniform1i( textureLocation, 0 );
		glSubstrate.uniform4fv( colorLocation, color );

		applyUniforms( glSubstrate );

		glSubstrate.activeTexture( GL.TEXTURE0 );
		glSubstrate.bindTexture( GL.TEXTURE_2D, glare.textureCache.getOrCreateTexture( glSubstrate, textureData ) );

		glSubstrate.enableVertexAttribArray( 0 );
		glSubstrate.vertexAttribPointer( 0, 2, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 0 );
		glSubstrate.enableVertexAttribArray( 1 );
		glSubstrate.vertexAttribPointer( 1, 2, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 2 * BufferSizeFloat );

		glSubstrate.bindBuffer( GL.ARRAY_BUFFER, glare.glBuffer );
		glSubstrate.bufferSubData( GL.ARRAY_BUFFER, 0, glare.buffer, 0, glare.bufferPointer );
		glSubstrate.drawArrays( GL.TRIANGLES, 0, glare.bufferPointer / BufferNumProperties );
	}

	protected void initUniforms( @Nonnull final GLSubstrate glSubstrate, @Nonnull final GLProgram program ) {}

	protected void applyUniforms( @Nonnull final GLSubstrate glSubstrate ) {}

	@Override
	public final void destroy( @Nonnull final GLSubstrate glSubstrate )
	{
		if( null != program )
		{
			glSubstrate.deleteProgram( program );
			program = null;
		}
	}

	@Nonnull
	public GlareTextureProgram alpha( final float value )
	{
		if( color[3] != value )
		{
			glare.activeProgram( this );
			glare.flush();

			color[3] = value;
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
	public final void draw(
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
	public final void draw(
			@Nonnull final Texture texture,
			final int x, final int y,
			final int w, final int h,
			final boolean flippedDiagonally,
			final boolean flippedHorizontally,
			final boolean flippedVertically )
	{
		glare.activeProgram( this );

		if( null == textureData )
		{
			assert 0 == glare.bufferPointer;

			textureData = texture.textureData; // begin writing
		}
		else if( texture.textureData != textureData ) // switch texture
		{
			if( 0 < glare.bufferPointer )
				glare.flush();

			textureData = texture.textureData;
		}
		else if( glare.bufferPointer + BufferTextureSize >= glare.bufferSize ) // avoid overflow
		{
			glare.flush();
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

		final float[] buffer = glare.buffer;

		int bufferPointer = glare.bufferPointer;

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

		glare.bufferPointer = bufferPointer;
	}
}