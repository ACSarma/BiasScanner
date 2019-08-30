package asarmaapps.com.biasscanner;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Advanced extends AppCompatActivity {

    private final String CLOUD_API_KEY = "API KEY HERE";
    private String advancedMessage;
    private TextView advText;
    private TextView advAnalysis;
    private Button button;
    private TextView t2;
    private TextView t3;
    private TextView t4;
    private TextView t5;
    private String message;
    private String transcript="";
    private String[] Addition = {"indeed",	"further","either", "as well",
    	"moreover",	"what is more",	"as a matter of fact",	"in all honesty",
    "and",	"furthermore",	"in addition ",	"besides ",	"to tell the truth",
    "or",	"in fact",	"actually",	"to say nothing of",
    "too","let alone","much less", "additionally",
    "nor",	"alternatively","on the other hand","not to mention"};
    private String[] Conflict = {"but", "by way of contrast", "while", "on the other hand",
            "however" , "yet", "whereas", "though", " in contrast", "when in fact", "conversely",  "still"};
    private String[] punct = {", ","; ",": ","-"};
    private String[] allPOS = {",",";",":","-",  " but", " by way of contrast", " while", " on the other hand",
            " however" , " yet", " whereas", " though", " in contrast", " when in fact", " conversely",  " still",
            " indeed",	" further"," either", " as well",
            " moreover",	" what is more",	" as a matter of fact",	" in all honesty",
            " and",	" furthermore",	" in addition ",	" besides ",	" to tell the truth",
            " or",	" in fact",	" actually",	" to say nothing of",
            " too"," let alone", " much less", " additionally",
            " nor",	" alternatively", " on the other hand", " not to mention"};
    private String[][] posArr = {punct, Conflict, Addition};
    private ArrayList<String> sentenceResult = new ArrayList<>();
    private ArrayList<String> phrases = new ArrayList<>();
    private ArrayList<String> keyWords = new ArrayList<>();
    private ArrayList<Float> phraseSent = new ArrayList<>();
    private ArrayList<Integer> colors = new ArrayList<>();
    private ProgressBar spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String[] messageArr = message.split("\\|");
        advancedMessage = messageArr[0];
        setContentView(R.layout.activity_advanced);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        t2 = findViewById(R.id.result2);
        t3 = findViewById(R.id.result3);
        t4 = findViewById(R.id.result4);
        t5 = findViewById(R.id.result5);
        t2.setText(messageArr[1]);
        t2.setBackgroundColor(getColor(R.color.c1));
        t3.setText(messageArr[2]);
        t3.setBackgroundColor(getColor(R.color.c2));
        t4.setText(messageArr[3]);
        t4.setBackgroundColor(getColor(R.color.c2));
        t5.setText(messageArr[4]);
        t5.setBackgroundColor(getColor(R.color.c2));
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.INVISIBLE);
        advText = findViewById(R.id.advText);
        advText.setText(advancedMessage);
        advAnalysis = findViewById(R.id.advAnalysis);
        button = findViewById(R.id.analyze_button);

        button.setOnClickListener(new View.OnClickListener() {
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
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setVisibility(View.VISIBLE);
                        goSplit();
                        goAnalyze(naturalLanguageService);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                advText.setText(Html.fromHtml(highlight(transcript)));
                                spinner.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }

        });
    }

    private void goSplit(){
        String[] arr = (advancedMessage.split("\\."));
        for(String string: arr){
            sentenceResult.add(string);
        }
        for(int r=0; r<sentenceResult.size(); r++){
            boolean hasSpecialWord = false;
            /*outerloop:
            for(int r2=0; r2<posArr.length; r2++) {*/
                for(String s: allPOS){
                    /*if(r2==0){
                        if(sentenceResult[r].contains(s)){
                            customSplit(sentenceResult[r], s);
                            hasSpecialWord = true;
                            break outerloop;
                        }
                    }*/
                    if(sentenceResult.get(r).contains(s + " ")){
                        customSplit(sentenceResult.get(r), s);
                        hasSpecialWord = true;
                        break;
                    }
                }
            //}
            if(!hasSpecialWord){
                phrases.add(sentenceResult.get(r));
            }
        }
    }

    public void customSplit(String input, String split){
        String s1 = input.substring(0, input.indexOf(split));
        String s2 = input.substring(input.indexOf(split));
        phrases.add(s1);
        phrases.add(s2);
        keyWords.add(split);
    }

    private void goAnalyze(CloudNaturalLanguage naturalLanguageService){

        for(String phrase: phrases) {
            final Document document = new Document();
            document.setType("PLAIN_TEXT");
            document.setLanguage("en-US");
            document.setContent(phrase);

            Features features = new Features();
            features.setExtractEntities(true);
            features.setExtractDocumentSentiment(true);

            final AnnotateTextRequest request = new AnnotateTextRequest();
            request.setDocument(document);
            request.setFeatures(features);

            try {
                AnnotateTextResponse response =
                        naturalLanguageService.documents()
                                .annotateText(request).execute();

                final float Sentiment = response.getDocumentSentiment().getScore();
                phraseSent.add(Sentiment);

                    if(Sentiment < -0.3){
                        colors.add(0);
                    }
                    if(Sentiment > 0.3){
                        colors.add(1);
                    }
                    if(Sentiment >= -0.3 && Sentiment  <= 0.3) {
                        colors.add(2);
                    }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        for (String s: phrases){
            transcript += s;
        }
        Log.i("SIZE", phrases.size() + " " + phraseSent.size() + " " + colors.size() + " " + keyWords.size());
    }

    private String highlight(String s){
        String[] color = {"<span style=\"color: #FF0000\">",
                "<span style=\"color: #4de14d\">",
                "<span style=\"color: #0000ff\">",
                "<span style=\"background-color: #FFFF00\">"};
        for(int i=0; i<phrases.size(); i++){
            s = s.replaceAll(phrases.get(i), color[colors.get(i)] + phrases.get(i) + "</span>");
        }
        for(int i=0; i<keyWords.size(); i++){
            s = s.replaceAll(keyWords.get(i) + " ", "<span style=\"color: #00000000; background-color: #FFFF00\">" + keyWords.get(i) + " " + "</span>");
        }
        return s;
    }
}
