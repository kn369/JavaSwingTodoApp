import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends Frame {
    public LoginFrame() {
        setTitle("Login");
        setSize(300, 200);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        setLocationRelativeTo(null);
        setLayout(null);

        Label userLabel = new Label("Username:");
        userLabel.setBounds(30, 30, 80, 25);
        add(userLabel);

        TextField userText = new TextField();
        userText.setBounds(120, 30, 120, 25);
        add(userText);

        Label passLabel = new Label("Password:");
        passLabel.setBounds(30, 70, 80, 25);
        add(passLabel);

        TextField passText = new TextField();
        passText.setEchoChar('*');  // Make password field show asterisks
        passText.setBounds(120, 70, 120, 25);
        add(passText);

        Button loginBtn = new Button("Login");
        loginBtn.setBounds(30, 110, 90, 25);
        add(loginBtn);

        Button registerBtn = new Button("Register");
        registerBtn.setBounds(150, 110, 90, 25);
        add(registerBtn);

        loginBtn.addActionListener(e -> {
            String username = userText.getText();
            String password = passText.getText();
            User user = Auth.login(username, password);
            if (user != null) {
                new TodoFrame(user);
                dispose();
            } else {
                Dialog dialog = new Dialog(this, "Error", true);
                dialog.setLayout(new FlowLayout());
                dialog.add(new Label("Login failed!"));
                Button okButton = new Button("OK");
                okButton.addActionListener(evt -> dialog.dispose());
                dialog.add(okButton);
                dialog.setSize(200, 100);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
            }
        });

        registerBtn.addActionListener(e -> {
            new RegisterFrame();
            dispose();
        });

        setVisible(true);
    }
}