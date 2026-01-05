const fetchChunk = async (chunkStr) => {
    const [chunkXStr, chunkYStr] = chunkStr.split(',');
    const chunkX = Number.parseInt(chunkXStr);
    const chunkY = Number.parseInt(chunkYStr);
    const minX = chunkX * window.mapsite.chunkSize;
    const maxX = minX + window.mapsite.chunkSize - 1;
    const minY = chunkY * window.mapsite.chunkSize;
    const maxY = minY + window.mapsite.chunkSize - 1;

    const result = await fetch(`/map?x=${minX},${maxX}&y=${minY},${maxY}`);
    window.mapsite.chunks[chunkStr] = await result.json();
    drawChunk({chunkX, chunkY, ctx: document.getElementById('canvas').getContext('2d')})
}

window.shouldProcessQueue = true;
const startFetcher = async () => {
    while (true) {
        if (window.shouldProcessQueue && window.mapsite.chunksToFetch.size > 0) {
            const chunkToFetch = [...window.mapsite.chunksToFetch][0];
            window.mapsite.chunksToFetch.delete(chunkToFetch);
            await fetchChunk(chunkToFetch)
        }
        await new Promise((resolve) => setTimeout(resolve, window.mapsite.millisPerChunkFetch));
    }
}