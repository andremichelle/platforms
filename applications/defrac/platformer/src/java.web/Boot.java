import defrac.lang.Bridge;
import defrac.ui.FrameBuilder;
import defrac.util.Color;
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

		final Launch app;

		if( hash.contains( "DepthSorting" ) )
		{
			app = Launch.DepthSorting;
		}
		else if( hash.contains( "GripeGame" ) )
		{
			app = Launch.GripeGame;
		}
		else if( hash.contains( "Physics" ) )
		{
			app = Launch.Physics;
		}
		else
		{
			app = Launch.SuperMario;
		}

		document.title = app.title();

		final int[] rgba = new int[ 4 ];
		Color.extract( app.backgroundColor(), rgba );
		body.style.backgroundColor = "rgba(" + rgba[ 0 ] + ", " + rgba[ 1 ] + ", " + rgba[ 2 ] + ", 1 )";

		FrameBuilder.
				forScreen( app.createScreen() ).
				show();

		final HTMLElement canvas = ( HTMLElement ) document.querySelector( "canvas" );
		assert null != canvas;

		canvas.focus();
	}
}