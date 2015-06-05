package platformer.gl;

import defrac.gl.GL;
import defrac.gl.GLMatrix;
import defrac.gl.GLProgram;
import defrac.gl.GLShader;
import defrac.gl.GLSubstrate;
import defrac.gl.GLUniformLocation;

import javax.annotation.Nonnull;

import static platformer.gl.GLUtils.createShader;
import static platformer.gl.GLUtils.linkProgram;

/**
 * @author Andre Michelle
 */
public interface GLRenderStrategy
{
	void initProgram( @Nonnull final GLSubstrate gl );

	void initDraw( @Nonnull final GLSubstrate gl, final GLMatrix glMatrix, final float alpha );

	class Orifice extends Default
	{
		public static Orifice get()
		{
			if( null == instance )
			{
				instance = new Orifice(
						"uniform sampler2D texture;" +
								"uniform vec3 circle;" +
								"uniform float alpha;" +
								"varying vec2 vUv;" +
								"varying vec2 vXy;" +
								"void main()" +
								"{" +
								" vec4 pixel = texture2D( texture, vUv );" +
								" float distanceAlpha = max(0.0,min(1.0,0.5*(distance(circle.xy, vXy) - circle.z)));" +
								" gl_FragColor = vec4( pixel.rgb, pixel.a * alpha * distanceAlpha );" +
								"}",
						"attribute vec3 position;" +
								"attribute vec2 uv;" +
								"uniform mat4 pMatrix;" +
								"varying vec2 vUv;" +
								"varying vec2 vXy;" +
								"void main()" +
								"{" +
								"	vUv = uv;" +
								"	vXy = position.xy;" +
								"	gl_Position = pMatrix * vec4( position.xy, 0, 1 );" +
								"}" );
			}

			return instance;
		}

		private static Orifice instance = null;

		private GLUniformLocation circleLocation;

		private float circleX = 0F;
		private float circleY = 0F;
		private float circleR = 0F;

		public Orifice( @Nonnull final String fCode, @Nonnull final String vCode )
		{
			super( fCode, vCode );
		}

		public void setCircle( final float x, final float y, final float radius )
		{
			circleX = x;
			circleY = y;
			circleR = radius;
		}

		@Override
		protected void loadUniforms( final @Nonnull GLSubstrate gl )
		{
			super.loadUniforms( gl );

			circleLocation = gl.getUniformLocation( program, "circle" );
		}

		@Override
		public void initDraw( @Nonnull final GLSubstrate gl, @Nonnull final GLMatrix glMatrix, final float alpha )
		{
			super.initDraw( gl, glMatrix, alpha );

			gl.uniform3f( circleLocation, circleX, circleY, circleR );
		}
	}

	class Default implements GLRenderStrategy
	{
		public static Default get()
		{
			if( null == instance )
			{
				instance = new Default(
						"uniform sampler2D texture;" +
								"uniform float alpha;" +
								"varying vec2 vUv;" +
								"void main()" +
								"{" +
								" vec4 pixel = texture2D( texture, vUv );" +
								" gl_FragColor = vec4( pixel.rgb, pixel.a * alpha );" +
								"}",
						"attribute vec3 position;" +
								"attribute vec2 uv;" +
								"uniform mat4 pMatrix;" +
								"varying vec2 vUv;" +
								"void main()" +
								"{" +
								"	vUv = uv;" +
								"	gl_Position = pMatrix * vec4( position.xy, 0, 1 );" +
								"}" );
			}

			return instance;
		}

		private static Default instance = null;

		private final String fCode;
		private final String vCode;

		protected GLProgram program;
		private GLUniformLocation matrixLocation;
		private GLUniformLocation textureLocation;
		private GLUniformLocation alphaLocation;

		private boolean ready;

		public Default( @Nonnull final String fCode, @Nonnull final String vCode )
		{
			this.fCode = fCode;
			this.vCode = vCode;
		}

		@Override
		public void initProgram( @Nonnull final GLSubstrate gl )
		{
			if( null != program )
			{
				gl.deleteProgram( program );
				program = null;
			}

			final GLShader vertexShader = createShader( gl, vCode, GL.VERTEX_SHADER );
			if( null == vertexShader )
				return;

			final GLShader fragmentShader = createShader( gl, fCode, GL.FRAGMENT_SHADER );
			if( null == fragmentShader )
				return;

			program = gl.createProgram();

			if( !linkProgram( gl, program, vertexShader, fragmentShader, "position", "uv" ) )
			{
				gl.deleteProgram( program );
				program = null;
				return;
			}

			loadUniforms( gl );

			ready = true;
		}

		protected void loadUniforms( final @Nonnull GLSubstrate gl )
		{
			matrixLocation = gl.getUniformLocation( program, "pMatrix" );
			textureLocation = gl.getUniformLocation( program, "texture" );
			alphaLocation = gl.getUniformLocation( program, "alpha" );
		}

		@Override
		public void initDraw( @Nonnull final GLSubstrate gl, @Nonnull final GLMatrix glMatrix, final float alpha )
		{
			if( !ready )
				return;

			gl.useProgram( program );
			gl.uniformMatrix4fv( matrixLocation, false, glMatrix.values );
			gl.uniform1i( textureLocation, 0 );
			gl.uniform1f( alphaLocation, alpha );
		}
	}
}