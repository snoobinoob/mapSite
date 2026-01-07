const connectWebSocket = () => {
    const ws = new WebSocket(`ws://${window.location.host}/ws`);
    ws.addEventListener('connect', () => console.log('Websocket connected'));
    ws.addEventListener('close', () => console.log('Websocket closed'));
    ws.addEventListener('message', ({data}) => handleMessage(data));

    window.mapsite.websocket = ws;
};

const handleMessage = (message) => {
    console.log(`Received message: '${message}'`);
}