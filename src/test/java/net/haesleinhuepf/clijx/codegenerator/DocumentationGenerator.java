package net.haesleinhuepf.clijx.codegenerator;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPluginService;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasLicense;
import net.haesleinhuepf.clijx.assistant.scriptgenerator.PyclesperantoGenerator;
import net.haesleinhuepf.clijx.assistant.utilities.AssistantUtilities;
import org.scijava.Context;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static net.haesleinhuepf.clijx.codegenerator.OpGenerator.*;

public class DocumentationGenerator {

    private final static String HTTP_ROOT = "https://clij.github.io/clij2-docs/";

    private static class DocumentationItem {
        public String parametersHeader;
        public String parametersCall;
        public String returnType;
        Class klass;
        String methodName;
        String parametersJava;
        String parametersMacro;
        String description;
        String author;
        String license;
        String categories;
    }

    private static boolean isCLIJ2;
    private static CLIJMacroPluginService service;
    private static CombinedUsageStats combinedUsageStats;

    public static void main(String ... args) throws IOException {
        combinedUsageStats = new CombinedUsageStats("../clij2-docs/src/main/macro/", "../scripts_hidden/", "../scripts/");
        service = new Context(CLIJMacroPluginService.class).getService(CLIJMacroPluginService.class);
        boolean[] booleans = new boolean[]{false, true};

        for (int b = 0; b < booleans.length; b++) {
            isCLIJ2 = booleans[b];
            HashMap<String, DocumentationItem> methodMap = new HashMap<String, DocumentationItem>();

            String processedNames = ";";

            int methodCount = 0;
            for (Class klass : CLIJxPlugins.classes) {
                boolean has_static_methods = false;
                for (Method method : sort(klass.getMethods())) {
                    if (Modifier.isStatic(method.getModifiers()) &&
                            Modifier.isPublic(method.getModifiers()) &&
                            method.getParameterCount() > 0 &&
                            (method.getParameters()[0].getType() == CLIJ.class ||
                            method.getParameters()[0].getType() == CLIJ2.class||
                            method.getParameters()[0].getType() == CLIJx.class) &&
                            OpGenerator.blockListOk(klass, method) //&&
                            //((!processedNames.contains(";" + method.getName()  + ";")) || (methodMap.get(method.getName()) != null && methodMap.get(method.getName()).description == null))
                    ) {
                        has_static_methods = true;
                        String methodName = method.getName();
                        String returnType = typeToString(method.getReturnType());
                        String parametersHeader = "";
                        String parametersCall = "";

                        if (methodName.startsWith("getMax")) {
                            System.out.println("Reading " + methodName);
                        }

                        for (Parameter parameter : method.getParameters()) {
                            if (parametersCall.length() == 0) { // first parameter
                                parametersCall = "clij";
                                continue;
                            }

                            if (parametersHeader.length() > 0) {
                                parametersHeader = parametersHeader + ", ";
                            }
                            parametersHeader = parametersHeader + parameter.getType().getSimpleName() + " " + parameter.getName();
                            parametersCall = parametersCall + ", " + parameter.getName();
                        }

                        String[] variableNames = guessParameterNames(service, methodName, parametersHeader.split(","));
                        if (variableNames.length > 0) {
                            for (int i = 0; i < variableNames.length; i++) {
                                parametersCall = parametersCall.replace("arg" + (i + 1), variableNames[i]);
                                parametersHeader = parametersHeader.replace("arg" + (i + 1), variableNames[i]);
                            }
                        }

                        if (!parametersHeader.contains("ClearCLImage ")) {// && !parametersHeader.contains("arg1")) { // we document only  buffer methods for now
                            CLIJMacroPlugin plugin = findPlugin(service, methodName);

                            DocumentationItem item = new DocumentationItem();

                            if (plugin != null) {
                                item.parametersMacro = plugin.getParameterHelpText();
                                if (plugin instanceof OffersDocumentation) {
                                    item.description = ((OffersDocumentation) plugin).getDescription();
                                    item.description = item.description.replace("Parameters\n----------", "### Parameters\n");
                                    item.description = item.description.replace("deprecated", "<b>deprecated</b>");
                                }
                                if (plugin instanceof HasAuthor) {
                                    item.author = ((HasAuthor) plugin).getAuthorName();
                                }
                                if (plugin instanceof HasLicense) {
                                    item.license = ((HasLicense) plugin).getLicense();
                                }
                                if (plugin instanceof IsCategorized) {
                                    item.categories = ((IsCategorized) plugin).getCategories();
                                }
                            }

                            item.klass = klass;
                            item.methodName = methodName;
                            item.parametersJava = parametersHeader;
                            item.parametersHeader = parametersHeader;
                            item.parametersCall = parametersCall;
                            item.returnType = returnType;

                            //if (methodName.contains("histogram")) {
                            //    System.out.println("Parsed " + methodName);
                            //}

                            if (methodMap.containsKey(methodName)) {
                                if (!item.parametersCall.contains("arg1")) {

                                    methodMap.remove(methodName);
                                    methodMap.put(methodName, item);
                                }
                            } else {
                                methodMap.put(methodName, item);
                            }

                            methodCount++;
                            processedNames = processedNames + method.getName() + ";";
                        } else {
                            //if (methodName.contains("getMax")) {
                            System.out.println("NOT Parsed1 " + methodName);
                            //}
                        }
                    } else {
                        if (method.getName().contains("getMax")) {
                            System.out.println("NOT Parsed2 " + method.getName());
                        }
                    }
                }
                if (!has_static_methods) {
                    DocumentationItem item = new DocumentationItem();

                    item.klass = klass;
                    item.methodName = klass.getSimpleName().substring(0,1).toLowerCase() + klass.getSimpleName().substring(1);
                    CLIJMacroPlugin plugin = findPlugin(service, item.methodName);

                    if (plugin != null) {
                        System.out.println("Adding " + klass + " which doesn't have static methods.");

                        item.parametersMacro = plugin.getParameterHelpText();
                        if (plugin instanceof OffersDocumentation) {
                            item.description = ((OffersDocumentation) plugin).getDescription();
                            item.description = item.description.replace("deprecated", "<b>deprecated</b>");
                        }
                        if (plugin instanceof HasAuthor) {
                            item.author = ((HasAuthor) plugin).getAuthorName();
                        }
                        if (plugin instanceof HasLicense) {
                            item.license = ((HasLicense) plugin).getLicense();
                        }
                        if (plugin instanceof IsCategorized) {
                            item.categories = ((IsCategorized) plugin).getCategories();
                        }

                        if (methodMap.containsKey(item.methodName)) {
                            if (!item.parametersCall.contains("arg1")) {

                                methodMap.remove(item.methodName);
                                methodMap.put(item.methodName, item);
                            }
                        } else {
                            methodMap.put(item.methodName, item);
                        }

                        methodCount++;
                        processedNames = processedNames + item.methodName + ";";

                    } else {
                        System.out.println("plugin not found " + item.methodName);
                    }
                }
            }

            ArrayList<String> names = new ArrayList<String>();
            names.addAll(methodMap.keySet());
            Collections.sort(names);


            // auto-completion list
            buildAutoCompletion(names, methodMap);
            buildReference(names, methodMap, "", "");
            buildReference(names, methodMap, "binary", " for processing binary images.");
            buildReference(names, methodMap, "filter", " for filtering images.");
            buildReference(names, methodMap, "matrix", " for working with matrices.");
            buildReference(names, methodMap, "label", " for processing labelled images.");
            buildReference(names, methodMap, "graph", " for processing graphs.");
            buildReference(names, methodMap, "detection", " for spot detection.");
            buildReference(names, methodMap, "segmentation", " for segmenting images.");
            buildReference(names, methodMap, "transform", " for transforming images in space.");
            buildReference(names, methodMap, "math", " for performing general mathematical operations on images.");
            buildReference(names, methodMap, "measurement", " for performing measurements in images.");
            buildReference(names, methodMap, "project", " for projecting images.");
            buildReference(names, methodMap, "pyclesperanto", " compatible with [pyclesperanto](https://github.com/clEsperanto/pyclesperanto_prototype).");
            buildReference(names, methodMap, "CLIc", " compatible with [CLIc](https://github.com/clEsperanto/CLIc_prototype).");

            buildIndiviualOperationReferences(names, methodMap);
        }
    }

    private static void buildIndiviualOperationReferences(ArrayList<String> names, HashMap<String, DocumentationItem> methodMap) throws IOException {

        for (String sortedName : names) {
            //if (sortedName.contains("generate")) {
            //    System.out.println("Name : " + sortedName);
            //}
            StringBuilder builder = new StringBuilder();
            DocumentationItem item = methodMap.get(sortedName);
            builder.append("## " + item.methodName + "\n");

            boolean isClij = false;
            boolean isClij2 = false;
            boolean isClijx = false;

            if (item.klass == Kernels.class) {
                builder.append("![Image](images/mini_clij1_logo.png)");
            }
            else {
                /*
                if (item.klass.getPackage().toString().contains("clij2") || item.klass.getPackage().toString().contains("clijx")) {
                    if (service.getCLIJMacroPlugin("CLIJ_" + item.methodName) != null) {
                        builder.append("![Image](images/mini_clij1_logo.png)");
                    }
                    builder.append("![Image](images/mini_clij2_logo.png)");
                    builder.append("![Image](images/mini_clijx_logo.png)");
                }
                if (item.klass.getPackage().toString().contains("clijx")) {
                    builder.append("![Image](images/mini_clijx_logo.png)");
                }*/
                if (service.getCLIJMacroPlugin("CLIJ_" + item.methodName) != null) {
                    builder.append("<img src=\"images/mini_clij1_logo.png\"/>");
                } else {
                    builder.append("<img src=\"images/mini_empty_logo.png\"/>");
                }
                if (service.getCLIJMacroPlugin("CLIJ2_" + item.methodName) != null) {
                    builder.append("<img src=\"images/mini_clij2_logo.png\"/>");
                } else {
                    builder.append("<img src=\"images/mini_empty_logo.png\"/>");
                }
                if (service.getCLIJMacroPlugin("CLIJx_" + item.methodName) != null) {
                    builder.append("<img src=\"images/mini_clijx_logo.png\"/>");
                } else {
                    builder.append("<img src=\"images/mini_empty_logo.png\"/>");
                }
            }
            if (isPartOfPyClEsperanto(item.methodName)) {
                builder.append("<img src=\"images/mini_cle_logo.png\"/>");
            } else {
                builder.append("<img src=\"images/mini_empty_logo.png\"/>");
            }


            builder.append("\n\n");
            if (item.author != null && item.author.length() > 0) {
                builder.append("By " + item.author + "\n\n");
            }

            if (isClij && (!isClij2)) {
                builder.append("<b>This method is deprecated.</b>\n\n");
            }
            if ((!isClij) && (!isClij2) && isClijx) {
                builder.append("<b>This method is experimental.</b>\n\n");
            }

            builder.append(item.description);

            if (item.categories != null) {
                String[] categories = item.categories.split(",");
                List<String> list = new ArrayList<>();
                for (int i = 0; i < categories.length; i++) {
                    list.add(categories[i]);
                }
                Collections.sort(list);
                if (categories.length > 1) {
                    builder.append("\n\nCategories: ");
                } else {
                    builder.append("\n\nCategory: ");
                }
                builder.append(linkCategories(list));
            }

            if (item.klass.getPackage().toString().contains(".clij2.")) {
                //if (item.methodName.compareTo("generateTouchMatrix") == 0 ) {
                //    System.out.println("Search for "+ item.methodName);
                //}
                HashMap<String, Integer> following = combinedUsageStats.getFollowing(item.methodName);
                if (following.size() > 0) {
                    builder.append("\n\n");
                    builder.append("### " + item.methodName + " often follows after\n");
                    for (String key : following.keySet()) {
                        builder.append("* <a href=\"reference_" + key + "\">" + key + "</a> (" + following.get(key) + ")\n");
                    }
                }

                HashMap<String, Integer> followers = combinedUsageStats.getFollowersOf(item.methodName);
                if (followers.size() > 0) {
                    builder.append("\n\n");
                    builder.append("### " + item.methodName + " is often followed by\n");
                    for (String key : followers.keySet()) {
                        builder.append("* <a href=\"reference_" + key + "\">" + key + "</a> (" + followers.get(key) + ")\n");
                    }
                }
            }


            builder.append("\n\n");

            if (item.parametersMacro != null) {
                builder.append("### Usage in ImageJ macro\n");
                builder.append("```\n");
                builder.append("Ext.CLIJ");
                if (item.klass.getPackage().toString().contains(".clij2.")) {
                    builder.append("2");
                } else if (item.klass != Kernels.class) {
                    builder.append("x");
                }
                builder.append("_" + item.methodName + "(" + item.parametersMacro.replace("ByRef ", "") + ");\n");
                builder.append("```\n");
                builder.append("\n\n");
            }

            if (item.parametersCall != null && !item.parametersCall.contains("arg1") && !new String("" + item.description).toLowerCase().contains("deprecated")) {
                String javaCode = generateJavaExampleCode(item.klass, item.methodName, item.parametersHeader, item.parametersCall, item.returnType);
                String matlabCode = generateMatlabExampleCode(item.klass, item.methodName, item.parametersHeader, item.parametersCall, item.returnType);
                String icyCode = generateIcyExampleCode(item.klass, item.methodName, item.parametersHeader, item.parametersCall, item.returnType);
                String cle_python_code = generateClePythonCode(item.klass, item.methodName, item.parametersHeader, item.parametersCall, item.returnType);
                String clic_code = generateCLIcCode(item.klass, item.methodName, item.parametersHeader, item.parametersCall, item.returnType);

                if (javaCode != null || matlabCode != null || icyCode != null) {
                     builder.append("### Usage in object oriented programming languages\n\n");

                     if (javaCode != null) {
                         builder.append(codeBlock("Java", javaCode));
                     }

                     if (matlabCode != null) {
                         builder.append(codeBlock("Matlab", matlabCode));
                     }

                     if (icyCode != null) {
                         builder.append(codeBlock("Icy JavaScript", icyCode));
                     }

                    if (cle_python_code != null) {
                        builder.append(codeBlock("clEsperanto Python (experimental)", cle_python_code));
                    }

                    if (clic_code != null) {
                        builder.append(codeBlock("clEsperanto CLIc C++ (experimental)", clic_code));
                    }
                    builder.append("\n\n");
                }
            }

            String linkToExamples =
                searchForExampleScripts("CLIJ2_" + item.methodName, "../clij2-docs/src/main/macro/", "https://github.com/clij/clij2-docs/blob/master/src/main/macro/", "macro") +
                searchForExampleScripts("clij2." + item.methodName, "../clatlab/src/main/matlab/", "https://github.com/clij/clatlab/blob/master/src/main/matlab/", "matlab") +
                searchForExampleScripts("clij2." + item.methodName, "../clicy/src/main/javascript/", "https://github.com/clij/clicy/blob/master/src/main/javascript/", "javascript") +
                searchForExampleScripts("clij2." + item.methodName, "../clij2-docs/src/main/javascript/", "https://github.com/clij/clij2-docs/blob/master/src/main/javascript/", "javascript") +
                searchForExampleScripts("clij2." + item.methodName, "../clij2-docs/src/main/groovy/", "https://github.com/clij/clij2-docs/blob/master/src/main/groovy/", "groovy") +
                searchForExampleScripts("clij2." + item.methodName, "../clij2-docs/src/main/beanshell/", "https://github.com/clij/clij2-docs/blob/master/src/main/beanshell/", "beanshell") +
                searchForExampleScripts("clij2." + item.methodName, "../clij2-docs/src/main/jython/", "https://github.com/clij/clij2-docs/blob/master/src/main/jython/", "jython") +
                        "";

            String[] pyclesperanto_demo_subfolders = {
                    "demo/basics",
                    "demo/napari_gui",
                    "demo/neighbors",
                    "demo/segmentation",
                    "demo/tissues",
                    "demo/transforms",
                    "demo/tribolium_morphometry",
                    "benchmarks"
            };
            for (String subfolder : pyclesperanto_demo_subfolders) {
                linkToExamples = linkToExamples +
                        searchForExampleScripts("cle." + new PyclesperantoGenerator(false).pythonize(item.methodName), "../pyclesperanto_prototype/" + subfolder + "/", "https://github.com/clEsperanto/pyclesperanto_prototype/tree/master/" + subfolder + "/", "python", ".py");
            }




            String exampleNotebooks =
                    searchForExampleScripts("CLIJ2_" + item.methodName, "../clij2-docs/md/", "https://clij.github.io/clij2-docs/md/", "macro");
            for (String subfolder : pyclesperanto_demo_subfolders) {
                exampleNotebooks = exampleNotebooks +
                        searchForExampleScripts("cle." + new PyclesperantoGenerator(false).pythonize(item.methodName), "../pyclesperanto_prototype/" + subfolder + "/", "https://github.com/clEsperanto/pyclesperanto_prototype/tree/master/" + subfolder + "/", "python", ".ipynb");
            }


            if(exampleNotebooks.length() > 0) {
                builder.append("\n\n### Example notebooks\n" + exampleNotebooks + "\n\n");
            }


            if(linkToExamples.length() > 0) {
                builder.append("\n\n### Example scripts\n" + linkToExamples + "\n\n");
            }

            if (item.license != null && item.license.length() > 0) {
                builder.append("\n\n### License terms\n");
                builder.append(item.license.replace("\n", "  \n") + "\n\n");
            }


            builder.append("[Back to CLIJ2 reference](https://clij.github.io/clij2-docs/reference)\n" +
                    "[Back to CLIJ2 documentation](https://clij.github.io/clij2-docs)\n" +
                    "\n" +
                    "[Imprint](https://clij.github.io/imprint)\n");

            File outputTarget = new File("../clij2-docs/reference_" + item.methodName + ".md");
            FileWriter writer = new FileWriter(outputTarget);
            writer.write(builder.toString());
            writer.close();
        }
    }

    private static String generateCLIcCode(Class klass, String methodName, String parametersHeader, String parametersCall, String returnType) {
        if (AssistantUtilities.isClicCompatible(methodName)) {

            StringBuilder code = new StringBuilder();

            String pythonized_method_name = new PyclesperantoGenerator(false).pythonize(methodName);

            code.append("<pre class=\"highlight\">");
            if (
                (!addFile(code, new File("../CLIc_prototype/test/" + pythonized_method_name + "_functions_test.cpp"))) &&
                (!addFile(code, new File("../CLIc_prototype/test/" + pythonized_method_name + "_test.cpp")))
            ) {
                return null;
            }
            code.append("</pre>\n\n");

            return code.toString();
        }

        return null;
    }

    private static boolean addFile(StringBuilder code, File file) {
        if (!file.exists()) {
            return false;
        }

        try {
            String text = new String(Files.readAllBytes(Paths.get(file.toURI())));
            String search = "\n    // Initialise GPU";
            if (text.contains(search)) {
                text = search + text.split(search)[1];
            }
            text = text.replace("\n    ", "\n");
            text = text.split("// Verify output")[0];

            text = text.replace("<", "&lt;");
            text = text.replace(">", "&gt;");

            code.append(text);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String generateClePythonCode(Class klass, String methodName, String parametersHeader, String parametersCall, String returnType) {
        if (AssistantUtilities.isCleCompatible(methodName)) {

            parametersCall = parametersCall
                    .replace("clij, ", "")
                    .replace("clij2, ", "")
                    .replace("clijx, ", "");

            StringBuilder code = new StringBuilder();

            code.append("<pre class=\"highlight\">");
            code.append("import pyclesperanto_prototype as cle\n" +
                    "\n" +
                    "cle." + new PyclesperantoGenerator(false).pythonize(methodName) + "(" + parametersCall + ")\n");
            code.append("\n</pre>\n\n");

            return code.toString();
        }
        return null;
    }

    private static boolean isPartOfPyClEsperanto(String methodName) {
        return AssistantUtilities.isCleCompatible(methodName);
    }

    private static boolean isPartOfClEsperantoCLIc(String methodName) {
        return AssistantUtilities.isClicCompatible(methodName);
    }

    private static String codeBlock(String headline, String text) {
        StringBuilder code = new StringBuilder();

        code.append("\n\n<details>\n\n");
        code.append("<summary>\n");
        code.append("" + headline + "\n");

        code.append("</summary>\n");
        code.append(text);
        code.append("\n\n</details>\n\n");

        return code.toString();
    }

    private static String linkCategories(Iterable<String> list) {
        StringBuilder builder = new StringBuilder();

        int count = 0;
        for (String entry : list) {
            if (count > 0) {
                builder.append(", ");
            }

            if (entry.toLowerCase().contains("binary")) {
                builder.append("[Binary](https://clij.github.io/clij2-docs/reference__binary)");
            } else if (entry.toLowerCase().contains("filter")) {
                builder.append("[Filter](https://clij.github.io/clij2-docs/reference__filter)");
            } else if (entry.toLowerCase().contains("graph")) {
                builder.append("[Graphs](https://clij.github.io/clij2-docs/reference__graph)");
            } else if (entry.toLowerCase().contains("label")) {
                builder.append("[Labels](https://clij.github.io/clij2-docs/reference__label)");
            } else if (entry.toLowerCase().contains("math")) {
                builder.append("[Math](https://clij.github.io/clij2-docs/reference__math)");
            } else if (entry.toLowerCase().contains("matrix")) {
                builder.append("[Matrices](https://clij.github.io/clij2-docs/reference__matrix)");
            } else if (entry.toLowerCase().contains("measurement")) {
                builder.append("[Measurements](https://clij.github.io/clij2-docs/reference__measurement)");
            } else if (entry.toLowerCase().contains("project")) {
                builder.append("[Projections](https://clij.github.io/clij2-docs/reference__project)");
            } else if (entry.toLowerCase().contains("transform")) {
                builder.append("[Transformations](https://clij.github.io/clij2-docs/reference__transform)");
            } else if (entry.toLowerCase().contains("segmentation")) {
                builder.append("[Segmentation](https://clij.github.io/clij2-docs/reference__segmentation)");
            } else if (entry.toLowerCase().contains("detection")) {
                builder.append("[Detection](https://clij.github.io/clij2-docs/reference__detection)");
            } else if (entry.toLowerCase().contains("pyclesperanto")) {
                builder.append("[Detection](https://clij.github.io/clij2-docs/reference__pyclesperanto)");
            } else {
                builder.append(entry);
            }

            count++;
        }


        return builder.toString();
    }

    private static String generateJavaExampleCode(Class klass, String methodName, String parametersWithType, String parameters, String returnType) {
        parameters = parameters.replace("clij, ", "");
        parameters = parameters.replace("clij2, ", "");
        parameters = parameters.replace("clijx, ", "");

        // just some example numbers for example code
        float[] floatParameterValues = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
        int[] integerParameterValues = {10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170};
        boolean[] booleanParameterValues = {true, false, false, true};
        int floatParameterIndex = 0;
        int integerParameterIndex = 0;
        int booleanParameterIndex = 0;

        StringBuilder code = new StringBuilder();

        String clijObjectName = "clij2";

        code.append("<pre class=\"highlight\">");
        code.append("// init CLIJ and GPU\n");

        if (klass.getPackage().toString().contains(".clij2.")) {
            code.append("import net.haesleinhuepf.clij2.CLIJ2;\n");
            code.append("import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;\n");
            code.append("CLIJ2 clij2 = CLIJ2.getInstance();\n\n");


        } else {
            code.append("import net.haesleinhuepf.clijx.CLIJx;\n");
            code.append("import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;\n");
            code.append("CLIJx clijx = CLIJx.getInstance();\n\n");

            clijObjectName = "clijx";
        }
        code.append("// get input parameters\n");
        String[] parametersArray = parametersWithType.split(",");
        String inputImage = "";
        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isInputParameter(parameter)) {
                if (inputImage.length() == 0) {
                    inputImage = parameterName;
                }
                code.append("ClearCLBuffer " + parameterName + " = " + clijObjectName + ".push(" + parameterName + "ImagePlus);\n");
            } else if (isOutputParameter(parameter)) {
                code.append(createOutputImageCodeJava(methodName, parameterName, inputImage, clijObjectName));
            } else if (parameter.startsWith("Float")) {
                code.append("float " + parameterName + " = " + floatParameterValues[floatParameterIndex]+ ";\n");
                floatParameterIndex++;
            } else if (parameter.startsWith("Integer")) {
                code.append("int " + parameterName + " = " + integerParameterValues[integerParameterIndex] + ";\n");
                integerParameterIndex++;
            } else if (parameter.startsWith("Boolean")) {
                code.append("boolean " + parameterName + " = " + booleanParameterValues[booleanParameterIndex] + ";\n");
                booleanParameterIndex++;
            } else if (parameter.startsWith("AffineTransform3D")) {
                code.append("import net.imglib2.realtransform.AffineTransform3D;\n");
                code.append("at = new AffineTransform3D();\n" +
                        "at.translate(4, 0, 0);\n");
            } else if (parameter.startsWith("AffineTransform2D")) {
                code.append("import net.imglib2.realtransform.AffineTransform2D;\n");
                code.append("at = new AffineTransform2D();\n" +
                        "at.translate(4, 0);\n");
            }
        }


        code.append("</pre>\n\n<pre class=\"highlight\">");
        code.append("\n// Execute operation on GPU\n");
        if (returnType.toLowerCase().compareTo("boolean") != 0) {
            code.append(returnType + " result" + methodName.substring(0,1).toUpperCase() + methodName.substring(1, methodName.length()) + " = ");
        }
        code.append(clijObjectName + "." + methodName + "(" + parameters + ");\n");
        code.append("</pre>\n\n<pre class=\"highlight\">");

        code.append("\n// show result\n");
        if (returnType.toLowerCase().compareTo("boolean") != 0) {
            code.append("System.out.println(result" + methodName.substring(0,1).toUpperCase() + methodName.substring(1, methodName.length()) + ");\n");
        }

        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isOutputParameter(parameter)) {
                code.append(parameterName + "ImagePlus = " + clijObjectName + ".pull(" + parameterName + ");\n");
                code.append(parameterName + "ImagePlus.show();\n");
            }
        }

        code.append("\n// cleanup memory on GPU\n");
        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isInputParameter(parameter) || isOutputParameter(parameter)) {
                code.append(clijObjectName + ".release(" + parameterName + ");\n");
            }
        }
        code.append("</pre>");

        return code.toString();
    }


    private static String generateIcyExampleCode(Class klass, String methodName, String parametersWithType, String parameters, String returnType) {
        parameters = parameters.replace("clij, ", "");
        parameters = parameters.replace("clij2, ", "");
        parameters = parameters.replace("clijx, ", "");

        // just some example numbers for example code
        float[] floatParameterValues = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
        int[] integerParameterValues = {10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170};
        boolean[] booleanParameterValues = {true, false, false, true};
        int floatParameterIndex = 0;
        int integerParameterIndex = 0;
        int booleanParameterIndex = 0;

        StringBuilder code = new StringBuilder();
        String clijObjectName = "clij2";;

        code.append("<pre class=\"highlight\">");

        code.append("// init CLIJ and GPU\n");


        if (klass.getPackage().toString().contains(".clij2.")) {
            code.append("importClass(net.haesleinhuepf.clicy.CLICY);\n" +
                    "importClass(Packages.icy.main.Icy);\n\n" +
                    "clij2 = CLICY.getInstance();\n\n");

        } else {
            return null;
        }
        code.append("// get input parameters\n");
        String[] parametersArray = parametersWithType.split(",");
        String inputImage = "";
        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isInputParameter(parameter)) {
                if (inputImage.length() == 0) {
                    inputImage = parameterName;
                }
                code.append(parameterName + "_sequence = getSequence();\n");
                code.append(parameterName + " = " + clijObjectName + ".pushSequence(" + parameterName + "_sequence);\n");
            } else if (isOutputParameter(parameter)) {
                code.append(createOutputImageCodeMatlabIcy(methodName, parameterName, inputImage, clijObjectName));
            } else if (parameter.startsWith("Float")) {
                code.append(parameterName + " = " + floatParameterValues[floatParameterIndex]+ ";\n");
                floatParameterIndex++;
            } else if (parameter.startsWith("Integer")) {
                code.append(parameterName + " = " + integerParameterValues[integerParameterIndex] + ";\n");
                integerParameterIndex++;
            } else if (parameter.startsWith("Boolean")) {
                code.append(parameterName + " = " + booleanParameterValues[booleanParameterIndex] + ";\n");
                booleanParameterIndex++;
            } else if (parameter.startsWith("AffineTransform3D")) {
                return null;
            } else if (parameter.startsWith("AffineTransform2D")) {
                return null;
            }
        }


        code.append("</pre>\n\n<pre class=\"highlight\">");
        code.append("\n// Execute operation on GPU\n");
        if (returnType.toLowerCase().compareTo("boolean") != 0) {
            code.append(returnType + " result" + methodName.substring(0,1).toUpperCase() + methodName.substring(1, methodName.length()) + " = ");
        }
        code.append(clijObjectName + "." + methodName + "(" + parameters + ");\n");
        code.append("</pre>\n\n<pre class=\"highlight\">");

        code.append("\n// show result\n");
        if (returnType.toLowerCase().compareTo("boolean") != 0) {
            code.append("System.out.println(result" + methodName.substring(0,1).toUpperCase() + methodName.substring(1, methodName.length()) + ");\n");
        }

        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isOutputParameter(parameter)) {
                code.append(parameterName + "_sequence = " + clijObjectName + ".pullSequence(" + parameterName + ")\n");
                code.append("Icy.addSequence("  + parameterName + "_sequence);");
            }
        }

        code.append("\n// cleanup memory on GPU\n");
        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isInputParameter(parameter) || isOutputParameter(parameter)) {
                code.append(clijObjectName + ".release(" + parameterName + ");\n");
            }
        }
        code.append("</pre>");

        return code.toString();
    }


    private static String generateMatlabExampleCode(Class klass, String methodName, String parametersWithType, String parameters, String returnType) {
        parameters = parameters.replace("clij, ", "");
        parameters = parameters.replace("clij2, ", "");
        parameters = parameters.replace("clijx, ", "");

        // just some example numbers for example code
        float[] floatParameterValues = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
        int[] integerParameterValues = {10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170};
        boolean[] booleanParameterValues = {true, false, false, true};
        int floatParameterIndex = 0;
        int integerParameterIndex = 0;
        int booleanParameterIndex = 0;

        StringBuilder code = new StringBuilder();
        String clijObjectName = "clij2";


        code.append("<pre class=\"highlight\">");
        code.append("% init CLIJ and GPU\n");

        if (klass.getPackage().toString().contains(".clij2.")) {
            code.append(clijObjectName + " = init_clatlab();\n\n");

        } else {
            clijObjectName = "clijx";
            code.append(clijObjectName + " = init_clatlabx();\n\n");
        }
        code.append("% get input parameters\n");
        String[] parametersArray = parametersWithType.split(",");
        String inputImage = "";
        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isInputParameter(parameter)) {
                if (inputImage.length() == 0) {
                    inputImage = parameterName;
                }
                code.append(parameterName + " = " + clijObjectName + ".pushMat(" + parameterName + "_matrix);\n");
            } else if (isOutputParameter(parameter)) {
                code.append(createOutputImageCodeMatlabIcy(methodName, parameterName, inputImage, clijObjectName));
            } else if (parameter.startsWith("Float")) {
                code.append(parameterName + " = " + floatParameterValues[floatParameterIndex]+ ";\n");
                floatParameterIndex++;
            } else if (parameter.startsWith("Integer")) {
                code.append(parameterName + " = " + integerParameterValues[integerParameterIndex] + ";\n");
                integerParameterIndex++;
            } else if (parameter.startsWith("Boolean")) {
                code.append(parameterName + " = " + booleanParameterValues[booleanParameterIndex] + ";\n");
                booleanParameterIndex++;
            } else if (parameter.startsWith("AffineTransform3D")) {
                return null;
            } else if (parameter.startsWith("AffineTransform2D")) {
                return null;
            }
        }


        code.append("</pre>\n\n<pre class=\"highlight\">");
        code.append("\n% Execute operation on GPU\n");
        if (returnType.toLowerCase().compareTo("boolean") != 0) {
            code.append(returnType + " result" + methodName.substring(0,1).toUpperCase() + methodName.substring(1, methodName.length()) + " = ");
        }
        code.append(clijObjectName + "." + methodName + "(" + parameters + ");\n");
        code.append("</pre>\n\n<pre class=\"highlight\">");

        code.append("\n% show result\n");
        if (returnType.toLowerCase().compareTo("boolean") != 0) {
            code.append("System.out.println(result" + methodName.substring(0,1).toUpperCase() + methodName.substring(1, methodName.length()) + ");\n");
        }

        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isOutputParameter(parameter)) {
                code.append(parameterName + " = " + clijObjectName + ".pullMat(" + parameterName + ")\n");
            }
        }

        code.append("\n% cleanup memory on GPU\n");
        for (String parameter : parametersArray) {
            parameter = parameter.trim();
            String parameterName = parameter.split(" ")[1];
            if (isInputParameter(parameter) || isOutputParameter(parameter)) {
                code.append(clijObjectName + ".release(" + parameterName + ");\n");
            }
        }

        code.append("</pre>");

        return code.toString();
    }

    private static void buildReference(ArrayList<String> names, HashMap<String, DocumentationItem> methodMap, String search_string, String search_description) throws IOException {
        StringBuilder builder = new StringBuilder();

        String additional_header = "";

        if (search_string.length() > 0) {
            additional_header = " in category '" + search_string + "'";
        }

        builder.append("# CLIJ 1/2/x reference" + additional_header + "\n");
        builder.append("This reference contains all methods currently available in CLIJ, CLIJ2 and CLIJx" + search_description + ". Read more about [CLIJs release cycle](https://clij.github.io/clij-docs/release_cycle) \n\n");
        builder.append("__Please note:__ CLIJ is deprecated. [Make the transition to CLIJ2](https://clij.github.io/clij2-docs/clij2_transition_notes).");
        builder.append("\n\n");
        builder.append("<img src=\"images/mini_clij1_logo.png\" width=\"18\" height=\"18\"/> Method is available in CLIJ (deprecated release)  \n");
        builder.append("<img src=\"images/mini_clij2_logo.png\" width=\"18\" height=\"18\"/> Method is available in CLIJ2 (stable release)  \n");
        builder.append("<img src=\"images/mini_clijx_logo.png\" width=\"18\" height=\"18\"/> Method is available in CLIJx (experimental release)  \n");
        builder.append("<img src=\"images/mini_cle_logo.png\" width=\"18\" height=\"18\"/> Method is available in clEsperanto (experimental)  \n");

        builder.append("\n\n\n__Categories:__ ");
        builder.append(linkCategories(Arrays.asList(new String("Binary,Filter,Graphs,Labels,Math,Matrices,Measurements,Projections,Transformations,pyclesperanto,CLIc").split(","))));


        builder.append("\n\n##ALPHABET##\n\n");

        String firstChar = " ";
        String listOfChars = " A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z";
        int count = 0;
        for (String sortedName : names) {
            DocumentationItem item = methodMap.get(sortedName);
            //System.out.println(item.klass + " >>> " + item.categories);
            if (search_string.length() == 0 ||
                    (item.description != null && (item.description.toLowerCase().contains(search_string.toLowerCase()))) ||
                    (item.categories != null && item.categories.toLowerCase().contains(search_string.toLowerCase())) ||
                    (sortedName.toLowerCase().contains(search_description.toLowerCase())) ||
                    (search_string.compareTo("pyclesperanto") == 0 && isPartOfPyClEsperanto(item.methodName)) ||
                    (search_string.compareTo("CLIc") == 0 && isPartOfClEsperantoCLIc(item.methodName)))
            {

                if (sortedName.substring(0, 1).toUpperCase().compareTo(firstChar.trim()) != 0) {
                    firstChar = sortedName.substring(0, 1).toUpperCase();
                    builder.append("<a name=\"" + firstChar + "\"></a>\n");
                    builder.append("\n## " + firstChar + "\n");

                    listOfChars = listOfChars.replace(" " + firstChar, "<a href=\"#" + firstChar + "\">\\[" + firstChar + "\\]</a>");
                }

                StringBuilder itemBuilder = new StringBuilder();
                itemBuilder.append("### ");
                boolean takeIt = false;

                boolean isClij = false;
                boolean isClij2 = false;
                boolean isClijx = false;

                if (item.klass == Kernels.class) {
                    itemBuilder.append("<img src=\"images/mini_clij1_logo.png\" width=\"18\" height=\"18\"/>");
                    itemBuilder.append("<img src=\"images/mini_empty_logo.png\" width=\"18\" height=\"18\"/>");
                    itemBuilder.append("<img src=\"images/mini_empty_logo.png\" width=\"18\" height=\"18\"/>");
                    takeIt = true;
                } else {
                    if (service.getCLIJMacroPlugin("CLIJ_" + item.methodName) != null) {
                        isClij = true;
                        takeIt = true;
                        itemBuilder.append("<img src=\"images/mini_clij1_logo.png\" width=\"18\" height=\"18\"/>");
                    } else {
                        itemBuilder.append("<img src=\"images/mini_empty_logo.png\" width=\"18\" height=\"18\"/>");
                    }
                    if (service.getCLIJMacroPlugin("CLIJ2_" + item.methodName) != null) {
                        isClij2 = true;
                        takeIt = true;
                        itemBuilder.append("<img src=\"images/mini_clij2_logo.png\" width=\"18\" height=\"18\"/>");
                    } else {
                        itemBuilder.append("<img src=\"images/mini_empty_logo.png\" width=\"18\" height=\"18\"/>");
                    }
                    if (service.getCLIJMacroPlugin("CLIJx_" + item.methodName) != null) {
                        isClijx = true;
                        takeIt = true;
                        itemBuilder.append("<img src=\"images/mini_clijx_logo.png\" width=\"18\" height=\"18\"/>");
                    } else {
                        itemBuilder.append("<img src=\"images/mini_empty_logo.png\" width=\"18\" height=\"18\"/>");
                    }
                }
                if (isPartOfPyClEsperanto(item.methodName) || isPartOfClEsperantoCLIc(item.methodName)) {
                    itemBuilder.append("<img src=\"images/mini_cle_logo.png\" width=\"18\" height=\"18\"/>");
                } else {
                    itemBuilder.append("<img src=\"images/mini_empty_logo.png\" width=\"18\" height=\"18\"/>");
                }

                itemBuilder.append("<a href=\"" + HTTP_ROOT + "reference_" + item.methodName + "\">");
                itemBuilder.append(item.methodName);
                //if (item.klass == Kernels.class) {
                //    builder.append("'");
                //}
                if (isClij && (!isClij2)) {
                    itemBuilder.append(" (Deprecated)");
                }
                if ((!isClij) && (!isClij2) && isClijx) {
                    itemBuilder.append(" (Experimental)");
                }
                itemBuilder.append("</a>  \n");

                if (item.description != null) {
                    String shortDescription = item.description.split("\n\n")[0];
                    shortDescription = shortDescription.replace("\n", " ");
                    itemBuilder.append(shortDescription + "\n\n");
                }

                if (takeIt) {
                    builder.append(itemBuilder.toString());
                }
                count++;
            }
        }
        builder.append("" + count + " methods listed.\n");


        File outputTarget = new File("../clij2-docs/reference.md");
        if (search_string.length()> 0) {
            outputTarget = new File("../clij2-docs/reference__" + search_string + ".md");
        }
        FileWriter writer = new FileWriter(outputTarget);
        writer.write(builder.toString().replace("##ALPHABET##", listOfChars));
        writer.close();

    }

    private static void buildAutoCompletion(ArrayList<String> names, HashMap<String, DocumentationItem> methodMap) {

        StringBuilder builder = new StringBuilder();
        builder.append("package net.haesleinhuepf.clijx.jython;\n");
        builder.append("import org.fife.ui.autocomplete.BasicCompletion;\n");
        builder.append("import net.haesleinhuepf.clijx.jython.ScriptingAutoCompleteProvider;\n");
        builder.append("import java.util.ArrayList;");

        builder.append("// this is generated code. See src/test/java/net/haesleinhuepf/clijx/codegenerator for details\n");
        if (isCLIJ2) {
            builder.append("class CLIJ2AutoComplete {\n");
        } else {
            builder.append("class CLIJxAutoComplete {\n");
        }
        builder.append("   \n");
        builder.append("   public static ArrayList<BasicCompletion> getCompletions(final ScriptingAutoCompleteProvider provider) {\n");

        builder.append("       ArrayList<BasicCompletion> list = new ArrayList<BasicCompletion>();\n");
        builder.append("       String headline;\n");
        builder.append("       String description;\n");

        int methodCount = 0;
        for (String name : names) {
            DocumentationItem item = methodMap.get(name);
            if (
                    (isCLIJ2 && item.klass.getPackage().toString().contains(".clij2.")) ||
                    ((!isCLIJ2) && (item.klass.getPackage().toString().contains(".clijx.") || item.klass.getPackage().toString().contains(".clij2.")))
            ) {

                String htmlDescription = item.description;
                if (htmlDescription != null) {
                    htmlDescription = htmlDescription.replace("\n", "<br>");
                    htmlDescription = htmlDescription.replace("\"", "&quot;");
                    htmlDescription = htmlDescription + "<br><br>Parameters:<br>" + item.parametersJava;
                }
                if (isCLIJ2) {
                    builder.append("       headline = \"clij2." + item.methodName + "(" + item.parametersJava + ")\";\n");
                } else {
                    builder.append("       headline = \"clijx." + item.methodName + "(" + item.parametersJava + ")\";\n");
                }
                builder.append("       description = \"<b>" + item.methodName + "</b><br><br>" + htmlDescription + "\";\n");
                builder.append("       list.add(new BasicCompletion(provider, headline, null, description));\n");

                methodCount++;
            }
        }

        builder.append("        return list;\n");
        builder.append("    }\n");
        builder.append("}\n");
        builder.append("// " + methodCount + " methods generated.\n");

        File outputTarget;
        if (isCLIJ2) {
            outputTarget = new File("../clijx/src/main/java/net/haesleinhuepf/clijx/jython/CLIJ2AutoComplete.java");
        } else {
            outputTarget = new File("../clijx/src/main/java/net/haesleinhuepf/clijx/jython/CLIJxAutoComplete.java");
        }
        try {
            FileWriter writer = new FileWriter(outputTarget);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static String searchForExampleScripts(String searchFor, String searchinFolder, String baseLink, String language) {
        return searchForExampleScripts(searchFor, searchinFolder, baseLink, language, null);
    }

    protected static String searchForExampleScripts(String searchFor, String searchinFolder, String baseLink, String language, String filename_must_contain) {
        StringBuilder result = new StringBuilder();
        System.out.println(searchinFolder);
        for (File file : new File(searchinFolder).listFiles()) {
            if (filename_must_contain == null || file.getName().contains(filename_must_contain)){
                if (!file.isDirectory()) {
                    String content = readFile(file.getAbsolutePath());
                    if (content.contains(searchFor)) {
                        result.append("<a href=\"" + baseLink + file.getName() + "\"><img src=\"images/language_" + language + ".png\" height=\"20\"/></a> [" + file.getName() + "](" + baseLink + file.getName() + ")  \n");
                    }
                } else {
                    File readme = new File(file + "/readme.md");
                    if (readme.exists()) {
                        String content = readFile(readme.getAbsolutePath());
                        if (content.contains(searchFor)) {
                            result.append("<a href=\"" + baseLink + file.getName() + "\"><img src=\"images/language_" + language + ".png\" height=\"20\"/></a> [" + file.getName() + "](" + baseLink + file.getName() + ")  \n");
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    public static String readFile(String filename) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected static boolean isOutputParameter(String parameter) {
        return (parameter.contains("ClearCLImage") || parameter.contains("ClearCLBuffer")) && (parameter.contains("destination") || parameter.contains("dst") || parameter.contains("output"));
    }

    protected static boolean isInputParameter(String parameter) {
        return (parameter.contains("ClearCLImage") || parameter.contains("ClearCLBuffer")) && (!parameter.contains("destination") && !parameter.contains("dst") && !parameter.contains("output"));
    }


    protected static String createOutputImageCodeJava(String methodName, String parameterName, String inputImage, String clijObjectName) {
        if (methodName.compareTo("resliceTop") == 0 ||
                methodName.compareTo("resliceBottom") == 0 ) {
            return parameterName + " = " + clijObjectName + ".create(new long[]{" + inputImage + ".getWidth(), " + inputImage + ".getDepth(), " + inputImage + ".getHeight()}, " + inputImage + ".getNativeType());\n";
        } else if (methodName.compareTo("resliceLeft") == 0 ||
                methodName.compareTo("resliceRight") == 0 ) {
            return parameterName + " = " + clijObjectName + ".create(new long[]{" + inputImage + ".getHeight(), " + inputImage + ".getDepth(), " + inputImage + ".getWidth()}, " + inputImage + ".getNativeType());\n";
        } else if (methodName.compareTo("maximumZProjection") == 0 ||
                methodName.compareTo("maximumXYZProjection") == 0  ||
                methodName.compareTo("meanZProjection") == 0  ||
                methodName.compareTo("copySlice") == 0  ||
                methodName.compareTo("minimumZProjection") == 0 ) {
            return parameterName + " = " + clijObjectName + ".create(new long[]{" + inputImage + ".getWidth(), " + inputImage + ".getHeight()}, " + inputImage + ".getNativeType());\n";
        } else if (methodName.compareTo("convertToImageJBinary") == 0) {
            return "from net.haesleinhuepf.clij.coremem.enums import NativeTypeEnum;\n" +
                    "ClearCLBuffer " + parameterName + " = clij.create(" + inputImage + ".getDimensions(), " + inputImage + ".getHeight()], NativeTypeEnum.UnsignedByte);\n";
        } else {
            return parameterName + " = " + clijObjectName + ".create(" + inputImage + ");\n";
        }
    }


    protected static String createOutputImageCodeMatlabIcy(String methodName, String parameterName, String inputImage, String clijObjectName) {
        if (methodName.compareTo("resliceTop") == 0 ||
                methodName.compareTo("resliceBottom") == 0 ) {
            return parameterName + " = " + clijObjectName + ".create([" + inputImage + ".getWidth(), " + inputImage + ".getDepth(), " + inputImage + ".getHeight()], " + inputImage + ".getNativeType());\n";
        } else if (methodName.compareTo("resliceLeft") == 0 ||
                methodName.compareTo("resliceRight") == 0 ) {
            return parameterName + " = " + clijObjectName + ".create([" + inputImage + ".getHeight(), " + inputImage + ".getDepth(), " + inputImage + ".getWidth()], " + inputImage + ".getNativeType());\n";
        } else if (methodName.compareTo("maximumZProjection") == 0 ||
                methodName.compareTo("maximumXYZProjection") == 0  ||
                methodName.compareTo("meanZProjection") == 0  ||
                methodName.compareTo("copySlice") == 0  ||
                methodName.compareTo("minimumZProjection") == 0 ) {
            return parameterName + " = " + clijObjectName + ".create([" + inputImage + ".getWidth(), " + inputImage + ".getHeight()], " + inputImage + ".getNativeType());\n";
        } else if (methodName.compareTo("convertToImageJBinary") == 0) {
            return "from net.haesleinhuepf.clij.coremem.enums import NativeTypeEnum;\n" +
                    "ClearCLBuffer " + parameterName + " = clij.create(" + inputImage + ".getDimensions(), " + inputImage + ".getHeight()], NativeTypeEnum.UnsignedByte);\n";
        } else {
            return parameterName + " = " + clijObjectName + ".create(" + inputImage + ");\n";
        }
    }

}
