package platformer.gl;

import defrac.display.TextureData;
import defrac.gl.GL;
import defrac.gl.GLSubstrate;
import defrac.gl.GLTexture;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Andre Michelle
 */
public final class GLTextureCache
{
	private final HashMap<TextureData, GLTexture> cache = new HashMap<>();

	@Nonnull
	public GLTexture getOrCreateTexture( @Nonnull final GLSubstrate glSubstrate, @Nonnull final TextureData textureData )
	{
		GLTexture glTexture = cache.get( textureData );

		if( null == glTexture )
		{
			final int width = textureData.width();
			final int height = textureData.height();

			final byte[] bytes = new byte[ width * height * 4 ];

			boolean valid = true;

			try
			{
				// Does not work inside a defrac.ui.GLSurface context
				// throws java.lang.ArrayIndexOutOfBoundsException: 0
				textureData.loadPixels(  ).getPixels( 0, 0, width, height, bytes, 0 );
			}
			catch( Throwable t )
			{
				t.printStackTrace();
				valid = false;
			}

			glTexture = glSubstrate.createTexture();
			glSubstrate.bindTexture( GL.TEXTURE_2D, glTexture);
			glSubstrate.texImage2D( GL.TEXTURE_2D, 0, GL.RGBA, width, height, 0, GL.RGBA, GL.UNSIGNED_BYTE, bytes );
			glSubstrate.texParameteri( GL.TEXTURE_2D, GL.TEXTURE_MAG_FILTER, GL.LINEAR );
			glSubstrate.texParameteri( GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.LINEAR_MIPMAP_NEAREST);
			glSubstrate.generateMipmap(GL.TEXTURE_2D);
			glSubstrate.bindTexture(GL.TEXTURE_2D, null);

			if( valid )
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