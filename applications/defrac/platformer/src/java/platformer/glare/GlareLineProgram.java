package platformer.glare;

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
public final class GlareLineProgram implements GlareProgram
{
	private static final int BufferSizeFloat = 4;
	private static final int BufferNumProperties = 6;
	private static final int BufferNumVertices = 2;
	private static final int BufferLineSize = BufferNumProperties * BufferNumVertices;

	private final Glare glare;
	private final String vCode;
	private final String fCode;
	private final float[] color;

	@Nullable
	private GLProgram program;

	@Nullable
	private GLUniformLocation matrixLocation;

	public GlareLineProgram( @Nonnull final Glare glare )
	{
		this.glare = glare;

		vCode = "" +
				"attribute vec2 position;" +
				"attribute vec4 color;" +
				"uniform mat4 pMatrix;" +
				"" +
				"varying vec4 vColor;" +
				"" +
				"void main()" +
				"{" +
				"	vColor = color;" +
				"	gl_Position = pMatrix * vec4(position, 0, 1);" +
				"}";
		fCode = "" +
				"varying vec4 vColor;" +
				"" +
				"void main()" +
				"{" +
				"	gl_FragColor = vColor;" +
				"}";

		color = new float[]{ 1f, 1f, 1f, 1f };
	}

	@Override
	public int bufferCapacity()
	{
		return 256 * BufferLineSize * BufferSizeFloat;
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
	}

	@Override
	public final boolean compiled()
	{
		return null != program;
	}

	@Override
	public final void finalizeDraw( @Nonnull final GLSubstrate glSubstrate )
	{
		assert null != matrixLocation;

		glSubstrate.enable( GL.BLEND );
		glSubstrate.blendFunc( GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA );

		glSubstrate.useProgram( program );
		glSubstrate.uniformMatrix4fv( matrixLocation, false, glare.glMatrix.values );

		glSubstrate.enableVertexAttribArray( 0 );
		glSubstrate.vertexAttribPointer( 0, 2, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 0 );
		glSubstrate.enableVertexAttribArray( 1 );
		glSubstrate.vertexAttribPointer( 1, 4, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 2 * BufferSizeFloat );

		glSubstrate.bindBuffer( GL.ARRAY_BUFFER, glare.glBuffer );
		glSubstrate.bufferSubData( GL.ARRAY_BUFFER, 0, glare.buffer, 0, glare.bufferPointer );
		glSubstrate.drawArrays( GL.LINES, 0, glare.bufferPointer / BufferNumProperties );
	}

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
	public GlareLineProgram color( @Nonnull final float[] rgba )
	{
		System.arraycopy( rgba, 0, color, 0, 4 );

		return this;
	}

	@Nonnull
	public GlareLineProgram color( final float r, final float g, final float b, final float a )
	{
		color[ 0 ] = r;
		color[ 1 ] = g;
		color[ 2 ] = b;
		color[ 3 ] = a;

		return this;
	}

	@Nonnull
	public GlareLineProgram line( final float x0, final float y0, final float x1, final float y1 )
	{
		glare.activeProgram( this );

		if( glare.bufferPointer + BufferLineSize >= glare.bufferSize ) // avoid overflow
			glare.flush();

		final float[] buffer = glare.buffer;

		final float r = color[ 0 ];
		final float g = color[ 1 ];
		final float b = color[ 2 ];
		final float a = color[ 3 ];

		int bufferPointer = glare.bufferPointer;
		buffer[ bufferPointer++ ] = x0;
		buffer[ bufferPointer++ ] = y0;
		buffer[ bufferPointer++ ] = r;
		buffer[ bufferPointer++ ] = g;
		buffer[ bufferPointer++ ] = b;
		buffer[ bufferPointer++ ] = a;
		buffer[ bufferPointer++ ] = x1;
		buffer[ bufferPointer++ ] = y1;
		buffer[ bufferPointer++ ] = r;
		buffer[ bufferPointer++ ] = g;
		buffer[ bufferPointer++ ] = b;
		buffer[ bufferPointer++ ] = a;
		glare.bufferPointer = bufferPointer;
		return this;
	}

	@Nonnull
	public GlareLineProgram outlineRect( final float x, final float y, final float w, final float h )
	{
		line( x, y, x + w, y );
		line( x + w, y, x + w, y + h );
		line( x + w, y + h, x, y + h );
		line( x, y + h, x, y );

		return this;
	}
}