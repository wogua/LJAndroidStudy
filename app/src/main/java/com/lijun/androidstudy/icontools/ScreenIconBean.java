package com.lijun.androidstudy.icontools;

/**
 * Created by lijun on 17-6-29.
 */

public class ScreenIconBean extends IconBean {
    public  int  screen;
    public  int  cellX;
    public  int  cellY;

    @Override
    public String toString() {
        return "["+"packageName:"+packageName+", className:"+className+",screen:"+screen+",cellX:"+cellX+",cellY:"+cellY+"]";
    }
}
