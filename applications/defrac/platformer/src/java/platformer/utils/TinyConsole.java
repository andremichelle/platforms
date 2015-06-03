package platformer.utils;

import defrac.display.Canvas;
import defrac.display.graphics.Graphics;
import defrac.display.graphics.TextBaseline;

/**
 * @author Andre Michelle
 */
public final class TinyConsole extends Canvas
{
	public static final int Width = 88;
	public static final int Height = 56;

	public static TinyConsole get()
	{
		if( null == instance )
		{
			instance = new TinyConsole();
		}

		return instance;
	}

	private static TinyConsole instance;

	public TinyConsole()
	{
		super( Width, Height );
	}

	public void log( final String... lines )
	{
		final Graphics graphics = graphics();
		graphics.clearRect( 0, 0, Width, Height );
		graphics.fillStyle( 0.15f, 0.15f, 0.15f, 1f );
		graphics.fillRect( 0, 0, Width, Height );
		graphics.fillStyle( 1f, 1f, 1f, 0.7f );
		graphics.textBaseline( TextBaseline.TOP );

		int top = 4;

		for( final String line : lines )
		{
			graphics.fillText( line, 4, top );

			top += 12;

			if( top > Height )
				return;
		}
	}
}