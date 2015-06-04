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
		body.style.margin = "0";
		body.style.padding = "0";
		body.style.width = "100%";
		body.style.height = "100%";
		body.style.display = "flex";
		body.style.display = "-webkit-flex";
		body.style.position = "absolute";
		body.style.overflow = "hidden";
		body.style.backgroundColor = "black";
		body.style.setProperty( "-webkit-align-items", "center" );
		body.style.setProperty( "align-items", "center" );
		body.style.setProperty( "justify-content", "center" );
		body.style.setProperty( "-webkit-justify-content", "center" );
		final HTMLElement div = ( HTMLElement ) document.createElement( "div" );
		div.style.width = project.width + "px";
		div.style.height = project.height + "px";
		body.appendChild( div );

		FrameBuilder.
				forScreen( new MainScreen( project.create ) ).
				disableCSS().container( div ).
				show();

		final HTMLElement canvas = ( HTMLElement ) document.querySelector( "canvas" );
		assert null != canvas;

		canvas.style.outline = "none";
		canvas.focus();
	}
}
