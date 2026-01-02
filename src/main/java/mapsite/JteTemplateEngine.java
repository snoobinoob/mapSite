package mapsite;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;

public class JteTemplateEngine {
    private static TemplateEngine templateEngine;

    public static void init() {
        templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
    }

    public static String render(String templateName, Object model) {
        TemplateOutput output = new StringOutput();
        templateEngine.render(templateName, model, output);
        return output.toString();
    }
}
