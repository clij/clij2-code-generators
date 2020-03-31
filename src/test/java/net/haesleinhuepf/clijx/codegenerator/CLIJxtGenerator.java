package net.haesleinhuepf.clijx.codegenerator;

import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPluginService;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.utilities.ProcessableInTiles;
import org.fife.ui.rtextarea.RTextAreaEditorKit;
import org.scijava.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CLIJxtGenerator {
    public static void main(String[] args) throws IOException {

        String templateFile = "../clijx/src/main/java/net/haesleinhuepf/clijx/tilor/implementations/AddImages.java";
        String targetPath = "../clijx/src/main/java/net/haesleinhuepf/clijx/tilor/implementations/";

        CLIJMacroPluginService service = new Context(CLIJMacroPluginService.class).getService(CLIJMacroPluginService.class);

        String template = new String(Files.readAllBytes(Paths.get(templateFile)));

        String fullClassToReplace = "net.haesleinhuepf.clij2.plugins.AddImages";
        String shortClassNameToReplace = "AddImages";
        String nameToReplace = "CLIJxt_addImages";

        for (String name : service.getCLIJMethodNames()) {
            String content = "" + template;
            //                                                                 thats the template
            if (name.startsWith("CLIJ2_") || name.startsWith("CLIJx_") && !name.endsWith("addImages")) {
                //System.out.println("name: " + name);
                //File outputTarget = new File(targetPath + name);

                CLIJMacroPlugin plugin = service.getCLIJMacroPlugin(name);
                if (plugin instanceof ProcessableInTiles && !plugin.getClass().isAnnotationPresent(Deprecated.class)) {
                    String fullClassName = plugin.getClass().getName();
                    String shortClassName = plugin.getClass().getSimpleName();

                    String newName = name.replace("CLIJ2_", "CLIJxt_").replace("CLIJx_", "CLIJxt_");
                    System.out.println(newName + " from " + plugin.getClass());

                    content = content.replace(fullClassToReplace, fullClassName);
                    content = content.replace(shortClassNameToReplace, shortClassName);
                    content = content.replace(nameToReplace, newName);
                    content = content.replace("\npublic class", "// this is generated code. See CLIJxtGenerator for details. \npublic class");

                    //System.out.println(content);

                    File outputTarget = new File(targetPath + shortClassName + ".java");

                    try {
                        FileWriter writer = new FileWriter(outputTarget);
                        writer.write(content);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
/*
        File folder = new File("src/main/java/net/haesleinhuepf/clij2/temp/");
        for (File file : folder.listFiles()) {
            if (!file.isDirectory() && file.getName().endsWith(".java")) {
                String content =
                content = content.replace("import net.haesleinhuepf.clijx.CLIJx;", "import net.haesleinhuepf.clij2.CLIJ2;");
                content = content.replace("import net.haesleinhuepf.clijx.utilities.AbstractCLIJxPlugin;", "import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;");
                content = content.replace("CLIJx", "CLIJ2");
                content = content.replace("clijx", "clij2");

                File outputTarget = new File(file.getAbsolutePath());

                try {
                    FileWriter writer = new FileWriter(outputTarget);
                    writer.write(content);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }
}
