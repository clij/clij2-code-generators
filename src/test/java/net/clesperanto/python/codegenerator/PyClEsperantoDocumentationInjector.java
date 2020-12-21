package net.clesperanto.python.codegenerator;

import jnr.ffi.Struct;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPluginService;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasLicense;
import net.haesleinhuepf.clijx.codegenerator.DocumentationGenerator;
import org.python.modules._py_compile;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static net.haesleinhuepf.clijx.codegenerator.CompareCLIJ1andCLIJ2MacroAPI.getMethods;

public class PyClEsperantoDocumentationInjector {

    final static String pycle_path = "C:/structure/code/pyclesperanto_prototype/pyclesperanto_prototype/";
    final static String[] sub_folders = {
            "_tier0",
            "_tier1",
            "_tier2",
            "_tier3",
            "_tier4",
            "_tier5",
            "_tier9"
    };


    public static void main(String[] args) throws IOException {

        StringBuilder methodListing = new StringBuilder();

        CLIJMacroPluginService service = new Context(CLIJMacroPluginService.class).getService(CLIJMacroPluginService.class);

        String[] allMethods = getMethods(service);
        Arrays.sort(allMethods);

        for (String clij2macroMethodName : allMethods) {
            if (clij2macroMethodName.startsWith("CLIJ2_") || clij2macroMethodName.startsWith("CLIJx_")) {

                String name = niceName(clij2macroMethodName);
                for (String sub_folder : sub_folders) {
                    String filename = pycle_path + sub_folder + "/_" + name + ".py";
                    //System.out.println(filename);
                    if (new File(filename).exists()) {
                        //System.out.println("ex " + clij2macroMethodName);

                        String content = new String(Files.readAllBytes(Paths.get(filename)));
                        String orignal_content = content;

                        String[] temp = content.split("\"\"\"");
                        System.out.println(temp.length);
                        if (temp.length > 2) {
                            clij2macroMethodName = clij2macroMethodName.split("\\(")[0];

                            //System.out.println("cnt" + clij2macroMethodName);

                            CLIJMacroPlugin plugin = service.getCLIJMacroPlugin(clij2macroMethodName);

                            StringBuilder documentation = new StringBuilder();

                            boolean numpy_style_parameters_contained = false;
                            if (plugin instanceof OffersDocumentation) {
                                String desc = ((OffersDocumentation) plugin).getDescription();
                                desc = handleLineBreaks(desc, 72);
                                desc = desc + "\n\n";
                                numpy_style_parameters_contained = ((OffersDocumentation) plugin).getDescription().contains("Parameters\n----");
                                documentation.append(desc);
                            }
                            if (plugin instanceof HasAuthor) {
                                documentation.append("Author(s): " + ((HasAuthor) plugin).getAuthorName() + "\n\n");
                            }
                            if (plugin instanceof HasLicense) {
                                documentation.append("License: " + ((HasLicense) plugin).getLicense() + "\n\n");
                            }
                            //if (plugin instanceof OffersDocumentation) {
                            //    documentation.append("    Available for: " + ((OffersDocumentation) plugin).getAvailableForDimensions() + "\n\n");
                            //}

                            String[] parameters = plugin.getParameterHelpText().split(",");

                            if (!numpy_style_parameters_contained) {
                                documentation.append("Parameters\n" +
                                        "----------\n");
                                for (String param : parameters) {
                                    String[] temp2 = param.trim().split(" ");
                                    documentation.append(
                                        temp2[temp2.length - 1] + " : " + temp2[temp2.length - 2] + "\n");
                                }
                                documentation.append("\n");
                            }

                            String parameters_call = "";
                            String returns = null;
                            for (String parameter : parameters) {
                                String[] temp2 = parameter.trim().split(" ");

                                if (parameters_call.length() > 0) {
                                    parameters_call = parameters_call + ", ";
                                }

                                if (temp2[0].contains("ByRef")) {
                                    returns = temp2[temp2.length - 1];
                                }
                                parameters_call = parameters_call + temp2[temp2.length - 1];
                            }

                            if (returns != null) {
                                documentation.append("\nReturns\n" +
                                        "-------\n" +
                                        returns + "\n\n");
                            }

                            String example = DocumentationGenerator.generateClePythonCode(plugin.getClass(), clij2macroMethodName, "", parameters_call, "");
                            if (example != null) {

                                example = ("\n" + example)
                                        .replace("<pre class=\"highlight\">", "")
                                        .replace("</pre>", "");
                                while (example.contains("\n\n")) {
                                    example = example.replace("\n\n", "\n");
                                }
                                example = example.substring(0, example.length() - 1 ).replace("\n", "\n>>> ");

                                documentation.append(
                                        "Examples\n" +
                                                "--------" +
                                                example +
                                                "\n\n");
                            }

                            documentation.append(
                                    "References\n" +
                                    "----------\n" +
                                    ".. [1] https://clij.github.io/clij2-docs/reference_" + clij2macroMethodName.replace("CLIJ2_", "").replace("CLIJx_", "") +
                                    "\n"
                            );

                            String temp2 = documentation.toString();

                            while (temp2.contains("\n\n\n")) {
                                temp2 = temp2.replace("\n\n\n", "\n\n");
                            }
                            temp2 = temp2.replace("\n", "\n    ");

                            temp[1] = temp2;

                            content = "";
                            for (int i = 0; i < temp.length; i++) {
                                if (i > 0) {
                                    content = content + "\"\"\"";
                                }
                                content = content + temp[i];
                            }
                            content = content.replace("    >>>     \n","");

                            if (content.replace("\r\n", "\n").compareTo(orignal_content.replace("\r\n", "\n")) != 0) {
                                Files.write(Paths.get(filename), content.getBytes());
                            }
                        }
                    }
                }
            }
        }

        /*
        """Function decorator to ensure correct types and values of all parameters.
    The given input parameters are either of type OCLArray (which the GPU
    understands) or are converted to this type (see push function). If output
    parameters of type OCLArray are not set, an empty image is created and
    handed over.


    Parameters
    ----------
    function : callable
        The function to be executed on the GPU.
    output_creator : callable, optional
        A function to create an output OCLArray given an input OCLArray. By
        default, we create float32 output images of the same shape as input
        images.


    Returns
    -------
    worker_function : callable
        The actual function call that will be executed, magically creating
        output arguments of the correct type.
    """

         */


    }

    private static String handleLineBreaks(String desc, int max_length) {
        StringBuilder output = new StringBuilder();
        desc = desc.replace("\r\n", "\n");
        desc = desc.replace("\t", "    ");

        //desc = desc.replace("\n", " \n ");
        int counter = 0;
        for (String element : desc.split(" ")) {
            if (element.contains("\n")) {
                counter = 0;
                output.append(element + " ");
                continue;
            }

            if (counter + element.length() >= max_length) {
                counter = 0;
                output.append("\n");
            }
            output.append(element + " ");
            counter = counter + element.length() + 1;
        }
        String temp = output.toString();
        while (temp.substring(temp.length()).compareTo(" ") == 0) {
            temp = temp.substring(0, temp.length() - 1 );
        }
        return temp;
    }

    private static String niceName(String name) {
        String result = "";

        name = name.replace("CLIJ2_", "");
        name = name.replace("CLIJx_", "");
        name = name.split("\\(")[0];

        for (int i = 0; i < name.length(); i++) {
            String ch = name.substring(i,i+1);
            if (!ch.toLowerCase().equals(ch)) {
                result = result + "_" + ch.toLowerCase();
            } else {
                result = result + ch;
            }
        }
        result = result.replace("paste3_d", "paste");
        result = result.replace("minimum3_d", "minimum");
        result = result.replace("maximum3_d", "maximum");
        result = result.replace("difference_of_gaussian3_d", "difference_of_gaussian");
        result = result.replace("mean3_d", "mean");
        result = result.replace("blur3_d", "blur");
        result = result.replace("flip3_d", "flip");
        result = result.replace("crop3_d", "crop");
        result = result.replace("_x_or", "_xor");
        result = result.replace("_x_y", "_xy");
        result = result.replace("_x_z", "_xz");
        result = result.replace("_y_z", "_yz");
        result = result.replace("2_d", "2d");
        result = result.replace("3_d", "3d");
        result = result.replace("_point_list", "_pointlist");
        result = result.replace("xgreater", "x_greater");
        result = result.replace("xsmaller", "x_smaller");
        result = result.replace("xequals", "x_equals");
        result = result.replace("_pixel_index", "_pixelindex");



        //result = result.replace("_", "\n");
        //result = result.replace(" ", "\n");
        //result = result.replace("-", "\n");
        return result;
    }


}
