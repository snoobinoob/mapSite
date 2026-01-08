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
    } catch (err) {
        console.error(err);
    }
}