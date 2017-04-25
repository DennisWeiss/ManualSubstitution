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
            lbls[i].setFont(new Font("Arial", scene.getWidth() / 35));
            subs[i].setFont(new Font("Arial", scene.getWidth() / 50));

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

        undo.setFont(new Font("Arial", scene.getWidth() / 50));
        redo.setFont(new Font("Arial", scene.getWidth() / 50));

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
                lbls[i].setFont(new Font("Arial", scene.getWidth() / 35));
                button.setFont(new Font("Arial", scene.getWidth() / 35));
                undo.setFont(new Font("Arial", scene.getWidth() / 50));
                redo.setFont(new Font("Arial", scene.getWidth() / 50));
                subs[i].setFont(new Font("Arial", scene.getWidth() / 50));
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
        button.setFont(new Font("Arial", scene.getWidth() / 35));
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

        primaryStage.setTitle("Manual Substitution");
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
        VBox root = new VBox();
        HBox hbox = new HBox();
        Region region1 = new Region();
        Region region2 = new Region();
        Button yes = new Button("Yes");
        Button no = new Button("No");
        yes.setFont(new Font("Arial", 15));
        no.setFont(new Font("Arial", 15));

        region1.setPrefWidth(70);
        region2.setPrefWidth(30);

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

        hbox.getChildren().addAll(region1, yes, region2, no);
        Scene scene = new Scene(root, 250, 280);
        Label[] label = new Label[7];
        label[0] = new Label("Your input: " + first + " -> " + third);
        label[1] = new Label("clashes with the input " + second + " -> " + third);
        label[2] = new Label("Do you still wish to retain your entries?");
        label[3] = new Label("If YES, your inputs will be adopted and the");
        label[4] = new Label("conflicting field will be set to indeterminate");
        label[5] = new Label("If NO, your input will be ignored and the field");
        label[6] = new Label("will be reset to its last valid value.");
        for (int i = 0; i < label.length; i++) {
            label[i].setFont(new Font("Arial", 12));
            root.getChildren().add(label[i]);
            if (i == 0 || i == 2 || i == 3 || i == 5) {
                label[i].setPadding(new Insets(15, 10, 0, 10));
            } else if (i == 6){
                label[i].setPadding(new Insets(5, 10, 25, 10));
            } else {
                label[i].setPadding(new Insets(5, 10, 0, 10));
            }

        }
        root.getChildren().add(hbox);
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
        HBox hBox = new HBox();
        TextArea text = new TextArea();
        text.setWrapText(true);
        root.setMargin(text, new Insets(7));
        Button ok = new Button("OK");
        Region region1 = new Region();
        Region region2 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);
        HBox.setHgrow(region2, Priority.ALWAYS);
        ok.setFont(new Font("Arial", 17));
        hBox.setPadding(new Insets(10));
        hBox.getChildren().addAll(region1, ok, region2);
        root.getChildren().addAll(text, hBox);
        VBox.setVgrow(text, Priority.ALWAYS);
        ok.setOnMouseClicked(event -> {
            initialText = text.getText();
            stage.close();
            updateText();
        });
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("Enter encrypted text");
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
