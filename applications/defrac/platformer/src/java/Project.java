import defrac.display.Stage;
import defrac.lang.Procedure;
import projects.DepthSortingTest;
import projects.SuperMarioTest;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public final class Project
{
	public static final Project SuperMario = new Project( SuperMarioTest::new, "SuperMarioTest", 384, 320 );
	public static final Project DepthSorting = new Project( DepthSortingTest::new, "DepthSortingTest", 320, 384 );

	public final Procedure<Stage> create;
	public final String title;
	public final int width;
	public final int height;

	private Project( @Nonnull final Procedure<Stage> create, @Nonnull final String title, final int width, final int height )
	{
		this.create = create;
		this.title = title;
		this.width = width;
		this.height = height;
	}
}