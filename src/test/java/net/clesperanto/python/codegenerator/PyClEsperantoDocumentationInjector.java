package net.clesperanto.python.codegenerator;

import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPluginService;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasLicense;
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
            "_tier9"
    };


    public static void main(String[] args) throws IOException {

        StringBuilder methodListing = new StringBuilder();

        CLIJMacroPluginService service = new Context(CLIJMacroPluginService.class).getService(CLIJMacroPluginService.class);

        String[] allMethods = getMethods(service);
        Arrays.sort(allMethods);

        for (String clij2macroMethodName : allMethods) {
            if (clij2macroMethodName.startsWith("CLIJ2_")) {

                String name = niceName(clij2macroMethodName);
                for (String sub_folder : sub_folders) {
                    String filename = pycle_path + sub_folder + "/_" + name + ".py";
                    System.out.println(filename);
                    if (new File(filename).exists()) {
                        System.out.println("ex " + clij2macroMethodName);

                        String content = new String(Files.readAllBytes(Paths.get(filename)));
                        String[] temp = content.split("\"\"\"");
                        System.out.println(temp.length);
                        if (temp.length > 2) {
                            clij2macroMethodName = clij2macroMethodName.split("\\(")[0];

                            System.out.println("cnt" + clij2macroMethodName);

                            CLIJMacroPlugin plugin = service.getCLIJMacroPlugin(clij2macroMethodName);

                            StringBuilder documentation = new StringBuilder();

                            boolean numpy_style_parameters_contained = false;
                            if (plugin instanceof OffersDocumentation) {
                                String desc = ((OffersDocumentation) plugin).getDescription();
                                documentation.append(desc.replace("\n", "\n    ") + "\n\n");
                                numpy_style_parameters_contained = plugin.getParameterHelpText().contains("Parameters\n----");
                            }
                            if (plugin instanceof HasAuthor) {
                                documentation.append("    Author(s): " + ((HasAuthor) plugin).getAuthorName() + "\n\n");
                            }
                            if (plugin instanceof HasLicense) {
                                documentation.append("    License: " + ((HasLicense) plugin).getLicense() + "\n\n");
                            }
                            if (plugin instanceof OffersDocumentation) {
                                documentation.append("    Available for: " + ((OffersDocumentation) plugin).getAvailableForDimensions() + "\n\n");
                            }

                            String[] parameters = plugin.getParameterHelpText().split(",");

                            if (!numpy_style_parameters_contained) {
                                documentation.append("    Parameters\n" +
                                        "    ----------\n");
                                for (String param : plugin.getParameterHelpText().split(",")) {
                                    documentation.append(
                                        "    " + param + "\n");
                                }

                                documentation.append("\n    Returns\n" +
                                        "    -------\n\n    ");
                            }

                            documentation.append(
                                    "    Examples\n" +
                                    "    --------\n" +
                                    "    \n" +
                                    "    ");

                            documentation.append(
                                    "    References\n" +
                                    "    ----------\n" +
                                    "    .. [1] https://clij.github.io/clij2-docs/reference_" + clij2macroMethodName.replace("CLIJ2_", "") +
                                    "    \n\n"
                            );


                            temp[1] = documentation.toString();

                            content = "";
                            for (int i = 0; i < temp.length; i++) {
                                if (i > 0) {
                                    content = content + "\"\"\"";
                                }
                                content = content + temp[i];
                            }

                            Files.write(Paths.get(filename), content.getBytes());
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

    private static String niceName(String name) {
        String result = "";

        name = name.replace("CLIJ2_", "");
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
        result = result.replace("_pixelindex", "_pixel_index");



        //result = result.replace("_", "\n");
        //result = result.replace(" ", "\n");
        //result = result.replace("-", "\n");
        return result;
    }


}
