package org.infinispan.tutorial.simple.spring.session;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpSession;

@Controller
@SessionAttributes("greetings")
public class WebController {
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
                           Model model, HttpSession session) {
        model.addAttribute("name", name);
        model.addAttribute("latest", session.getAttribute("latest"));
        session.setAttribute("latest", name);
        return "greeting";
    }

}
