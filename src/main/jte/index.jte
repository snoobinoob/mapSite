@import mapsite.webmodel.ServerInfoModel
@import mapsite.Settings

@param ServerInfoModel model
<head>
    <title>MapSite</title>
    <link rel="stylesheet" href="styles.css">
    <script src="index.js"></script>
    <script src="chunkFetcher.js"></script>
    <script src="websocket.js"></script>
</head>
<body>
<canvas id="canvas" class="back-canvas"></canvas>
<div class="overlay">
    <div>
        <p>Mouse Position: <strong id="pointer-location">-</strong></p>
    </div>
    <button onclick="goToLocation(window.mapsite.spawn)">Go to Spawn</button>
</div>
<script>
    window.mapsite = {
        ...window.mapsite,
        spawn: {
            x: ${model.spawn.x},
            y: ${model.spawn.y},
        },
        chunkSize: ${Settings.mapChunkSize},
        chunkFetchRateMs: ${Settings.mapChunkFetchRateMs},
        pixelsPerTile: 16,
    };
</script>
</body>
