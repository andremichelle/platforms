package platformer.glare;

import defrac.gl.GLProgram;
import defrac.gl.GLSubstrate;
import defrac.gl.GLUniformLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Same as GlareTextureProgram but takes circle parameters (x,y,radius) working as an aperture into account.
 *
 * @author Andre Michelle
 */
public final class GlareTextureProgramAperture extends GlareTextureProgram implements GlareProgram
{
	@Nullable
	private GLUniformLocation circleLocation;

	private final float[] circle;

	public GlareTextureProgramAperture( @Nonnull final Glare machine )
	{
		super(
				machine,
				"" +
						"attribute vec2 position;" +
						"attribute vec2 uv;" +
						"uniform mat4 pMatrix;" +
						"varying vec2 vUv;" +
						"varying vec2 vXy;" +
						"void main()" +
						"{" +
						"	vUv = uv;" +
						"	vXy = position.xy;" +
						"	gl_Position = pMatrix * vec4( position, 0, 1 );" +
						"}",
				"" +
						"uniform sampler2D texture;" +
						"uniform vec3 circle;" +
						"uniform vec4 color;" +
						"varying vec2 vUv;" +
						"varying vec2 vXy;" +
						"void main()" +
						"{" +
						"	vec4 pixel = texture2D( texture, vUv ) * color;" +
						"	float distanceAlpha = max(0.0,min(1.0,0.5*(distance(circle.xy, vXy) - circle.z)));" +
						"	gl_FragColor = vec4( pixel.rgb, pixel.a * distanceAlpha );" +
						"}"
		);

		circle = new float[ 3 ];
	}

	@Override
	protected void initUniforms( @Nonnull final GLSubstrate glSubstrate, @Nonnull final GLProgram program )
	{
		circleLocation = glSubstrate.getUniformLocation( program, "circle" );
	}

	@Override
	protected void applyUniforms( @Nonnull final GLSubstrate glSubstrate )
	{
		assert circleLocation != null;

		glSubstrate.uniform3fv( circleLocation, circle );
	}

	public void setCircle( final float x, final float y, final float radius )
	{
		circle[ 0 ] = x;
		circle[ 1 ] = y;
		circle[ 2 ] = radius;
	}
}