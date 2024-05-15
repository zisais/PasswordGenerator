/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package passwordgenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;
import java.math.BigInteger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 *
 * @author ztisa
 */
public class PasswordGenerator extends Application {
    byte[] genHash = new byte[32];
    char[] alphanumKeys = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    char[] specialKeys = "!@#$%^&*()_+{}[]|\\:;\"'<>,.?/~".toCharArray();
    
    @Override
    public void start(Stage primaryStage) {
        Background paneBack = new Background(new BackgroundFill(Color.LIGHTGRAY,null,null));
        
        // controls
        Label lblHead = new Label("Password Generator");
        lblHead.getStyleClass().add("title-label");
        Label lblInstruct1 = new Label("Click and drag inside of the pane to begin generating your random password.");
        Label lblInstruct2 = new Label("The longer you drag for, the more random your password will be.");
        Label lblPassLabel = new Label("Password generated: ");
        TextField txtResult = new TextField();
        txtResult.getStyleClass().add("rslt");
        txtResult.setEditable(false);
        txtResult.setPrefWidth(300);
        Label lblSlider = new Label("Number of characters:\t");
        CheckBox specCheck = new CheckBox("Include special characters");
        Slider slider = new Slider(8,32,8);
        Pane clickBox = new Pane();
        clickBox.getStyleClass().add("drag-box");
        clickBox.setBackground(paneBack);
        specCheck.setLayoutX(clickBox.getLayoutBounds().getMinX());
        
        // paddings
        Insets head = new Insets(5,0,10,0);
        Insets instruct1 = new Insets(5,0,0,0);
        Insets instruct2 = new Insets(0,0,10,0);
        Insets hboxpadding = new Insets(15,0,10,35);
        Insets slide = new Insets(5,0,5,0);
        Insets speckey = new Insets(0,0,5,0);
        
        clickBox.setOnMouseDragged(e -> {
            double paneX = clickBox.getLayoutBounds().getMinX();
            double paneY = clickBox.getLayoutBounds().getMinY();
            double paneW = clickBox.getWidth();
            double paneH = clickBox.getHeight();
            double numChars = slider.getValue();
            
            double x = (double)e.getX();
            double y = (double)e.getY();
            double r = 3;
            
            if (x >= paneX && x <= (paneX + paneW) && y >= paneY && y <= (paneY + paneH)) {
                Circle circ = new Circle(x,y,r,Color.PALETURQUOISE);
                clickBox.getChildren().add(circ);
            }
            
            txtResult.setText(genPass(x,y,numChars, specCheck.isSelected()));
        });
        
        // containers
        HBox hbox = new HBox(lblPassLabel, txtResult);
        HBox hbox2 = new HBox(lblSlider,slider);
        VBox vbox = new VBox(lblHead,lblInstruct1, lblInstruct2, hbox2, specCheck, clickBox, hbox);
        
        // format
        lblHead.setPadding(head);
        lblInstruct1.setPadding(instruct1);
        lblInstruct2.setPadding(instruct2);
        lblPassLabel.setPadding(instruct1);
        hbox.setPadding(hboxpadding);
        lblSlider.setPadding(slide);
        slider.setPadding(slide);
        slider.setMinWidth(400);
        slider.setMaxWidth(400);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        specCheck.setPadding(speckey);
        clickBox.setPrefSize(590, 225);
        clickBox.setMaxSize(590,225);
        hbox2.setAlignment(Pos.CENTER);
        vbox.setAlignment(Pos.CENTER);

        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        
        Scene scene = new Scene(root, 650, 475);
        scene.getStylesheets().add("/css/styles.css");
        
        primaryStage.setTitle("Password Generator");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public String genPass(double x, double y, double len, boolean specKeys) {
        String password = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * 2 + genHash.length);
            
            buffer.put(genHash);
            buffer.putDouble(x);
            buffer.putDouble(y);
            
            byte[] input = buffer.array();
            genHash = md.digest(input);
            // Convert the byte array to hexadecimal string
            String hexString = genHex();
            System.out.println(hexString);
            password = hexToPass(hexString, len, specKeys);
        }
        catch (NoSuchAlgorithmException e) { }
        
        return password;
    }
    
    public String genHex() {
        StringBuilder hexString = new StringBuilder();
        for (byte b : genHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    public String hexToPass(String hexString, double len, boolean specKeys) {
        String pw;

        BigInteger hashNum = new BigInteger(hexString, 16);
        StringBuilder password = new StringBuilder();

        int nextSegment = 0;
        for(int i=0;i<len;i++) {
            String segment = hexString.substring(nextSegment, nextSegment+2);
            int segInt = Integer.parseInt(segment,16);
            hashNum = hashNum.subtract(BigInteger.valueOf(segInt));
            BigInteger place = hashNum.mod(BigInteger.valueOf(alphanumKeys.length));
            password.append(alphanumKeys[place.intValue()]);
            nextSegment += 2; // move one byte at a time
        }

        // include special keys if needed by replacing existing chars
        if (specKeys) {
            for (int i=0;i<(int)(Math.random() * len / 2 + 1);i++) { // replace no more than half the chars with a special
                int newPlace = (int)(Math.random() * specialKeys.length);
                int oldPlace = (int)(Math.random() * password.length());
                password.setCharAt(oldPlace, specialKeys[newPlace]);
            }
        }
        else { }

        pw = password.toString();
            
        System.out.println(pw);
        return pw;
    }
}
