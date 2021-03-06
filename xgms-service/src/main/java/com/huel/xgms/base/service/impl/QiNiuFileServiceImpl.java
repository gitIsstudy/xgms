package com.huel.xgms.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.huel.xgms.base.bean.QnPutRet;
import com.huel.xgms.base.service.IQiNiuFileService;
import com.huel.xgms.base.service.IRedisOpService;
import com.huel.xgms.system.bean.SystemConfigCode;
import com.huel.xgms.system.service.ISystemConfigService;
import com.huel.xgms.util.Constants;
import com.huel.xgms.util.JedisContants;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * 七牛文件服务接口实现
 * @author wsq
 * @date 2018/3/13
 */
@Service
public class QiNiuFileServiceImpl implements IQiNiuFileService {
    private static final Logger LOG = LoggerFactory.getLogger(QiNiuFileServiceImpl.class);

    @Autowired
    private ISystemConfigService systemConfigService;

    @Autowired
    private IRedisOpService redisOpService;

    @Override
    public QnPutRet uploadFile(String filePath, String key) {
        LOG.debug("上传本地文件到七牛服务器：fileUrl:{}", filePath);
        try {
            // 构造一个带指定Zone对象的配置类
            Configuration cfg = new Configuration(Zone.zone0());
            // ...其他参数参考类注释
            UploadManager uploadManager = new UploadManager(cfg);

            StringMap putPolicy = new StringMap();
            putPolicy.put("returnBody", QnPutRet.returnBody);

            String upToken = getUpToken();
            // 默认不指定key的情况下，以文件内容的hash值作为文件名
            Response response = uploadManager.put(filePath, key, upToken);
            // 解析上传成功的结果
            QnPutRet putRet = JSON.parseObject(response.bodyString(), QnPutRet.class);
            return putRet;
        } catch (QiniuException ex) {
            LOG.error("上传本地文件失败：{}" + ex);
            throw new RuntimeException("上传本地文件失败");
        }
    }

    @Override
    public QnPutRet uploadBytes(byte[] dataBytes, String key) {
        ByteArrayInputStream byteInputStream=new ByteArrayInputStream(dataBytes);
        return uploadInputStream(byteInputStream, key);
    }

    @Override
    public QnPutRet uploadInputStream(InputStream inputStream, String key) {// 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
        // ...其他参数参考类注释
        try {
            UploadManager uploadManager = new UploadManager(cfg);
            String upToken = getUpToken();
            Response response = uploadManager.put(inputStream, key, upToken, null, null);
            // 解析上传成功的结果
            QnPutRet qnPutRet = JSON.parseObject(response.bodyString(), QnPutRet.class);
            return qnPutRet;
        } catch (QiniuException ex) {
            LOG.error("上传字节数组失败：{}" + ex);
            throw new RuntimeException("上传字节数组失败");
        }
    }

    @Override
    public QnPutRet uploadFileByBp(String filePath, String key) {
        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
        // ...其他参数参考类注释

        // ...生成上传凭证，然后准备上传
        String accessKey = systemConfigService.getValueByCode(SystemConfigCode.QINIU_FILE_ACCESSKEY);
        String secretKey = systemConfigService.getValueByCode(SystemConfigCode.QINIU_FILE_SECRETKEY);
        String bucket = systemConfigService.getValueByCode(SystemConfigCode.QINIU_FILE_BUCKET_XGMS1);

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        String localTempDir = Paths.get(System.getProperty("java.io.tmpdir"), bucket).toString();
        try {
            // 设置断点续传文件进度保存目录
            FileRecorder fileRecorder = new FileRecorder(localTempDir);
            UploadManager uploadManager = new UploadManager(cfg, fileRecorder);
            try {
                // 默认不指定key的情况下，以文件内容的hash值作为文件名
                Response response = uploadManager.put(filePath, key, upToken);
                // 解析上传成功的结果
                DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取上传Token
     * @return
     */
    private String getUpToken(){
        // ...生成上传凭证，然后准备上传
        String token = redisOpService.get(JedisContants.QINIU_TOKEN);
        if(token == null){
            // 生成Token 并存入redis
            String accessKey = systemConfigService.getValueByCode(SystemConfigCode.QINIU_FILE_ACCESSKEY);
            String secretKey = systemConfigService.getValueByCode(SystemConfigCode.QINIU_FILE_SECRETKEY);
            // 默认不指定key的情况下，以文件内容的hash值作为文件名
            Auth auth = Auth.create(accessKey, secretKey);
            StringMap putPolicy = new StringMap();
            putPolicy.put("returnBody", QnPutRet.returnBody);
            // 文件存储镜像
            String bucket = systemConfigService.getValueByCode(SystemConfigCode.QINIU_FILE_BUCKET_XGMS1);
            token = auth.uploadToken(bucket, null, Constants.QINIU_TOKEN_EXPIRES_TIME, putPolicy);
            // token redis过期时间小于在七牛注册的时间。为了token一定是正常的
            redisOpService.set(JedisContants.QINIU_TOKEN, token, Constants.QINIU_TOKEN_EXPIRES_TIME - 10);
        }
        return token;
    }
}
