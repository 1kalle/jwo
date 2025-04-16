package com.example.jwo.views;

import com.example.jwo.entity.Kala;
import com.example.jwo.entity.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Admin Panel")
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {
    private final RestClient restClient;
    private Grid<User> userGrid;
    private Grid<Kala> kalaGrid;
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleField;
    private TextField lajiField;
    private NumberField pituusField;
    private NumberField painoField;
    private ComboBox<User> userComboBox;
    private User selectedUser;
    private Kala selectedKala;

    public AdminView(@Autowired HttpServletRequest request) {
        addClassName("admin-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        // Initialize RestClient with session cookie
        String sessionId = request.getSession().getId();
        System.out.println("Session ID: " + sessionId);
        restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader("Cookie", "JSESSIONID=" + sessionId)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();

        // Users Section
        H1 userTitle = new H1("Käyttäjien hallinta");
        userGrid = createUserGrid();
        FormLayout userForm = createUserForm();
        HorizontalLayout userActions = createUserActions();

        // Kalat Section
        H1 kalaTitle = new H1("Kalojen hallinta");
        kalaGrid = createKalaGrid();
        FormLayout kalaForm = createKalaForm();
        HorizontalLayout kalaActions = createKalaActions();

        // Load initial data
        loadUsers();
        loadKalat();

        // Add components
        add(userTitle, userGrid, userForm, userActions, kalaTitle, kalaGrid, kalaForm, kalaActions);
    }

    private Grid<User> createUserGrid() {
        Grid<User> grid = new Grid<>(User.class, false);
        grid.addColumn(User::getUsername).setHeader("Käyttäjänimi").setSortable(true).setWidth("30%");
        grid.addColumn(User::getRole).setHeader("Rooli").setSortable(true).setWidth("30%");
        grid.addComponentColumn(user -> new Button("Poista", e -> deleteUser(user)))
                .setHeader("Toiminnot").setWidth("40%");

        // Set grid size
        grid.setSizeFull();
        grid.getStyle().set("min-height", "300px")
                .set("min-width", "100px")
                .set("margin", "10px"); // Optional: Add some margin for spacing

        grid.asSingleSelect().addValueChangeListener(e -> {
            selectedUser = e.getValue();
            if (selectedUser != null) {
                usernameField.setValue(selectedUser.getUsername());
                passwordField.clear();
                roleField.setValue(selectedUser.getRole());
            } else {
                clearUserForm();
            }
        });
        return grid;
    }

    private FormLayout createUserForm() {
        usernameField = new TextField("Käyttäjänimi");
        passwordField = new PasswordField("Salasana");
        roleField = new ComboBox<>("Rooli");
        roleField.setItems("USER", "ADMIN");
        roleField.setValue("USER");
        return new FormLayout(usernameField, passwordField, roleField);
    }

    private HorizontalLayout createUserActions() {
        Button saveButton = new Button("Tallenna", e -> saveUser());
        Button clearButton = new Button("Tyhjennä", e -> clearUserForm());
        return new HorizontalLayout(saveButton, clearButton);
    }

    private Grid<Kala> createKalaGrid() {
        Grid<Kala> grid = new Grid<>(Kala.class, false);
        grid.addColumn(Kala::getLaji).setHeader("Laji").setSortable(true).setWidth("25%");
        grid.addColumn(Kala::getPituus).setHeader("Pituus (cm)").setSortable(true).setWidth("20%");
        grid.addColumn(Kala::getPaino).setHeader("Paino (kg)").setSortable(true).setWidth("20%");
        grid.addColumn(kala -> kala.getUser() != null ? kala.getUser().getUsername() : "Ei käyttäjää")
                .setHeader("Käyttäjä").setSortable(true).setWidth("20%");
        grid.addComponentColumn(kala -> new Button("Poista", e -> deleteKala(kala)))
                .setHeader("Toiminnot").setWidth("15%");

        grid.setSizeFull();
        grid.getStyle().set("min-height", "300px")
                .set("min-width", "100px")
                .set("margin", "10px");

        grid.asSingleSelect().addValueChangeListener(e -> {
            selectedKala = e.getValue();
            if (selectedKala != null) {
                lajiField.setValue(selectedKala.getLaji());
                pituusField.setValue(selectedKala.getPituus());
                painoField.setValue(selectedKala.getPaino());
                userComboBox.setValue(selectedKala.getUser());
            } else {
                clearKalaForm();
            }
        });
        return grid;
    }

    private FormLayout createKalaForm() {
        lajiField = new TextField("Laji");
        pituusField = new NumberField("Pituus (cm)");
        pituusField.setMin(0);
        painoField = new NumberField("Paino (kg)");
        painoField.setMin(0);
        userComboBox = new ComboBox<>("Käyttäjä");
        userComboBox.setItemLabelGenerator(user -> user != null ? user.getUsername() : "Ei käyttäjää");
        return new FormLayout(lajiField, pituusField, painoField, userComboBox);
    }

    private HorizontalLayout createKalaActions() {
        Button saveButton = new Button("Tallenna", e -> saveKala());
        Button clearButton = new Button("Tyhjennä", e -> clearKalaForm());
        return new HorizontalLayout(saveButton, clearButton);
    }

    private void loadUsers() {
        try {
            var response = restClient.get()
                    .uri("/api/admin/users")
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<User>>() {});
            System.out.println("Users response status: " + response.getStatusCode());
            List<User> users = response.getBody();
            System.out.println("Loaded users: " + (users != null ? users.size() : 0));
            if (users != null && !users.isEmpty()) {
                System.out.println("Users: " + users);
            }
            userGrid.setItems(users != null ? users : Collections.emptyList());
            userComboBox.setItems(users != null ? users : Collections.emptyList());
        } catch (RestClientException e) {
            System.err.println("Failed to load users: " + e.getMessage());
            Notification.show("Käyttäjien lataus epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void saveUser() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();
        String role = roleField.getValue();

        if (username == null || username.trim().isEmpty() || (password.isEmpty() && selectedUser == null) || role == null) {
            Notification.show("Täytä kaikki kentät!", 3000, Notification.Position.MIDDLE);
            return;
        }

        User user = selectedUser != null ? selectedUser : new User();
        user.setUsername(username);
        if (!password.isEmpty()) {
            user.setPassword(password);
        }
        user.setRole(role);

        try {
            var response = selectedUser != null ?
                    restClient.put()
                            .uri("/api/admin/users/{id}", selectedUser.getId())
                            .body(user)
                            .retrieve()
                            .toEntity(Void.class) :
                    restClient.post()
                            .uri("/api/admin/users")
                            .body(user)
                            .retrieve()
                            .toEntity(Void.class);
            System.out.println("Save user response status: " + response.getStatusCode());
            if (response.getStatusCode().is2xxSuccessful()) {
                Notification.show(selectedUser != null ? "Käyttäjä päivitetty!" : "Käyttäjä lisätty!", 3000, Notification.Position.MIDDLE);
                loadUsers();
                clearUserForm();
            } else {
                String errorMsg = response.getHeaders().getLocation() != null ?
                        "HTTP " + response.getStatusCode() + " (Redirect to: " + response.getHeaders().getLocation() + ")" :
                        "HTTP " + response.getStatusCode();
                Notification.show("Käyttäjän tallennus epäonnistui: " + errorMsg, 5000, Notification.Position.MIDDLE);
            }
        } catch (RestClientException e) {
            System.err.println("Failed to save user: " + e.getMessage());
            Notification.show("Käyttäjän tallennus epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void deleteUser(User user) {
        if (user == null) {
            Notification.show("Valitse käyttäjä!", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            System.out.println("Deleting user: " + user.getId());
            var response = restClient.delete()
                    .uri("/api/admin/users/{id}", user.getId())
                    .retrieve()
                    .toEntity(Void.class);
            System.out.println("Delete user response status: " + response.getStatusCode());
            if (response.getStatusCode().is2xxSuccessful()) {
                Notification.show("Käyttäjä poistettu!", 3000, Notification.Position.MIDDLE);
                loadUsers();
                clearUserForm();
            } else {
                String errorMsg = response.getHeaders().getLocation() != null ?
                        "HTTP " + response.getStatusCode() + " (Redirect to: " + response.getHeaders().getLocation() + ")" :
                        "HTTP " + response.getStatusCode();
                Notification.show("Käyttäjän poisto epäonnistui: " + errorMsg, 5000, Notification.Position.MIDDLE);
            }
        } catch (RestClientException e) {
            System.err.println("Failed to delete user: " + e.getMessage());
            Notification.show("Käyttäjän poisto epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void clearUserForm() {
        usernameField.clear();
        passwordField.clear();
        roleField.setValue("USER");
        selectedUser = null;
        userGrid.deselectAll();
    }

    private void loadKalat() {
        try {
            var response = restClient.get()
                    .uri("/api/admin/kalat")
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<Kala>>() {});
            System.out.println("Kalat response status: " + response.getStatusCode());
            List<Kala> kalat = response.getBody();
            System.out.println("Loaded kalat: " + (kalat != null ? kalat.size() : 0));
            if (kalat != null && !kalat.isEmpty()) {
                System.out.println("Kalat: " + kalat);
            }
            kalaGrid.setItems(kalat != null ? kalat : Collections.emptyList());
        } catch (RestClientException e) {
            System.err.println("Failed to load kalat: " + e.getMessage());
            Notification.show("Kalojen lataus epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void saveKala() {
        String laji = lajiField.getValue();
        Double pituus = pituusField.getValue();
        Double paino = painoField.getValue();
        User user = userComboBox.getValue();

        if (laji == null || laji.trim().isEmpty() || pituus == null || paino == null) {
            Notification.show("Täytä kaikki kentät!", 3000, Notification.Position.MIDDLE);
            return;
        }

        Kala kala = selectedKala != null ? selectedKala : new Kala();
        kala.setLaji(laji);
        kala.setPituus(pituus);
        kala.setPaino(paino);
        kala.setUser(user);

        try {
            var response = selectedKala != null ?
                    restClient.put()
                            .uri("/api/admin/kalat/{id}", selectedKala.getId())
                            .body(kala)
                            .retrieve()
                            .toEntity(Void.class) :
                    restClient.post()
                            .uri("/api/admin/kalat")
                            .body(kala)
                            .retrieve()
                            .toEntity(Void.class);
            System.out.println("Save kala response status: " + response.getStatusCode());
            if (response.getStatusCode().is2xxSuccessful()) {
                Notification.show(selectedKala != null ? "Kala päivitetty!" : "Kala lisätty!", 3000, Notification.Position.MIDDLE);
                loadKalat();
                clearKalaForm();
            } else {
                String errorMsg = response.getHeaders().getLocation() != null ?
                        "HTTP " + response.getStatusCode() + " (Redirect to: " + response.getHeaders().getLocation() + ")" :
                        "HTTP " + response.getStatusCode();
                Notification.show("Kalan tallennus epäonnistui: " + errorMsg, 5000, Notification.Position.MIDDLE);
            }
        } catch (RestClientException e) {
            System.err.println("Failed to save kala: " + e.getMessage());
            Notification.show("Kalan tallennus epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void deleteKala(Kala kala) {
        if (kala == null) {
            Notification.show("Valitse kala!", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            System.out.println("Deleting kala: " + kala.getId());
            var response = restClient.delete()
                    .uri("/api/admin/kalat/{id}", kala.getId())
                    .retrieve()
                    .toEntity(String.class);
            System.out.println("Delete kala response status: " + response.getStatusCode());
            System.out.println("Delete kala headers: " + response.getHeaders());
            System.out.println("Delete kala body: " + response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                Notification.show("Kala poistettu!", 3000, Notification.Position.MIDDLE);
                loadKalat();
                clearKalaForm();
            } else {
                String errorMsg = response.getHeaders().getLocation() != null ?
                        "HTTP " + response.getStatusCode() + " (Redirect to: " + response.getHeaders().getLocation() + ")" :
                        "HTTP " + response.getStatusCode() + ": " + response.getBody();
                Notification.show("Kalan poisto epäonnistui: " + errorMsg, 5000, Notification.Position.MIDDLE);
            }
        } catch (RestClientException e) {
            System.err.println("Failed to delete kala: " + e.getMessage());
            Notification.show("Kalan poisto epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }


    private void clearKalaForm() {
        lajiField.clear();
        pituusField.clear();
        painoField.clear();
        userComboBox.clear();
        selectedKala = null;
        kalaGrid.deselectAll();
    }
}