package net.haesleinhuepf.clijx.codegenerator;

import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPluginService;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Collections;

public class CLIJxMethodLister {
    public static void main(String... args) {
        CLIJMacroPluginService service = new Context(CLIJMacroPluginService.class).getService(CLIJMacroPluginService.class);
        ArrayList<String> list = new ArrayList<>();
        list.addAll(service.getCLIJMethodNames());
        Collections.sort(list);

        for (String name : list) {
            CLIJMacroPlugin plugin = service.getCLIJMacroPlugin(name);
            if (!name.startsWith("CLIJ_") && !name.startsWith("CLIJ2") && !name.startsWith("CLIJxt") && !plugin.getClass().getPackage().toString().contains("wrapper")) {
                System.out.println("* [" + name.replace("CLIJx_", "") + "](https://clij.github.io/clij2-docs/reference_" + name.replace("CLIJx_", "") + ")");
            }
        }

    }
}
