@import mapsite.webmodel.ServerInfoModel

@param ServerInfoModel model
<head>
    <title>MapSite</title>
    <link rel="stylesheet" href="styles.css">
    <script src="index.js"></script>
    <script src="chunkFetcher.js"></script>
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

    <label for="centerX">Center X:</label>
    <input id="centerX" type="text" inputmode="numeric" pattern="-?\d+" value="${model.spawn.x}">

    <label for="centerY">Center Y:</label>
    <input id="centerY" type="text" inputmode="numeric" pattern="-?\d+" value="${model.spawn.y}">

    <button onclick="drawMapData()">Refresh</button>
</div>
<script>
    window.mapsite.spawn = {
        tileX: ${model.spawn.x},
        tileY: ${model.spawn.y},
    };
</script>
</body>