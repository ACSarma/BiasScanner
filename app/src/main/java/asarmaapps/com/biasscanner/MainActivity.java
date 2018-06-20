package asarmaapps.com.biasscanner;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.api.services.language.v1beta2.model.Sentence;
import com.google.api.services.language.v1beta2.model.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private final String CLOUD_API_KEY = "AIzaSyDVgwrzDOpJyHu1LAUaDJtekK6jAUcMJgE";
    public final static String EXTRA_MESSAGE = "asarmaapps.com.biasscanner";
    private TextView textView;
    private TextView resultText;
    private TextView t1;
    private TextView t2;
    private TextView t3;
    private TextView t4;
    private TextView t5;
    private EditText editText;
    private float sentiment;
    private float magnitude;
    private String messageSentiment;
    private String messageSyntax;
    private String topEntity;
    private String transcript;
    private String advMessage;
    private List<Entity> entityList;
    private List<Sentence> sentencesList;
    private ArrayList<String[]> syntaxElements = new ArrayList<>();
    private ArrayList<String> highlightedWords = new ArrayList<>();
    private ArrayList<String> syntaxHighlights = new ArrayList<>();
    private ArrayList<Integer> colors = new ArrayList<>();
    private ArrayList<Float> sentenceSent = new ArrayList<>();
    private String[] overallAnalysis = {
            "This indicates a ",
            "These article's mood can be described as: ",
            "This passage is written from ",
            "Tenses used: ",
            "Voices used: "};
    private ProgressBar spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.landscape);
        }else{
            setContentView(R.layout.portrait);
        }
        textView = findViewById(R.id.speech_to_text_result);
        resultText = findViewById(R.id.result0);
        t1 = findViewById(R.id.result1);
        t2 = findViewById(R.id.result2);
        t3 = findViewById(R.id.result3);
        t4 = findViewById(R.id.result4);
        t5 = findViewById(R.id.result5);
        editText = findViewById(R.id.editText);
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.INVISIBLE);
        Button advancedButton = findViewById(R.id.advanced);
        Button enterButton = findViewById(R.id.browse_button);
//These words indicate the mood of the article and the relationships between topics in the article:
        AlertDialog dialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Welcome!")
                        .setMessage("Enter an article, press \"Enter\" and then \"Analyze\" to get results. \n" +
                                "- Words to indicate the mood and the relationships between topics in the article are highlighted in yellow.\n" +
                                "- The font color indicates overall feeling of the text. \n" +
                                "- For deeper analysis, press \"Advanced\".")
                        .setNeutralButton("Okay", null)
                        .create();
        dialog.show();

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(editText.getText());
                advMessage = editText.getText().toString();
                editText.setText("");
            }
        });
        advancedButton.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view) {
               if (!textView.getText().equals("")) {
                   Intent intent = new Intent(MainActivity.this, Advanced.class);
                   String message = advMessage + "|"+overallAnalysis[1] + "|"+overallAnalysis[2] + "|"+overallAnalysis[3] + "|"+overallAnalysis[4];
                   intent.putExtra(EXTRA_MESSAGE, message);
                   startActivity(intent);
               }else{
                   Toast.makeText(MainActivity.this, "Perform Basic Analysis before Advanced Analysis", Toast.LENGTH_LONG);
               }
           }
        });

        Button analyzeButton = findViewById(R.id.analyze_button);
        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setVisibility(View.VISIBLE);
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
                         spinner.setVisibility(View.VISIBLE);
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
                                        "\n" + overallAnalysis[0];
                                resultText.setText(message);
                                t2.setText(overallAnalysis[1] + "\n" + "(Words in this color also indicate relationships between subjects in the sentences).");
                                t2.setBackgroundColor(getColor(R.color.c1));
                                t3.setText(overallAnalysis[2]);
                                t3.setBackgroundColor(getColor(R.color.c2));
                                t4.setText(overallAnalysis[3]);
                                t4.setBackgroundColor(getColor(R.color.c2));
                                t5.setText(overallAnalysis[4]);
                                t5.setBackgroundColor(getColor(R.color.c2));

                                textView.setText(Html.fromHtml(highlight(transcript)));
                                Log.i("Final", "Final");
                                spinner.setVisibility(View.GONE);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
            sentencesList = response.getSentences();
            sentiment = response.getDocumentSentiment().getScore();
            magnitude = response.getDocumentSentiment().getMagnitude();

            for(int i=0; i<sentencesList.size(); i++){
                sentenceSent.add(sentencesList.get(i).getSentiment().getScore());
            }

Log.i("Sentence", sentencesList.get(0).getText().getContent());
            for(int i=0; i<sentenceSent.size(); i++){
                if(sentenceSent.get(i) < -0.3){
                    highlightedWords.add(sentencesList.get(i).getText().getContent());
                    colors.add(0);
                }
                if(sentenceSent.get(i) > 0.3){
                    highlightedWords.add(sentencesList.get(i).getText().getContent());
                    colors.add(1);
                }
                if(sentenceSent.get(i) >= -0.3 && sentenceSent.get(i) <= 0.3) {
                    highlightedWords.add(sentencesList.get(i).getText().getContent());
                    colors.add(2);
                }
            }
Log.i("Data", highlightedWords.get(0) + " " + sentenceSent.get(0) + " " + colors.get(0));
            if(sentiment >= -0.3 && sentiment <= 0.3){
                messageSentiment = "neutral feelings";
                if(magnitude !=0.0){
                    messageSentiment = "mixed feelings";
                }
                if(overallAnalysis[0].equals("This indicates a "))
                overallAnalysis[0] += "neutral tone.";
            }
            if(sentiment < -0.3){
                messageSentiment = "negative feelings";
                if(magnitude > 4.0){
                    messageSentiment = "strongly " + messageSentiment;
                }if(overallAnalysis[0].equals("This indicates a "))
                    overallAnalysis[0] += "negative tone.";
            }
            if(sentiment > 0.3){
                messageSentiment = "positive feelings";
                if(magnitude > 4.0){
                    messageSentiment = "strongly " + messageSentiment;
                }if(overallAnalysis[0].equals("This indicates a "))
                    overallAnalysis[0] += "positive tone.";
            }

        }catch (java.io.IOException e){
            e.printStackTrace();
        }
        return messageSentiment;
    }

    public void getPhrases(AnnotateTextRequest response){


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
                    if(!token.getPartOfSpeech().getMood().equalsIgnoreCase("MOOD_UNKNOWN")){
                        if(!overallAnalysis[1].contains(token.getPartOfSpeech().getMood())) {
                            overallAnalysis[1] += token.getPartOfSpeech().getMood() + " ";
                        }
                        syntaxHighlights.add(token.getText().getContent());
                        /*colors.add(3);*/
                    }
                    if(!token.getPartOfSpeech().getPerson().equalsIgnoreCase("PERSON_UNKNOWN")){
                        if(!overallAnalysis[2].contains(token.getPartOfSpeech().getPerson())) {
                            overallAnalysis[2] += token.getPartOfSpeech().getPerson() + ", ";
                        }
                        /*highlightedWords.add(token.getText().getContent());
                        colors.add((Color.YELLOW));*/
                    }
                    if(!token.getPartOfSpeech().getTense().equalsIgnoreCase("TENSE_UNKNOWN")){
                        if(!overallAnalysis[3].contains(token.getPartOfSpeech().getTense())) {
                            overallAnalysis[3] += token.getPartOfSpeech().getTense() + ", ";
                            /*highlightedWords.add(token.getText().getContent());
                            colors.add((Color.LTGRAY));*/
                        }
                    }
                    if(!token.getPartOfSpeech().getVoice().equalsIgnoreCase("VOICE_UNKNOWN")){
                        if(!overallAnalysis[4].contains(token.getPartOfSpeech().getVoice())) {
                            overallAnalysis[4] += token.getPartOfSpeech().getVoice() + ", ";
                        }
                        /*highlightedWords.add(token.getText().getContent());
                        colors.add((Color.MAGENTA));*/
                    }
                    syntaxElements.add(message.split(","));
                  //  Log.i("Syntax", message);
                }
                if(overallAnalysis[2].equals("This passage is written from ")){
                    overallAnalysis[2] = "";
                }else {
                    overallAnalysis[2] += "person.";
                }
                if(overallAnalysis[4].equals("with this voice: ")){
                    overallAnalysis[4] = "";
                }else {
                    overallAnalysis[4] += "voice. ";
                }
            }catch (java.io.IOException e) {
                e.printStackTrace();
            }
    }

    public void finishMessage (){
        String entities = "";
        messageSyntax = "";
        topEntity = "";
        if(entityList.size()>0) {
            for (Entity entity : entityList) {
                entities += entity.getName().toUpperCase() + " " + entity.getSalience() + "\n";
            }
            if (entityList.size() > 3) {
                for (int i = 0; i < 4; i++) {
                    topEntity += "\t\n" + entityList.get(i).getName().toUpperCase();
                }
            } else {
                topEntity = entities.substring(0, entities.indexOf(" "));

            }
        }
        for(int s=0; s<overallAnalysis.length; s++){
            messageSyntax += overallAnalysis[s] + "\n";
        }
    }

    private String highlight(String s){
        String[] color = {"<span style=\"color: #FF0000\">",
                "<span style=\"color: #4de14d\">",
                "<span style=\"color: #0000ff\">",
                "<span style=\"background-color: #FFFF00\">"};
        String[] font = {"<font color='red'>", "<font color='low green'>", "<font color='high blue'>"};
        String f = "";
        for(int i=highlightedWords.size()-1; i>=0; i--){
/*
            f +=  color[colors.get(i)] + s.substring(s.indexOf(highlightedWords.get(i)), s.indexOf(highlightedWords.get(i)) + highlightedWords.get(i).length()) + "</span>";
*/
            s = s.replaceAll(highlightedWords.get(i), color[colors.get(i)] + highlightedWords.get(i) + "</span>");
        }
        for(int i=0; i<syntaxHighlights.size(); i++){
            s = s.replaceAll(" " + syntaxHighlights.get(i) + " ", "<span style=\"color: #00000000; background-color: #FFFF00\">" + " " + syntaxHighlights.get(i) + " " + "</span>");
        }
        return s;
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
                Log.i("Highlight", highlightedWords.get(index));
                highlight.setSpan(
                        new BackgroundColorSpan(colors.get(index)),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return highlight;
    }

    private void updateLayout(){

    }
}
