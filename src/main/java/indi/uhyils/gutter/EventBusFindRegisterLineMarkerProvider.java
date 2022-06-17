package indi.uhyils.gutter;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;
import indi.uhyils.util.EventCache;
import indi.uhyils.util.Icons;
import indi.uhyils.util.JavaUtils;
import indi.uhyils.util.PsiUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年06月14日 10时20分
 */
public class EventBusFindRegisterLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement psiElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (PsiUtils.isNotJava(psiElement)) {
            return;
        }
        if (PsiUtils.isEventBusPost(psiElement)) {
            if (psiElement instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
                try {
                    List<PsiElement> targetElement = new ArrayList<>();
                    final PsiExpressionList argumentList = expression.getArgumentList();
                    PsiType[] expressionTypes = argumentList.getExpressionTypes();
                    for (PsiType expressionType : expressionTypes) {
                        final String eventClassName = expressionType.getCanonicalText();
                        final Project project = psiElement.getProject();
                        final Optional<PsiClass> clazz = JavaUtils.findClazz(project, eventClassName);
                        clazz.ifPresent(t -> targetElement.addAll(EventCache.findEventTarget(project, t)));
                    }
                    NavigationGutterIconBuilder<PsiElement> builder =
                        NavigationGutterIconBuilder.create(Icons.JUMP_TO_REGISTER_ICON)
                                                   .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                                                   .setTargets(targetElement)
                                                   .setTooltipTitle("找到数据访问对象 - ");
                    result.add(builder.createLineMarkerInfo(psiElement));
                } catch (Exception ee) {
                    ee.fillInStackTrace();
                }

            }

        }
        return;
    }
}
