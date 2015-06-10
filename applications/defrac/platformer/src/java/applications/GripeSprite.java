package applications;

import defrac.display.Stage;
import defrac.display.Texture;
import defrac.display.TextureData;
import defrac.display.event.UIEventManager;
import defrac.resource.TextureDataResource;
import defrac.util.KeyCode;
import platformer.Platformer;
import platformer.glare.GlareTextureProgram;
import platformer.renderer.RendererContext;
import platformer.renderer.Sprite;
import platformer.tmx.MapData;
import platformer.tmx.MapTileLayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Do not take this examples code seriously.
 * I have been trying to make the collisions by a point test which has a lot of special cases.
 * Better use a solid collision-detection with dynamic intersection on a rectangle.
 * However at least it starts to live :)
 * @author Andre Michelle
 */
public final class GripeSprite implements Sprite
{
	private static final int Size = 32;
	private static final double DragFall = 0.02;
	private static final double DragMove = 0.3;
	private static final double MoveForce = 0.8;
	private static final double JumpForce = 7.0;
	private static final double Gravity = 0.2;

	private final Platformer platformer;
	private final Stage stage;

	@Nullable
	private GripeSpriteAtlas sheet;

	private double positionX = 64.0;
	private double positionY = 362.0;
	private double velocityX = 0.0;
	private double velocityY = 0.0;

	private boolean flip;
	private boolean jumpEnabled = true;
	private boolean jumping = true;
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
			final boolean j = eventManager.isKeyDown( KeyCode.UP );

			if( !jumping && jumpEnabled && j )
			{
				velocityY = -JumpForce;
				jumping = true;
				jumpEnabled = false;
			}

			if( !jumpEnabled && !j )
				jumpEnabled = true;

			final double forceX;

			if( l && !r )
				forceX = -MoveForce;
			else if( r && !l )
				forceX = MoveForce;
			else
				forceX = 0.0;

			velocityX += forceX;
			velocityY += Gravity;

			if( Math.abs( velocityX ) < 0.1 )
				velocityX = 0.0;
			else
				flip = velocityX < 0.0;

			if( Math.abs( velocityY ) < 0.1 )
				velocityY = 0.0;

			// Simple tile physics (I mean it.)
			//
			final MapData mapData = platformer.mapData();
			final MapTileLayer collisions = ( MapTileLayer ) mapData.getLayerByName( "collision" );
			assert null != collisions : "collision layer not found.";

			final int leftOffset = Size / 2;

			boolean floor = false;

			if( 0.0 != velocityY )
			{
				final int snapX = ( int ) ( positionX + velocityX + leftOffset );
				final int tileX = snapX / mapData.tileWidth;
				final int tileY = ( int ) ( positionY + velocityY ) / mapData.tileHeight;

				if( velocityY > 0.0 )
				{
					final int blockIndex = ( tileY + 1 ) * collisions.width + tileX;

					if( 0 == snapX % mapData.tileWidth )
					{
						// Since we are trying to get it to work as a point object, we need a special treatment here.
						//
						if( 0 != collisions.data[ blockIndex ] && 0 != collisions.data[ blockIndex - 1 ] )
						{
							velocityY = 0.0;
							positionY = tileY * mapData.tileHeight;
							floor = true;
							jumping = false;
						}
					}
					else if( 0 != collisions.data[ blockIndex ] )
					{
						velocityY = 0.0;
						positionY = tileY * mapData.tileHeight;
						floor = true;
						jumping = false;
					}
				}
				else if( velocityY < 0.0 )
				{
					final int blockIndex = tileY * collisions.width + tileX;

					if( 0 == snapX % mapData.tileWidth )
					{
						if( 0 != collisions.data[ blockIndex ] && 0 != collisions.data[ blockIndex - 1 ] )
						{
							velocityY = 0.0;
							positionY = ( tileY + 1 ) * mapData.tileHeight;
						}
					}
					else if( 0 != collisions.data[ blockIndex ] )
					{
						velocityY = 0.0;
						positionY = ( tileY + 1 ) * mapData.tileHeight;
					}
				}
			}

			if( velocityX < 0.0 )
			{
				final int tileX = ( int ) ( ( positionX + velocityX + leftOffset ) / mapData.tileWidth );
				final int tileY = ( int ) ( ( positionY + velocityY ) / mapData.tileHeight );

				if( 0 != collisions.data[ tileY * collisions.width + tileX ] )
				{
					velocityX = 0.0;
					positionX = tileX * mapData.tileWidth;
				}
				else if( positionX + velocityX < 0.0 )
				{
					velocityX = 0.0;
					positionX = 0.0;
				}
			}
			else if( velocityX > 0.0 )
			{
				final int tileX = ( int ) ( ( positionX + velocityX + leftOffset ) / mapData.tileWidth );
				final int tileY = ( int ) ( ( positionY + velocityY ) / mapData.tileHeight );

				if( 0 != collisions.data[ tileY * collisions.width + tileX ] )
				{
					velocityX = 0.0;
					positionX = ( tileX - 1 ) * mapData.tileWidth;
				}
				else if( positionX + velocityX > ( mapData.width - 2 ) * mapData.tileWidth )
				{
					velocityX = 0.0;
					positionX = ( mapData.width - 2 ) * mapData.tileWidth;
				}
			}

			velocityX -= DragMove * velocityX;

			if( !floor )
			{
				velocityY -= DragFall * velocityY;
			}

			if( 0.0 != velocityX || 0.0 != velocityY )
				timeMove = platformer.currentTime();

			positionX += velocityX;
			positionY += velocityY;
		} );
	}

	@Override
	public float x()
	{
		return ( int ) positionX;
	}

	@Override
	public float y()
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

		if( jumping )
		{
			texture = sheet.jump;
		}
		else if( 0.0 < velocityY )
		{
			texture = sheet.fall[ ( context.currentTime() / 80 ) % sheet.fall.length ];
		}
		else if( run )
		{
			texture = sheet.run[ ( context.currentTime() / 80 ) % sheet.run.length ];
		}
		else
		{
			// Some cuteness for idle
			final int idleDuration = 4000;
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

		context.glare().getProgram( GlareTextureProgram.class ).draw(
				texture,
				( int ) positionX - context.offsetX(),
				( int ) positionY - context.offsetY() - Size + context.tileHeight() + 1,
				Size, Size, false, flip, false );
	}
}