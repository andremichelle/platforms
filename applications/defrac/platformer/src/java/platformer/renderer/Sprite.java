package platformer.renderer;

import defrac.display.Texture;

import javax.annotation.Nullable;

/**
 * @author Andre Michelle
 */
public interface Sprite
{
	void moveTo( final int x, final int y );

	int x();

	int y();

	int width();

	int height();

	@Nullable
	Texture texture();
}