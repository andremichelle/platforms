import defrac.concurrent.Dispatchers;
import defrac.ui.FrameBuilder;
import defrac.util.Color;

/**
 * @author Andre Michelle
 */
public final class Boot
{
	public static void main( String[] args )
	{
//		final Project project = Project.SuperMario;
		final Project project = Project.GripeGame;
//		final Project project = Project.DepthSorting;

		Dispatchers.FOREGROUND.exec( () ->
				FrameBuilder.
						forScreen( new MainScreen( project ) ).
						resizable().
						width( project.width ).
						height( project.height ).
						title( project.title ).
						backgroundColor( Color.Web.BLACK ).
						show());
	}
}
