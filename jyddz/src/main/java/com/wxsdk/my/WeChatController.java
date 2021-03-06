package com.wxsdk.my;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;

import com.jingyu.rrddz.UnityPlayerActivity;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXMusicObject;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXVideoObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unity3d.player.UnityPlayer;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Created by Administrator on 2016/9/6 0006.
 */
public class WeChatController {
    static private IWXAPI api;
    private static WeChatController _instance;
    static public String APP_ID;
    private  WeChatController(){};

    private UnityPlayerActivity mainActivity;
    public static WeChatController GetInstance(){
        if(_instance == null)
        {
            _instance = new WeChatController();
        }
        return _instance;
    }

    public void SetMainActivity(UnityPlayerActivity activity) {
        mainActivity = activity;
    }

    public void RegisterToWeChat(Context context, String appId){
        APP_ID = appId;
        api = WXAPIFactory.createWXAPI(context,APP_ID);
        boolean issuccess =  api.registerApp(APP_ID);
        if (issuccess)
        {
            UnityPlayer.UnitySendMessage("SDK_callback", "Log","RegToWx success~~~~~~~" + appId);
            //UnityPlayer.UnitySendMessage("Android", "CallBack", "RegToWx success~~~~~~~" + appId);
        }else{
            UnityPlayer.UnitySendMessage("SDK_callback", "Log","RegToWx failure~~~~~~~~~" + appId);
            //UnityPlayer.UnitySendMessage("Android", "CallBack", "RegToWx failure~~~~~~~~~" + appId);
        }
    }

    //????????????
    public void ShareText(JSONObject jsonObject) {
        //String description = "";
        String text = "";
        boolean isCircleOfFriends = false;
        try {
            //description = jsonObject.getString("description");
            text = jsonObject.getString("text");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            UnityPlayer.UnitySendMessage("SDK_callback", "Log","ShareText failure: " + e.toString());
            //Toast.makeText(mainActivity, e.toString(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        // ???WXTextObject?????????????????????WXMediaMessage??????
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // ?????????????????????????????????????title??????????????????
//         msg.title = "Will be ignored";
        //msg.description = description;
        // ????????????????Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = Transaction.ShareText; // transaction??????????????????????????????????????
        req.message = msg;
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        // ??????api???????????????????????????????
        SendReq(req);
    }

    public void ShareImage (JSONObject jsonObject) {
        String imgFile = "";
        boolean isCircleOfFriends = false;
        try {
            imgFile = jsonObject.getString("imgFile");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            UnityPlayer.UnitySendMessage("SDK_callback", "Log","ShareImage failure: " + e.toString());
            //Toast.makeText(mainActivity, e.toString(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        //Resources re = MainActivity.Instance.getResources();
        UnityPlayer.UnitySendMessage("SDK_callback", "Log","Environment Dir:" + Environment.getExternalStorageDirectory().getAbsolutePath());

        Resources re = mainActivity.getResources();
        Bitmap bmp = BitmapFactory.decodeFile(imgFile);
        if(bmp == null) {
            UnityPlayer.UnitySendMessage("SDK_callback", "Log","Load imgFile failed:" + imgFile);
            return;
        }
        //Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));
        //Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", mainActivity.getPackageName()));

        WXImageObject imgObj = new WXImageObject(bmp);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        // ????????????????????????
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 100, 100, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = Transaction.ShareImage;
        req.message = msg;
        SendReq(req);
    }

    public void ShareVideo (JSONObject jsonObject) {
        String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            //Toast.makeText(mainActivity, e.toString(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }

        WXVideoObject video = new WXVideoObject();
        video.videoUrl = url;

        Resources re = mainActivity.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", mainActivity.getPackageName()));
        //Resources re = MainActivity.Instance.getResources();
        //Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));

        WXMediaMessage msg = new WXMediaMessage();
        msg.title = title;
        msg.description = description;
        msg.mediaObject = video;

        // ????????????????????????
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 100, 100, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = Transaction.ShareVideo;
        req.message = msg;
        SendReq(req);
    }

    public void ShareMusic (JSONObject jsonObject) {
        @SuppressWarnings("unused")
		String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            //Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
            Toast.makeText(mainActivity, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = "url";

        Resources re = mainActivity.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", mainActivity.getPackageName()));
        //Resources re = MainActivity.Instance.getResources();
        //Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));

        WXMediaMessage msg = new WXMediaMessage();
        msg.title = title;
        msg.description = description;

        msg.mediaObject = music;

        // ????????????????????????
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 100, 100, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = Transaction.ShareMusic;
        req.message = msg;
        SendReq(req);
    }

    public void ShareLinkUrl(JSONObject jsonObject) {
        String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            UnityPlayer.UnitySendMessage("SDK_callback", "Log","ShareLinkUrl failure: " + e.toString());
            //Toast.makeText(mainActivity, e.toString(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        //???WXMebpageObject ?????????????????????WXMediaMessage??????????????????????????????

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;  //??????????????????????????????????????????????????????????????????????????
        //????????????
        Resources re = mainActivity.getResources();  //??????????????????????Activity  (UnityPlayerActivity._instance)??????????????????Activity
        //Resources re = MainActivity.Instance.getResources();  //??????????????????????Activity  (UnityPlayerActivity._instance)??????????????????Activity
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", mainActivity.getPackageName()));
        //Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 100, 100, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
//
//        int id = re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName());
//        if (id == 0 )
//        {
//            Toast.makeText(MainActivity.Instance, "et app_icon fail ", Toast.LENGTH_SHORT).show();
//        }else
//        {
//            Bitmap thumb = BitmapFactory.decodeResource(re,id); //????????????32k
//            msg.thumbData = Util.bmpToByteArray(thumb, true);
//        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = Transaction.ShareUrl;
        req.message = msg;
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        SendReq(req);
    }
    public void WeChatLogin()
    {
        SendAuth.Req req = new SendAuth.Req();
        req.transaction = Transaction.RequestLogin;
        req.scope = "snsapi_userinfo";   // ????????????????????????????????????????????????????????????snsapi_userinfo
        req.state = "wechat_sdk_demo_test";
        SendReq(req);
        UnityPlayer.UnitySendMessage("SDK_callback", "Log","SendReq ~~~~~~~~~");
        //UnityPlayer.UnitySendMessage("Android", "CallBack", "SendReq ~~~~~~~~~");
    }
    public void SendReq(BaseReq req)
    {
        boolean issuccess = api.sendReq(req);
        if (!issuccess)
        {
            UnityPlayer.UnitySendMessage("SDK_callback", "OnWeChatError", "SendReqFail" + ":" + req.transaction);

            UnityPlayer.UnitySendMessage("SDK_callback", "Log","SendReq ~~~~~~~~~ fail");
            //UnityPlayer.UnitySendMessage("Android", "CallBack", "SendReq ~~~~~~~~~ fail");
        }else{
            UnityPlayer.UnitySendMessage("SDK_callback", "Log","SendReq ~~~~~~~~~ succes");
            //UnityPlayer.UnitySendMessage("Android", "CallBack", "SendReq ~~~~~~~~~ succes");
        }
    }

    public interface Type {
        int WeiChatInterfaceType_IsWeiChatInstalled = 1; //????????????????????????
        int WeiChatInterfaceType_RequestLogin = 2; //????????????
        int WeiChatInterfaceType_ShareUrl = 3; //????????????
        int WeiChatInterfaceType_ShareText = 4; //????????????
        int WeiChatInterfaceType_ShareMusic = 5;//????????????
        int WeiChatInterfaceType_ShareVideo = 6;//????????????
        int WeiChatInterfaceType_ShareImage = 7;//????????????
    }

    public interface Transaction {
        String IsWeiChatInstalled = "isInstalled"; //????????????????????????
        String RequestLogin = "login"; //????????????
        String ShareUrl = "shareUrl"; //????????????
        String ShareText = "shareText"; //????????????
        String ShareMusic = "shareMusic";//????????????
        String ShareVideo = "shareVideo";//????????????
        String ShareImage = "shareImage";//????????????
    }
}

