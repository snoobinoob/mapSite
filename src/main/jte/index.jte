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
    @if(model.onlinePlayers.isEmpty())
        <p>No players online</p>
    @else
        <p>Online players:</p>
        <ul>
            @for(String playerName : model.onlinePlayers)
                <li>${playerName}</li>
            @endfor
        </ul>
    @endif

    <button onclick="goToSpawn()">Go to Spawn</button>
</div>
<script>
    window.mapsite = {
        ...window.mapsite,
        spawn: {
            tileX: ${model.spawn.x},
            tileY: ${model.spawn.y},
        },
        chunkSize: ${Settings.mapChunkSize},
        chunkFetchRateMs: ${Settings.mapChunkFetchRateMs},
        pixelsPerTile: 16,
    };
</script>
</body>
