import defrac.concurrent.Dispatchers;
import defrac.ui.FrameBuilder;

/**
 * @author Andre Michelle
 */
public final class Boot
{
	public static void main( String[] args )
	{
//		final Launch launch = Launch.SuperMario;
//		final Launch launch = Launch.SuperMarioSurface;
		final Launch launch = Launch.Physics;
//		final Launch launch = Launch.Performance;
//		final Launch launch = Launch.GripeGame;
//		final Launch launch = Launch.DepthSorting;

		Dispatchers.FOREGROUND.exec( () -> FrameBuilder.
				forScreen( launch.createScreen() ).
				resizable().
				width( launch.width() ).
				height( launch.height() ).
				title( launch.title() ).
				backgroundColor( 0xFF000000 | launch.backgroundColor() ).
				show() );
	}
}