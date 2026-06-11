package nic.meg.mcap.controllers.pageControllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/role-management")
public class RolePageController {

    @GetMapping("/roles")
    public String rolesPage() {
        return "role-management/roles"; 
        // Thymeleaf template: src/main/resources/templates/role-management/roles.html
    }

    @GetMapping("/menus")
    public String menusPage() {
        return "role-management/menus"; 
        // Template: role-management/menus.html
    }

    @GetMapping("/assign-access")
    public String assignAccessPage() {
        return "role-management/assign-access"; 
        // Template: role-management/assign-access.html
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "role-management/dashboard";
    }

}
