import defrac.concurrent.Dispatchers;
import defrac.ui.FrameBuilder;

/**
 * @author Andre Michelle
 */
public final class Boot
{
	public static void main( String[] args )
	{
//		final Project project = Project.SuperMario;
		final Project project = Project.DepthSorting;

		Dispatchers.FOREGROUND.exec( () ->
				FrameBuilder.
						forScreen( new MainScreen( project.create ) ).
						width( project.width ).
						height( project.height ).
						title( project.title ).
						show() );
	}
}