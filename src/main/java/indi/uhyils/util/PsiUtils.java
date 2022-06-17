package indi.uhyils.util;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.Language;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.likfe.ideaplugin.eventbus3.utils.MLog;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtConstructorCalleeExpression;
import org.jetbrains.kotlin.psi.KtModifierList;
import org.jetbrains.kotlin.psi.KtNamedFunction;

/**
 * modify by likfe ( https://github.com/likfe/ ) on 2018/03/05.
 */
public class PsiUtils {

    public static PsiClass getClass(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            return ((PsiClassType) psiType).resolve();
        }
        return null;
    }

    public static boolean isEventBusReceiver(PsiElement psiElement) {
        if (psiElement.getLanguage().is(Language.findLanguageByID("JAVA"))) {

            if (psiElement instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) psiElement;
                PsiModifierList modifierList = method.getModifierList();
                for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                    if (safeEquals(psiAnnotation.getQualifiedName(), Constants.FUN_ANNOTATION)) {
                        return true;
                    }
                }
            }
        } else if (psiElement.getLanguage().is(Language.findLanguageByID("kotlin"))) {

            if (psiElement instanceof KtNamedFunction) {
                KtNamedFunction function = (KtNamedFunction) psiElement;
                KtModifierList modifierList = function.getModifierList();
                if (modifierList != null) {
                    for (KtAnnotationEntry annotationEntry : modifierList.getAnnotationEntries()) {
                        KtConstructorCalleeExpression calleeExpression = annotationEntry.getCalleeExpression();
                        if (calleeExpression != null && safeEquals(calleeExpression.getText(), Constants.FUN_ANNOTATION_KT)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isEventBusPost(PsiElement psiElement) {
         PsiElement firstChild = psiElement.getFirstChild();
        if (psiElement instanceof PsiMethodCallExpressionImpl && firstChild != null && firstChild instanceof PsiReferenceExpressionImpl) {
            PsiReferenceExpressionImpl all = (PsiReferenceExpressionImpl) firstChild;
            if (all.getFirstChild() instanceof PsiMethodCallExpressionImpl && all.getLastChild() instanceof PsiIdentifierImpl) {
                PsiMethodCallExpressionImpl start = (PsiMethodCallExpressionImpl) all.getFirstChild();
                PsiIdentifierImpl post = (PsiIdentifierImpl) all.getLastChild();
                final PsiType type = start.getType();

                if ((Objects.equals(post.getText(), Constants.FUN_NAME) || Objects.equals(post.getText(), Constants.FUN_NAME2)) && (Objects.equals(type.getCanonicalText(), Constants.FUN_START) || Objects.equals(type.getCanonicalText(), Constants.FUN_START))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEventBusClass(PsiClass psiClass) {
        return safeEquals(psiClass.getName(), Constants.FUN_EVENT_CLASS_NAME);
    }

    private static boolean isSuperClassEventBus(PsiClass psiClass) {
        PsiClass[] supers = psiClass.getSupers();
        if (supers.length == 0) {
            return false;
        }
        for (PsiClass superClass : supers) {
            if (safeEquals(superClass.getName(), Constants.FUN_EVENT_CLASS_NAME)) {
                return true;
            }
        }
        return false;
    }

    private static boolean safeEquals(String obj, String value) {
        return obj != null && obj.equals(value);
    }

    public static boolean isKotlin(PsiElement psiElement) {
        return psiElement.getLanguage().is(Language.findLanguageByID("kotlin"));
    }

    public static boolean isJava(PsiElement psiElement) {
        return psiElement.getLanguage().is(Language.findLanguageByID("JAVA"));
    }

    public static boolean isNotJava(PsiElement psiElement) {
        return !isJava(psiElement);
    }

    /**
     * is kotlin plug installed and enable
     *
     * @return boolean
     */
    public static boolean checkIsKotlinInstalled() {
        PluginId pluginId = PluginId.findId("org.jetbrains.kotlin");
        if (pluginId != null) {
            IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(pluginId);
            return pluginDescriptor != null && pluginDescriptor.isEnabled();
        }
        return false;
    }

    private static void logPluginList() {
        IdeaPluginDescriptor[] pluginDescriptors = PluginManager.getPlugins();
        MLog.debug("== list plug ==");
        for (IdeaPluginDescriptor item : pluginDescriptors) {
            MLog.debug("id: " + item.getPluginId().getIdString() + " name: " + item.getName() + " isEnable: " + item.isEnabled());
        }
        MLog.debug("== list plug end ==");
    }

}
