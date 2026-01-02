@import mapsite.webmodel.ServerInfoModel

@param ServerInfoModel model
<head>
    <title>MapSite</title>
</head>
<body>
<h2>Hello World!</h2>
<div>
    <p>There are ${model.onlinePlayers.size()} online players</p>
    <ul>
        @for(String playerName : model.onlinePlayers)
            <li>${playerName}</li>
        @endfor
    </ul>
</div>
</body>