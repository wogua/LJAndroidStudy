package com.lijun.androidstudy.icontools;

/**
 * Created by lijun on 17-7-24.
 */

public class ThemeIconBean extends IconBean {
    public String title;
    public String iconName;
    public boolean isSystem;

    public String getConfigString(){
        return packageName+"|"+packageName+"$"+className+"|"+title+"#"+iconName;
    }
}
