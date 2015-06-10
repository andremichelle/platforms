import defrac.ui.Screen;
import screens.CenteredDisplayListScreen;
import screens.CenteredGLSurfaceScreen;

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
			return 224;
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

	Launch SuperMarioSurface = new Launch()
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
			return 224;
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
			return new CenteredGLSurfaceScreen( applications.SuperMarioSurface::new, width(), height() );
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
			return "Gripe";
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

	Launch Physics = new Launch()
	{
		@Nonnull
		@Override
		public String title()
		{
			return "Physics";
		}

		@Override
		public int width()
		{
			return 512;
		}

		@Override
		public int height()
		{
			return 384;
		}

		@Override
		public int backgroundColor()
		{
			return 0xFFFFFFFF;
		}

		@Nonnull
		@Override
		public Screen createScreen()
		{
			return new CenteredDisplayListScreen( applications.Physics::new, width(), height() );
		}
	};

	Launch Performance = new Launch()
	{
		@Nonnull
		@Override
		public String title()
		{
			return "Performance";
		}

		@Override
		public int width()
		{
			return 1024;
		}

		@Override
		public int height()
		{
			return 512;
		}

		@Override
		public int backgroundColor()
		{
			return 0xFFFFFFFF;
		}

		@Nonnull
		@Override
		public Screen createScreen()
		{
			return new CenteredDisplayListScreen( applications.Performance::new, width(), height() );
		}
	};
}