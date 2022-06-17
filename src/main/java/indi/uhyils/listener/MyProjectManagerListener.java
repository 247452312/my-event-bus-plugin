package indi.uhyils.listener;

import com.intellij.lang.jvm.JvmElementVisitor;
import com.intellij.lang.jvm.JvmMethod;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import indi.uhyils.util.JavaUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2022年06月15日 16时10分
 */
public class MyProjectManagerListener implements ProjectManagerListener {

    public MyProjectManagerListener() {
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        System.out.println("projectName:" + project.getName());


        ProjectManagerListener.super.projectOpened(project);
    }

}
