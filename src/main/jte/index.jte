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
<div class="top-left">
    <div class="info-float">
        <p id="mouse-position" class="no-margin">Mouse position: -</p>
    </div>
    <div class="info-float list">
        <h4 class="section-label">Layers</h4>
        <label class="layer-label">
            <input type="checkbox" id="player-layer" checked>
            Players
        </label>
        <label class="layer-label">
            <input type="checkbox" id="settlement-layer">
            Settlements
        </label>
    </div>
</div>
<div class="info-float top-right">
    <div>
        <h4 id="player-label" class="section-label">Online players: (0/${model.slots})</h4>
    </div>
    <div id="player-list" class="list"></div>
</div>
<div class="bottom-right">
    <div class="info-float">
        <div>
            <button class="button zoom-button" onclick="zoom(1)">+</button>
        </div>
        <div>
            <button class="button zoom-button" onclick="zoom(-1)">−</button>
        </div>
    </div>
    <div class="info-float">
        <button class="button link-button" onclick="copyLink()">☍</button>
    </div>
</div>
<script>
    window.mapsite = {
        ...window.mapsite,
        spawn: {
            x: ${model.spawn.x},
            y: ${model.spawn.y},
        },
        maxPlayers: ${model.slots},
        chunkSize: ${Settings.mapChunkSize},
        chunkFetchRateMs: ${Settings.mapChunkFetchRateMs},
        pixelsPerTile: 4,
    };
</script>
</body>
