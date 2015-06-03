package platformer.gl;

import defrac.display.TextureData;
import defrac.display.render.GLUniformLocationCache;
import defrac.filter.Filter;
import defrac.geom.Point;
import defrac.gl.GL;
import defrac.gl.GLProgram;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Andre Michelle
 */
public final class MonitorFilter implements Filter
{
	@Nonnull
	@Override
	public Point computeExtent( final float width, final float height, @Nonnull final Point point )
	{
		point.x = 0.0f;
		point.y = 0.0f;

		return point;
	}

	@Override
	public void appendCode(
			final int pass,
			final float width,
			final float height,
			final float viewportWidth,
			final float viewportHeight,
			@Nonnull final StringBuilder builder )
	{
		final float ratioX = viewportWidth / width;
		final float ratioY = viewportHeight / height;

		//noinspection StringConcatenationInsideStringBufferAppend
		builder.append( "" +
						"float pi = 3.141592653589793;" +
						"float bend = 0.15;" +
						"float alphaDecay = 0.3;" +
						"float alphaOver = 1.25;" +
						"vec2 ratio = vec2(" + ratioX + ", " + ratioY + ");" +
						"vec2 uv = v_uv.xy * ratio;" +
						// Screen displacement
						"vec2 center = uv-0.5;" +
						"float distance = dot(center,center)*bend;" +
						"uv += center*(1.0+distance)*distance;" +
						// Scanlines
						"vec2 xy = floor(uv/ratio * u_resolution.zw);" +
						"float scanX = floor(mod(xy.x,3.0)*0.5);" +
						"float scanY = mod(xy.y,2.0);" +
						"float alpha =" +
						"mix(1.0, 0.95, scanX) * mix(0.99, 1.01, scanY) *" +
						"pow(max(0.0,alphaOver*sin(uv.x*pi)),alphaDecay) *" +
						"pow(max(0.0,alphaOver*sin(uv.y*pi)),alphaDecay);" +
						"color.rgb = getPixel(uv/ratio).rgb * alpha;"
		);
	}

	@Override
	public void appendUniforms(
			final int pass,
			final float width,
			final float height,
			final float viewportWidth,
			final float viewportHeight,
			@Nonnull final StringBuilder builder )
	{
	}

	@Override
	public void applyUniforms(
			final int pass,
			final float width,
			final float height,
			final float viewportWidth,
			final float viewportHeight,
			@Nonnull final GL gl,
			@Nonnull final GLProgram program,
			@Nonnull final GLUniformLocationCache uniforms )
	{
	}

	@Override
	public int numPasses()
	{
		return 1;
	}

	@Nullable
	@Override
	public TextureData[] inputs( final int pass )
	{
		return null;
	}

	@Override
	public boolean isSampling( final int pass )
	{
		return 1 == pass;
	}

	@Override
	public boolean preserveOriginal( final int pass )
	{
		return false;
	}

	@Override
	public boolean mustRunExclusive()
	{
		return false;
	}

	@Override
	public int modStamp()
	{
		return 0;
	}
}