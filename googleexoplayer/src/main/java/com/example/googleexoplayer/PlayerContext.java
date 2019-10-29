package com.example.googleexoplayer;

public final class PlayerContext {

    private static final String TAG = "PlayerContext";
    private final PlayerStateStore playerStateStore;
    private final PlayerViewManager mCVPlayerViewManager;
    private PlayerViewActionGenerator playerViewActionGenerator;
    public PlayerContext(PlayerViewManager cvPlayerViewManager,
                         PlayerViewActionGenerator playerViewActionGenerator, PlayerStateStore playerStateStore) {

        this.mCVPlayerViewManager = cvPlayerViewManager;
        this.playerViewActionGenerator = playerViewActionGenerator;
        this.playerStateStore = playerStateStore;
    }

    public PlayerViewActionGenerator getPlayerViewActionGenerator() {
        return playerViewActionGenerator;
    }

    public PlayList getPlayerList(){
        return playerStateStore.getPlayerList();
    }

    public PlayerViewManager getPlayerViewManager() {
        return mCVPlayerViewManager;
    }

    public void setPlayerView(IPlayerView videoview){
        mCVPlayerViewManager.setCurrentPlayerView(this,videoview);
    }
}
