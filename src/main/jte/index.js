window.mapsite = {
    chunks: {},
    chunksToFetch: new Set(),
    players: [],
    settlements: [],
    dragStartTile: null,
    dragDelta: {x: 0, y: 0},
    mousePos: {tile: null, canvas: null},
    drawPlayerNames: true,
};

const drawFullMap = () => {
    const canvas = document.getElementById('canvas');

    const {x: minTileX, y: minTileY} = canvasCoordsToTileCoords({canvasX: 0, canvasY: 0});
    const {x: maxTileX, y: maxTileY} = canvasCoordsToTileCoords({canvasX: canvas.width, canvasY: canvas.height});

    const ctx = canvas.getContext('2d');

    ctx.fillStyle = 'black';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    drawChunks({ctx, minTileX, minTileY, maxTileX, maxTileY});
    if (document.getElementById('settlement-layer').checked) {
        drawSettlements({ctx, minTileX, minTileY, maxTileX, maxTileY});
    }
    if (document.getElementById('player-layer').checked) {
        drawPlayers({ctx, minTileX, minTileY, maxTileX, maxTileY});
    }
    drawTooltip({ctx});

    requestAnimationFrame(drawFullMap);
}

const drawTooltip = ({ctx}) => {
    if (!window.mapsite.mousePos.canvas) {
        return;
    }

    if (document.getElementById('player-layer').checked && !window.mapsite.drawPlayerNames) {
        const {player, dist2} = window.mapsite.players.reduce((acc, player) => {
            const dist2 = distToMouse2({tileX: player.x, tileY: player.y});
            return dist2 > acc.dist2 ? acc : {player, dist2};
        }, {dist2: NaN});
        if (dist2 < 100) {
            drawPlayerName({ctx, player});
            return;
        }
    }

    if (document.getElementById('settlement-layer').checked) {
        const mouseTile = window.mapsite.mousePos.tile;
        for (const settlement of window.mapsite.settlements) {
            const {bounds} = settlement;
            if (bounds[0][0] <= mouseTile.x && bounds[0][1] <= mouseTile.y && mouseTile.x <= bounds[1][0] && mouseTile.y <= bounds[1][1]) {
                drawSettlementTooltip({ctx, settlement});
                return;
            }
        }
    }
}

const drawPlayerName = ({ctx, player}) => {
    ctx.font = '16px Courier New';
    const {width: textWidth} = ctx.measureText(player.name);
    const {x, y} = tileCoordsToCanvasCoords({tileX: player.x, tileY: player.y});
    ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
    ctx.fillRect(x - (textWidth / 2 + 2), y - 26, textWidth + 4, 16)
    ctx.fillStyle = 'black';
    ctx.fillText(player.name, x - (textWidth / 2), y - 12);
}

const drawSettlementTooltip = ({ctx, settlement}) => {
    const textLines = [
        {text: settlement.name, align: 1},
        {text: `Owner: ${settlement.owner}`, align: 0}
    ];
    ctx.font = '16px Courier New';
    const lines = textLines.map((line) => ({
        ...line,
        width: ctx.measureText(line.text).width,
    }));
    const maxWidth = lines.reduce((acc, {width}) => Math.max(acc, width), 0);
    const {x, y} = window.mapsite.mousePos.canvas;
    ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
    ctx.fillRect(x - maxWidth / 2 - 2, y - 4 - 16 * lines.length, maxWidth + 4, 16 * lines.length)
    ctx.fillStyle = 'black';
    lines.forEach((line, i) => {
        const startX = x - maxWidth / 2 + line.align / 2 * (maxWidth - line.width);
        ctx.fillText(line.text, startX, y - 8 + 16 * (i - 1));
    });
}

const distToMouse2 = ({tileX, tileY}) => {
    const {x, y} = tileCoordsToCanvasCoords({tileX, tileY});
    return (x - window.mapsite.mousePos.canvas?.x) ** 2 + (y - window.mapsite.mousePos.canvas?.y) ** 2;
}

const drawPlayers = ({ctx, minTileX, minTileY, maxTileX, maxTileY}) => {
    for (const player of window.mapsite.players) {
        if (player.levelID === 'surface' && minTileX <= player.x && player.x <= maxTileX && minTileY <= player.y && player.y <= maxTileY) {
            const {x, y} = tileCoordsToCanvasCoords({tileX: player.x, tileY: player.y});
            ctx.drawImage(window.mapsite.playerImage, x - 12, y - 12, 24, 24);
            if (window.mapsite.drawPlayerNames) {
                drawPlayerName({ctx, player});
            }
        }
    }
}

const drawSettlements = ({ctx, minTileX, minTileY, maxTileX, maxTileY}) => {
    for (const settlement of window.mapsite.settlements) {
        const {bounds} = settlement;
        if (bounds[0][0] > maxTileX || bounds[0][1] > maxTileY || bounds[1][0] < minTileX || bounds[1][1] < minTileY) {
            break;
        }

        const startCoords = tileCoordsToCanvasCoords({tileX: bounds[0][0], tileY: bounds[0][1]});
        const endCoords = tileCoordsToCanvasCoords({tileX: bounds[1][0], tileY: bounds[1][1]});
        const width = endCoords.x - startCoords.x + window.mapsite.pixelsPerTile;
        const height = endCoords.y - startCoords.y + window.mapsite.pixelsPerTile;

        ctx.fillStyle = 'rgba(80, 255, 150, 0.2)';
        ctx.fillRect(startCoords.x, startCoords.y, width, height);
        ctx.strokeStyle = 'rgba(80, 255, 150, 0.6)';
        ctx.strokeRect(startCoords.x, startCoords.y, width, height);
    }
}

const drawChunks = ({ctx, minTileX, minTileY, maxTileX, maxTileY}) => {
    const chunkBounds = {
        minX: Math.floor(minTileX / window.mapsite.chunkSize),
        minY: Math.floor(minTileY / window.mapsite.chunkSize),
        maxX: Math.floor(maxTileX / window.mapsite.chunkSize),
        maxY: Math.floor(maxTileY / window.mapsite.chunkSize),
    }
    for (let chunkY = chunkBounds.minY; chunkY <= chunkBounds.maxY; chunkY++) {
        for (let chunkX = chunkBounds.minX; chunkX <= chunkBounds.maxX; chunkX++) {
            drawChunk({ctx, chunkX, chunkY});
        }
    }
}

const drawChunk = ({ctx, chunkX, chunkY}) => {
    const {y: offsetY, x: offsetX} = tileCoordsToCanvasCoords({
        tileX: chunkX * window.mapsite.chunkSize,
        tileY: chunkY * window.mapsite.chunkSize
    });
    const chunkData = window.mapsite.chunks[`${chunkX},${chunkY}`];
    const chunkSizeInPixels = window.mapsite.pixelsPerTile * window.mapsite.chunkSize;
    if (!chunkData || chunkData === 'PENDING') {
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

    if (chunkData[0] === 0) {
        ctx.fillStyle = 'black';
        ctx.fillRect(0, 0, chunkCanvas.width, chunkCanvas.height);
    } else {
        const imageData = ctx.createImageData(chunkCanvas.width, chunkCanvas.height);
        for (let y = 0; y < chunkCanvas.height; y++) {
            for (let x = 0; x < chunkCanvas.width; x++) {
                const imageIndex = 4 * (y * chunkCanvas.width + x);
                const dataIndex = 1 + 3 * (y * chunkCanvas.width + x);
                imageData.data[imageIndex] = chunkData[dataIndex];
                imageData.data[imageIndex + 1] = chunkData[dataIndex + 1];
                imageData.data[imageIndex + 2] = chunkData[dataIndex + 2];
                imageData.data[imageIndex + 3] = 255;
            }
        }
        ctx.putImageData(imageData, 0, 0);
    }
    window.mapsite.chunks[`${chunkX},${chunkY}`] = chunkCanvas;
    drawChunk({chunkX, chunkY, ctx: document.getElementById('canvas').getContext('2d')});
}

const loadSettlements = () => {
    fetch('/settlements')
        .then((res) => res.json())
        .then((settlementData) => window.mapsite.settlements = settlementData);
}

const loadMissingChunks = () => {
    const canvas = document.getElementById('canvas');
    const minTileCoords = canvasCoordsToTileCoords({canvasX: 0, canvasY: 0});
    const maxTileCoords = canvasCoordsToTileCoords({canvasX: canvas.width, canvasY: canvas.height});
    const minChunkX = Math.floor(minTileCoords.x / window.mapsite.chunkSize);
    const minChunkY = Math.floor(minTileCoords.y / window.mapsite.chunkSize);
    const maxChunkX = Math.floor(maxTileCoords.x / window.mapsite.chunkSize);
    const maxChunkY = Math.floor(maxTileCoords.y / window.mapsite.chunkSize);
    for (let chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
        for (let chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            const chunkKey = `${chunkX},${chunkY}`;
            if (!window.mapsite.chunks[chunkKey]) {
                window.mapsite.chunks[chunkKey] = 'PENDING';
                window.mapsite.chunksToFetch.add(chunkKey);
            }
        }
    }
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
    const hashComponents = [];
    const isSpawn = x === window.mapsite.spawn.x && y === window.mapsite.spawn.y;
    if (!isSpawn) {
        hashComponents.push(`x:${x},y:${y}`);
    }
    const isDefaultZoom = window.mapsite.pixelsPerTile === 4;
    if (!isDefaultZoom) {
        hashComponents.push(`z:${window.mapsite.pixelsPerTile}`);
    }
    const hashStr = hashComponents.length > 0 ? '#' + hashComponents.join(',') : '';
    const newUrl = window.location.href.replace(/#.*/, '') + hashStr;
    window.history.replaceState(null, '', newUrl);
}

const goToStartingLocation = () => {
    const zoomMatch = window.location.hash.match(/z:(\d+)/);
    if (zoomMatch) {
        const pixelsPerTile = Number.parseInt(zoomMatch[1]);
        if ([1, 2, 4, 8, 16, 32].includes(pixelsPerTile)) {
            window.mapsite.pixelsPerTile = pixelsPerTile;
        }
    }
    const posMatch = window.location.hash.match(/x:(-?\d+),y:(-?\d+)/);
    if (posMatch) {
        goToLocation({
            x: Number.parseInt(posMatch[1]),
            y: Number.parseInt(posMatch[2]),
        });
    } else {
        goToLocation(window.mapsite.spawn);
    }
}

const goToLocation = ({x, y}) => {
    window.mapsite.centerCoords = {x, y};
    updateUrl();
    loadMissingChunks();
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
    loadMissingChunks();
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
}

const updateMousePos = ({x, y}) => {
    if (window.mapsite.dragStartTile) {
        return;
    }
    window.mapsite.mousePos = {
        tile: canvasCoordsToTileCoords({canvasX: x, canvasY: y}),
        canvas: {x, y},
    };
    document.getElementById('mouse-position').innerText = `Mouse position: (${window.mapsite.mousePos.tile.x}, ${window.mapsite.mousePos.tile.y})`;
}

const clearMousePos = () => {
    window.mapsite.mousePos = {tile: null, canvas: null};
}

const zoom = (amount) => {
    const levels = [1, 2, 4, 8, 16, 32];
    let currLevel = levels.indexOf(window.mapsite.pixelsPerTile);
    if (currLevel === -1) {
        currLevel = 2;
    }
    const newLevel = Math.min(Math.max(0, currLevel + amount), levels.length - 1);
    window.mapsite.pixelsPerTile = levels[newLevel];
    updateUrl();
    loadMissingChunks();
}

const copyLink = () => {
    return navigator.clipboard.writeText(window.location.href);
}

const assignCanvasMouseListeners = () => {
    const canvas = document.getElementById('canvas');
    canvas.addEventListener('mousedown', canvasDragStart);
    canvas.addEventListener('mouseup', canvasDragStop);
    canvas.addEventListener('mouseleave', canvasDragStop);
    canvas.addEventListener('mousemove', canvasDrag);
    canvas.addEventListener('mousemove', updateMousePos);
    canvas.addEventListener('mouseleave', clearMousePos);
}

window.mapsite.playerImage = new Image();
window.mapsite.playerImage.src = 'player.png';

addEventListener('load', () => {
    resizeCanvas();
    addEventListener('resize', () => {
        resizeCanvas();
        loadMissingChunks();
    });
    assignCanvasMouseListeners();
    connectWebSocket();
    startFetcher();
    loadSettlements();
    goToStartingLocation();
    requestAnimationFrame(drawFullMap);
});
