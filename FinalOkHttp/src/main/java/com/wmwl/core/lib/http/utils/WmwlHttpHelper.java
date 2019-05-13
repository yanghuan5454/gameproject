package com.wmwl.game.common.request;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.wmwl.core.common.constants.HttpCode;
import com.wmwl.core.common.listener.PostListener;
import com.wmwl.core.common.util.LoggerUtil;
import com.wmwl.core.lib.http.finalokhttp.AjaxCallBack;
import com.wmwl.core.lib.http.finalokhttp.AjaxParams;
import com.wmwl.core.lib.http.finalokhttp.FinalOkHttp;
import com.wmwl.core.lib.http.finalokhttp.data.BaseHttpBean;
import com.wmwl.core.lib.http.utils.JsonUtil;
import com.wmwl.game.app.entity.http.CollectionListBean;
import com.wmwl.game.app.entity.http.GameAllListBean;
import com.wmwl.game.app.entity.http.GameIndexBean;
import com.wmwl.game.app.entity.http.GameListBean;
import com.wmwl.game.app.entity.http.RaiseRankBean;
import com.wmwl.game.common.constants.RequestUrl;

import java.lang.reflect.ParameterizedType;

/**
 * Created by Administrator on 2018/9/15.
 */

public class WmwlHttpHelper {

    private static final WmwlHttpHelper mInstance = new WmwlHttpHelper();

    private FinalOkHttp okHttp;

    private WmwlHttpHelper(){
        okHttp = new FinalOkHttp();
    }

    public static WmwlHttpHelper getInstance(){
        return mInstance;
    }

    private <T extends BaseHttpBean> void request(String url, AjaxParams params, final Callback<T> listener){
        okHttp.post(url, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object t) {
                try {
                    String result = t.toString() ;
                    LoggerUtil.d("yanghuan", "requestGameIndex result>>"+result);
                    if (TextUtils.isEmpty(result)) {
                        onFailure(null, -1, "");
                        return;
                    }

                    Class<T> entityClass = (Class<T>) ((ParameterizedType) listener.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];

                    LoggerUtil.d("yanghuan", "T Class>>"+entityClass);
                    T data = (T) JsonUtil.getInstance().fromJson(result, entityClass);
                    if(data.status.equals(String.valueOf(HttpCode.DATA_UI_SUC_HASDATA_CODE))){
                        listener.onSuccess(data) ;
                        return ;
                    }

                    if(!TextUtils.isEmpty(data.errCode)){
                        onFailure(null, Integer.parseInt(data.errCode), data.errMsg) ;
                    }else{
                        onFailure(null, -1, data.errMsg) ;
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                try {
                    LoggerUtil.d("requestRaiseRank result>>"+errorNo);
                    listener.onFailurl(t, String.valueOf(errorNo), strMsg) ;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) ;
    }

    public <T> void  requestCollectionList(String type,String userToken,final PostListener<CollectionListBean.CollectionDataBean<T>> listener){
        String url = RequestUrl.getCollectionListUrl();
        AjaxParams params = new AjaxParams();
        params.put("type", type);
        params.put("userId",userToken);

        LoggerUtil.d("url>>>" + url + "?" + params.toString());
        request(url,params,new Callback<CollectionListBean<T>>(){
            @Override
            public void onSuccess(CollectionListBean<T> data) {
                listener.onSuccess(data.data);
            }

            @Override
            public void onFailurl(Throwable t, String errorNo, String strMsg) {
                listener.onFailurl(t, errorNo, strMsg) ;
            }
        });
    }

    /**
     * 游戏列表信息
     * @param type
     * @param pageSize
     * @param pageIndex
     * @param listener
     */
    public void requestGameAllList(String type, int pageSize, int pageIndex, final PostListener<GameAllListBean.GameAllListData> listener){
        String url = RequestUrl.getGameListUrl();
        AjaxParams params = new AjaxParams();
        if(!TextUtils.isEmpty(type)) {
            params.put("type", type);
        }
        params.put("pageSize",String.valueOf(pageSize));
        params.put("pageStart",String.valueOf(pageIndex));

        LoggerUtil.d("url>>>" + url + "?" + params.toString());
        request(url,params,new Callback<GameAllListBean>(){
            @Override
            public void onSuccess(GameAllListBean data) {
                listener.onSuccess(data.data);
            }

            @Override
            public void onFailurl(Throwable t, String errorNo, String strMsg) {
                listener.onFailurl(t, errorNo, strMsg) ;
            }
        });
    }

    /**
     * 游戏列表信息
     * @param type
     * @param pageSize
     * @param pageIndex
     * @param listener
     */
    public void requestGameList(String type, int pageSize, int pageIndex, final PostListener<GameListBean.GameListData> listener){
        String url = RequestUrl.getGameListUrl();
        AjaxParams params = new AjaxParams();
        if(!TextUtils.isEmpty(type)) {
            params.put("type", type);
        }
        params.put("pageSize",String.valueOf(pageSize));
        params.put("pageStart",String.valueOf(pageIndex));

        LoggerUtil.d("url>>>" + url + "?" + params.toString());
        request(url,params,new Callback<GameListBean>(){
            @Override
            public void onSuccess(GameListBean data) {
                listener.onSuccess(data.data);
            }

            @Override
            public void onFailurl(Throwable t, String errorNo, String strMsg) {
                listener.onFailurl(t, errorNo, strMsg) ;
            }
        });
    }

    /**
     * 游戏首页
     * @param userToken
     * @param listener
     */
    public void requestGameIndex(String userToken, final PostListener<GameIndexBean.GameIndexData> listener){
        String url = RequestUrl.getGameIndexUrl();
        AjaxParams params = new AjaxParams();
        if(!TextUtils.isEmpty(userToken)) {
            params.put("userId", userToken);
        }

        LoggerUtil.d("url>>>" + url + "?" + params.toString());
        request(url,params,new Callback<GameIndexBean>(){
            @Override
            public void onSuccess(GameIndexBean data) {
                listener.onSuccess(data.data);
            }

            @Override
            public void onFailurl(Throwable t, String errorNo, String strMsg) {
                listener.onFailurl(t, errorNo, strMsg) ;
            }
        });
    }

    /**
     * 涨跌幅榜
     * @param raiseType 涨跌幅类型
     * @param listener*/
    public void requestRaiseRank(String raiseType, final PostListener<RaiseRankBean.CoinData> listener){
        String url = RequestUrl.getCoinRaiseRankUrl();
        AjaxParams params = new AjaxParams();
        params.put("raiseType", raiseType);

        LoggerUtil.d("url>>>" + url + "?" + params.toString());
        request(url, params, new Callback<RaiseRankBean>() {
            @Override
            public void onSuccess(RaiseRankBean data) {
                listener.onSuccess(data.data) ;
            }

            @Override
            public void onFailurl(Throwable t, String errorNo, String strMsg) {
                listener.onFailurl(t, errorNo, strMsg) ;
            }
        });
    }

    private interface Callback<T>{
        void onSuccess(T data);
        void onFailurl(Throwable t, String errorNo, String strMsg);
    }
}
