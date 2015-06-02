package platformer.tmx;

/**
 * @author Andre Michelle
 */
public final class TileFlags
{
	public static final int FlipHorizontally = 0x80000000;
	public static final int FlipVertically = 0x40000000;
	public static final int FlipDiagonally = 0x20000000;
	public static final int FlippedMask = ~( FlipHorizontally | FlipVertically | FlipDiagonally );
}