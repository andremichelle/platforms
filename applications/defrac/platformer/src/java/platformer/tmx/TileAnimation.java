package platformer.tmx;

import defrac.lang.Lists;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andre Michelle
 */
public final class TileAnimation
{
	public static final class Builder
	{
		private final List<Integer> times;
		private final List<Integer> frames;

		private int size = 0;
		private int currentTime = 0;

		public Builder( final int capacity )
		{
			times = Lists.newArrayList( capacity );
			frames = Lists.newArrayList( capacity );
		}

		public void add( final int time, final int frame )
		{
			assert currentTime < time;

			times.add( time );
			frames.add( frame );

			size++;

			currentTime = time;
		}

		@Nonnull
		public TileAnimation build()
		{
			final int[] arrayTimes = new int[ size ];
			final int[] arrayFrames = new int[ size ];

			for( int i = 0 ; i < size ; ++i )
			{
				arrayTimes[ i ] = times.get( i );
				arrayFrames[ i ] = frames.get( i );
			}

			return new TileAnimation( arrayTimes, arrayFrames, currentTime );
		}
	}

	private final int[] times;
	private final int[] frames;
	private final int duration;

	private TileAnimation( @Nonnull final int[] times, @Nonnull final int[] frames, final int duration )
	{
		this.times = times;
		this.frames = frames;
		this.duration = duration;
	}

	public int getCurrentIndex( final int time )
	{
		final int wrappedTime = time % duration;

		final int minIndex = times.length - 1;

		assert 0 < minIndex : "animation must have at least 2 entries.";

		int min = minIndex;
		int max = 0;

		while( max <= min )
		{
			int index = ( max + min ) >>> 1;

			final int delta = times[ index ] - wrappedTime;

			if( delta < 0.0 )
				max = index + 1;
			else if( delta > 0.0 )
				min = index - 1;
			else
				return frames[ index + 1 ];
		}

		return frames[ Math.min( minIndex, max ) ];
	}

	@Override
	public String toString()
	{
		return "[TileAnimation" +
				" times: " + Arrays.toString( times ) +
				" frames: " + Arrays.toString( frames ) +
				"]";
	}
}