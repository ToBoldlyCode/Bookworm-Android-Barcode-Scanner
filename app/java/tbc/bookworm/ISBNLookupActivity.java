package tbc.bookworm;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.String;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ISBNLookupActivity extends AppCompatActivity {
    private TextView statusMessage;
    private TextView title;
    private String barcode;
    private String searchQuery;
    private String API_KEY = BuildConfig.API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        statusMessage = (TextView) findViewById(R.id.status_message);
        title = (TextView) findViewById(R.id.title);
        Intent intent = getIntent();
        barcode = intent.getStringExtra("barcode");
        Log.d("error", barcode);
        new findBookTitle().execute();
    }

    private class findBookTitle extends AsyncTask<String, String, String> {
        protected String doInBackground(String... params) {
            String id = "";
            String query = "https://www.goodreads.com/search/index.xml?key=" + API_KEY + "&q=" + barcode;

            try {
                URL url = new URL(query);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                int response = con.getResponseCode();

                InputStream in = con.getInputStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String read;

                while ((read = br.readLine()) != null) {
                    sb.append(read);
                }

                br.close();
                String xml = sb.toString();

                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource src = new InputSource();
                src.setCharacterStream(new StringReader(xml));

                Document doc = builder.parse(src);
                String authorDetails = doc.getElementsByTagName("author").item(0).getTextContent();
                id = authorDetails.replaceAll("[^0-9]", "");

            } catch (IOException | ParserConfigurationException | SAXException e) {
                //do something
            }

            return id;
        }

        protected void onPostExecute(String result) {
            searchQuery = result;
            Intent intent = new Intent(ISBNLookupActivity.this, AuthorLookupActivity.class);
            intent.putExtra("id", searchQuery);
            startActivity(intent);
        }
    }
}
