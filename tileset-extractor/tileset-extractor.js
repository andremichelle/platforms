function onLoad()
{
	var choose = document.querySelector( 'input[type="file"]' );
	var loadDemoButton = document.querySelector( 'button[demo]' );
	var loadDemoBigButton = document.querySelector( 'button[demo-big]' );
	var tileWidthInput = document.querySelector( 'input[name="tile-width"]' );
	var tileHeightInput = document.querySelector( 'input[name="tile-height"]' );
	var toleranceInput = document.querySelector( 'input[name="tolerance"]' );
	var progress = document.querySelector( 'progress' );
	var console = document.querySelector( 'div[console]' );
	var tilesLayer = document.querySelector( 'div[tiles]' );
	var tilesetLayer = document.querySelector( 'div[tileset]' );
	var resultLayer = document.querySelector( 'div[result]' );

	var downloadMapLink = document.querySelector( "a[download-map]" );
	var downloadTilesLink = document.querySelector( "a[download-tiles]" );
	var downloadTileMapLink = document.querySelector( "a[download-tilemap]" );

	var map = null;
	var tiles = null;
	var source = null;
	var worker = null;
	var sourceWidth = 0;
	var sourceHeight = 0;
	var tileWidth = 0;
	var tileHeight = 0;

	function reset()
	{
		if( worker )
		{
			worker.terminate();
			worker = null;
		}

		map = null;
		tiles = null;

		console.innerHTML = "";
		tilesLayer.innerHTML = "";
		tilesetLayer.innerHTML = "";
		progress.value = 0;

		resultLayer.setAttribute( "hidden", "" );
	}

	function fullReset()
	{
		reset();

		source = null;
		sourceWidth = 0;
		sourceHeight = 0;
		tileWidth = 0;
		tileHeight = 0;
	}

	function log( msg )
	{
		var line = document.createElement( "p" );
		line.setAttribute( "fine", "" );
		line.textContent = msg;

		console.appendChild( line );
	}

	function error( msg )
	{
		var line = document.createElement( "p" );
		line.setAttribute( "error", "" );
		line.textContent = msg;

		console.appendChild( line );
	}

	function checkSourceSize()
	{
		var tileWidth = tileWidthInput.value;
		var tileHeight = tileHeightInput.value;
		var numCols = source.width / tileWidth;
		var numRows = source.height / tileHeight;
		var valid = true;

		if( 0 == numCols || numCols != Math.floor( numCols ) )
		{
			error( "image-width not dividable by tile-width." );
			valid = false;
		}
		if( 0 == numRows || numRows != Math.floor( numRows ) )
		{
			error( "image-height not dividable by tile-height." );
			valid = false;
		}
		if( valid )
		{
			log( "map " + numCols + " x " + numRows );
		}
		return valid;
	}

	function extract( src )
	{
		tileWidth = tileWidthInput.value;
		tileHeight = tileHeightInput.value;

		source = new Image();
		source.src = src;
		source.onload = function ()
		{
			sourceWidth = source.width;
			sourceHeight = source.height;

			log( "size " + sourceWidth + " x " + sourceHeight + "px" );

			if( checkSourceSize() )
				beginExtractionWorker();
		};
		source.onError = function ()
		{
			error( "Could not load image." );
		};
	}

	function extractSourceData( source )
	{
		var canvas = document.createElement( "canvas" );
		canvas.setAttribute( "width", source.width );
		canvas.setAttribute( "height", source.height );

		var context = canvas.getContext( "2d" );
		context.drawImage( source, 0, 0, source.width, source.height );

		return context.getImageData( 0, 0, source.width, source.height );
	}

	function beginExtractionWorker()
	{

		worker = new Worker( 'tileset-extractor-worker.js' );
		worker.onmessage = function ( event )
		{
			var data = event.data;
			var action = data.action;

			if( action == "extract-start" )
			{
				progress.removeAttribute( "hidden" );
			}
			else if( action == "extract-progress" )
			{
				progress.value = Math.min( data.progress, 1.0 );
			}
			else if( action == "extract-result" )
			{
				progress.setAttribute( "hidden", "" );
				resultLayer.removeAttribute( "hidden" );

				map = data.map;
				tiles = data.tiles;

				log( "tiles " + tiles.length );
				log( "time " + data.time + "ms" );
				log( "complete" );

				showExtractedTiles();

				var numCols = (source.width / tileWidth) | 0;
				var numRows = (source.height / tileHeight) | 0;

				showTileset( numCols, numRows );

				downloadMapLink.href = window.URL.createObjectURL( new Blob( [JSON.stringify( {
							map: map,
							numCols: numCols,
							numRows: numRows
						} )], {type: 'text/plain'} )
				);
				downloadMapLink.download = "map.json";
			}
		};

		worker.postMessage( {
			action: "extract",
			tileWidth: tileWidthInput.value,
			tileHeight: tileHeightInput.value,
			tolerance: toleranceInput.value * 1024,
			imageData: extractSourceData( source )
		} );
	}

	function showExtractedTiles()
	{
		for( var i = 0, n = tiles.length; i < n; ++i )
		{
			var canvas = document.createElement( "canvas" );
			canvas.setAttribute( "width", tileWidth.toString() );
			canvas.setAttribute( "height", tileHeight.toString() );
			canvas.getContext( "2d" ).putImageData( tiles[i], 0, 0 );

			tilesLayer.appendChild( canvas );
		}

		downloadTilesLink.href = createTilesDataURL();
		downloadTilesLink.download = "tiles.png";
	}

	function showTileset( numCols, numRows )
	{
		var canvas = document.createElement( 'canvas' );
		canvas.setAttribute( "width", sourceWidth.toString() );
		canvas.setAttribute( "height", sourceHeight.toString() );

		var context = canvas.getContext( '2d' );

		var index = 0;
		for( var y = 0; y < numRows; ++y )
			for( var x = 0; x < numCols; ++x )
				context.putImageData( tiles[map[index++]], x * tileWidth, y * tileHeight );

		tilesetLayer.appendChild( canvas );

		downloadTileMapLink.href = canvas.toDataURL();
		downloadTileMapLink.download = "tilemap.png";
	}

	function createTilesDataURL()
	{
		var numTiles = tiles.length;
		var numRows = Math.sqrt( numTiles ) | 0;
		var numCols = Math.ceil( numTiles / numRows ) | 0;

		var canvas = document.createElement( "canvas" );
		canvas.setAttribute( "width", (numCols * tileWidth).toString() );
		canvas.setAttribute( "height", (numRows * tileHeight).toString() );

		var context = canvas.getContext( '2d' );

		for( var i = 0; i < numTiles; ++i )
		{
			var x = (i % numCols) * tileWidth;
			var y = ((i / numCols) | 0) * tileHeight;

			context.putImageData( tiles[i], x, y );
		}

		return canvas.toDataURL();
	}

	choose.addEventListener( "change", function ( e )
	{
		fullReset();

		var file = e.target.files[0];
		if( !file )
		{
			error( "No file selected." );
			return;
		}
		if( file.type != "image/png" && file.type != "image/gif" )
		{
			error( "Not a png or gif file." );
			return;
		}

		extract( URL.createObjectURL( file ) );
	} );

	loadDemoButton.addEventListener( "click", function ()
	{
		fullReset();
		extract( "tileset-extractor-demo.png" );
	} );

	loadDemoBigButton.addEventListener( "click", function ()
	{
		fullReset();
		extract( "tileset-extractor-demo-big.png" );
	} );

	toleranceInput.addEventListener( "change", function ()
	{
		if( null === source )
			return;

		reset();
		beginExtractionWorker();
	} );
}