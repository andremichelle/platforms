package platformer.renderer;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public interface Sprite
{
	int x();

	int y();

	int width();

	int height();

	void requestRender( @Nonnull final RendererContext context );
}