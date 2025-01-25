package com.github.fineke.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

public class MavenProjectUtils {

    public static MavenProject getMavenProjectForFile(Project project, VirtualFile virtualFile) {
        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
        if (mavenProjectsManager == null) {
            return null;
        }

        // 遍历所有 Maven 项目，检查文件是否属于某个项目
        for (MavenProject mavenProject : mavenProjectsManager.getProjects()) {
            VirtualFile projectRoot = mavenProject.getDirectoryFile();
            if (isAncestor(projectRoot, virtualFile)) {
                return mavenProject;
            }
        }

        return null;
    }

    /**
     * 判断一个 VirtualFile 是否是另一个 VirtualFile 的祖先目录
     *
     * @param ancestor 祖先目录
     * @param file     要检查的文件
     * @return 如果 ancestor 是 file 的祖先目录，则返回 true
     */
    private static boolean isAncestor(VirtualFile ancestor, VirtualFile file) {
        if (ancestor == null || file == null) {
            return false;
        }
        String ancestorPath = ancestor.getPath();
        String filePath = file.getPath();

        // 判断文件路径是否以祖先路径开头，并确保后续是文件分隔符
        return filePath.startsWith(ancestorPath) &&
                (filePath.length() == ancestorPath.length() || filePath.charAt(ancestorPath.length()) == '/');
    }
}
