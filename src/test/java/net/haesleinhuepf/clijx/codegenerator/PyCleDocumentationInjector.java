package net.haesleinhuepf.clijx.codegenerator;

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

public class PyCleDocumentationInjector {

    final static String pycle_path = "C:/structure/code/pyclesperanto_prototype/clesperanto/plugins/";


    public static void main(String[] args) throws IOException {

        StringBuilder methodListing = new StringBuilder();

        CLIJMacroPluginService service = new Context(CLIJMacroPluginService.class).getService(CLIJMacroPluginService.class);

        String[] allMethods = getMethods(service);
        Arrays.sort(allMethods);

        for (String clij2macroMethodName : allMethods) {
            if (clij2macroMethodName.startsWith("CLIJ2_")) {


                String name = niceName(clij2macroMethodName);
                String filename = pycle_path + name + ".py";
                //System.out.println(filename);
                if (new File(filename).exists()) {
                    //System.out.println("ex " + clij2macroMethodName);

                    String content = new String(Files.readAllBytes(Paths.get(filename)));
                    String[] temp = content.split("\"\"\"");
                    if (temp.length > 2) {
                        clij2macroMethodName = clij2macroMethodName.split("\\(")[0];

                        System.out.println("cnt" + clij2macroMethodName);

                        CLIJMacroPlugin plugin = service.getCLIJMacroPlugin(clij2macroMethodName);

                        StringBuilder documentation = new StringBuilder();
                        if (plugin instanceof OffersDocumentation) {

                            documentation.append(((OffersDocumentation) plugin).getDescription().replace("\n", "\n    ") + "\n\n");
                        }
                        if (plugin instanceof HasAuthor) {
                            documentation.append("    Author(s): " + ((HasAuthor) plugin).getAuthorName() + "\n\n");
                        }
                        if (plugin instanceof HasLicense) {
                            documentation.append("    License: " + ((HasLicense) plugin).getLicense() + "\n\n");
                        }
                        if (plugin instanceof OffersDocumentation){
                            documentation.append("    Available for: " + ((OffersDocumentation) plugin).getAvailableForDimensions() + "\n\n");
                        }


                        String[] parameters = plugin.getParameterHelpText().split(",");

                        documentation.append("    Parameters\n" +
                                             "    ----------\n" +
                                             "    (" + plugin.getParameterHelpText() + ")\n" +
                                             "    todo: Better documentation will follow\n");
                        documentation.append("          In the meantime, read more: https://clij.github.io/clij2-docs/reference_" + clij2macroMethodName.replace("CLIJ2_", "") + "\n\n");


                        documentation.append("\n    Returns\n" +
                                             "    -------\n\n    ");

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

        //result = result.replace("_", "\n");
        //result = result.replace(" ", "\n");
        //result = result.replace("-", "\n");
        return result;
    }


}
