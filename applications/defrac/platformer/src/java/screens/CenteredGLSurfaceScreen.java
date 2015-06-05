package screens;

import defrac.lang.Procedure;
import defrac.ui.GLSurface;
import defrac.ui.Gravity;
import defrac.ui.LinearLayout;
import defrac.ui.PixelUnits;
import defrac.ui.Screen;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public final class CenteredGLSurfaceScreen extends Screen
{
	private final Procedure<GLSurface> onCreate;
	private final int width;
	private final int height;

	public CenteredGLSurfaceScreen( @Nonnull final Procedure<GLSurface> onCreate, final int width, final int height )
	{
		this.onCreate = onCreate;
		this.width = width;
		this.height = height;
	}

	@Override
	protected void onCreate()
	{
		super.onCreate();

		final LinearLayout layout =
				LinearLayout.
						horizontal().
						gravity( Gravity.CENTER );

		final GLSurface surface = new GLSurface();
		surface.layoutConstraints(
				new LinearLayout.LayoutConstraints(
						width,
						height, PixelUnits.DP ).
						gravity( Gravity.CENTER ) );

		layout.addView( surface );

		rootView( layout );

		onCreate.apply( surface );
	}
}