package platformer;

import defrac.display.DisplayObject;
import defrac.display.GLSurface;
import defrac.display.graphics.Graphics;
import defrac.display.graphics.TextBaseline;
import defrac.gl.GLFrameBuffer;
import defrac.gl.GLSubstrate;
import defrac.lang.Lists;
import defrac.util.Color;
import platformer.glare.Glare;
import platformer.glare.GlareLineProgram;
import platformer.glare.GlareRectangleProgram;
import platformer.glare.GlareTextureProgram;
import platformer.glare.GlareTextureProgramAperture;
import platformer.renderer.Renderer;
import platformer.renderer.RendererContext;
import platformer.renderer.Sprite;
import platformer.tmx.MapData;
import platformer.tmx.TileSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Andre Michelle
 */
public final class Platformer implements RendererContext
{
	private final MapData mapData;
	private final int width;
	private final int height;

	private final int pixelWidth;
	private final int pixelHeight;

	private final Glare glare;

	private final List<Renderer> renderPipe;

	@Nullable
	private Graphics lazyGraphics;

	private long startTime;
	private int time;

	private int offsetX = 0;
	private int offsetY = 0;

	public Platformer( @Nonnull final MapData mapData, final int width, final int height )
	{
		this.mapData = mapData;
		this.width = width;
		this.height = height;

		pixelWidth = width * mapData.tileWidth;
		pixelHeight = height * mapData.tileHeight;

		glare = new Glare.Builder().
				addProgram( GlareTextureProgram.class, GlareTextureProgram::new ).
				addProgram( GlareTextureProgramAperture.class, GlareTextureProgramAperture::new ).
				addProgram( GlareRectangleProgram.class, GlareRectangleProgram::new ).
				addProgram( GlareLineProgram.class, GlareLineProgram::new ).
				build();
		glare.background( Color.extract( mapData.backgroundColor, new float[ 4 ] ) );

		renderPipe = Lists.newArrayList();

		startTime = System.currentTimeMillis();
	}

	@Override
	public int width()
	{
		return width;
	}

	@Override
	public int height()
	{
		return height;
	}

	@Override
	public int mapWidth()
	{
		return mapData.width;
	}

	@Override
	public int mapHeight()
	{
		return mapData.height;
	}

	@Override
	public int tileWidth()
	{
		return mapData.tileWidth;
	}

	@Override
	public int tileHeight()
	{
		return mapData.tileHeight;
	}

	@Override
	public int pixelWidth()
	{
		return pixelWidth;
	}

	@Override
	public int pixelHeight()
	{
		return pixelHeight;
	}

	@Override
	public int offsetX()
	{
		return offsetX;
	}

	@Override
	public int offsetY()
	{
		return offsetY;
	}

	@Nonnull
	@Override
	public TileSet[] tileSets()
	{
		return mapData.tileSets;
	}

	@Nonnull
	public Glare glare()
	{
		return glare;
	}

	@Override
	public int currentTime()
	{
		return time;
	}

	public void addRenderer( @Nonnull final Renderer renderer )
	{
		renderPipe.add( renderer );
	}

	public void removeRenderer( @Nonnull final Renderer renderer )
	{
		renderPipe.remove( renderer );
	}

	/**
	 * Moves the view-port by the given x, y offset in pixels
	 *
	 * @param deltaX The offset for the x-coordinate
	 * @param deltaY The offset for the y-coordinate
	 * @return True, if the view-port has been moved (clamped by map-dimensions)
	 */
	public boolean moveBy( final int deltaX, final int deltaY )
	{
		return moveTo( offsetX + deltaX, offsetY + deltaY );
	}

	/**
	 * Moves the view-port to the given x, y position in pixels
	 *
	 * @param offsetX The value for the x-coordinate
	 * @param offsetY The value for the y-coordinate
	 * @return True, if the view-port has been moved (clamped by map-dimensions)
	 */
	public boolean moveTo( final int offsetX, final int offsetY )
	{
		final int newX = Math.max( 0, Math.min( offsetX, ( mapData.width - width ) * mapData.tileWidth ) );
		final int newY = Math.max( 0, Math.min( offsetY, ( mapData.height - height ) * mapData.tileHeight ) );

		if( this.offsetX == newX && this.offsetY == newY )
			return false;

		this.offsetX = newX;
		this.offsetY = newY;

		return true;
	}

	/**
	 * Centers a sprite.
	 *
	 * @param sprite The target sprite to be aligned to.
	 */
	public void center( @Nonnull final Sprite sprite )
	{
		moveTo( ( int ) sprite.x() + ( sprite.width() - pixelWidth ) / 2, ( int ) sprite.y() - pixelHeight / 2 );
	}

	/**
	 * Restarts the local time for animations
	 */
	public void restartTime()
	{
		startTime = System.currentTimeMillis();
	}

	@Nonnull
	public MapData mapData()
	{
		return mapData;
	}

	/**
	 * Creates a displayObject for the displayList
	 *
	 * @return A DisplayObject
	 */
	@Nonnull
	public DisplayObject createDisplayObject()
	{
		return new GLSurface( pixelWidth, pixelHeight, ( surface1, gl, frameBuffer, renderBuffer, surfaceTexture, width, height, viewportWidth, viewportHeight, transparent ) -> {
			renderCycle( gl );
		} );
	}

	/**
	 * Creates a ui.GLSurface.Renderer
	 *
	 * @return A ui.GLSurface.Renderer
	 */
	@Nonnull
	public defrac.ui.GLSurface.Renderer createGLSurfaceRenderer()
	{
		return new defrac.ui.GLSurface.Renderer()
		{
			@Override
			public void onSurfaceRender( @Nonnull final GLSubstrate glSubstrate )
			{
				renderCycle( glSubstrate );
			}

			@Override
			public void onSurfaceChanged( @Nonnull final GLSubstrate glSubstrate, final GLFrameBuffer glFrameBuffer, final int i, final int i1 )
			{
				// TODO Invalide GL programs
			}

			@Override
			public void onSurfaceCreated( @Nonnull final GLSubstrate glSubstrate, final GLFrameBuffer glFrameBuffer )
			{
				// TODO Invalide GL programs
			}
		};
	}

	@Override
	public String toString()
	{
		return "[Screen" +
				" width: " + width +
				", height: " + height +
				", pixelWidth: " + pixelWidth +
				", pixelHeight: " + pixelHeight +
				"]";
	}

	private void renderCycle( @Nonnull final GLSubstrate glSubstrate )
	{
		time = ( int ) ( System.currentTimeMillis() - startTime );

		glare.prepare( glSubstrate, pixelWidth, pixelHeight );

		for( final Renderer pipe : renderPipe )
		{
			pipe.renderLayer();
		}

		glare.flush();

		log( "GPU #" + glare.drawCalls );

		glare.complete();
	}

	private void log( @Nonnull final String... lines )
	{
		if( null == lazyGraphics )
			lazyGraphics = new Graphics( pixelWidth, pixelHeight );

		lazyGraphics.clearRect( 0, 0, pixelWidth, pixelHeight );
		lazyGraphics.fillStyle( 0f, 0f, 0f, 1f );
		lazyGraphics.textBaseline( TextBaseline.TOP );

		int top = 4;

		for( final String line : lines )
		{
			lazyGraphics.fillText( line, 4, top );

			top += 12;

			if( top > pixelHeight )
				break;
		}

		// TODO Graphics returns a pre-multiplied alpha :(
		// TODO Needs to be power of two.
		//glare.getProgram( GlareTextureProgram.class ).alpha( 1f ).draw( lazyGraphics.texture(), 0, 0, pixelWidth, pixelHeight );

		// TEST other programs
		//glare.getProgram( GlareRectangleProgram.class ).color( new float[]{ 1f, 1f, 0f, 0.7f } ).rect( 128, 64, 16, 16 );
	}
}