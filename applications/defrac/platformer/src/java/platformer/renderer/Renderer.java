package platformer.renderer;

import platformer.tmx.MapLayer;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public interface Renderer
{
	void renderLayer();

	@Nonnull
	MapLayer layer();
}