const fetchChunk = async (chunkStr) => {
    const [chunkXStr, chunkYStr] = chunkStr.split(',');
    const chunkX = Number.parseInt(chunkXStr);
    const chunkY = Number.parseInt(chunkYStr);

    const result = await fetch(`/map?x=${chunkX}&y=${chunkY}`);
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
