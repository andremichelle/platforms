function onLoad()
{
	var choose = document.querySelector( 'input[type="file"]' );
	var loadDemoButton = document.querySelector( 'button[demo]' );
	var loadDemoBigButton = document.querySelector( 'button[demo-big]' );
	var tileWidthInput = document.querySelector( 'input[name="tile-width"]' );
	var tileHeightInput = document.querySelector( 'input[name="tile-height"]' );
	var toleranceInput = document.querySelector( 'input[name="tolerance"]' );
	var progress = document.querySelector( 'progress' );
	var consoleLayer = document.querySelector( 'div[console]' );
	var tilesLayer = document.querySelector( 'div[tiles]' );
	var tilesetLayer = document.querySelector( 'div[tileset]' );
	var resultLayer = document.querySelector( 'div[result]' );

	var downloadMapLink = document.querySelector( "a[download-map]" );
	var downloadTilesLink = document.querySelector( "a[download-tiles]" );
	var downloadTileMapLink = document.querySelector( "a[download-tilemap]" );
	var downloadTiledTMXLink = document.querySelector( "a[download-tmx]" );

	var map = null;
	var tiles = null;
	var source = null;
	var worker = null;
	var sourceWidth = 0;
	var sourceHeight = 0;
	var numCols = 0;
	var numRows = 0;
	var tileWidth = 0;
	var tileHeight = 0;
	var extractedTilesWidth = 0;
	var extractedTilesHeight = 0;

	function reset()
	{
		if( worker )
		{
			worker.terminate();
			worker = null;
		}

		map = null;
		tiles = null;

		consoleLayer.innerHTML = "";
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
		numCols = 0;
		numRows = 0;
		tileWidth = 0;
		tileHeight = 0;
		extractedTilesWidth = 0;
		extractedTilesHeight = 0;
	}

	function log( header, content )
	{
		var line = document.createElement( "p" );
		line.setAttribute( "fine", "" );

		var spanHeader = document.createElement( "span" );
		spanHeader.textContent = header;
		var spanContent = document.createElement( "span" );
		spanContent.textContent = content;

		line.appendChild( spanHeader );
		line.appendChild( spanContent );

		consoleLayer.appendChild( line );
	}

	function error( msg )
	{
		var line = document.createElement( "p" );
		line.setAttribute( "error", "" );
		line.textContent = msg;

		consoleLayer.appendChild( line );
	}

	function checkSourceSize()
	{
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

			numCols = sourceWidth / tileWidth;
			numRows = sourceHeight / tileHeight;

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

	function exportTiledFormat()
	{
		var xmlMap = document.createElement( "map" );
		xmlMap.setAttribute( "version", "1.0" );
		xmlMap.setAttribute( "orientation", "orthogonal" );
		xmlMap.setAttribute( "renderorder", "right-down" );
		xmlMap.setAttribute( "width", numCols );
		xmlMap.setAttribute( "height", numRows );
		xmlMap.setAttribute( "tilewidth", tileWidth );
		xmlMap.setAttribute( "tileheight", tileHeight );
		xmlMap.setAttribute( "nextobjectid", "1" );

		var xmlTileSet = document.createElement( "tileset" );
		xmlTileSet.setAttribute( "firstgid", "1" );
		xmlTileSet.setAttribute( "name", "tiles" );
		xmlTileSet.setAttribute( "tilewidth", tileWidth );
		xmlTileSet.setAttribute( "tileheight", tileHeight );
		var xmlImage = document.createElement( "image" );
		xmlImage.setAttribute( "source", "tiles.png" );
		xmlImage.setAttribute( "width", extractedTilesWidth );
		xmlImage.setAttribute( "height", extractedTilesHeight );
		xmlTileSet.appendChild( xmlImage );
		xmlMap.appendChild( xmlTileSet );

		console.log( extractedTilesWidth, extractedTilesHeight  );

		var xmlLayer = document.createElement( "layer" );
		xmlLayer.setAttribute( "name", "layer" );
		xmlLayer.setAttribute( "width", numCols );
		xmlLayer.setAttribute( "height", numRows );
		var xmlData = document.createElement( "data" );
		for( var i = 0, n = map.length; i < n; ++i )
		{
			var xmlTile = document.createElement( "tile" );
			xmlTile.setAttribute( "gid", map[i] + 1 );
			xmlData.appendChild( xmlTile );
		}
		xmlLayer.appendChild( xmlData );
		xmlMap.appendChild( xmlLayer );

		return '<?xml version="1.0" encoding="UTF-8"?>\n' + new XMLSerializer().serializeToString( xmlMap );
	}

	function beginExtractionWorker()
	{
		log( "Size:", sourceWidth + " x " + sourceHeight + "px" );
		log( "Map:", numCols + " x " + numRows );

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

				log( "Tiles:", tiles.length );
				log( "Time:", data.time + "ms" );

				showExtractedTiles();

				showTileset();

				downloadMapLink.download = "map.json";
				downloadMapLink.href = window.URL.createObjectURL( new Blob( [JSON.stringify( {
					map: map,
					numCols: numCols,
					numRows: numRows
				} )], {type: 'text/plain'} ) );

				downloadTiledTMXLink.download = "tiled.tmx";
				downloadTiledTMXLink.href = window.URL.createObjectURL( new Blob( [exportTiledFormat()], {type: 'text/xml'} ) );
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

	function showTileset()
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
		// Try to get as squared as possible
		//
		var numTiles = tiles.length;
		var numRows = Math.sqrt( numTiles ) | 0;
		var numCols = Math.ceil( numTiles / numRows ) | 0;

		extractedTilesWidth = numCols * tileWidth;
		extractedTilesHeight = numRows * tileHeight;

		var canvas = document.createElement( "canvas" );
		canvas.setAttribute( "width", (extractedTilesWidth).toString() );
		canvas.setAttribute( "height", (extractedTilesHeight).toString() );

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