package asarmaapps.com.biasscanner;

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
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
//import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Entity;
import com.google.api.services.language.v1beta2.model.Features;
import com.google.api.services.language.v1beta2.model.Token;

import java.util.ArrayList;
import java.util.List;

/*import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Document;*/

public class TestClass extends AppCompatActivity {
    private final String CLOUD_API_KEY = "AIzaSyDVgwrzDOpJyHu1LAUaDJtekK6jAUcMJgE";
    private TextView textView;
    private TextView resultText;
    private EditText editText;
    private float sentiment;
    private float magnitude;
    private String messageSentiment;
    private String messageSyntax;
    private List<Entity> entityList;
    private ArrayList<String[]> syntaxElements;

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
                /*resultText.setText(tryLanguage(textView.getText().toString()));*/
            }
        });
    }


   /* public String tryLanguage(String doc) {
        String message = "-";
        try {
            LanguageServiceClient language = LanguageServiceClient.create(); {
                // set content to the text string
                Document doc2 = Document.newBuilder()
                        .setContent(doc)
                        .setType(com.google.cloud.language.v1.Document.Type.PLAIN_TEXT)
                        .build();
                ClassifyTextRequest request = ClassifyTextRequest.newBuilder()
                        .setDocument(doc2)
                        .build();
                // detect categories in the given text
                ClassifyTextResponse response = language.classifyText(request);

                for (ClassificationCategory category : response.getCategoriesList()) {
                    message = ("Category name : %s, Confidence : %.3f\n"+
                            category.getName()+ " " + category.getConfidence());
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return message;
    }*/
}
