package asarmaapps.com.biasscanner;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta2.model.AnalyzeSyntaxRequest;
import com.google.api.services.language.v1beta2.model.AnalyzeSyntaxResponse;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Entity;
import com.google.api.services.language.v1beta2.model.Features;
import com.google.api.services.language.v1beta2.model.Token;

import java.util.ArrayList;
import java.util.List;

/*
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.LanguageServiceClient;
*/

public class MainActivity extends AppCompatActivity {
    private final String CLOUD_API_KEY = "AIzaSyDVgwrzDOpJyHu1LAUaDJtekK6jAUcMJgE";
    private TextView textView;
    private TextView resultText;
    private EditText editText;
    private float sentiment;
    private float magnitude;
    private String messageSentiment;
    private String messageSyntax;
    private String topEntity;
    private List<Entity> entityList;
    private ArrayList<String[]> syntaxElements = new ArrayList<>();
    private String[] overallAnalysis = {"In this passage, the author is very:  ",
                                        "The author\'s attitude towards the topics can be described as: ",
                                        "This passage is written from ",
                                        "in this tense: ",
                                        "with this voice: "};
    private boolean[] hasAttribute = new boolean[overallAnalysis.length];
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
                        Log.i("Syntax", "Done1");
                        messageSentiment = (getSentiment(document, naturalLanguageService));
                        Log.i("Sentiment", "Done2");
                        goAnalyze();
                        Log.i("Analyze", "Done3");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String message = "This passage has " + messageSentiment + " about " + topEntity + "." +
                                        "\n" + messageSyntax;
                                resultText.setText(message);
                                /*AlertDialog dialog =
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Sentiment: " + sentiment + " Mag: " + magnitude)
                                                .setMessage(message)
                                                .setNeutralButton("Okay", null)
                                                .create();
                                dialog.show();*/
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
                messageSentiment = "neutral feelings";
                if(magnitude !=0.0){
                    messageSentiment = "mixed feelings";
                }
            }
            if(sentiment < -0.1){
                messageSentiment = "negative feelings";
                if(magnitude > 4.0){
                    messageSentiment = "strongly " + messageSentiment;
                }
            }
            if(sentiment > 0.1){
                messageSentiment = "positive feelings";
                if(magnitude > 4.0){
                    messageSentiment = "strongly " + messageSentiment;
                }
            }
        }catch (java.io.IOException e){
            e.printStackTrace();
        }
        return messageSentiment;
    }

    public String getSyntax(Document doc, CloudNaturalLanguage naturalLanguageService){
        String message="-";
        String messageR="-";

            try{
                final AnalyzeSyntaxRequest request = new AnalyzeSyntaxRequest();
                request.setDocument(doc);
                // analyze the syntax in the given text
                AnalyzeSyntaxResponse response =
                        naturalLanguageService.documents().
                                analyzeSyntax(request).execute();
                // print the response
                for (Token token : response.getTokens()) {
                    messageR += token.toPrettyString() + "\n";
                    Log.i("getText", token.getText().getContent());
                    /*message = token.getText().getContent() + "," +
                            token.getPartOfSpeech().getCase() + "," +
                            token.getPartOfSpeech().getMood() + "," +
                            token.getPartOfSpeech().getPerson() + "," +
                            token.getPartOfSpeech().getTense() + "," +
                            token.getPartOfSpeech().getVoice();*/
                    if(!token.getPartOfSpeech().getCase().equalsIgnoreCase("CASE_UNKNOWN")){
                        if(!overallAnalysis[0].contains(token.getPartOfSpeech().getCase())) {
                            overallAnalysis[0] += token.getPartOfSpeech().getCase() + " ";
                        }
                    }
                    if(!token.getPartOfSpeech().getMood().equalsIgnoreCase("MOOD_UNKNOWN")){
                        if(!overallAnalysis[1].contains(token.getPartOfSpeech().getMood())) {
                            overallAnalysis[1] += token.getPartOfSpeech().getMood() + " ";
                        }
                    }
                    if(!token.getPartOfSpeech().getPerson().equalsIgnoreCase("PERSON_UNKNOWN")){
                        if(!overallAnalysis[2].contains(token.getPartOfSpeech().getPerson())) {
                            overallAnalysis[2] += token.getPartOfSpeech().getPerson() + " ";
                        }                    }
                    if(!token.getPartOfSpeech().getTense().equalsIgnoreCase("TENSE_UNKNOWN")){
                        if(!overallAnalysis[3].contains(token.getPartOfSpeech().getTense())) {
                            overallAnalysis[3] += token.getPartOfSpeech().getTense() + " ";
                        }                    }
                    if(!token.getPartOfSpeech().getVoice().equalsIgnoreCase("VOICE_UNKNOWN")){
                        if(!overallAnalysis[4].contains(token.getPartOfSpeech().getVoice())) {
                            overallAnalysis[4] += token.getPartOfSpeech().getVoice() + " ";
                        }
                    }
                    Log.i("message", message);
                    syntaxElements.add(message.split(","));
                  //  Log.i("Syntax", message);
                }
                overallAnalysis[2] += "person.";
                overallAnalysis[3] += "tense.";
                overallAnalysis[4] += "voice. ";

            }catch (java.io.IOException e) {
                e.printStackTrace();
            }
            return message;
    }

    public void goAnalyze (){
        String entities = "";
        Log.i("SizeE", ""+entityList.size());
        if(entityList.size()>0) {
            for (Entity entity : entityList) {
                entities += entity.getName().toUpperCase() + " " + entity.getSalience() + "\n";
            }
            Log.i("AnalysisE", entityList.get(0).getName());
            topEntity = entities.substring(0, entities.indexOf(" "));
            Log.i("Entity", topEntity);
        }
       /* Log.i("Size", ""+syntaxElements.size());
        for(int i=0; i<syntaxElements.size(); i++){
            Log.i("ArraySize", ""+syntaxElements.get(i).length);
            int counter = 0;
            for(int r=0; r<syntaxElements.get(i).length; r++){
                if(!syntaxElements.get(i)[r].contains("_UNKNOWN")){
                    overallAnalysis[counter] += syntaxElements.get(i)[r] + ", ";
                    Log.i("Data", syntaxElements.get(i)[r]);
                    hasAttribute[counter] = true;
                    counter++;
                }
                Log.i("AnalysisW", "Loop2");
            }
            Log.i("AnalysisW", "Loop1");
        }
        Log.i("AnalysisW", "Done!");
        for (int b=0; b<hasAttribute.length; b++){
            if(hasAttribute[b] == false){
                overallAnalysis[b] = "";
            }
        }*/
        for(int s=0; s<overallAnalysis.length; s++){
            messageSyntax += overallAnalysis[s] + "\n";
            Log.i("Final MSG", messageSyntax);
        }
        return;
    }
}
