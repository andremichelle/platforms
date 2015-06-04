import defrac.lang.Bridge;
import defrac.ui.FrameBuilder;
import defrac.web.HTMLDocument;
import defrac.web.HTMLElement;
import defrac.web.Location;
import defrac.web.Toplevel;

/**
 * @author Andre Michelle
 */
public final class Boot
{
	public static void main( String[] args )
	{
		final HTMLDocument document = Toplevel.document();
		final HTMLElement body = document.body;
		assert null != body;

		final Location location = Toplevel.window().location;

		assert null != location : "no location";

		final String hash = ( String ) Bridge.getUnsafeObject( location, "hash" );

		final Project project;

		if( hash.contains( "DepthSorting" ) )
		{
			project = Project.DepthSorting;
		}
		else
		{
			project = Project.SuperMario;
		}

		document.title = project.title;
		body.style.backgroundColor = "black";

		FrameBuilder.
				forScreen( new MainScreen( project ) ).
				show();

		final HTMLElement canvas = ( HTMLElement ) document.querySelector( "canvas" );
		assert null != canvas;

		canvas.focus();
	}
}
