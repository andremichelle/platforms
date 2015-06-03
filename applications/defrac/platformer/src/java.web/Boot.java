import defrac.ui.FrameBuilder;

/**
 * @author Andre Michelle
 */
public final class Boot
{
	public static void main( String[] args )
	{
		FrameBuilder.
				forScreen( new MainScreen() ).
				show();
	}
}
