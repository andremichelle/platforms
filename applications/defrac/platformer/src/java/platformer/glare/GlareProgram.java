package platformer.glare;

import defrac.gl.GL;
import defrac.gl.GLProgram;
import defrac.gl.GLShader;
import defrac.gl.GLSubstrate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Andre Michelle
 */
public interface GlareProgram
{
	interface Factory
	{
		@Nonnull
		GlareProgram create( @Nonnull final Glare machine );
	}

	int bufferCapacity();

	void compile( @Nonnull final GLSubstrate glSubstrate );

	boolean compiled();

	void finalizeDraw( @Nonnull final GLSubstrate glSubstrate );

	void destroy( @Nonnull final GLSubstrate glSubstrate );

	@Nullable
	static GLShader createShader(
			@Nonnull final GLSubstrate glSubstrate,
			@Nonnull final String source,
			final int type )
	{
		final GLShader shader = glSubstrate.createShader( type );

		glSubstrate.shaderSource( shader, source );
		glSubstrate.compileShader( shader );

		if( GL.FALSE == glSubstrate.getShaderParameter( shader, GL.COMPILE_STATUS ) )
		{
			System.out.println( "========================" );
			System.out.println( "Could not compile shader" );
			System.out.println( "========================" );
			System.out.println( glSubstrate.getShaderInfoLog( shader ) );
			glSubstrate.deleteShader( shader );
			return null;
		}

		return shader;
	}

	static boolean linkProgram(
			@Nonnull final GLSubstrate glSubstrate,
			@Nonnull final GLProgram program,
			@Nonnull final GLShader vertexShader,
			@Nonnull final GLShader fragmentShader,
			@Nonnull final String... attributes )
	{
		glSubstrate.attachShader( program, vertexShader );
		glSubstrate.attachShader( program, fragmentShader );

		int n = attributes.length;

		for( int i = 0 ; i < n ; ++i )
		{
			glSubstrate.bindAttribLocation( program, i, attributes[ i ] );
		}

		glSubstrate.linkProgram( program );

		glSubstrate.detachShader( program, vertexShader );
		glSubstrate.detachShader( program, fragmentShader );
		glSubstrate.deleteShader( vertexShader );
		glSubstrate.deleteShader( fragmentShader );

		if( GL.FALSE == glSubstrate.getProgramParameter( program, GL.LINK_STATUS ) )
		{
			System.out.println( "======================" );
			System.out.println( "Could not link program" );
			System.out.println( "======================" );
			System.out.println( glSubstrate.getProgramInfoLog( program ) );
			return false;
		}

		glSubstrate.validateProgram( program );

		if( GL.FALSE == glSubstrate.getProgramParameter( program, GL.VALIDATE_STATUS ) )
		{
			System.out.println( "==========================" );
			System.out.println( "Could not validate program" );
			System.out.println( "==========================" );
			System.out.println( glSubstrate.getProgramInfoLog( program ) );
			return false;
		}

		return true;
	}
}