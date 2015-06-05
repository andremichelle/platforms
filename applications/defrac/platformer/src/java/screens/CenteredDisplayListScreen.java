package screens;

import defrac.display.Stage;
import defrac.lang.Procedure;
import defrac.ui.DisplayList;
import defrac.ui.Gravity;
import defrac.ui.LinearLayout;
import defrac.ui.PixelUnits;
import defrac.ui.Screen;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public final class CenteredDisplayListScreen extends Screen
{
	private final Procedure<Stage> onCreate;
	private final int width;
	private final int height;

	private DisplayList displayList;

	public CenteredDisplayListScreen( @Nonnull final Procedure<Stage> onCreate, final int width, final int height )
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

		displayList = new DisplayList();

		displayList.root().onSuccess( onCreate );

		displayList.layoutConstraints(
				new LinearLayout.LayoutConstraints(
						width,
						height, PixelUnits.DP ).
						gravity( Gravity.CENTER ) );

		layout.addView( displayList );

		rootView( layout );
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		displayList.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		displayList.onResume();
	}
}
