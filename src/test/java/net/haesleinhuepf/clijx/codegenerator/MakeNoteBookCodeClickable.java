package net.haesleinhuepf.clijx.codegenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class MakeNoteBookCodeClickable {
    public static void main(String[] args) throws IOException {
        File mdFolder = new File("../clij2-docs/md/");
        File docsFolder = new File("../clij2-docs");


        ArrayList<String> documented_commands = new ArrayList<>();
        for (File reference : docsFolder.listFiles()) {
            if (!reference.isDirectory()) {
                if (reference.getName().startsWith("reference_") && reference.getName().endsWith(".md")) {
                    documented_commands.add(reference.getName().replace(".md", ""));
                }
            }
        }

        Collections.sort(documented_commands);
        Collections.reverse(documented_commands);
        for (File notebook_folder : mdFolder.listFiles()) {
            if (notebook_folder.isDirectory() && new File(notebook_folder + "/readme.md").exists()) {
                String content = new String(Files.readAllBytes(Paths.get(notebook_folder + "/readme.md")));
                for (String reference : documented_commands) {
                    String method = reference.replace("reference_", "CLIJ2_");
                    String mdLink = "[method](https://clij.github.io/clij2-docs/" + reference + "\")";
                    content = content.replace("." + mdLink, "." + method);

                    String link = "<a href=\"https://clij.github.io/clij2-docs/" + reference + "\">" + method + "</a>";
                    if (!content.contains(link)) {
                        content = content.replace("." + method, "." + link);
                    }
                }
                content = content.replace("```java", "<pre class=\"highlight\">");
                content = content.replace("```\n```", "");
                content = content.replace("```", "</pre>");

                content = content.replace("width=\"250\"", "width=\"224\"");

                Files.write(Paths.get(notebook_folder + "/readme.md"), content.getBytes());
            }
        }
    }
}
