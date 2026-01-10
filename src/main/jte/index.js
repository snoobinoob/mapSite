window.mapsite = {
    chunks: {},
    chunksToFetch: new Set(),
    players: [],
    dragStartTile: null,
    dragDelta: {x: 0, y: 0},
    mouseTile: null,
};

const drawFullMap = (loadMissingRegions) => {
    const canvas = document.getElementById('canvas');

    const {x: minTileX, y: minTileY} = canvasCoordsToTileCoords({canvasX: 0, canvasY: 0});
    const {x: maxTileX, y: maxTileY} = canvasCoordsToTileCoords({canvasX: canvas.width, canvasY: canvas.height});

    const ctx = canvas.getContext('2d');

    ctx.fillStyle = 'black';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    drawChunks({ctx, minTileX, minTileY, maxTileX, maxTileY, loadMissingRegions});
    drawPlayers({ctx, minTileX, minTileY, maxTileX, maxTileY});
}

const drawPlayers = ({ctx, minTileX, minTileY, maxTileX, maxTileY}) => {
    for (const player of window.mapsite.players) {
        if (minTileX <= player.x && player.x <= maxTileX && minTileY <= player.y && player.y <= maxTileY) {
            const {x, y} = tileCoordsToCanvasCoords({tileX: player.x, tileY: player.y});
            ctx.fillStyle = 'lightblue';
            ctx.beginPath();
            ctx.arc(x, y, 8, 0, 2 * Math.PI);
            ctx.fill();

            ctx.font = '16px Courier New';
            const {width: textWidth} = ctx.measureText(player.name);
            ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
            ctx.fillRect(x - (textWidth / 2 + 2), y - 26, textWidth + 4, 16)
            ctx.fillStyle = 'black';
            ctx.fillText(player.name, x - (textWidth / 2), y - 12);
        }
    }
}

const drawChunks = ({ctx, minTileX, minTileY, maxTileX, maxTileY, loadMissingRegions}) => {
    const chunkBounds = {
        minX: Math.floor(minTileX / window.mapsite.chunkSize),
        minY: Math.floor(minTileY / window.mapsite.chunkSize),
        maxX: Math.floor(maxTileX / window.mapsite.chunkSize),
        maxY: Math.floor(maxTileY / window.mapsite.chunkSize),
    }
    for (let chunkY = chunkBounds.minY; chunkY <= chunkBounds.maxY; chunkY++) {
        for (let chunkX = chunkBounds.minX; chunkX <= chunkBounds.maxX; chunkX++) {
            drawChunk({chunkX, chunkY, ctx, loadMissingRegions});
        }
    }
}

const drawChunk = ({chunkX, chunkY, ctx, loadMissingRegions}) => {
    const {y: offsetY, x: offsetX} = tileCoordsToCanvasCoords({
        tileX: chunkX * window.mapsite.chunkSize,
        tileY: chunkY * window.mapsite.chunkSize
    });
    const chunkData = window.mapsite.chunks[`${chunkX},${chunkY}`];
    const chunkSizeInPixels = window.mapsite.pixelsPerTile * window.mapsite.chunkSize;
    if (!chunkData || chunkData === 'PENDING') {
        if (!chunkData && loadMissingRegions) {
            window.mapsite.chunks[`${chunkX},${chunkY}`] = 'PENDING';
            window.mapsite.chunksToFetch.add(`${chunkX},${chunkY}`);
        }
        ctx.fillStyle = 'lightgrey';
        ctx.fillRect(offsetX, offsetY, chunkSizeInPixels, chunkSizeInPixels);
        return;
    }
    ctx.imageSmoothingEnabled = false;
    ctx.drawImage(chunkData, offsetX, offsetY, chunkSizeInPixels, chunkSizeInPixels);
}

const assignChunkData = ({chunkX, chunkY, chunkData}) => {
    const chunkCanvas = document.createElement('canvas');
    const ctx = chunkCanvas.getContext('2d');

    chunkCanvas.height = window.mapsite.chunkSize;
    chunkCanvas.width = window.mapsite.chunkSize;

    const imageData = ctx.createImageData(chunkCanvas.width, chunkCanvas.height);
    for (let y = 0; y < chunkCanvas.height; y++) {
        for (let x = 0; x < chunkCanvas.width; x++) {
            const index = 4 * (y * chunkCanvas.width + x);
            const {r, g, b, a} = toColor(chunkData[y][x]);
            imageData.data[index] = r;
            imageData.data[index + 1] = g;
            imageData.data[index + 2] = b;
            imageData.data[index + 3] = a;
        }
    }
    ctx.putImageData(imageData, 0, 0);
    window.mapsite.chunks[`${chunkX},${chunkY}`] = chunkCanvas;
    drawChunk({chunkX, chunkY, ctx: document.getElementById('canvas').getContext('2d')});
}

const canvasCoordsToTileCoords = ({canvasX, canvasY}) => {
    const canvas = document.getElementById('canvas');

    const x = window.mapsite.centerCoords.x + Math.floor((canvasX - (canvas.width / 2)) / window.mapsite.pixelsPerTile);
    const y = window.mapsite.centerCoords.y + Math.floor((canvasY - (canvas.height / 2)) / window.mapsite.pixelsPerTile);

    return {x, y};
}

const tileCoordsToCanvasCoords = ({tileX, tileY}) => {
    const canvas = document.getElementById('canvas');

    const x = Math.floor(canvas.width / 2) + (tileX - window.mapsite.centerCoords.x) * window.mapsite.pixelsPerTile;
    const y = Math.floor(canvas.height / 2) + (tileY - window.mapsite.centerCoords.y) * window.mapsite.pixelsPerTile;

    return {x, y};
}

const toColor = (rgbInt) => {
    // rgbInt >>>= 0;
    const b = rgbInt & 0xFF;
    const g = (rgbInt & 0xFF00) >>> 8;
    const r = (rgbInt & 0xFF0000) >>> 16;
    const a = ((rgbInt & 0xFF000000) >>> 24);
    return {r, g, b, a};
}

const resizeCanvas = () => {
    const canvas = document.getElementById('canvas');
    canvas.height = canvas.offsetHeight;
    canvas.width = canvas.offsetWidth;
}

const updateUrl = () => {
    const {x, y} = window.mapsite.centerCoords;
    const isSpawn = x === window.mapsite.spawn.x && y === window.mapsite.spawn.y;
    const hashStr = isSpawn ? '' : `#x:${x},y:${y}`;
    const newUrl = window.location.href.replace(window.location.hash, '') + hashStr;
    window.history.replaceState(null, '', newUrl);
}

const goToStartingLocation = () => {
    const posMatch = window.location.hash.match(/x:(-?\d+),y:(-?\d+)/);
    if (posMatch) {
        window.mapsite.centerCoords = {
            x: Number.parseInt(posMatch[1]),
            y: Number.parseInt(posMatch[2]),
        }
    } else {
        window.mapsite.centerCoords = {
            x: window.mapsite.spawn.x,
            y: window.mapsite.spawn.y,
        }
    }
    drawFullMap(true);
}

const goToLocation = ({x, y}) => {
    window.mapsite.centerCoords = {x, y};
    updateUrl();
    drawFullMap(true);
}

const canvasDragStart = ({x, y}) => {
    window.mapsite.dragStartTile = canvasCoordsToTileCoords({canvasX: x, canvasY: y});
    window.mapsite.dragDelta = {
        x: window.mapsite.dragStartTile.x - window.mapsite.centerCoords.x,
        y: window.mapsite.dragStartTile.y - window.mapsite.centerCoords.y,
    };
}

const canvasDragStop = () => {
    if (window.mapsite.dragStartTile === null) {
        return;
    }

    window.mapsite.dragStartTile = null;
    window.mapsite.centerCoords = {
        x: Math.round(window.mapsite.centerCoords.x),
        y: Math.round(window.mapsite.centerCoords.y),
    };

    updateUrl();
    drawFullMap(true);
}

const canvasDrag = ({movementX, movementY}) => {
    if (window.mapsite.dragStartTile === null) {
        return;
    }

    window.mapsite.dragDelta.x += movementX / window.mapsite.pixelsPerTile;
    window.mapsite.dragDelta.y += movementY / window.mapsite.pixelsPerTile;

    window.mapsite.centerCoords = {
        x: window.mapsite.dragStartTile.x - window.mapsite.dragDelta.x,
        y: window.mapsite.dragStartTile.y - window.mapsite.dragDelta.y,
    };
    drawFullMap(false);
}

const updateMouseTile = ({x, y}) => {
    if (window.mapsite.dragStartTile) {
        return;
    }
    window.mapsite.mouseTile = canvasCoordsToTileCoords({canvasX: x, canvasY: y});
    document.getElementById('pointer-location').innerText = `(${window.mapsite.mouseTile.x}, ${window.mapsite.mouseTile.y})`;
}

const assignCanvasMouseListeners = () => {
    const canvas = document.getElementById('canvas');
    canvas.addEventListener('mousedown', canvasDragStart);
    canvas.addEventListener('mouseup', canvasDragStop);
    canvas.addEventListener('mouseleave', canvasDragStop);
    canvas.addEventListener('mousemove', canvasDrag);
    canvas.addEventListener('mousemove', updateMouseTile);
}

addEventListener('load', () => {
    resizeCanvas();
    addEventListener('resize', () => {
        resizeCanvas();
        drawFullMap(true);
    });
    assignCanvasMouseListeners();
    connectWebSocket();
    startFetcher();
    goToStartingLocation();
});
