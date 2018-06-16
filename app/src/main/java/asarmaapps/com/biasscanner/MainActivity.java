package asarmaapps.com.biasscanner;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private String transcript;
    private List<Entity> entityList;
    private ArrayList<String[]> syntaxElements = new ArrayList<>();
    private ArrayList<String> highlightedWords = new ArrayList<>();
    private ArrayList<Integer> colors = new ArrayList<>();
    private String[] overallAnalysis = {
            "This indicates a ",
            "The author\'s attitude towards the topics can be described as: ",
            "This passage is written from ",
            "in this tense: ",
            "with this voice: "};
    private ToneWords toneWords = new ToneWords();
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

                transcript = ((TextView)findViewById(R.id.speech_to_text_result))
                                .getText().toString();

                final Document document = new Document();
                document.setType("PLAIN_TEXT");
                document.setLanguage("en-US");
                document.setContent(transcript);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                     try{
                        getSyntax(document, naturalLanguageService);
                        Log.i("Syntax", "Done1");
                        messageSentiment = (getSentiment(document, naturalLanguageService));
                        Log.i("Sentiment", "Done2");
                        finishMessage();
                        Log.i("Analyze", "Done3");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String message = "This passage has " + messageSentiment + " about " + topEntity + "." +
                                        "\n" + messageSyntax;
                                resultText.setText(message);
                                textView.setText(highlightSearchKey(transcript));
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

    public void updateOverall(){
        Spannable  highlight;
        int        word_index;
        String     title_str;

        word_index = overallAnalysis.length;
        for(int i=0; i<word_index; i++) {
            String s = overallAnalysis[i];
            title_str = Html.fromHtml(s).toString();
            highlight = (Spannable) Html.fromHtml(s);
            highlight.setSpan(
                    new BackgroundColorSpan(colors.get(i)),
                    0,
                    title_str.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

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
                overallAnalysis[0] += toneWords.getWords("neut");
            }
            if(sentiment < -0.1){
                messageSentiment = "negative feelings";
                if(magnitude > 4.0){
                    messageSentiment = "strongly " + messageSentiment;
                }
                overallAnalysis[0] += toneWords.getWords("neg");
            }
            if(sentiment > 0.1){
                messageSentiment = "positive feelings";
                if(magnitude > 4.0){
                    messageSentiment = "strongly " + messageSentiment;
                }
                overallAnalysis[0] += toneWords.getWords("pos");
            }
        }catch (java.io.IOException e){
            e.printStackTrace();
        }
        overallAnalysis[0] += "tone.";
        return messageSentiment;
    }

    public void getSyntax(Document doc, CloudNaturalLanguage naturalLanguageService){
        String message="";

            try{
                final AnalyzeSyntaxRequest request = new AnalyzeSyntaxRequest();
                request.setDocument(doc);
                // analyze the syntax in the given text
                AnalyzeSyntaxResponse response =
                        naturalLanguageService.documents().
                                analyzeSyntax(request).execute();
                // print the response
                for (Token token : response.getTokens()) {
                    message += token.toPrettyString() + "\n";
                    Log.i("getText", token.getText().getContent());
                    if(!token.getPartOfSpeech().getMood().equalsIgnoreCase("MOOD_UNKNOWN")){
                        if(!overallAnalysis[1].contains(token.getPartOfSpeech().getMood())) {
                            overallAnalysis[1] += token.getPartOfSpeech().getMood() + " ";
                        }
                        highlightedWords.add(token.getText().getContent());
                        colors.add((Color.CYAN));
                    }
                    if(!token.getPartOfSpeech().getPerson().equalsIgnoreCase("PERSON_UNKNOWN")){
                        if(!overallAnalysis[2].contains(token.getPartOfSpeech().getPerson())) {
                            overallAnalysis[2] += token.getPartOfSpeech().getPerson() + " ";
                        }
                        highlightedWords.add(token.getText().getContent());
                        colors.add((Color.YELLOW));
                    }
                    if(!token.getPartOfSpeech().getTense().equalsIgnoreCase("TENSE_UNKNOWN")){
                        if(!overallAnalysis[3].contains(token.getPartOfSpeech().getTense())) {
                            overallAnalysis[3] += token.getPartOfSpeech().getTense() + " ";
                            highlightedWords.add(token.getText().getContent());
                            colors.add((Color.LTGRAY));
                        }
                    }
                    if(!token.getPartOfSpeech().getVoice().equalsIgnoreCase("VOICE_UNKNOWN")){
                        if(!overallAnalysis[4].contains(token.getPartOfSpeech().getVoice())) {
                            overallAnalysis[4] += token.getPartOfSpeech().getVoice() + " ";
                        }
                        highlightedWords.add(token.getText().getContent());
                        colors.add((Color.RED));
                    }
                    Log.i("message", message);
                    syntaxElements.add(message.split(","));
                  //  Log.i("Syntax", message);
                }
                overallAnalysis[2] += "person.";
                overallAnalysis[4] += "voice. ";

            }catch (java.io.IOException e) {
                e.printStackTrace();
            }
    }

    public void finishMessage (){
        String entities = "";
        messageSyntax = "";
        Log.i("SizeE", ""+entityList.size());
        if(entityList.size()>0) {
            for (Entity entity : entityList) {
                entities += entity.getName().toUpperCase() + " " + entity.getSalience() + "\n";
            }
            Log.i("AnalysisE", entityList.get(0).getName());
            topEntity = entities.substring(0, entities.indexOf(" "));
            Log.i("Entity", topEntity);
        }
        for(int s=0; s<overallAnalysis.length; s++){
            messageSyntax += overallAnalysis[s] + "\n";
            Log.i("Final MSG", messageSyntax);
        }
    }

    private Spannable highlightSearchKey(String title) {
        Spannable  highlight;
        Pattern pattern;
        Matcher matcher;
        int        word_index;
        String     title_str;

        word_index = highlightedWords.size();
        title_str  = Html.fromHtml(title).toString();
        highlight  = (Spannable) Html.fromHtml(title);
        for (int index = 0; index < word_index; index++) {
            pattern = Pattern.compile("(?i)" + highlightedWords.get(index));
            matcher = pattern.matcher(title_str);
            while (matcher.find()) {
                highlight.setSpan(
                        new BackgroundColorSpan(colors.get(index)),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return highlight;
    }
}
