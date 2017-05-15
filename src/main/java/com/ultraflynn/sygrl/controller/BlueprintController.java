package com.ultraflynn.sygrl.controller;

import com.ultraflynn.sygrl.industry.Blueprint;
import com.ultraflynn.sygrl.industry.Industry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
public class BlueprintController {
    @Autowired
    private Industry industry;

    @RequestMapping("/blueprint")
    String index(Map<String, Object> model) {
        List<Blueprint> blueprints = industry.getBlueprints();
        renderBlueprints(model, blueprints);
        return "blueprint";
    }

    private void renderBlueprints(Map<String, Object> model, List<Blueprint> blueprints) {
        blueprints.forEach(blueprint -> model.put(blueprint.getName(), blueprint.getJobEndDate()));
    }
}
