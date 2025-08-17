/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.impl;
import android.content.*;
import org.json.*;
import com.losthiro.ottohubclient.utils.*;

public class ClientSettings{
    private static final ClientSettings INSTANCE = new ClientSettings();
    private static final String MAINFEST = "config/mainfest.json";
    private JSONObject mainfest;
    private Context ctx;
    
    public static final ClientSettings getInstance(){
        return INSTANCE;
    }
    
    public final synchronized void register(Context c) throws Exception{
        ctx = c;
        String path = FileUtils.getStorage(c, MAINFEST);
        JSONObject setting = new JSONObject(FileUtils.readFile(c, path));
        if(setting == null){
            mainfest = new JSONObject();
            FileUtils.createFile(c, path, mainfest.toString(4));
        }else{
            mainfest = setting;
        }
    }
    
    public final synchronized void release() throws Exception{
        FileUtils.writeFile(ctx, FileUtils.getStorage(ctx, MAINFEST), mainfest.toString(4));
    }
}
