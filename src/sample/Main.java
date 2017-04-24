package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {
    int scale;
    char[] key = new char[26];
    String initialText;
    TextArea text = new TextArea();
    boolean retainEntries;
    TextField[] subs = new TextField[26];
    int currentChar;
    ArrayList<char[]> history = new ArrayList<>();
    int historyPos = 0;
    boolean automaticInsert = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        text.setEditable(false);
        text.setWrapText(true);
        VBox root = new VBox();
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        Scene scene = new Scene(root, 500, 400);
        Button button = new Button("Enter encrypted text");
        Region[] regions = new Region[2];
        for (int i = 0; i < 2; i++) {
            regions[i] = new Region();
            regions[i].setMinWidth(scene.getWidth() / 8);
        }
        GridPane grid = new GridPane();
        HBox[] elms = new HBox[26];
        Label[] lbls = new Label[26];
        for (int i = 0; i < 26; i++) {
            key[i] = '*';
        }
        saveKeyToHistory();
        for (int i = 0; i < 26; i++) {
            elms[i] = new HBox();
            int width = (int) scene.getWidth() / 60;
            elms[i].setPadding(new Insets(2, width, 2, width));
            subs[i] = new TextField("*");
            lbls[i] = new Label(String.valueOf((char) (i + 97)) + ":");
            lbls[i].setMinWidth(scene.getWidth() / 19);
            lbls[i].setFont(new Font("Arial", scene.getWidth() / 28));
            subs[i].setFont(new Font("Arial", scene.getWidth() / 40));

            final int x = i;

            subs[i].textProperty().addListener(((observable, oldValue, newValue) -> {
                key[x] = newValue.charAt(0);
                if (!Substitution.keyValid(key)) {
                    try {
                        if (x == Substitution.firstChar) {
                            currentChar = x;
                            keyIsNotValid(new Stage(), (char) (Substitution.firstChar + 97), (char) (Substitution.secondChar + 97), key[x]);
                        } else {
                            currentChar = x;
                            keyIsNotValid(new Stage(), (char) (Substitution.secondChar + 97), (char) (Substitution.firstChar + 97), key[x]);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!automaticInsert) saveKeyToHistory();
                }
                if (initialText != null) {
                    text.setText(Substitution.substituted(initialText, key));
                }
            }));

            subs[i].lengthProperty().addListener(((observable, oldValue, newValue) -> {
                if ((int) newValue > 1) {
                    subs[x].setText(subs[x].getText().substring(0, 1));
                } else if ((int) newValue == 0) {
                    subs[x].setText("*");
                }
            }));

            elms[i].getChildren().addAll(lbls[i], subs[i]);
            grid.add(elms[i], i % 7, i / 7);
        }

        Button undo = new Button("<--");
        Button redo = new Button("-->");

        undo.setAlignment(Pos.BASELINE_RIGHT);
        redo.setAlignment(Pos.BASELINE_RIGHT);

        undo.setFont(new Font("Arial", scene.getWidth() / 40));
        redo.setFont(new Font("Arial", scene.getWidth() / 40));

        grid.add(undo, 5, 3);
        grid.add(redo, 6, 3);

        undo.setOnMouseClicked(event -> undo());

        redo.setOnMouseClicked(event -> redo());

        text.setText(initialText);

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            for (int i = 0; i < 26; i++) {
                lbls[i].setMinWidth(scene.getWidth() / 19);
                int width = (int) scene.getWidth() / 60;
                elms[i].setPadding(new Insets(2, width, 2, width));
                lbls[i].setMinWidth(scene.getWidth() / 19);
                lbls[i].setFont(new Font("Arial", scene.getWidth() / 28));
                button.setFont(new Font("Arial", scene.getWidth() / 28));
                undo.setFont(new Font("Arial", scene.getWidth() / 40));
                redo.setFont(new Font("Arial", scene.getWidth() / 40));
                subs[i].setFont(new Font("Arial", scene.getWidth() / 40));
            }

            for (int i = 0; i < 2; i++) {
                regions[i] = new Region();
                regions[i].setMinWidth(scene.getWidth() / 8);
            }

        });
        vbox.getChildren().addAll(regions[0], grid, regions[1]);
        hbox.getChildren().addAll(vbox);
        root.setMargin(text, new Insets(10));
        root.setMargin(hbox, new Insets(7, 3, 3, 3));
        VBox.setVgrow(text, Priority.ALWAYS);
        button.setFont(new Font("Arial", scene.getWidth() / 28));
        Region space = new Region();
        space.setPrefWidth(10);
        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(space, button);

        button.setOnMouseClicked(event -> {
            try {
                enterText(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        root.getChildren().addAll(hbox, hBox2, text);

        text.widthProperty().addListener((observable, oldValue, newValue) -> text.setFont(new Font("Arial", fontSize(text, scale))));

        text.heightProperty().addListener((observable, oldValue, newValue) -> text.setFont(new Font("Arial", fontSize(text, scale))));

        KeyCombination combination1 = new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN);
        KeyCombination combination2 = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN);

        text.setOnKeyPressed(event -> {
            if (combination1.match(event)) {
                scale += 40;
            } else if (combination2.match(event)) {
                scale -= 40;
            }
            text.setFont(new Font("Arial", fontSize(text, scale)));
        });

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(scene);
        primaryStage.show();

        text.setFont(new Font("Arial", fontSize(text, scale)));
    }

    private void saveKeyToHistory() {
        if (historyPos != history.size()-1) {
            for (int i = history.size()-1; i > historyPos; i--) {
                history.remove(i);
            }
        }
        char[] tempKey = new char[key.length];
        for (int i = 0; i < tempKey.length; i++) {
            tempKey[i] = key[i];
        }
        if (history.size() != 0) historyPos++;
        history.add(tempKey);
    }

    private void undo() {
        System.out.println("undo");
        if (historyPos > 0 && history.size() > 0) {
            historyPos--;
            updateKeyInputs();
        }
    }

    private void redo() {
        System.out.println("redo");
        if (historyPos < history.size()-1) {
            historyPos++;
            updateKeyInputs();
        }
    }

    private void updateKeyInputs() {
        automaticInsert = true;
        for (int i = 0; i < 26; i++) {
            subs[i].setText(String.valueOf(history.get(historyPos)[i]));
        }
        automaticInsert = false;
    }

    private void keyIsNotValid(Stage stage, char first, char second, char third) throws Exception {
        //TODO: improve design
        VBox root = new VBox();
        HBox hbox = new HBox();
        Button yes = new Button("Yes");
        Button no = new Button("No");

        yes.setOnMouseClicked(event -> {
            retainEntries = true;
            stage.close();
            updateKey();
        });

        no.setOnMouseClicked(event -> {
            retainEntries = false;
            stage.close();
            updateKey();
        });

        stage.setOnCloseRequest(event -> {
            retainEntries = false;
            stage.close();
            updateKey();
        });

        hbox.getChildren().addAll(yes, no);
        Scene scene = new Scene(root, 250, 300);
        Label label1 = new Label("Your input: " + first + " -> " + third);
        Label label2 = new Label("clashes with the input " + second + " -> " + third);
        Label label3 = new Label("Do you still wish to retain your entries?");
        Label label4 = new Label("If YES, your inputs will be adopted and the");
        Label label5 = new Label("conflicting field will be set to indeterminate");
        Label label6 = new Label("If NO, your input will be ignored and the field");
        Label label7 = new Label("will be reset to its last valid value.");
        root.getChildren().addAll(label1, label2, label3, label4, label5, label6, label7, hbox);
        stage.setResizable(false);
        stage.setTitle("Conflict");
        stage.setScene(scene);
        stage.show();
    }

    private void updateKey() {
        if (retainEntries) {
            if (currentChar == Substitution.firstChar) {
                key[Substitution.secondChar] = '*';
                subs[Substitution.secondChar].setText("*");
            } else {
                key[Substitution.firstChar] = '*';
                subs[Substitution.firstChar].setText("*");
            }
        } else {
            if (currentChar == Substitution.firstChar) {
                key[Substitution.firstChar] = '*';
                subs[Substitution.firstChar].setText("*");
            } else {
                key[Substitution.secondChar] = '*';
                subs[Substitution.secondChar].setText("*");
            }
        }
    }

    void enterText(Stage stage) throws Exception {
        VBox root = new VBox();
        TextArea text = new TextArea();
        Button ok = new Button("OK");
        root.getChildren().addAll(text, ok);
        //TODO: make proper UI
        ok.setOnMouseClicked(event -> {
            initialText = text.getText();
            stage.close();
            updateText();
        });
        Scene scene = new Scene(root, 200, 200);
        stage.setTitle("Hello World");
        stage.setScene(scene);
        stage.show();
    }

    void updateText() {
        text.setText(initialText);
    }


    int fontSize(TextArea text, int scale) {
        return (int) (Math.pow(text.getWidth(), 0.6) * Math.pow(text.getHeight(), 0.3) / 10 * Math.pow(1.002, scale));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
