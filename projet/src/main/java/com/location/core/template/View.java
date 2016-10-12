package com.location.core.template;

import java.io.StringWriter;
import java.util.Properties;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import spark.ModelAndView;
import spark.TemplateEngine;

public class View extends TemplateEngine {
    private final VelocityEngine velocityEngine;

    public View() {
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        this.velocityEngine = new VelocityEngine(properties);
    }

    /**
     * @param modelAndView modelAndView
     * @return String
     */

    public String render(ModelAndView modelAndView) {
        Template template = this.velocityEngine.getTemplate(modelAndView.getViewName(), "UTF-8");
        Object model = modelAndView.getModel();

        if(model instanceof Map) {
            Map modelMap = (Map)model;
            VelocityContext context = new VelocityContext(modelMap);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            return writer.toString();
        }
        else {
            throw new IllegalArgumentException("modelAndView must be of type java.util.Map");
        }
    }
}