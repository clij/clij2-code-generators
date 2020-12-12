package net.haesleinhuepf.clijx.codegenerator;

import clojure.lang.Compiler;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPluginService;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clijx.assistant.scriptgenerator.JythonGenerator;
import net.haesleinhuepf.clijx.assistant.scriptgenerator.PyclesperantoGenerator;
import net.haesleinhuepf.clijx.assistant.utilities.AssistantUtilities;
import org.scijava.Context;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class OpGenerator {
    static final int PLATFORM_CLIJ2 = 1;
    static final int PLATFORM_CLIJx = 2;
    static final int PLATFORM_CLESPERANTOJ_CAMEL = 3;
    static final int PLATFORM_CLESPERANTOJ_SNAKE = 4;

    public static void main(String ... args) throws IOException {


        for (int platform : new int[]{PLATFORM_CLIJ2, PLATFORM_CLIJx, PLATFORM_CLESPERANTOJ_SNAKE, PLATFORM_CLESPERANTOJ_CAMEL}) {


            CLIJMacroPluginService service = new Context(CLIJMacroPluginService.class).getService(CLIJMacroPluginService.class);

            StringBuilder builder = new StringBuilder();
            StringBuilder clEsperantoJcompatibilityList = new StringBuilder();
            if (platform == PLATFORM_CLIJ2) {
                builder.append("package net.haesleinhuepf.clij2;\n");
                builder.append("import net.haesleinhuepf.clij2.CLIJ2;\n");
            } else if (platform == PLATFORM_CLIJx) {
                builder.append("package net.haesleinhuepf.clijx.utilities;\n");
                builder.append("import net.haesleinhuepf.clij2.CLIJ2;\n");
                builder.append("import net.haesleinhuepf.clijx.CLIJx;\n");
                builder.append("import net.haesleinhuepf.clijx.weka.CLIJxWeka2;\n");
            } else {
                builder.append("package net.clesperanto.javaprototype;\n\n");
                builder.append("import net.haesleinhuepf.clij2.CLIJ2;\n");
                builder.append("import net.haesleinhuepf.clijx.CLIJx;\n");
                builder.append("import net.haesleinhuepf.clijx.weka.CLIJxWeka2;\n");
            }
            builder.append("import net.haesleinhuepf.clij.CLIJ;\n");
            builder.append("import net.haesleinhuepf.clij.clearcl.ClearCLKernel;\n");
            builder.append("import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;\n");
            builder.append("import net.haesleinhuepf.clij.clearcl.ClearCLImage;\n");
            builder.append("import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;\n");
            builder.append("import ij.measure.ResultsTable;\n");
            builder.append("import ij.gui.Roi;\n");
            builder.append("import ij.plugin.frame.RoiManager;\n");
            builder.append("import java.util.HashMap;\n");
            builder.append("import ij.ImagePlus;\n");
            builder.append("import java.util.List;\n");
            builder.append("import java.util.ArrayList;\n");

            for (Class klass : CLIJxPlugins.classes) {
                if (
                        (platform == PLATFORM_CLIJ2 && klass.getPackage().toString().contains(".clij2.")) ||
                        ((platform == PLATFORM_CLIJx) && (klass.getPackage().toString().contains(".clijx.") || klass.getSimpleName().equals("Kernels"))) ||
                        ((platform == PLATFORM_CLESPERANTOJ_CAMEL || platform == PLATFORM_CLESPERANTOJ_SNAKE) && (!klass.getSimpleName().equals("Kernels")))
                ) {
                    builder.append("import " + klass.getName() + ";\n");
                }
            }

            builder.append("// this is generated code. See src/test/java/net/haesleinhuepf/clijx/codegenerator for details\n");
            if (platform == PLATFORM_CLIJ2) {
                builder.append("public abstract interface CLIJ2Ops {\n");
            } else if (platform == PLATFORM_CLIJx) {
                builder.append("public abstract interface CLIJxOps {\n");
            } else if (platform == PLATFORM_CLESPERANTOJ_CAMEL) {
                builder.append("abstract class CamelInterface extends CommonAPI{\n");
            } else if (platform == PLATFORM_CLESPERANTOJ_SNAKE) {
                builder.append("abstract class SnakeInterface extends CommonAPI {\n");
            }
            if (platform == PLATFORM_CLIJ2) {
                builder.append("   CLIJ getCLIJ();\n");
                builder.append("   CLIJ2 getCLIJ2();\n");
            } else if (platform == PLATFORM_CLIJx){
                builder.append("   CLIJ getCLIJ();\n");
                builder.append("   CLIJ2 getCLIJ2();\n");
                builder.append("   CLIJx getCLIJx();\n");
            } else {
                builder.append("   static CLIJ getCLIJ() {\n" +
                        "       return CLIJ.getInstance();\n" +
                        "   }\n");
                builder.append("   static CLIJ2 getCLIJ2() {\n" +
                        "       return CLIJ2.getInstance();\n" +
                        "   }\n");
                builder.append("   static CLIJx getCLIJx() {\n" +
                               "       return CLIJx.getInstance();\n" +
                               "   }\n");
            }
            if (platform == PLATFORM_CLIJ2 || platform == PLATFORM_CLIJx) {
                builder.append("   boolean doTimeTracing();\n");
                builder.append("   void recordMethodStart(String method);\n");
                builder.append("   void recordMethodEnd(String method);\n");
            }
            builder.append("   \n");


            int methodCount = 0;
            for (Class klass : CLIJxPlugins.classes) {
                if (
                        (klass.getPackage().toString().contains(".clij2.") && platform == PLATFORM_CLIJ2) ||
                        ((klass == Kernels.class || klass.getPackage().toString().contains(".clijx.")) && platform == PLATFORM_CLIJx) ||
                        (klass != Kernels.class && !klass.getPackage().toString().contains("wrap") && (platform == PLATFORM_CLESPERANTOJ_CAMEL || platform == PLATFORM_CLESPERANTOJ_SNAKE))
                ) {
                    builder.append("\n    // " + klass.getName() + "\n");
                    builder.append("    //----------------------------------------------------\n");
                    for (Method method : sort(klass.getMethods())) {
                        if (Modifier.isStatic(method.getModifiers()) &&
                                Modifier.isPublic(method.getModifiers()) &&
                                method.getParameterCount() > 0 &&
                                (method.getParameters()[0].getType() == CLIJ.class ||
                                        method.getParameters()[0].getType() == CLIJ2.class ||
                                        method.getParameters()[0].getType() == CLIJx.class
                                ) && blockListOk(klass, method)) {

                            String methodName = method.getName();
                            String returnType = typeToString(method.getReturnType());
                            String parametersHeader = "";
                            String parametersCall = "";
                            for (Parameter parameter : method.getParameters()) {
                                if (parametersCall.length() == 0) { // first parameter
                                    if (method.getParameters()[0].getType() == CLIJ.class) {
                                        parametersCall = "getCLIJ()";
                                    } else if (method.getParameters()[0].getType() == CLIJ2.class) {
                                        parametersCall = "getCLIJ2()";
                                    } else {
                                        parametersCall = "getCLIJx()";
                                    }
                                    continue;
                                }

                                if (parametersHeader.length() > 0) {
                                    parametersHeader = parametersHeader + ", ";
                                }
                                if (parameter.getType() == Float.class) {
                                    parametersHeader = parametersHeader + "double " + parameter.getName();
                                    parametersCall = parametersCall + ", new Double (" + parameter.getName() + ").floatValue()";
                                } else if (parameter.getType() == Integer.class) {
                                    parametersHeader = parametersHeader + "double " + parameter.getName();
                                    parametersCall = parametersCall + ", new Double (" + parameter.getName() + ").intValue()";
                                } else if (parameter.getType() == Boolean.class) {
                                    parametersHeader = parametersHeader + "boolean " + parameter.getName();
                                    parametersCall = parametersCall + ", " + parameter.getName();
                                } else if (
                                        parameter.getType() == net.imglib2.realtransform.AffineTransform2D.class ||
                                                parameter.getType() == net.imglib2.realtransform.AffineTransform3D.class
                                ) {
                                    parametersHeader = parametersHeader + parameter.getType().getName() + " " + parameter.getName();
                                    parametersCall = parametersCall + ", " + parameter.getName();
                                } else {
                                    parametersHeader = parametersHeader + parameter.getType().getSimpleName() + " " + parameter.getName();
                                    parametersCall = parametersCall + ", " + parameter.getName();
                                }
                            }

                            String[] variableNames = guessParameterNames(service, methodName, parametersHeader.split(","));
                            if (variableNames.length > 0) {
                                for (int i = 0; i < variableNames.length; i++) {
                                    parametersCall = parametersCall.replace("arg" + (i + 1), variableNames[i]);
                                    parametersHeader = parametersHeader.replace("arg" + (i + 1), variableNames[i]);
                                }
                            }


                            boolean deprecated = false;
                            for (Annotation annotation : method.getDeclaredAnnotations()) {
                                if (annotation instanceof Deprecated) {
                                    deprecated = true;
                                }
                            }

                            if (deprecated && (platform == PLATFORM_CLESPERANTOJ_CAMEL || platform == PLATFORM_CLESPERANTOJ_SNAKE)) {
                                continue;
                            }


                            String documentation = findDocumentation(service, methodName, deprecated);
                            //System.out.println(documentation);

                            builder.append("    /**\n");
                            builder.append("     * " + documentation.replace("\n", "\n     * ") + "\n");
                            builder.append("     */\n");


                            if (deprecated) {
                                builder.append("    @Deprecated\n");
                            }


                            if (platform == PLATFORM_CLIJ2 || platform == PLATFORM_CLIJx) {
                                builder.append("    default " + returnType + " " + methodName + "(");
                            } else if (platform == PLATFORM_CLESPERANTOJ_CAMEL){
                                builder.append("    public static " + returnType + " " + methodName + "(");
                            } else if (platform == PLATFORM_CLESPERANTOJ_SNAKE){
                                clEsperantoJcompatibilityList.append(AssistantUtilities.niceName(methodName).trim().replace(" ", "_").toLowerCase() + "\n");
                                builder.append("    public static " + returnType + " " + AssistantUtilities.niceName(methodName).trim().replace(" ", "_").toLowerCase() + "(");
                            }
                            builder.append(parametersHeader);
                            builder.append(") {\n");

                            if (deprecated) {
                                builder.append("        System.out.println(\"" + methodName +  " is deprecated. Check the documentation for a replacement. https://clij.github.io/clij2-doccs/reference\");\n");
                            }

                            if (returnType.compareTo("void") == 0) {
                                if (platform == PLATFORM_CLIJ2 || platform == PLATFORM_CLIJx) {
                                    builder.append("        if (doTimeTracing()) {recordMethodStart(\"" + klass.getSimpleName() + "\");}\n");
                                }
                                builder.append("        " + klass.getSimpleName() + "." + methodName + "(" + parametersCall + ");\n");
                                if (platform == PLATFORM_CLIJ2 || platform == PLATFORM_CLIJx) {
                                    builder.append("        if (doTimeTracing()) {recordMethodEnd(\"" + klass.getSimpleName() + "\");}\n");
                                }
                            } else {
                                if (platform == PLATFORM_CLIJ2 || platform == PLATFORM_CLIJx) {
                                    builder.append("        if (doTimeTracing()) {recordMethodStart(\"" + klass.getSimpleName() + "\");}\n");
                                }
                                builder.append("        " + returnType + " result = " + klass.getSimpleName() + "." + methodName + "(" + parametersCall + ");\n");
                                if (platform == PLATFORM_CLIJ2 || platform == PLATFORM_CLIJx) {
                                    builder.append("        if (doTimeTracing()) {recordMethodEnd(\"" + klass.getSimpleName() + "\");}\n");
                                }
                                builder.append("        return result;\n");
                            }
                            builder.append("    }\n\n");

                            methodCount++;
                        }
                    }
                }
            }
            builder.append("}\n");
            builder.append("// " + methodCount + " methods generated.\n");

            File outputTarget;
            if (platform == PLATFORM_CLIJ2) {
                outputTarget = new File("../clij2/src/main/java/net/haesleinhuepf/clij2/CLIJ2Ops.java");
            } else if (platform == PLATFORM_CLIJx) {
                outputTarget = new File("../clijx/src/main/java/net/haesleinhuepf/clijx/utilities/CLIJxOps.java");
            } else if (platform == PLATFORM_CLESPERANTOJ_CAMEL) {
                outputTarget = new File("../assistant/src/main/java/net/clesperanto/javaprototype/CamelInterface.java");
            } else { // if (platform == PLATFORM_CLESPERANTOJ_SNAKE) {


                FileWriter writer = new FileWriter("../assistant/src/main/resources/clEsperantoJ_compatibility.config");
                writer.write(clEsperantoJcompatibilityList.toString());
                writer.close();

                outputTarget = new File("../assistant/src/main/java/net/clesperanto/javaprototype/SnakeInterface.java");
            }
            FileWriter writer = new FileWriter(outputTarget);
            writer.write(builder.toString());
            writer.close();
        }
    }

    public static Iterable<? extends Method> sort(Method[] methods) {
        ArrayList<Method> methodList = new ArrayList<>();
        for (int i = 0; i < methods.length; i++) {
            methodList.add(methods[i]);
        }
        Collections.sort(methodList, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return methodList;
    }

    static boolean blockListOk(Class klass, Method method) {
        String searchString = klass.getSimpleName() + "." + method.getName();
        if (CLIJxPlugins.blockList.contains(";" + searchString + ";")) {
            //System.out.println("BLOCKING " + searchString);
            return false;
        }
        return true;
    }

    public static String[] guessParameterNames(CLIJMacroPluginService service, String methodName, String[] parametersHeader) {
        CLIJMacroPlugin plugin = findPlugin(service, methodName);

        //if(methodName.contains("paste"))
        //{
        //    System.out.println("P " + plugin + " " + Arrays.toString(parametersHeader));
        //}

        if (plugin != null) {
            String[] parameters = plugin.getParameterHelpText().split(",");
            if (parameters.length != parametersHeader.length) {
                //if(methodName.contains("generate")) {
                //          System.out.println("Leaving 1");
                //}
                return new String[0];
            }
            String[] parameterNames = new String[parametersHeader.length];
            for (int i = 0; i < parameters.length; i++) {
                int parameterNameIndex = 1;
                String typeA = parameters[i].trim().split(" ")[0];
                if (typeA.toLowerCase().compareTo("byref") == 0) {
                    typeA = parameters[i].trim().split(" ")[1];
                    parameterNameIndex = 2;
                }

                String typeB = parametersHeader[i].trim().split(" ")[0];


                if (
                        ((typeA.compareTo("String") == 0 || typeA.compareTo("Image") == 0) && (typeB.compareTo("ClearCLBuffer") == 0 || typeB.compareTo("ClearCLImage") == 0 || typeB.compareTo("ClearCLImageInterface") == 0)) ||
                        ((
                                typeA.compareTo("Number") == 0) && (typeB.compareTo("Double") == 0 ||
                                typeB.compareTo("Float") == 0 || typeB.compareTo("Integer") == 0)) ||
                                (typeA.compareTo("Boolean") == 0 && typeB.compareTo("Boolean") == 0) ||
                                (typeA.compareTo("String") == 0 && typeB.compareTo("String") == 0)
                ) {
                    parameterNames[i] = parameters[i].trim().split(" ")[parameterNameIndex ];
                } else {

                    //if(methodName.contains("generate")) {
                    //    System.out.println("Leaving because " + methodName + "  " + typeA + " != " + typeB);
                    //}
                    return new String[0];
                }

            }
            //if(methodName.contains("generate")) {
            //    System.out.println(Arrays.toString(parameterNames));
            //}
            return parameterNames;
        }
//        if(methodName.contains("paste")) {
//            System.out.println("nothing");
//        }

        return new String[0];
    }

    static String typeToString(Class klass) {
        String result = "" + klass.getSimpleName();
        if (result.compareTo("[F") == 0) {
            return "float[]";
        }
        return result;
    }

    static CLIJMacroPlugin findPlugin(CLIJMacroPluginService service, String methodName) {
        String[] potentialMethodNames = {
                "CLIJ2_" + methodName,
                "CLIJ2_" + methodName + "2D",
                "CLIJ2_" + methodName + "3D",
                "CLIJ2_" + methodName + "Images",
                "CLIJ2_" + methodName.replace( "Sphere", "2DSphere"),
                "CLIJ2_" + methodName.replace( "Sphere", "3DSphere"),
                "CLIJ2_" + methodName.replace( "Box", "2DBox"),
                "CLIJ2_" + methodName.replace( "Box", "3DBox"),
                "CLIJ2_" + methodName.replace( "Pixels", "OfAllPixels"),
                "CLIJ2_" + methodName.replace( "SliceBySlice", "3DSliceBySlice"),
                "CLIJ2_" + methodName.replace( "3DSliceBySlice", "SliceBySlice"),
                "CLIJx_" + methodName,
                "CLIJx_" + methodName + "2D",
                "CLIJx_" + methodName + "3D",
                "CLIJx_" + methodName + "Images",
                "CLIJx_" + methodName.replace( "Sphere", "2DSphere"),
                "CLIJx_" + methodName.replace( "Sphere", "3DSphere"),
                "CLIJx_" + methodName.replace( "Box", "2DBox"),
                "CLIJx_" + methodName.replace( "Box", "3DBox"),
                "CLIJx_" + methodName.replace( "Pixels", "OfAllPixels"),
                "CLIJx_" + methodName.replace( "SliceBySlice", "3DSliceBySlice"),
                "CLIJx_" + methodName.replace( "3DSliceBySlice", "SliceBySlice")

        };

        for (String name : potentialMethodNames) {
            name = findName(service, name);
            CLIJMacroPlugin plugin = service.getCLIJMacroPlugin(name);
            if (plugin != null) {
                return plugin;
            }
        }
        return null;
    }

    static String findDocumentation(CLIJMacroPluginService service, String methodName, boolean deprecated) {
        if (methodName.endsWith("IJ")) {
            return "This method is deprecated. Consider using " + methodName.replace("IJ", "Box") + " or " + methodName.replace("IJ", "Sphere") + " instead.";
        }

        CLIJMacroPlugin plugin = findPlugin(service, methodName);

        if (plugin != null) {
            if (plugin instanceof OffersDocumentation) {
                return ((OffersDocumentation) plugin).getDescription();
            } else {
                return plugin.getParameterHelpText();
            }
        }

        System.out.println("No documentation found for " + methodName + (deprecated?" (deprecated)":""));
        return "";
    }

    protected static String findName(CLIJMacroPluginService service, String name) {
        for (String potentialName : service.getCLIJMethodNames()) {
            if (potentialName.toLowerCase().compareTo(name.toLowerCase()) == 0) {
                return potentialName;
            }
        }
        return name;
    }

}
