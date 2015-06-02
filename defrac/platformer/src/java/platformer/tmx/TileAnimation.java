package platformer.tmx;

import javax.annotation.Nonnull;
import java.util.TreeMap;

/**
 * @author Andre Michelle
 */
public final class TileAnimation
{
	private final TreeMap<Integer, Integer> frames; // time, tileId
	private final int duration;
	private final boolean oneCycle;

	TileAnimation( @Nonnull final TreeMap<Integer, Integer> frames, final int duration, final boolean oneCycle )
	{
		this.frames = frames;
		this.duration = duration;
		this.oneCycle = oneCycle;
	}

	public int getCurrentIndex( final int time )
	{
		assert 0 <= time;

		if( oneCycle && time >= duration )
			return frames.lastEntry().getValue();

		final int relativeTime = time % duration;

		return frames.floorEntry( relativeTime ).getValue();
	}

	@Override
	public String toString()
	{
		return "[Animation" +
				" frames: " + frames +
				", duration: " + duration +
				", oneCycle: " + oneCycle +
				"]";
	}
}