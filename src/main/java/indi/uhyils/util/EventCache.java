package indi.uhyils.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import indi.uhyils.util.pojo.entity.SuperEventClassInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年06月15日 17时18分
 */
public class EventCache {


    /**
     * 事件连接监听缓存
     * key: 事件
     * value: 对应的监听者指定的行
     */
    public static final Map<PsiClass, List<PsiElement>> eventLinkCache = new ConcurrentHashMap<>();

    /**
     * 是否初始化
     */
    public static volatile Boolean init = false;

    /**
     * 检查指定类是不是在缓存中,如果不是,则不一定是event 如果是,则一定是event
     *
     * @param psiClass
     *
     * @return
     */
    public static boolean checkClassInCache(PsiClass psiClass) {
        return eventLinkCache.containsKey(psiClass);
    }

    public static List<PsiElement> findEventTarget(Project project, PsiClass event) {
        // 检查是否在缓存中
        if (checkClassInCache(event)) {
            return eventLinkCache.get(event);
        }
        // 检查是否是event
        SuperEventClassInfo allEventSuperClass = getAllEventSuperClass(event);
        if (!allEventSuperClass.isEvent()) {
            return Collections.emptyList();
        }

        if (!init) {
            init(project);
        }
        List<PsiClass> psiClasses = allEventSuperClass.allSuperClasses();
        psiClasses.add(event);
        return psiClasses.stream()
                         .map(eventLinkCache::get)
                         .filter(Objects::nonNull)
                         .distinct()
                         .flatMap(Collection::stream)
                         .filter(Objects::nonNull)
                         .collect(Collectors.toList());
    }

    private static void init(Project project) {
        Optional<PsiClass[]> classes = JavaUtils.findClasses(project, "indi.uhyils.protocol.register.base.Register");
        if (classes.isPresent()) {
            PsiClass register = classes.get()[0];
            // 获取所有的监听者
            Query<PsiClass> search = ClassInheritorsSearch.search(register);
            Collection<PsiClass> all = search.findAll();
            // todo 筛除掉 没有@Register的
            // 遍历所有register 查询指定的event
            for (PsiClass realRegister : all) {
                PsiMethod[] targetEvents = realRegister.findMethodsByName("targetEvent", true);
                if (targetEvents.length == 0) {
                    continue;
                }

                PsiMethod targetEvent = targetEvents[0];
                Collection<PsiKeyword> childrenOfType = PsiTreeUtil.findChildrenOfType(targetEvent, PsiKeyword.class);
                for (PsiKeyword psiKeyword : childrenOfType) {
                    if (!Objects.equals(psiKeyword.getTokenType().getDebugName(), "CLASS_KEYWORD")) {
                        continue;
                    }
                    PsiElement parent = psiKeyword.getParent();
                    if (!(parent instanceof PsiClassObjectAccessExpression)) {
                        continue;
                    }
                    PsiClassObjectAccessExpression psiClassObjectAccessExpression = (PsiClassObjectAccessExpression) parent;
                    PsiType type = psiClassObjectAccessExpression.getOperand().getType();
                    Optional<PsiClass[]> registerEventOptional = JavaUtils.findClasses(project, type.getCanonicalText());
                    // 如果没有找到.class 指定的类在project中,则忽略
                    if (!registerEventOptional.isPresent()) {
                        continue;
                    }
                    PsiClass[] psiClassEvents = registerEventOptional.get();
                    if (psiClassEvents.length == 0) {
                        continue;
                    }
                    PsiClass psiClassEvent = psiClassEvents[0];
                    // 保证.class的是event
                    SuperEventClassInfo allEventSuperClass = getAllEventSuperClass(psiClassEvent);
                    if (!allEventSuperClass.isEvent()) {
                        continue;
                    }
                    List<PsiElement> psiElements = null;
                    if (eventLinkCache.containsKey(psiClassEvent)) {
                        psiElements = eventLinkCache.get(psiClassEvent);
                    } else {
                        psiElements = new ArrayList<>();
                    }
                    psiElements.add(psiKeyword);
                    eventLinkCache.put(psiClassEvent, psiElements);
                }
            }
        }
        init = true;
    }


    /**
     * 是否继承自Event类
     *
     * @param psiClass
     *
     * @return
     */
    private static SuperEventClassInfo getAllEventSuperClass(PsiClass psiClass) {
        List<PsiClass> result = new ArrayList<>();
        SuperEventClassInfo superEventClassInfo = null;

        PsiClass[] interfaces = psiClass.getInterfaces();
        if (interfaces.length > 0) {
            for (PsiClass interfaceClass : interfaces) {
                if (Objects.equals(interfaceClass.getQualifiedName(), "indi.uhyils.pojo.cqe.event.base.BaseEvent")) {
                    superEventClassInfo = new SuperEventClassInfo(true, result);
                } else {
                    SuperEventClassInfo allEventSuperClass = getAllEventSuperClass(interfaceClass);
                    superEventClassInfo = getClassInfo(result, superEventClassInfo, interfaceClass, allEventSuperClass);
                }
            }
        }

        PsiClass[] supers = psiClass.getSupers();
        if (supers.length > 0) {
            for (PsiClass superClass : supers) {
                SuperEventClassInfo allEventSuperClass = getAllEventSuperClass(superClass);
                superEventClassInfo = getClassInfo(result, superEventClassInfo, superClass, allEventSuperClass);
            }
        }
        if (superEventClassInfo == null) {
            superEventClassInfo = new SuperEventClassInfo(false, null);
        }

        return superEventClassInfo;
    }

    private static SuperEventClassInfo getClassInfo(List<PsiClass> result, SuperEventClassInfo superEventClassInfo, PsiClass interfaceClass, SuperEventClassInfo allEventSuperClass) {
        if (allEventSuperClass.isEvent()) {
            if (superEventClassInfo == null) {
                superEventClassInfo = new SuperEventClassInfo(true, result);
            }
            result.add(interfaceClass);
            result.addAll(allEventSuperClass.allSuperClasses());
        }
        return superEventClassInfo;
    }

}
