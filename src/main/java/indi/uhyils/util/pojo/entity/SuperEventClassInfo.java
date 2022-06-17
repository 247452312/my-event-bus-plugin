package indi.uhyils.util.pojo.entity;

import com.intellij.psi.PsiClass;
import java.util.List;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年06月16日 10时17分
 */
public class SuperEventClassInfo {

    /**
     * 是否是Event
     */
    private final Boolean isEvent;

    /**
     * 也是event的父类们
     */
    private final List<PsiClass> eventSuperClasses;

    public SuperEventClassInfo(Boolean isEvent, List<PsiClass> eventSuperClasses) {
        this.isEvent = isEvent;
        this.eventSuperClasses = eventSuperClasses;
    }


    public boolean isEvent() {
        return isEvent;
    }

    public void addSuperClass(PsiClass psiClass) {
        eventSuperClasses.add(psiClass);
    }

    public void addSuperClass(List<PsiClass> psiClasses) {
        eventSuperClasses.addAll(psiClasses);
    }

    public List<PsiClass> allSuperClasses() {
        return eventSuperClasses;
    }


}
