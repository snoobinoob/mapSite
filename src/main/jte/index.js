window.mapsite = {
    zoom: 4,
    chunkSize: 64,
    chunks: {},
    chunksToFetch: new Set(),
    millisPerChunkFetch: 500,
    dragStartTile: null,
    dragDelta: {x: 0, y: 0},
};

const drawMapData = () => {
    const canvas = document.getElementById('canvas');

    const {x: minTileX, y: minTileY} = canvasCoordsToTileCoords({canvasX: 0, canvasY: 0});
    const {x: maxTileX, y: maxTileY} = canvasCoordsToTileCoords({canvasX: canvas.width, canvasY: canvas.height});
    const chunkBounds = {
        minX: Math.floor(minTileX / window.mapsite.chunkSize),
        minY: Math.floor(minTileY / window.mapsite.chunkSize),
        maxX: Math.floor(maxTileX / window.mapsite.chunkSize),
        maxY: Math.floor(maxTileY / window.mapsite.chunkSize),
    }

    const ctx = canvas.getContext('2d');
    ctx.fillStyle = 'black';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    for (let chunkY = chunkBounds.minY; chunkY <= chunkBounds.maxY; chunkY++) {
        for (let chunkX = chunkBounds.minX; chunkX <= chunkBounds.maxX; chunkX++) {
            drawChunk({chunkX, chunkY, ctx});
        }
    }
}

const canvasCoordsToTileCoords = ({canvasX, canvasY}) => {
    const pixelsPerTile = 32 / window.mapsite.zoom;
    const canvas = document.getElementById('canvas');

    const x = window.mapsite.centerCoords.x + Math.floor((canvasX - (canvas.width / 2)) / pixelsPerTile);
    const y = window.mapsite.centerCoords.y + Math.floor((canvasY - (canvas.height / 2)) / pixelsPerTile);

    return {x, y};
}

const tileCoordsToCanvasCoords = ({tileX, tileY}) => {
    const pixelsPerTile = 32 / window.mapsite.zoom;
    const canvas = document.getElementById('canvas');

    const x = Math.floor(canvas.width / 2) + (tileX - window.mapsite.centerCoords.x) * pixelsPerTile;
    const y = Math.floor(canvas.height / 2) + (tileY - window.mapsite.centerCoords.y) * pixelsPerTile;

    return {x, y};
}

const drawChunk = ({chunkX, chunkY, ctx}) => {
    const {y: offsetY, x: offsetX} = tileCoordsToCanvasCoords({
        tileX: chunkX * window.mapsite.chunkSize,
        tileY: chunkY * window.mapsite.chunkSize
    });
    const pixelsPerTile = 32 / window.mapsite.zoom;
    const chunkData = window.mapsite.chunks[`${chunkX},${chunkY}`];
    if (!chunkData || chunkData === 'PENDING') {
        if (!chunkData) {
            window.mapsite.chunks[`${chunkX},${chunkY}`] = 'PENDING';
            window.mapsite.chunksToFetch.add(`${chunkX},${chunkY}`);
        }
        ctx.fillStyle = 'lightgrey';
        ctx.fillRect(offsetX, offsetY, window.mapsite.chunkSize * pixelsPerTile, window.mapsite.chunkSize * pixelsPerTile);
        return;
    }
    for (let tileY = 0; tileY < chunkData.length; tileY++) {
        for (let tileX = 0; tileX < chunkData[tileY].length; tileX++) {
            ctx.fillStyle = toColor(chunkData[tileY][tileX]);
            ctx.fillRect(offsetX + tileX * pixelsPerTile, offsetY + tileY * pixelsPerTile, pixelsPerTile, pixelsPerTile);
        }
    }
}

const toColor = (rgbInt) => {
    rgbInt >>>= 0;
    const b = rgbInt & 0xFF,
        g = (rgbInt & 0xFF00) >>> 8,
        r = (rgbInt & 0xFF0000) >>> 16,
        a = ((rgbInt & 0xFF000000) >>> 24) / 255;
    return 'rgba(' + [r, g, b, a].join(',') + ')';
}

const resizeCanvas = () => {
    const canvas = document.getElementById('canvas');
    canvas.height = canvas.offsetHeight;
    canvas.width = canvas.offsetWidth;
}

const goToSpawn = () => {
    window.mapsite.centerCoords = {
        x: window.mapsite.spawn.tileX,
        y: window.mapsite.spawn.tileY,
    };
    drawMapData();
}

const canvasDragStart = ({x, y}) => {
    window.mapsite.dragStartTile = canvasCoordsToTileCoords({canvasX: x, canvasY: y});
    window.mapsite.dragDelta = {
        x: window.mapsite.dragStartTile.x - window.mapsite.centerCoords.x,
        y: window.mapsite.dragStartTile.y - window.mapsite.centerCoords.y,
    };
    window.shouldProcessQueue = false;
}

let canvasDragTimer;
const canvasDragStop = () => {
    window.mapsite.dragStartTile = null;
    if (canvasDragTimer !== null) {
        clearTimeout(canvasDragTimer);
        window.shouldProcessQueue = true;
    }
}

const canvasDrag = ({movementX, movementY}) => {
    if (window.mapsite.dragStartTile === null) {
        return;
    }
    window.shouldProcessQueue = false;

    const pixelsPerTile = 32 / window.mapsite.zoom;
    window.mapsite.dragDelta.x += movementX / pixelsPerTile;
    window.mapsite.dragDelta.y += movementY / pixelsPerTile;

    window.mapsite.centerCoords = {
        x: Math.round(window.mapsite.dragStartTile.x - window.mapsite.dragDelta.x),
        y: Math.round(window.mapsite.dragStartTile.y - window.mapsite.dragDelta.y),
    };
    drawMapData();
    canvasDragTimer = setTimeout(() => window.shouldProcessQueue = true, 500);
}

const assignCanvasMouseListeners = () => {
    const canvas = document.getElementById("canvas");
    canvas.addEventListener("mousedown", canvasDragStart);
    canvas.addEventListener("mouseup", canvasDragStop);
    canvas.addEventListener("mouseleave", canvasDragStop);
    canvas.addEventListener("mousemove", canvasDrag);
}

addEventListener('load', () => {
    resizeCanvas();
    assignCanvasMouseListeners();
    startFetcher();
    goToSpawn();
});