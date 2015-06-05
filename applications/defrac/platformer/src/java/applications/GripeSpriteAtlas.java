package applications;

import defrac.display.Texture;
import defrac.display.TextureData;

import javax.annotation.Nonnull;

public final class GripeSpriteAtlas
{
	public final Texture jump;
	public final Texture[] idle;
	public final Texture[] fall;
	public final Texture[] run;

	public GripeSpriteAtlas( @Nonnull final TextureData textureData )
	{
		jump = new Texture( textureData, 94, 1, 32, 32, 0, 3, 2, 28, 30 );
		idle = new Texture[] {
			new Texture( textureData, 20, 61, 32, 32, 0, 6, 4, 22, 28 ),
			new Texture( textureData, 87, 90, 32, 32, 0, 6, 4, 20, 28 ),
			new Texture( textureData, 87, 61, 32, 32, 0, 6, 4, 20, 28 ),
			new Texture( textureData, 66, 84, 32, 32, 0, 6, 4, 20, 28 ),
			new Texture( textureData, 45, 84, 32, 32, 0, 6, 4, 20, 28 ),
			new Texture( textureData, 66, 84, 32, 32, 0, 6, 4, 20, 28 ),
			new Texture( textureData, 87, 61, 32, 32, 0, 6, 4, 20, 28 ),
			new Texture( textureData, 87, 90, 32, 32, 0, 6, 4, 20, 28 )
		};
		fall = new Texture[] {
			new Texture( textureData, 63, 1, 32, 32, 0, 1, 3, 30, 26 ),
			new Texture( textureData, 63, 28, 32, 32, 0, 2, 3, 28, 26 ),
			new Texture( textureData, 32, 1, 32, 32, 0, 0, 2, 30, 28 ),
			new Texture( textureData, 1, 1, 32, 32, 0, 1, 2, 30, 28 )
		};
		run = new Texture[] {
			new Texture( textureData, 1, 92, 32, 32, 0, 6, 4, 22, 28 ),
			new Texture( textureData, 24, 90, 32, 32, 0, 7, 4, 20, 28 ),
			new Texture( textureData, 1, 30, 32, 32, 0, 9, 2, 18, 30 ),
			new Texture( textureData, 62, 55, 32, 32, 0, 8, 4, 20, 28 ),
			new Texture( textureData, 92, 32, 32, 32, 0, 6, 4, 22, 28 ),
			new Texture( textureData, 41, 30, 32, 32, 0, 7, 4, 20, 28 ),
			new Texture( textureData, 1, 61, 32, 32, 0, 9, 2, 18, 30 ),
			new Texture( textureData, 20, 30, 32, 32, 0, 8, 4, 20, 28 )
		};
	}
}