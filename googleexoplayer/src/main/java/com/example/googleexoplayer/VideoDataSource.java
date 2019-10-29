package com.example.googleexoplayer;


import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;

public class VideoDataSource implements DataSource {

    private static final String TAG = "VideoDataSource";

    public static final String SCHEME = "online_vod";

    private final DataSource dataSource;

    private static Uri videoUri;

    public static Uri create(String videoUrl){

        videoUri  = Uri.parse(videoUrl);
        return videoUri;
    }

    public VideoDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {
        dataSource.addTransferListener(transferListener);
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
//        if (!SCHEME.equals(dataSpec.uri.getScheme())){
//            return dataSource.open(dataSpec);
//        }
//        return dataSource.open(dataSpec.withUri(videoUri));
        return dataSource.open(dataSpec);
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return dataSource.read(buffer,offset,readLength);
    }

    @Nullable
    @Override
    public Uri getUri() {
        return dataSource.getUri();
    }

    @Override
    public void close() throws IOException {
        dataSource.close();
    }

    public static class Factory implements DataSource.Factory{

        private final DataSource.Factory factory;
        public Factory(DataSource.Factory factory){
            this.factory = factory;
        }


        @Override
        public DataSource createDataSource() {
            return new VideoDataSource(factory.createDataSource());
        }
    }

}
