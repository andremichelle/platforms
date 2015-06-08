package platformer.glare;

import defrac.display.TextureData;
import defrac.gl.GL;
import defrac.gl.GLSubstrate;
import defrac.gl.GLTexture;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Andre Michelle
 */
public final class GlareTextureCache
{
	private final HashMap<TextureData, GLTexture> cache = new HashMap<>();

	@Nonnull
	public GLTexture getOrCreateTexture( @Nonnull final GLSubstrate glSubstrate, @Nonnull final TextureData textureData )
	{
		GLTexture glTexture = cache.get( textureData );

		if( null == glTexture )
		{
			glTexture = glSubstrate.createTexture();
			glSubstrate.bindTexture( GL.TEXTURE_2D, glTexture );
			glSubstrate.texParameteri( GL.TEXTURE_2D, GL.TEXTURE_MAG_FILTER, GL.LINEAR );
			glSubstrate.texParameteri( GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.LINEAR );
			textureData.texImage2D( glSubstrate, GL.TEXTURE_2D, 0, textureData.format().hasAlpha() ? GL.RGBA : GL.RGB, 0 );
			glSubstrate.bindTexture( GL.TEXTURE_2D, null );

			cache.put( textureData, glTexture );
		}

		return glTexture;
	}

	public void clear()
	{
		// TODO Check if we need to dispose the textures
		cache.clear();
	}
}