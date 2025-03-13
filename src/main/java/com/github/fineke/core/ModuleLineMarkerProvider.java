package com.github.fineke.core;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.*;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.github.fineke.core.DeployAction.createDeployAction;

public class ModuleLineMarkerProvider extends RunLineMarkerContributor {


    public static final String TARGET_ANNOTATION = "com.oklink.blockchain.parser.annotation.Module";
    public static final String UNKNOWN = "Unknown";

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {


        if (isModule(element)) {
            var annotations = ((PsiClass) element.getParent()).getAnnotations();

            List<DeployAction> list = new ArrayList<>();
            for (PsiAnnotation annotation : annotations) {
                if (TARGET_ANNOTATION.equals(annotation.getQualifiedName())){
                    String module = getAnnotationValue(annotation);
                    if (UNKNOWN.equals(module)){
                        continue;
                    }
                    list.add(createDeployAction(false,module));
                    list.add(createDeployAction(true,module));
                }

                System.out.println(annotation.getQualifiedName());
            }


//            String module = getAnnotationValue(((PsiClass) element.getParent()).getAnnotation(TARGET_ANNOTATION));
            return new RunLineMarkerContributor.Info(AllIcons.Actions.Execute, new Function<PsiElement, String>() {
                @Override
                public String fun(PsiElement psiElement) {
                    return "Compile and Run";
                }
            }, list.toArray(new DeployAction[list.size()]));
        }

        return null;
    }


    private boolean isModule(PsiElement element) {
        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element.getParent();

            // 检查类是否有 @Module 注解
            PsiAnnotation moduleAnnotation = psiClass.getAnnotation(TARGET_ANNOTATION);
            if (moduleAnnotation != null) {
                return true;
            }
        }
        return false;
    }

    private String getAnnotationValue(PsiAnnotation annotation) {
        PsiAnnotationMemberValue valueAttribute = annotation.findAttributeValue("moduleType");
        if (valueAttribute instanceof PsiLiteralExpression) {
            Object value = ((PsiLiteralExpression) valueAttribute).getValue();
            return value != null ? value.toString() : UNKNOWN;
        }
        return UNKNOWN;
    }
}
