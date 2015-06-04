package projects;

import defrac.display.Stage;
import defrac.display.Texture;
import defrac.display.TextureData;
import defrac.display.event.UIEventManager;
import defrac.resource.TextureDataResource;
import defrac.util.KeyCode;
import platformer.Platformer;
import platformer.renderer.RendererContext;
import platformer.renderer.Sprite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Andre Michelle
 */
public final class GripeSprite implements Sprite
{
	private static final double Drag = 0.5;
	private static final double Force = 1.4;
	private static final int Size = 32;

	private final Platformer platformer;
	private final Stage stage;

	@Nullable
	private GripeSpriteAtlas sheet;

	private double positionX = 96.0;
	private double positionY = 432.0;
	private double velocityX = 0.0;
	private double velocityY = 0.0;

	private boolean flip;
	private int timeMove = 0;

	public GripeSprite( @Nonnull final Platformer platformer, @Nonnull final Stage stage )
	{
		this.platformer = platformer;
		this.stage = stage;

		TextureDataResource.from( "gripe.png" ).listener( new TextureDataResource.SimpleListener()
		{
			@Override
			public void onResourceComplete( @Nonnull final TextureDataResource resource, @Nonnull final TextureData textureData )
			{
				sheet = new GripeSpriteAtlas( textureData );

				init();
			}
		} ).load();
	}

	private void init()
	{
		stage.globalEvents().onEnterFrame.add( ( event ) -> {
			final UIEventManager eventManager = stage.eventManager();
			final boolean l = eventManager.isKeyDown( KeyCode.LEFT );
			final boolean r = eventManager.isKeyDown( KeyCode.RIGHT );
			final boolean u = eventManager.isKeyDown( KeyCode.UP );
			final boolean d = eventManager.isKeyDown( KeyCode.DOWN );

			final double forceX;
			final double forceY;

			if( l && !r )
				forceX = -Force;
			else if( r && !l )
				forceX = Force;
			else
				forceX = 0.0;

			if( u && !d )
				forceY = -Force;
			else if( d && !u )
				forceY = Force;
			else
				forceY = 0.0;

			velocityX += forceX;
			velocityY += forceY;

			velocityX -= Drag * velocityX;
			velocityY -= Drag * velocityY;

			if( Math.abs( velocityX ) < 0.1 )
				velocityX = 0.0;
			else
				flip = velocityX < 0.0;

			if( Math.abs( velocityY ) < 0.1 )
				velocityY = 0.0;

			if( 0.0 != velocityX || 0.0 != velocityY )
				timeMove = platformer.currentTime();

			positionX += velocityX;
//			positionY += velocityY;
		} );
	}

	@Override
	public int x()
	{
		return ( int ) positionX;
	}

	@Override
	public int y()
	{
		return ( int ) positionY;
	}

	@Override
	public int width()
	{
		return Size;
	}

	@Override
	public int height()
	{
		return Size;
	}

	@Override
	public void requestRender( @Nonnull final RendererContext context )
	{
		if( null == sheet )
			return;

		final boolean run = 0.0 < Math.abs( velocityX );

		final Texture texture;

		if( run )
		{
			texture = sheet.run[ ( context.currentTime() / 80 ) % sheet.run.length ];
		}
		else
		{
			// Some cuteness for idle
			final int idleDuration = 10000;
			final int lookDuration = 2000;
			final int blinkDuration = 400;
			final int timeStanding = platformer.currentTime() - timeMove;

			if( timeStanding < idleDuration )
			{
				texture = sheet.idle[ 0 ];
			}
			else if( timeStanding < idleDuration + lookDuration )
			{
				texture = sheet.idle[ 1 ];
			}
			else
			{
				final int timeBlink = timeStanding - ( idleDuration + lookDuration );

				if( timeBlink >= blinkDuration )
				{
					if( timeBlink - blinkDuration > lookDuration )
					{
						timeMove = platformer.currentTime();
						texture = sheet.idle[ 0 ];
					}
					else
					{
						texture = sheet.idle[ 1 ];
					}
				}
				else
				{
					final int index = 1 + ( int ) ( timeBlink / ( double ) blinkDuration * ( sheet.idle.length - 1 ) );

					texture = sheet.idle[ index ];
				}
			}
		}

		context.imageRenderer().draw(
				texture,
				( int ) positionX - context.offsetX(),
				( int ) positionY - context.offsetY() - Size + context.tileHeight() + 1,
				Size, Size, false, flip, false );
	}
}