package com.example.jwo.views;

import com.example.jwo.entity.User;
import com.example.jwo.repository.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route(value = "register")
@PageTitle("Rekisteröidy")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        addClassName("register-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Rekisteröidy");

        TextField usernameField = new TextField("Käyttäjänimi");
        PasswordField passwordField = new PasswordField("Salasana");

        Button registerButton = new Button("Rekisteröidy", e -> {
            User user = new User();
            user.setUsername(usernameField.getValue());
            user.setPassword(passwordEncoder.encode(passwordField.getValue()));
            //user.setRole("ADMIN");
            user.setRole("USER");

            try{
                userRepository.save(user);
                Notification.show("Käyttäjä rekisteröity");
                getUI().ifPresent(ui -> ui.navigate("login"));
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            // Save user logic here (e.g., using a service or repository)

        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(usernameField, passwordField,registerButton);

        add(title, formLayout);
    }
}
