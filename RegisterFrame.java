import java.awt.*;
import java.awt.event.*;

public class RegisterFrame extends Frame {
    public RegisterFrame() {
        setTitle("Register");
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
        passText.setEchoChar('*');
        passText.setBounds(120, 70, 120, 25);
        add(passText);

        Button registerBtn = new Button("Register");
        registerBtn.setBounds(90, 110, 100, 25);
        add(registerBtn);

        registerBtn.addActionListener(e -> {
            String username = userText.getText();
            String password = passText.getText();
            if (Auth.register(username, password)) {
                Dialog successDialog = new Dialog(this, "Success", true);
                successDialog.setLayout(new FlowLayout());
                successDialog.add(new Label("Registered successfully!"));
                Button okButton = new Button("OK");
                okButton.addActionListener(evt -> {
                    successDialog.dispose();
                    new LoginFrame();
                    dispose();
                });
                successDialog.add(okButton);
                successDialog.setSize(200, 100);
                successDialog.setLocationRelativeTo(this);
                successDialog.setVisible(true);
            } else {
                Dialog errorDialog = new Dialog(this, "Error", true);
                errorDialog.setLayout(new FlowLayout());
                errorDialog.add(new Label("Username already exists!"));
                Button okButton = new Button("OK");
                okButton.addActionListener(evt -> errorDialog.dispose());
                errorDialog.add(okButton);
                errorDialog.setSize(200, 100);
                errorDialog.setLocationRelativeTo(this);
                errorDialog.setVisible(true);
            }
        });

        setVisible(true);
    }
}