package indi.uhyils.util;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

/**
 * Created by likfe on 2018/3/6.
 */
public class Constants {

    public static final Boolean IS_DEBUG = true;

    public static final String ICON_PATH = "/icons/icon.png";
    public static final Icon ICON = IconLoader.getIcon(Constants.ICON_PATH);
    public static final int MAX_USAGES = 100;

    public static final String FUN_START = "indi.uhyils.bus.Bus";
    public static final String FUN_START2 = "indi.uhyils.bus.BusInterface";
    public static final String FUN_NAME = "commitAndPush";
    public static final String FUN_NAME2 = "commit";

    public static final String FUN_ANNOTATION = "org.greenrobot.eventbus.Subscribe";
    public static final String FUN_ANNOTATION_KT = "Subscribe";
    public static final String FUN_EVENT_CLASS = "org.greenrobot.eventbus.EventBus";
    public static final String FUN_EVENT_CLASS_NAME = "EventBus";

}
