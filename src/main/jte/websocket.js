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
            window.mapsite.players = messageJson.players;
            document.getElementById('player-label').innerText = `Online players (${window.mapsite.players.length}/${window.mapsite.maxPlayers})`;
            const playerList = document.getElementById('player-list');
            const playerNodes = window.mapsite.players.map(createPlayerListElement);
            playerList.replaceChildren(...playerNodes);
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
        }
        if (messageJson.chunkUpdates !== void 0) {
            messageJson.chunkUpdates.forEach(([chunkX, chunkY]) => {
                const chunkKey = `${chunkX},${chunkY}`;
                if (window.mapsite.chunks[chunkKey]) {
                    window.mapsite.chunksToFetch.add(chunkKey);
                }
            })
        }
    } catch (err) {
        console.error(err);
    }
}

const createPlayerListElement = ({name, x, y}) => {
    const outerDiv = document.createElement('div');
    const innerButton = document.createElement('button');
    innerButton.className = 'button player-button';
    innerButton.innerText = name;
    innerButton.onclick = () => goToLocation({x: Math.round(x), y: Math.round(y)});
    outerDiv.appendChild(innerButton);
    return outerDiv;
}
