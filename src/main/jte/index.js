window.mapsite = {
    zoom: 4,
    chunkSize: 64,
    chunks: {},
    chunksToFetch: new Set(),
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
    for (let chunkY = chunkBounds.minY; chunkY <= chunkBounds.maxY; chunkY++) {
        for (let chunkX = chunkBounds.minX; chunkX <= chunkBounds.maxX; chunkX++) {
            drawChunk({chunkX, chunkY, ctx});
        }
    }
}

const canvasCoordsToTileCoords = ({canvasX, canvasY}) => {
    const pixelsPerTile = 32 / window.mapsite.zoom;
    const canvas = document.getElementById('canvas');
    const centerX = Number.parseInt(document.getElementById('centerX').value);
    const centerY = Number.parseInt(document.getElementById('centerY').value);

    const x = centerX + Math.floor((canvasX - (canvas.width / 2)) / pixelsPerTile);
    const y = centerY + Math.floor((canvasY - (canvas.height / 2)) / pixelsPerTile);

    return {x, y};
}

const tileCoordsToCanvasCoords = ({tileX, tileY}) => {
    const pixelsPerTile = 32 / window.mapsite.zoom;
    const canvas = document.getElementById('canvas');
    const centerX = Number.parseInt(document.getElementById('centerX').value);
    const centerY = Number.parseInt(document.getElementById('centerY').value);

    const x = Math.floor(canvas.width / 2) + (tileX - centerX) * pixelsPerTile;
    const y = Math.floor(canvas.height / 2) + (tileY - centerY) * pixelsPerTile;

    return {x, y};
}

const drawChunk = ({chunkX, chunkY, ctx}) => {
    const {y: offsetY, x: offsetX} = tileCoordsToCanvasCoords({
        tileX: chunkX * window.mapsite.chunkSize,
        tileY: chunkY * window.mapsite.chunkSize
    });
    const pixelsPerTile = 32 / window.mapsite.zoom;
    const chunkData = window.mapsite.chunks[`${chunkX},${chunkY}`];
    if (!chunkData) {
        window.mapsite.chunks[`${chunkX},${chunkY}`] = 'PENDING';
        window.mapsite.chunksToFetch.add(`${chunkX},${chunkY}`);
        ctx.fillStyle = 'rgb(128,128,128)';
        ctx.fillRect(offsetX, offsetY, window.mapsite.chunkSize * pixelsPerTile, window.mapsite.chunkSize * pixelsPerTile);
        return;
    }
    if (chunkData === 'PENDING') {
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

addEventListener('load', () => {
    resizeCanvas();
    drawMapData();
    run();
});