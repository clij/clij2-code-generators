package net.haesleinhuepf.clijx.codegenerator;

import net.haesleinhuepf.clij.macro.CLIJMacroPluginService;
import org.scijava.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FallBackCLIJxMacroServiceGenerator {
    public static void main(String... args) throws IOException {

        CLIJMacroPluginService cmps = new Context(CLIJMacroPluginService.class).getService(CLIJMacroPluginService.class);

        StringBuilder builder = new StringBuilder();
        builder.append("package net.haesleinhuepf.clijx.legacy;\n\n");

        builder.append("// this is generated code. See src/test/java/net/haesleinhuepf/clijx/codegenerator for details\n");
        builder.append("public class FallBackCLIJMacroPluginServiceInitializer {\n");
        builder.append("   public static void initialize(FallBackCLIJMacroPluginService service) {\n");


        for (String methodName : cmps.getCLIJMethodNames()) {
            System.out.println(methodName);
            String classname = cmps.getCLIJMacroPlugin(methodName).getClass().getName();
            if (!classname.contains("wrap")) {
                builder.append("       service.registerPlugin(\"" + methodName + "\" ," + classname + ".class);\n");
            }
        }
        builder.append("   }\n");
        builder.append("}\n");

        File outputTarget = new File("../clijx/src/main/java/net/haesleinhuepf/clijx/legacy/FallBackCLIJMacroPluginServiceInitializer.java");

        FileWriter writer = new FileWriter(outputTarget);
        writer.write(builder.toString());
        writer.close();

    }

}
