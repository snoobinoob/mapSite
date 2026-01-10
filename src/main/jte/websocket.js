const connectWebSocket = () => {
    const ws = new WebSocket(`ws://${window.location.host}/ws`);
    ws.addEventListener('connect', () => console.log('Websocket connected'));
    ws.addEventListener('close', () => console.log('Websocket closed'));
    ws.addEventListener('message', ({data}) => handleMessage(data));

    window.mapsite.websocket = ws;
};

const handleMessage = (message) => {
    try {
        const messageJson = JSON.parse(message);
        if (messageJson.players !== void 0) {
            messageJson.players.forEach((playerUpdate) => {
                const player = window.mapsite.players.find(({name}) => name === playerUpdate.name);
                if (player) {
                    Object.assign(player, playerUpdate);
                } else {
                    window.mapsite.players.push(playerUpdate);
                }
            });
            drawFullMap(false);
        }
        if (messageJson.mapUpdates !== void 0) {
            messageJson.mapUpdates.forEach(([tileX, tileY, rgbInt]) => {
                const chunkX = Math.floor(tileX / window.mapsite.chunkSize);
                const chunkY = Math.floor(tileY / window.mapsite.chunkSize);
                const chunkData = window.mapsite.chunks[`${chunkX},${chunkY}`];
                if (chunkData?.tagName === 'CANVAS') {
                    const {r, g, b} = toColor(rgbInt);
                    const ctx = chunkData.getContext('2d');
                    const offsetX = tileX - chunkX * window.mapsite.chunkSize;
                    const offsetY = tileY - chunkY * window.mapsite.chunkSize;
                    ctx.fillStyle = `rgb(${r}, ${g}, ${b})`;
                    ctx.fillRect(offsetX, offsetY, 1, 1);
                }
            });
            drawFullMap(false);
        }
    } catch (err) {
        console.error(err);
    }
}