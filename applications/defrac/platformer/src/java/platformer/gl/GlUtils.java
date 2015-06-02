package platformer.gl;

import defrac.gl.GL;
import defrac.gl.GLProgram;
import defrac.gl.GLShader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Joa Ebert
 */

final class GlUtils
{
	@Nullable
	static GLShader createShader(
			@Nonnull final GL gl,
			@Nonnull final String source,
			final int type )
	{
		final GLShader shader = gl.createShader( type );

		gl.shaderSource( shader, source );
		gl.compileShader( shader );

		if( GL.FALSE == gl.getShaderParameter( shader, GL.COMPILE_STATUS ) )
		{
			System.out.println( "========================" );
			System.out.println( "Could not compile shader" );
			System.out.println( "========================" );
			System.out.println( gl.getShaderInfoLog( shader ) );
			gl.deleteShader( shader );
			return null;
		}

		return shader;
	}

	static boolean linkProgram(
			@Nonnull final GL gl,
			@Nonnull final GLProgram program,
			@Nonnull final GLShader vertexShader,
			@Nonnull final GLShader fragmentShader,
			@Nonnull final String... attributes )
	{
		gl.attachShader( program, vertexShader );
		gl.attachShader( program, fragmentShader );

		int n = attributes.length;

		for( int i = 0 ; i < n ; ++i )
		{
			gl.bindAttribLocation( program, i, attributes[ i ] );
		}

		gl.linkProgram( program );

		gl.detachShader( program, vertexShader );
		gl.detachShader( program, fragmentShader );
		gl.deleteShader( vertexShader );
		gl.deleteShader( fragmentShader );

		if( GL.FALSE == gl.getProgramParameter( program, GL.LINK_STATUS ) )
		{
			System.out.println( "======================" );
			System.out.println( "Could not link program" );
			System.out.println( "======================" );
			System.out.println( gl.getProgramInfoLog( program ) );
			return false;
		}

		gl.validateProgram( program );

		if( GL.FALSE == gl.getProgramParameter( program, GL.VALIDATE_STATUS ) )
		{
			System.out.println( "==========================" );
			System.out.println( "Could not validate program" );
			System.out.println( "==========================" );
			System.out.println( gl.getProgramInfoLog( program ) );
			return false;
		}

		return true;
	}
}