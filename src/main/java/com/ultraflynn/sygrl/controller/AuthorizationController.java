package com.ultraflynn.sygrl.controller;

import com.ultraflynn.sygrl.authentication.SSOAuthenticator;
import com.ultraflynn.sygrl.authentication.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Controller
public class AuthorizationController {
    @Autowired
    private UserRegistry userRegistry;

    @Autowired
    private SSOAuthenticator ssoAuthenticator;

    private
    @RequestMapping("/authorize")
    RedirectView authorize(RedirectAttributes attributes) {
        String clientId = System.getenv().get("CLIENT_ID");
        attributes.addAttribute("response_type", "code");
        attributes.addAttribute("redirect_uri", "https://still-temple-92202.herokuapp.com/callback");
        attributes.addAttribute("client_id", clientId);
        attributes.addAttribute("scope", "publicData characterStatsRead");
        attributes.addAttribute("state", ssoAuthenticator.requestState());
        return new RedirectView("https://login.eveonline.com/oauth/authorize");
    }


    @RequestMapping("/callback")
    String callback(Map<String, Object> model,
                    @RequestParam("code") String code,
                    @RequestParam("state") String state) {
        userRegistry.addNewUser(code, state);
        return "callback";
    }
}
