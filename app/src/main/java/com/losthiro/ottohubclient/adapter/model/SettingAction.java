/**
 * @Author Hiro
 * @Date 
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.adapter.model;

public class SettingAction extends SettingBasic{
    private Runnable action;
    
    public SettingAction(String title, Runnable command){
        super(title);
        action = command;
    }
    
    public SettingAction(String title, String text, Runnable command){
        super(title, text);
        action = command;
    }
    
    public void run(){
        if(action == null){
            return;
        }
        action.run();
    }
}
