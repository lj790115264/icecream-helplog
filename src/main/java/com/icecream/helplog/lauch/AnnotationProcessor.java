package com.icecream.helplog.lauch;

import cn.hutool.core.util.ObjectUtil;
import com.icecream.helplog.annoation.HelpLogA;
import com.icecream.helplog.annoation.HelpLogP;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;

/**
 * @author andre.lan
 */
@SupportedAnnotationTypes({"com.icecream.helplog.annoation.HelpLogA"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
//@AutoService(Process.class)
public class AnnotationProcessor extends AbstractProcessor {

    /**
     * 消息记录器
     */
    private Messager messager;

    /**
     * 可将Element转换为JCTree的工具。(注: 简单的讲，处理AST, 就是处理一个又一个CTree)
     */
    private JavacTrees trees;

    private Context context;

    private Names names;
    /**
     * JCTree制作器
     */
    private TreeMaker treeMaker;

    // 定义elementUtils字段，并在init()方法中初始化
    private JavacElements elementUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.trees = JavacTrees.instance(processingEnv);
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.elementUtils = (JavacElements) processingEnv.getElementUtils();
    }
    String _HELP_LOG_TMP_KEY33 = "_HELP_LOG_TMP_KEY33";


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation)
                    .forEach(element -> {

                        if (element instanceof ExecutableElement) {
                            ExecutableElement executableElement = (ExecutableElement) element;

                            // 获取方法参数列表
                            java.util.List<? extends VariableElement> parameters = executableElement.getParameters();

                            Boolean noHelpLogP = true;
                            Name logKeyName = null;
                            JCTree.JCVariableDecl logKeyVar = null;
                            for (VariableElement parameter : parameters) {
                                // 获取方法参数上的注解信息
                                if (parameter.getAnnotation(HelpLogP.class) != null) {
                                    noHelpLogP = false;
                                    logKeyName = getNameFromString(parameter.getSimpleName().toString());

                                }
                            }
                            // 形参上没有注解
                            if (noHelpLogP) {
                                HelpLogA helpLogAnnotation = element.getAnnotation(HelpLogA.class);
                                String value = helpLogAnnotation.value();
                                if (ObjectUtil.isEmpty(value)) {
                                    value = element.getSimpleName()
                                            .toString();
                                }

                                logKeyVar = createVarDef(treeMaker.Modifiers(0), _HELP_LOG_TMP_KEY33, memberAccess("java.lang.String"), treeMaker.Literal(value));
                                logKeyName = logKeyVar.name;
                            }

                            // 获取方法节点
                            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) trees.getTree(element);

                            // pos的作用无法体会
                            treeMaker.pos = jcMethodDecl.pos;

                            JCTree.JCExpressionStatement addLog = treeMaker.Exec( // 创建可执行语句
                                    treeMaker.Apply( // 创建JCMethodInvocation
                                            List.of(memberAccess("java.lang.String")),
                                            memberAccess("com.icecream.helplog.util.HelpLog.add"),
                                            List.of(treeMaker.Ident(logKeyName))));

                            JCTree.JCExpressionStatement delLog = treeMaker.Exec( // 创建可执行语句
                                    treeMaker.Apply( // 创建JCMethodInvocation
                                            List.of(memberAccess("java.lang.String")),
                                            memberAccess("com.icecream.helplog.util.HelpLog.del"),
                                            List.of(treeMaker.Ident(logKeyName))));

                            JCTree.JCBlock finallyBlock = treeMaker.Block(0, List.of(delLog));

                            if (null != logKeyVar) {
                                // 更新方法体
                                jcMethodDecl.body = treeMaker.Block(0,
                                        List.of(
                                                logKeyVar,
                                                addLog,
                                                treeMaker.Try(jcMethodDecl.body,
                                                        List.nil(), finallyBlock)
                                        ));
                            } else {
                                // 更新方法体
                                jcMethodDecl.body = treeMaker.Block(0,
                                        List.of(
                                                addLog,
                                                treeMaker.Try(jcMethodDecl.body,
                                                        List.nil(), finallyBlock)
                                        ));
                            }

                        }


                    });
        }

        // return false 代表这个注解还会被其他处理器继续处理
        return true;
    }

    /**
     * 创建变量语句
     *
     * @param modifiers
     * @param name      变量名
     * @param varType   变量类型
     * @param init      变量初始化语句
     * @return
     */
    private JCTree.JCVariableDecl createVarDef(JCTree.JCModifiers modifiers, String name, JCTree.JCExpression varType, JCTree.JCExpression init) {
        return treeMaker.VarDef(
                modifiers,
                //名字
                getNameFromString(name),
                //类型
                varType,
                //初始化语句
                init
        );
    }

    /**
     * 根据字符串获取Name，（利用Names的fromString静态方法）
     *
     * @param s
     * @return
     */
    private com.sun.tools.javac.util.Name getNameFromString(String s) {
        return names.fromString(s);
    }

    /**
     * 创建 域/方法 的多级访问, 方法的标识只能是最后一个
     *
     * @param components
     * @return
     */
    private JCTree.JCExpression memberAccess(String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(getNameFromString(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = treeMaker.Select(expr, getNameFromString(componentArray[i]));
        }
        return expr;
    }

}