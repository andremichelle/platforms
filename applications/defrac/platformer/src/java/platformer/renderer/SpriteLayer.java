package platformer.renderer;

import defrac.lang.Lists;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Andre Michelle
 */
public final class SpriteLayer implements Renderer
{
	private final RendererContext context;

	private final List<Sprite> sprites;

	public SpriteLayer( @Nonnull final RendererContext context )
	{
		this.context = context;

		sprites = Lists.newLinkedList();
	}

	@Override
	public void renderLayer()
	{
		for( final Sprite sprite : sprites )
			sprite.requestRender( context );
	}

	public void addSprite( @Nonnull final Sprite sprite )
	{
		sprites.add( sprite );
	}

	public void removeSprite( @Nonnull final Sprite sprite )
	{
		sprites.remove( sprite );
	}
}