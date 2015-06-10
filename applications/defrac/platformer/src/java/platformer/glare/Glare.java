package platformer.glare;

import defrac.gl.GL;
import defrac.gl.GLBuffer;
import defrac.gl.GLMatrix;
import defrac.gl.GLSubstrate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Glare is a GL Wrapper for simple drawing operations demanding fast performance.
 *
 * Each program is a single-ton inside the Glare, strictly bounded to its Glare instance.
 *
 * @Joa:
 * web texture size pow2
 * retina blurry
 *
 * @author Andre Michelle
 */
public final class Glare
{
	public static class Builder
	{
		private final HashMap<Class<? extends GlareProgram>, GlareProgram.Factory> register;

		public Builder()
		{
			register = new HashMap<>();
		}

		public Builder addProgram(
				@Nonnull final Class<? extends GlareProgram> type,
				@Nonnull final GlareProgram.Factory factory )
		{
			register.put( type, factory );

			return this;
		}

		@Nonnull
		public Glare build()
		{
			return new Glare( register );
		}
	}

	@Nonnull
	private final HashMap<Class<? extends GlareProgram>, GlareProgram.Factory> register;

	private final HashMap<Class<? extends GlareProgram>, GlareProgram> programs;

	private final float[] background;

	@Nullable
	private GlareProgram program;

	@Nullable
	private GLSubstrate glSubstrate;

	@Nullable
	public GLBuffer glBuffer;
	public final GlareTextureCache textureCache;
	public final GLMatrix glMatrix;
	public final float[] buffer;
	public final int bufferSize;
	public int bufferPointer;
	public int drawCalls;

	private Glare( @Nonnull final HashMap<Class<? extends GlareProgram>, GlareProgram.Factory> register )
	{
		assert !register.isEmpty();

		this.register = register;

		programs = initPrograms( new HashMap<>( register.size() ) );

		buffer = createBuffer();
		bufferSize = buffer.length;

		textureCache = new GlareTextureCache();

		glMatrix = new GLMatrix();

		background = new float[ 4 ];
	}

	public void background( @Nonnull final float[] rgba )
	{
		System.arraycopy( rgba, 0, background, 0, 4 );
	}

	public void prepare( @Nonnull final GLSubstrate glSubstrate, final float width, final float height )
	{
		assert 0 == bufferPointer;

		// Reset GL
		glSubstrate.disable( GL.DITHER );
		glSubstrate.disable( GL.STENCIL_TEST );
		glSubstrate.disable( GL.CULL_FACE );
		glSubstrate.disable( GL.DEPTH_TEST );
		glSubstrate.depthFunc( GL.ALWAYS );
		glSubstrate.enable( GL.BLEND );
		glSubstrate.blendEquationSeparate( GL.FUNC_ADD, GL.FUNC_ADD );
		glSubstrate.blendFuncSeparate( GL.ONE, GL.ONE_MINUS_SRC_ALPHA, GL.ONE, GL.ONE );

		for( final GlareProgram glareProgram : programs.values() ) // on start-up or first occurrence?
			if( !glareProgram.compiled() )
				glareProgram.compile( glSubstrate );

		if( null == glBuffer )
		{
			glBuffer = glSubstrate.createBuffer();
			glSubstrate.bindBuffer( GL.ARRAY_BUFFER, glBuffer );
			glSubstrate.bufferData( GL.ARRAY_BUFFER, buffer, 0, buffer.length, GL.STATIC_DRAW );
		}
		else
		{
			glSubstrate.bindBuffer( GL.ARRAY_BUFFER, glBuffer );
		}

		glMatrix.identity();
		glMatrix.ortho( 0f, width, height, 0f, 0f, 1f );

		glSubstrate.viewport( 0, 0, ( int ) width, ( int ) height );
		glSubstrate.clearColor( background[ 0 ], background[ 1 ], background[ 2 ], background[ 3 ] );
		glSubstrate.clear( GL.COLOR_BUFFER_BIT );

		drawCalls = 0;
		bufferPointer = 0;

		this.glSubstrate = glSubstrate;
	}

	@Nonnull
	public <P extends GlareProgram> P getProgram( @Nonnull final Class<P> type )
	{
		final GlareProgram program = programs.get( type );

		assert null != program : type + " has never been registered in " + this;

		//noinspection unchecked
		return ( P ) program;
	}

	public void activeProgram( @Nonnull final GlareProgram program )
	{
		if( null == glSubstrate )
			throw new RuntimeException( "Glare cannot be accessed when not in render phase." );

		if( this.program != program )
		{
			flush();

			this.program = program;
		}
	}

	public int complete()
	{
		if( null == glSubstrate )
			throw new RuntimeException( "Glare cannot be accessed when not in render phase." );

		flush();

		bufferPointer = 0;
		glSubstrate = null;
		program = null;

		return drawCalls;
	}

	public void flush()
	{
		if( null == glSubstrate )
			throw new RuntimeException( "Glare cannot be accessed when not in render phase." );

		if( null == program || 0 == bufferPointer )
			return;

		if( !program.compiled() )
			program.compile( glSubstrate );

		program.finalizeDraw( glSubstrate );

		drawCalls++;
		bufferPointer = 0;
	}

	public void destroy( @Nonnull final GLSubstrate glSubstrate )
	{
		for( final GlareProgram program : programs.values() )
			program.destroy( glSubstrate );

		initPrograms( programs );
		textureCache.clear();
		glBuffer = null;
	}

	@Nonnull
	private HashMap<Class<? extends GlareProgram>, GlareProgram> initPrograms(
			@Nonnull final HashMap<Class<? extends GlareProgram>, GlareProgram> programs )
	{
		programs.clear();

		for( Map.Entry<Class<? extends GlareProgram>, GlareProgram.Factory> entry : register.entrySet() )
			programs.put( entry.getKey(), entry.getValue().create( this ) );

		return programs;
	}

	@Nonnull
	private float[] createBuffer()
	{
		int size = 0;

		for( final GlareProgram program : programs.values() )
			size = Math.max( program.bufferCapacity(), size );

		System.out.println( "Glare buffer-size: " + ( size >> 10 ) + "kb" );

		return new float[ size ];
	}
}