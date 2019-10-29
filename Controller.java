package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public class Controller {
    private static final String API_KEY = "&units=imperial&appid=e7faebfe494a131c02e0b1fbbadd1b19";
    private static final String GET_URL = "https://api.openweathermap.org/data/2.5/weather?";
    private static String CITY_OR_ZIP;

    private boolean submitZipInput = false;
    private boolean submitCityInput = false;

    @FXML
    private TextField zipTextInput;
    @FXML
    private Label tempLabel;
    @FXML
    private Label descLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label humidityLabel;
    @FXML
    private Button submitButton;
    @FXML
    private Label errorLabel;
    @FXML
    private VBox borderPaneTop;
    @FXML
    private ImageView iconImageView;
    private Image iconImage;

    public void initialize() {
        submitButton.setDisable(true);
        errorLabel.setVisible(false);
        errorLabel.prefWidthProperty().bind(borderPaneTop.widthProperty().multiply(.5));
    }

    public void getRequest() throws IOException {
        if(submitZipInput) {
            CITY_OR_ZIP = "zip=" + zipTextInput.getText();
        }

        if(submitCityInput) {
            CITY_OR_ZIP = "q=" + zipTextInput.getText();
        }

        URL urlObj = new URL(GET_URL + CITY_OR_ZIP + API_KEY);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setRequestMethod("GET");
        try {
            int responseCode = con.getResponseCode();
            System.out.println("Response: " + responseCode);
            if (responseCode==200) {
                errorLabel.setVisible(false);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject obj = new JSONObject(response.toString());
                System.out.println(obj);
                System.out.println(obj.names());
                int temp = (int)obj.getJSONObject("main").getDouble("temp");
                int humidity = obj.getJSONObject("main").getInt("humidity");
                JSONObject weatherObj = obj.getJSONArray("weather").getJSONObject(0);
                String weatherDesc = weatherObj.getString("description");
                displayIcon(weatherObj.getString("icon"));
                String city = obj.getString("name");

                tempLabel.setText("Temperature: " + temp);
                descLabel.setText(weatherDesc.substring(0,1).toUpperCase() + weatherDesc.substring(1).toLowerCase());
                cityLabel.setText(city);
                humidityLabel.setText("Humidity: " + humidity);
            } else {
                errorLabel.setText("You must enter a valid city or zip code.");
                errorLabel.setVisible(true);
            }
        } catch(UnknownHostException exc) {
            errorLabel.setText("Unable to connect to weather service.");
            errorLabel.setVisible(true);
        }
    }

    public void displayIcon(String iconCode) {
        System.out.println(iconCode);
        iconImage = new Image("http://openweathermap.org/img/wn/" + iconCode + "@2x.png");
        iconImageView.setImage(iconImage);
    }

    public void onZipTextInput(KeyEvent e) throws IOException {
        if(zipTextInput.getText().matches("\\d{5}")) {
            submitButton.setDisable(false);
            submitZipInput = true;
            submitCityInput = false;
        } else if(zipTextInput.getText().matches("([A-Za-z]+,*\\s*)+")) {
            submitButton.setDisable(false);
            submitZipInput = false;
            submitCityInput = true;
        } else {
            submitButton.setDisable(true);
        }
        if ((submitCityInput || submitZipInput) && (e.getCode() == KeyCode.ENTER)) {
            getRequest();
        }
    }
}
