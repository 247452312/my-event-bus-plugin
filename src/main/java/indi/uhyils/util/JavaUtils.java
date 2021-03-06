package indi.uhyils.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type Java utils.
 *
 * @author yanglin
 */
public final class JavaUtils {

    private JavaUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Is model clazz boolean.
     *
     * @param clazz the clazz
     *
     * @return the boolean
     */
    public static boolean isModelClazz(@Nullable PsiClass clazz) {
        return null != clazz && !clazz.isAnnotationType() && !clazz.isInterface() && !clazz.isEnum() && clazz.isValid();
    }

    /**
     * Find settable psi field optional.
     *
     * @param clazz        the clazz
     * @param propertyName the property name
     *
     * @return the optional
     */
    public static Optional<PsiField> findSettablePsiField(@NotNull PsiClass clazz, @Nullable String propertyName) {
        return Optional.ofNullable(PropertyUtil.findPropertyField(clazz, propertyName, false));
    }

    /**
     * Find settable psi fields psi field [ ].
     *
     * @param clazz the clazz
     *
     * @return the psi field [ ]
     */
    @NotNull
    public static PsiField[] findSettablePsiFields(@NotNull PsiClass clazz) {
        PsiMethod[] methods = clazz.getAllMethods();
        List<PsiField> fields = Lists.newArrayList();
        for (PsiMethod method : methods) {
            if (PropertyUtil.isSimplePropertySetter(method)) {
                Optional<PsiField> psiField = findSettablePsiField(clazz, PropertyUtil.getPropertyName(method));
                psiField.ifPresent(fields::add);
            }
        }
        return fields.toArray(new PsiField[0]);
    }

    /**
     * Is element within interface boolean.
     *
     * @param element the element
     *
     * @return the boolean
     */
    public static boolean isElementWithinInterface(@Nullable PsiElement element) {
        if (element instanceof PsiClass && ((PsiClass) element).isInterface()) {
            return true;
        }
        PsiClass type = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        return Optional.ofNullable(type).isPresent() && type.isInterface();
    }

    /**
     * Find clazz optional.
     *
     * @param project   the project
     * @param clazzName the clazz name
     *
     * @return the optional
     */
    public static Optional<PsiClass> findClazz(@NotNull Project project, @NotNull String clazzName) {
        String classNameNeedFind = clazzName;
        if (classNameNeedFind.contains("$")) {
            classNameNeedFind = classNameNeedFind.replace("$", ".");
        }
        final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
        return Optional.ofNullable(instance.findClass(classNameNeedFind, GlobalSearchScope.allScope(project)));
    }

    /**
     * Find clazz optional.
     *
     * @param project   the project
     * @param clazzName the clazz name
     *
     * @return the optional
     */
    public static Optional<PsiClass[]> findClasses(@NotNull Project project, @NotNull String clazzName) {
        JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
        return Optional.ofNullable(instance.findClasses(clazzName, GlobalSearchScope.allScope(project)));
    }

    /**
     * Find method optional.
     *
     * @param project    the project
     * @param clazzName  the clazz name
     * @param methodName the method name
     *
     * @return the optional
     */
    public static Optional<PsiMethod> findMethod(@NotNull Project project, @Nullable String clazzName, @Nullable String methodName) {
        if (StringUtils.isBlank(clazzName) && StringUtils.isBlank(methodName)) {
            return Optional.empty();
        }
        Optional<PsiClass> clazz = findClazz(project, clazzName);
        if (clazz.isPresent()) {
            PsiMethod[] methods = clazz.get().findMethodsByName(methodName, true);
            return ArrayUtils.isEmpty(methods) ? Optional.<PsiMethod>empty() : Optional.of(methods[0]);
        }
        return Optional.empty();
    }

    /**
     * Find method optional.
     *
     * @param project    the project
     * @param clazzName  the clazz name
     * @param methodName the method name
     *
     * @return the optional
     */
    public static Optional<PsiMethod[]> findMethods(@NotNull Project project, @Nullable String clazzName, @Nullable String methodName) {
        if (StringUtils.isBlank(clazzName) && StringUtils.isBlank(methodName)) {
            return Optional.empty();
        }
        Optional<PsiClass[]> classes = findClasses(project, clazzName);
        if (classes.isPresent()) {

            List<PsiMethod> collect = Arrays.stream(classes.get())
                                            .map(psiClass -> psiClass.findMethodsByName(methodName, true))
                                            .flatMap(Arrays::stream)
                                            .collect(Collectors.toList());
            return collect.isEmpty() ? Optional.empty() : Optional.of(collect.toArray(new PsiMethod[0]));

        }
        return Optional.empty();
    }


    /**
     * Has import clazz boolean.
     *
     * @param file      the file
     * @param clazzName the clazz name
     *
     * @return the boolean
     */
    public static boolean hasImportClazz(@NotNull PsiJavaFile file, @NotNull String clazzName) {
        PsiImportList importList = file.getImportList();
        if (null == importList) {
            return false;
        }
        PsiImportStatement[] statements = importList.getImportStatements();
        for (PsiImportStatement tmp : statements) {
            if (null != tmp && Objects.equals(tmp.getQualifiedName(), clazzName)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<List<PsiElement>> findRegisterClassByEventClass(Project project, PsiClass psiClass) {
        List<PsiElement> eventTarget = EventCache.findEventTarget(project, psiClass);
        return Optional.ofNullable(eventTarget);
    }

}
