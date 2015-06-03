import defrac.concurrent.Dispatchers;
import defrac.ui.FrameBuilder;

/**
 * @author Andre Michelle
 */
public final class Boot
{
	public static void main( String[] args )
	{
		Dispatchers.FOREGROUND.exec( () ->
				FrameBuilder.
						forScreen( new MainScreen() ).
						title( "Test" ).
						width( 384 ).
						height( 320 ).
						title( "Platformer" ).
						show() );
	}
}