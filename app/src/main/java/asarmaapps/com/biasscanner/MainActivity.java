package asarmaapps.com.biasscanner;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;

import com.google.api.services.language.v1beta2.model.AnalyzeSyntaxRequest;
import com.google.api.services.language.v1beta2.model.AnalyzeSyntaxResponse;
import com.google.api.services.language.v1beta2.model.AnalyzeSentimentRequest;
import com.google.api.services.language.v1beta2.model.AnalyzeSentimentResponse;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Entity;
import com.google.api.services.language.v1beta2.model.Features;
import com.google.api.services.language.v1beta2.model.Token;
//import com.google.cloud.language.v1.LanguageServiceClient;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String CLOUD_API_KEY = "AIzaSyDVgwrzDOpJyHu1LAUaDJtekK6jAUcMJgE";
    private TextView textView;
    private TextView resultText;
    private EditText editText;
    private float sentiment;
    private float magnitude;
    private String messageSentiment;
    private String messageSyntax;
    private List<Entity> entityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.speech_to_text_result);
        resultText = findViewById(R.id.result);
        editText = findViewById(R.id.editText);

        Button enterButton = findViewById(R.id.browse_button);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(editText.getText());
            }
        });

        Button analyzeButton = findViewById(R.id.analyze_button);
        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //NaturalLanguageService
                final CloudNaturalLanguage naturalLanguageService =
                        new CloudNaturalLanguage.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new AndroidJsonFactory(),
                                null
                        ).setCloudNaturalLanguageRequestInitializer(
                                new CloudNaturalLanguageRequestInitializer(CLOUD_API_KEY)
                        ).build();

                String transcript =
                        ((TextView)findViewById(R.id.speech_to_text_result))
                                .getText().toString();

                final Document document = new Document();
                document.setType("PLAIN_TEXT");
                document.setLanguage("en-US");
                document.setContent(transcript);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                     try{
                        messageSyntax = (getSyntax(document, naturalLanguageService));
                        messageSentiment = (getSentiment(document, naturalLanguageService));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultText.setText(messageSyntax);
                                String entities = "";
                                for (Entity entity : entityList) {
                                    entities += "\n" + entity.getName().toUpperCase() + " " + entity.getSalience();
                                }
                                AlertDialog dialog =
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Sentiment: " + sentiment + " Mag: " + magnitude)
                                                .setMessage(messageSentiment+ "\n" + "This audio file talks about :"
                                                        + entities + "\n" + messageSyntax)
                                                .setNeutralButton("Okay", null)
                                                .create();
                                dialog.show();
                            }
                        });
                     }catch (Exception  IOException){

                     }

                    }
                });
            }
        });
    }

    public String getSentiment(Document doc, CloudNaturalLanguage naturalLanguageService){
        String message="-";

        Features features = new Features();
        features.setExtractEntities(true);
        features.setExtractDocumentSentiment(true);

        final AnnotateTextRequest request = new AnnotateTextRequest();
        request.setDocument(doc);
        request.setFeatures(features);

        try{
            AnnotateTextResponse response =
                    naturalLanguageService.documents()
                            .annotateText(request).execute();

            entityList = response.getEntities();
            sentiment = response.getDocumentSentiment().getScore();
            magnitude = response.getDocumentSentiment().getMagnitude();

            if(sentiment >= -0.1 && sentiment <= 0.1){
                message = "This text is neutral";
            }
            if(sentiment < -0.1){
                message = "This text is negative";
            }
            if(sentiment > 0.1){
                message = "This text is positive";
            }

        }catch (java.io.IOException e){
            e.printStackTrace();
        }

        return message;
    }

    public String getSyntax(Document doc, CloudNaturalLanguage naturalLanguageService){
        String message="-";

            try{
                final AnalyzeSyntaxRequest request = new AnalyzeSyntaxRequest();
                request.setDocument(doc);
                // analyze the syntax in the given text
                AnalyzeSyntaxResponse response =
                        naturalLanguageService.documents().
                                analyzeSyntax(request).execute();
                // print the response
                for (Token token : response.getTokens()) {
                    message = token.toPrettyString() + "\n";
                   // message = message.substring(message.indexOf("\"case\":"), message.indexOf(","))+ "\n";
                            /*("Gender: " + token.getPartOfSpeech().getGender()) +
                            ("\tMood: " + token.getPartOfSpeech().getMood()) +
                            ("\tNumber: " + token.getPartOfSpeech().getNumber()) +
                            ("\tPerson: " + token.getPartOfSpeech().getPerson()) +
                            ("\tProper: " + token.getPartOfSpeech().getProper()) +
                            ("\tnReciprocity: " + token.getPartOfSpeech().getReciprocity()) +
                            ("\tTense: " + token.getPartOfSpeech().getTense()) +
                            ("\tVoice: " + token.getPartOfSpeech().getVoice()) + "\n";*/
                    Log.i("Syntax", token.toPrettyString());
                }

            }catch (java.io.IOException e){
                e.printStackTrace();
            }
            return message;
    }

}
