package platformer.renderer;

import defrac.lang.Lists;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author Andre Michelle
 */
public final class SpriteDepthRenderer implements TileRenderer.DepthRenderer
{
	private final RendererContext context;
	private final List<Sprite> sprites;

	public SpriteDepthRenderer( @Nonnull final RendererContext context )
	{
		this.context = context;

		sprites = Lists.newLinkedList();
	}

	public void addSprite( @Nonnull final Sprite sprite )
	{
		sprites.add( sprite );
	}

	public void removeSprite( @Nonnull final Sprite sprite )
	{
		sprites.remove( sprite );
	}

	@Override
	public void beforeRowRender( final int rowIndex )
	{
		Collections.sort( sprites, ( a, b ) -> {
			if( a.y() > b.y() ) return 1;
			if( a.y() < b.y() ) return -1;
			return 0;
		} );

		for( final Sprite sprite : sprites )
		{
			if( rowIndex == sprite.y() / context.tileHeight() + 1 )
				context.imageRenderer().draw(
						sprite.texture(),
						sprite.x() - context.offsetX(),
						sprite.y() - context.offsetY() - sprite.height() + context.tileHeight(),
						sprite.width(), sprite.height() );
		}
	}
}