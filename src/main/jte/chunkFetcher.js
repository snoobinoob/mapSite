const fetchChunk = async (chunkStr) => {
    const [chunkXStr, chunkYStr] = chunkStr.split(',');
    const chunkX = Number.parseInt(chunkXStr);
    const chunkY = Number.parseInt(chunkYStr);
    const minX = chunkX * window.mapsite.chunkSize;
    const maxX = minX + window.mapsite.chunkSize - 1;
    const minY = chunkY * window.mapsite.chunkSize;
    const maxY = minY + window.mapsite.chunkSize - 1;

    const result = await fetch(`/map?x=${minX},${maxX}&y=${minY},${maxY}`);
    if (result.ok) {
        assignChunkData({chunkX, chunkY, chunkData: await result.bytes()});
    } else {
        setTimeout(() => window.mapsite.chunksToFetch.add(chunkStr), 500);
    }
}

let intervalTimer = null;
const startFetcher = () => {
    clearInterval(intervalTimer);
    intervalTimer = setInterval(() => {
        if (window.mapsite.chunksToFetch.size > 0) {
            const chunkToFetch = [...window.mapsite.chunksToFetch][0];
            window.mapsite.chunksToFetch.delete(chunkToFetch);
            return fetchChunk(chunkToFetch);
        }
    }, window.mapsite.chunkFetchRateMs);
}
