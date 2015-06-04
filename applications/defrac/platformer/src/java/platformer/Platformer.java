package platformer;

import defrac.display.DisplayObject;
import defrac.display.GLSurface;
import defrac.display.Stage;
import defrac.gl.GL;
import defrac.gl.GLFrameBuffer;
import defrac.gl.GLRenderBuffer;
import defrac.gl.GLTexture;
import defrac.lang.Lists;
import platformer.gl.GlRenderer;
import platformer.renderer.Renderer;
import platformer.renderer.RendererContext;
import platformer.renderer.Sprite;
import platformer.tmx.MapData;
import platformer.tmx.TileSet;
import platformer.utils.TinyConsole;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Andre Michelle
 */
public final class Platformer implements GLSurface.Renderer, RendererContext
{
	private final MapData mapData;
	private final int width;
	private final int height;

	private final int pixelWidth;
	private final int pixelHeight;

	private final GLSurface surface;

	private final GlRenderer glRenderer;

	private final List<Renderer> renderPipe;

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

		surface = new GLSurface( pixelWidth, pixelHeight, this );

		glRenderer = new GlRenderer( 256 );

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
	@Override
	public GlRenderer imageRenderer()
	{
		return glRenderer;
	}

	@Override
	public int currentTime()
	{
		return time;
	}

	@Override
	public void onGLSurfaceRender( @Nonnull final GLSurface surface,
								   @Nonnull final GL gl,
								   @Nonnull final GLFrameBuffer frameBuffer,
								   @Nullable final GLRenderBuffer renderBuffer,
								   @Nonnull final GLTexture surfaceTexture,
								   final float width, final float height,
								   final int viewportWidth, final int viewportHeight,
								   final boolean transparent )
	{
		time = ( int ) ( System.currentTimeMillis() - startTime );

		final Stage stage = surface.stage();

		if( null == stage )
			return;

		glRenderer.begin( stage, gl, width, height, mapData.backgroundColor );

		for( final Renderer pipe : renderPipe )
			pipe.renderLayer();

		glRenderer.complete();

		final TinyConsole console = TinyConsole.get();

		console.log( "Calls " + glRenderer.drawCalls, "Triangles " + glRenderer.drawTriangles );
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

	public void center( final Sprite sprite )
	{
		moveTo( sprite.x() + ( sprite.width() - pixelWidth ) / 2, sprite.y() - pixelHeight / 2 );
	}

	/**
	 * Restarts the local time for animations
	 */
	public void restartTime()
	{
		startTime = System.currentTimeMillis();
	}

	/**
	 * @return The displayObject to be added on stage
	 */
	@Nonnull
	public DisplayObject displayObject()
	{
		return surface;
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
}