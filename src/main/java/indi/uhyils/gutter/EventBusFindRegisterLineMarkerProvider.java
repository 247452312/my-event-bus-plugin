package indi.uhyils.gutter;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.tree.java.PsiNewExpressionImpl;
import indi.uhyils.util.Icons;
import indi.uhyils.util.JavaUtils;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年06月14日 10时20分
 */
public class EventBusFindRegisterLineMarkerProvider extends RelatedItemLineMarkerProvider {



    private static final String FIND_KEY_WORD = "new";

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiKeyword)) {
            return;
        }
        PsiKeyword psiKeyword = (PsiKeyword) element;
        if (!Objects.equals(psiKeyword.getText(), FIND_KEY_WORD)) {
            return;
        }
        PsiNewExpressionImpl parent = (PsiNewExpressionImpl) psiKeyword.getParent();
        PsiType type = parent.getType();
        Optional<PsiClass> clazz = JavaUtils.findClazz(psiKeyword.getProject(), type.getCanonicalText());
        if (!clazz.isPresent()) {
            return;
        }

        PsiClass psiClass = clazz.get();
        Optional<List<PsiElement>> registerClass = JavaUtils.findRegisterClassByEventClass(element.getProject(), psiClass);
        if (!registerClass.isPresent()) {
            return;
        }
        List<PsiElement> psiElements = registerClass.get();
        if (CollectionUtils.isEmpty(psiElements)) {
            return;
        }
        NavigationGutterIconBuilder<PsiElement> builder =
            NavigationGutterIconBuilder.create(Icons.JUMP_TO_REGISTER_ICON)
                                       .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                                       .setTargets(psiElements)
                                       .setTooltipTitle("找到数据访问对象 - " + psiClass.getQualifiedName());
        result.add(builder.createLineMarkerInfo(parent));
    }
}
