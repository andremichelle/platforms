import defrac.ui.FrameBuilder;
import defrac.web.HTMLDocument;
import defrac.web.HTMLElement;
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

		body.style.margin = "0";
		body.style.padding = "0";
		body.style.width = "100%";
		body.style.height = "100%";
		body.style.display = "flex";
		body.style.position = "absolute";
		body.style.overflow = "hidden";
		body.style.backgroundColor = "black";
		body.style.setProperty( "align-items", "center" );
		body.style.setProperty( "justify-content", "center" );
		final HTMLElement div = ( HTMLElement ) document.createElement( "div" );
		div.style.width = "384px";
		div.style.height = "320px";
		body.appendChild( div );

		FrameBuilder.
				forScreen( new MainScreen() ).
				disableCSS().container( div ).
				show();

		final HTMLElement canvas = ( HTMLElement ) document.querySelector( "canvas" );
		assert null != canvas;

		canvas.style.outline = "none";
		canvas.focus();
	}
}
