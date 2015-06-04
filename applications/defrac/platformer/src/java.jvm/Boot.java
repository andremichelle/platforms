import defrac.concurrent.Dispatchers;
import defrac.ui.FrameBuilder;

import java.awt.*;

/**
 * @author Andre Michelle
 */
public final class Boot
{
	public static void main( String[] args )
	{
		//final Project project = Project.SuperMario;
		final Project project = Project.DepthSorting;

		Dispatchers.FOREGROUND.exec( () -> {
			final Frame frame =
					FrameBuilder.
							forScreen( new MainScreen( project ) ).
							resizable().
							width( project.width + 100 ).
							height( project.height + 100 ).
							title( project.title ).
							show();

			frame.setBackground(Color.BLACK);
		});
	}
}
