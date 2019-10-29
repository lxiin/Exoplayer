package com.example.googleexoplayer;

public interface ErrorCode {
    /**
     * 网络错误
     */
    int NET_ERROR = -1;
    /**
     * 本地文件异常
     */
    int LOCAL_FILE_ERROR = -2;
    /**
     * 点播获取播放地址失败
     */
    int VOD_GET_PLAYURL_ERROR = 10;

    /**
     * 打开本地文件失败,(请删除后重新下载)
     */
    int VOD_LOCAL_FILE_ERROR = 11;

    /**
     * 获取回放信息失败
     */
    int RECORD_GET_INFO_ERROR = 100;
    /**
     * 获取回放聊天错误
     */
    int RECORD_GET_CHAT_INFO_ERROR = 101;
    /**
     * 获取回放文档错误
     */
    int RECORD_GET_DOC_INFO_ERROR = 102;
    /**
     * 获取回放笔画信息错误
     */
    int RECORD_GET_DOC_PAINT_INFO_ERROR = 103;
    /**
     * 打开本地回放文件失败,(请删除后重新下载)
     */
    int RECORD_LOCAL_FILE_ERROR = 104;
}
