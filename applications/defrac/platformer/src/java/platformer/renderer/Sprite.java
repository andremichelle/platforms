package platformer.renderer;

import defrac.display.Texture;

import javax.annotation.Nullable;

/**
 * @author Andre Michelle
 */
public interface Sprite
{
	int x();

	int y();

	int width();

	int height();

	@Nullable
	Texture texture();
}