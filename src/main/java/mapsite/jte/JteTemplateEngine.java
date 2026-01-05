package mapsite.jte;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;

public class JteTemplateEngine {
    private static TemplateEngine templateEngine;
    private static CodeResolver modResolver;

    public static void init() {
        templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        modResolver = new ModJarResolver("snoobinoob.mapsite", "resources/web/");
    }

    public static String render(String templateName, Object model) {
        TemplateOutput output = new StringOutput();
        templateEngine.render(templateName, model, output);
        return output.toString();
    }

    public static String renderResource(String templateName) {
        return modResolver.resolve(templateName);
    }
}
