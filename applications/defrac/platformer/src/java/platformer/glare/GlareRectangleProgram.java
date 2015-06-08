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
 * Draft
 *
 * @author Andre Michelle
 */
public final class GlareRectangleProgram implements GlareProgram
{
	private static final int BufferSizeFloat = 4;
	private static final int BufferNumProperties = 6;
	private static final int BufferNumVertices = 6;
	private static final int BufferRectangleSize = BufferNumProperties * BufferNumVertices;

	private final Glare glare;
	private final String vCode;
	private final String fCode;

	@Nullable
	private GLProgram program;

	@Nullable
	private GLUniformLocation matrixLocation;

	private final float[] color;

	public GlareRectangleProgram( @Nonnull final Glare glare )
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

		color = new float[]{ 1f, 0f, 1f, 1f };
	}

	@Override
	public int bufferCapacity()
	{
		return 256 * BufferRectangleSize * BufferSizeFloat;
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
		glSubstrate.vertexAttribPointer( 1, 2, GL.FLOAT, false, BufferNumProperties * BufferSizeFloat, 2 * BufferSizeFloat );

		glSubstrate.bindBuffer( GL.ARRAY_BUFFER, glare.glBuffer );
		glSubstrate.bufferSubData( GL.ARRAY_BUFFER, 0, glare.buffer, 0, glare.bufferPointer );
		glSubstrate.drawArrays( GL.TRIANGLES, 0, glare.bufferPointer / BufferNumProperties );
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
	public GlareRectangleProgram color( @Nonnull final float[] rgba )
	{
		System.arraycopy( rgba, 0, color, 0, 4 );

		return this;
	}

	@Nonnull
	public GlareRectangleProgram rect( final float x, final float y, final float width, final float height )
	{
		glare.activeProgram( this );

		// TODO Move overflow test to Glare
		// TODO Write strategies for distributing colors to each vertices

		if( glare.bufferPointer + BufferRectangleSize >= glare.bufferSize ) // avoid overflow
			glare.flush();

		final float[] buffer = glare.buffer;

		final float right = x + width;
		final float bottom = y + height;
		final float r = color[ 0 ];
		final float g = color[ 1 ];
		final float b = color[ 2 ];
		final float a = color[ 3 ];

		// TODO Test
		final float brightness = 0.5f;

		int bufferPointer = glare.bufferPointer;

		buffer[ bufferPointer++ ] = x;
		buffer[ bufferPointer++ ] = y;
		buffer[ bufferPointer++ ] = r;
		buffer[ bufferPointer++ ] = g;
		buffer[ bufferPointer++ ] = b;
		buffer[ bufferPointer++ ] = a;
		buffer[ bufferPointer++ ] = right;
		buffer[ bufferPointer++ ] = y;
		buffer[ bufferPointer++ ] = r;
		buffer[ bufferPointer++ ] = g;
		buffer[ bufferPointer++ ] = b;
		buffer[ bufferPointer++ ] = a;
		buffer[ bufferPointer++ ] = right;
		buffer[ bufferPointer++ ] = bottom;
		buffer[ bufferPointer++ ] = r * brightness;
		buffer[ bufferPointer++ ] = g * brightness;
		buffer[ bufferPointer++ ] = b * brightness;
		buffer[ bufferPointer++ ] = a;
		buffer[ bufferPointer++ ] = x;
		buffer[ bufferPointer++ ] = y;
		buffer[ bufferPointer++ ] = r;
		buffer[ bufferPointer++ ] = g;
		buffer[ bufferPointer++ ] = b;
		buffer[ bufferPointer++ ] = a;
		buffer[ bufferPointer++ ] = right;
		buffer[ bufferPointer++ ] = bottom;
		buffer[ bufferPointer++ ] = r * brightness;
		buffer[ bufferPointer++ ] = g * brightness;
		buffer[ bufferPointer++ ] = b * brightness;
		buffer[ bufferPointer++ ] = a;
		buffer[ bufferPointer++ ] = x;
		buffer[ bufferPointer++ ] = bottom;
		buffer[ bufferPointer++ ] = r * brightness;
		buffer[ bufferPointer++ ] = g * brightness;
		buffer[ bufferPointer++ ] = b * brightness;
		buffer[ bufferPointer++ ] = a;

		glare.bufferPointer = bufferPointer;
		return this;
	}
}