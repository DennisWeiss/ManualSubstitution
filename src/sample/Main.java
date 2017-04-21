package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    int scale;
    boolean ctrlPressed = false;

    @Override
    public void start(Stage primaryStage) throws Exception{
        VBox root = new VBox();
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        Scene scene = new Scene(root, 400, 300);
        Region[] regions = new Region[2];
        for (int i = 0; i < 2; i++) {
            regions[i] = new Region();
            regions[i].setMinWidth(scene.getWidth()/8);
        }
        GridPane grid = new GridPane();
        TextField[] subs = new TextField[26];
        HBox[] elms = new HBox[26];
        Label[] lbls = new Label[26];

        for (int i = 0; i < 26; i++) {
            elms[i] = new HBox();
            int width = (int) scene.getWidth()/60;
            elms[i].setPadding(new Insets(2, width, 2, width));
            subs[i] = new TextField("*");
            lbls[i] = new Label(String.valueOf((char) (i + 97)) + ":");
            lbls[i].setMinWidth(scene.getWidth() / 19);
            lbls[i].setFont(new Font("Arial", scene.getWidth()/28));
            subs[i].setFont(new Font("Arial", scene.getWidth()/40));

            final int x = i;

            subs[i].lengthProperty().addListener(((observable, oldValue, newValue) -> {
                if ((int) newValue > 1) {
                    subs[x].setText(subs[x].getText().substring(0, 1));
                }
            }));

            elms[i].getChildren().addAll(lbls[i], subs[i]);
            grid.add(elms[i], i%7, i/7);
        }

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                for (int i = 0; i < 26; i++) {
                    lbls[i].setMinWidth(scene.getWidth() / 19);
                    int width = (int) scene.getWidth()/60;
                    elms[i].setPadding(new Insets(2, width, 2, width));
                    lbls[i].setMinWidth(scene.getWidth() / 19);
                    lbls[i].setFont(new Font("Arial", scene.getWidth()/28));
                    subs[i].setFont(new Font("Arial", scene.getWidth()/40));
                }

                for (int i = 0; i < 2; i++) {
                    regions[i] = new Region();
                    regions[i].setMinWidth(scene.getWidth()/8);
                }

            }
        });
        vbox.getChildren().addAll(regions[0], grid, regions[1]);
        hbox.getChildren().addAll(vbox);
        TextArea text = new TextArea();
        root.setMargin(text, new Insets(10));
        root.setMargin(hbox, new Insets(7, 3, 3, 3));
        VBox.setVgrow(text, Priority.ALWAYS);
        root.getChildren().addAll(hbox, text);

        text.widthProperty().addListener((observable, oldValue, newValue) -> text.setFont(new Font("Arial", fontSize(text, scale))));

        text.widthProperty().addListener((observable, oldValue, newValue) -> text.setFont(new Font("Arial", fontSize(text, scale))));

        scene.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("CONTROL")) {
                ctrlPressed = true;
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode().toString().equals("CONTROL")) {
                ctrlPressed = false;
            }
        });

        text.setOnScroll(event -> {
            if (ctrlPressed) {
                scale += event.getDeltaY();
            }
            text.setFont(new Font("Arial", fontSize(text, scale)));
        });

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(scene);
        primaryStage.show();

        text.setFont(new Font("Arial", fontSize(text, scale)));
    }

    int fontSize(TextArea text, int scale) {
        return (int) (Math.pow(text.getWidth(), 0.6) * Math.pow(text.getHeight(), 0.3) / 10 * Math.pow(1.002, scale));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
