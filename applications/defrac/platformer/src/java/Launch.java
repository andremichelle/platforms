import defrac.ui.Screen;
import screens.CenteredDisplayListScreen;

import javax.annotation.Nonnull;

/**
 * @author Andre Michelle
 */
public interface Launch
{
	@Nonnull
	String title();

	int width();

	int height();

	int backgroundColor();

	@Nonnull
	Screen createScreen();

	Launch SuperMario = new Launch()
	{
		@Nonnull
		@Override
		public String title()
		{
			return "Super Mario";
		}

		@Override
		public int width()
		{
			return 384;
		}

		@Override
		public int height()
		{
			return 320;
		}

		@Override
		public int backgroundColor()
		{
			return 0;
		}

		@Nonnull
		@Override
		public Screen createScreen()
		{
			return new CenteredDisplayListScreen( applications.SuperMario::new, width(), height() );
		}
	};

	Launch DepthSorting = new Launch()
	{
		@Nonnull
		@Override
		public String title()
		{
			return "DepthSorting";
		}

		@Override
		public int width()
		{
			return 320;
		}

		@Override
		public int height()
		{
			return 384;
		}

		@Override
		public int backgroundColor()
		{
			return 0;
		}

		@Nonnull
		@Override
		public Screen createScreen()
		{
			return new CenteredDisplayListScreen( applications.DepthSorting::new, width(), height() );
		}
	};

	Launch GripeGame = new Launch()
	{
		@Nonnull
		@Override
		public String title()
		{
			return "GripeGame";
		}

		@Override
		public int width()
		{
			return 384;
		}

		@Override
		public int height()
		{
			return 304;
		}

		@Override
		public int backgroundColor()
		{
			return 0;
		}

		@Nonnull
		@Override
		public Screen createScreen()
		{
			return new CenteredDisplayListScreen( applications.Gripe::new, width(), height() );
		}
	};
}